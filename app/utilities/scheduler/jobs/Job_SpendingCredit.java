package utilities.scheduler.jobs;


import com.google.inject.Inject;
import com.typesafe.config.Config;
import models.Model_Invoice;
import models.Model_Product;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.Configuration;
import utilities.Server;
import utilities.emails.Email;
import utilities.enums.PaymentMethod;
import utilities.enums.PaymentWarning;
import utilities.financial.fakturoid.Fakturoid;
import utilities.financial.goPay.GoPay;
import utilities.logger.Logger;
import utilities.scheduler.Scheduled;


import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is a job class that is used for spending credit of a product.
 */
@Scheduled("0 30 3 * * ?")
public class Job_SpendingCredit implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_SpendingCredit.class);

//**********************************************************************************************************************

    private Config config;
    private Fakturoid fakturoid;
    private GoPay goPay;

    @Inject
    public Job_SpendingCredit(Config config, Fakturoid fakturoid, GoPay goPay) {
        this.config = config;
        this.fakturoid = fakturoid;
        this.goPay = goPay;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute: Executing Job_SpendingCredit");

        if (!spend_credit_thread.isAlive()) spend_credit_thread.start();
    }

    /**
     * Thread that finds number of all products, that credit should be spent for.
     * Product must be active, must have some extensions and must be older than 16h.
     * Then gets products in paged list (25 for page) and passes them to method spend().
     */
    private Thread spend_credit_thread = new Thread() {

        @Override
        public void run() {

            logger.debug("spend_credit_thread: concurrent thread started on {}", new Date());

            try {

                Date created = new Date(new Date().getTime() - TimeUnit.HOURS.toMillis(16));

                int total = Model_Product.find.query()
                        .where()
                        .eq("active", true)
                        //.ne("business_model", Enum_BusinessModel.alpha)
                        .isNotNull("extensions.id")
                        .lt("created", created)
                        .findCount();

                if (total != 0) {

                    int page_total = total / 25;

                    if (total % 25 > 0) page_total++;

                    for (int page = 0; page <= page_total - 1; page++) {

                        logger.debug("spend_credit_thread: procedure for page {} from {}", (page + 1), page_total);

                    /*
                     * Filter slouží k hledání těch produktů, kde by mělo dojít ke stržení kreditu
                     * Kredit se strhává u všech produktů, starších než 16 hodin (Aby někdo před půlnocí nezaložil produkt a hned se mu nestrhla raketa)
                     */
                        List<Model_Product> products = Model_Product.find.query()
                                .where()
                                .eq("active", true)
                                //.ne("business_model", Enum_BusinessModel.alpha)
                                .isNotNull("extensions.id")
                                .lt("created", created)
                                .order("created")
                                .setFirstRow(page)
                                .setMaxRows(25)
                                .findPagedList()
                                .getList();

                        products.forEach(this::spend);
                    }
                }
            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.debug("spend_credit_thread: thread stopped on {}", new Date());
        }

        /**
         * Based on business model decides what to do with the product.
         * @param product Model product the credit is spent for.
         */
        public void spend(Model_Product product) {
            try {

                logger.info("spend: product ID: {}", product.id );

                switch (product.business_model) {
                    case ALPHA:{

                        Date alpha_ending = new Date(config.getLong("Financial.alpha_ending"));

                        if (new Date().after(alpha_ending)) {
                            product.active = false;
                            product.update();

                            product.notificationDeactivation(" Public Alpha testing has ended.");

                            logger.debug("spend: product is ALPHA - Alpha has ended on {} - Deactivating product", alpha_ending.toString() );
                        } else
                            logger.debug("spend: product is ALPHA - nothing happens - Alpha ends on {}", alpha_ending.toString());

                        break;
                    }
                    case SAAS:{

                        spendCreditSaas(product);
                        break;
                    }
                    case FEE:{

                        spendCreditFee(product);
                        break;
                    }
                    case CAL:{

                        spendCreditCal(product);
                        break;
                    }
                    case INTEGRATOR:{

                        logger.debug("spend: integrator model - TODO"); // TODO
                        break;
                    }
                    case INTEGRATION:{

                        logger.debug("spend: integration model - TODO"); // TODO
                        break;
                    }
                    default: {
                        throw new Exception("Could not determine the business model. Fail to check credit on product: " + product.name +  " with id: " + product.id);
                    }
                }
            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }

        /**
         * This method takes all extensions of the product and sums up their prices.
         * Then it subtracts the calculated credit.
         * Does appropriate action depending on how many days will credit suffice.
         * If it is needed the method will create an invoice and sends it to the user or creates a payment.
         * If the product has on_demand true, the method will try to take money from credit card.
         * If the payment is not yet received, the method will send a warning to the user.
         * Deactivates the product, if the user reaches negative limit.
         * @param product Given product for spending.
         */
        private void spendCreditSaas(Model_Product product) {

            Long total_spending = product.price(); // Extension prices summary

            logger.debug("spendCreditSaas: total spending: {}", total_spending);
            logger.debug("spendCreditSaas: state before: {}", product.credit);

            product.credit_spend(total_spending);

            logger.debug("spendCreditSaas: actual state: {}", product.credit);

            Long daily_spending = Server.financial_spendDailyPeriod * total_spending; // Expenses for one day

            double double_days = (double)product.credit / (double)daily_spending;

            int days; // Determines how many days will credit suffice.

            if (double_days >= 0) days = (int) (double_days + 0.5);
            else days = (int) (double_days - 0.5);

            logger.debug("spendCreditSaas: daily_spending: {}, days: {}", daily_spending, days);

            Model_Invoice invoice = product.pending_invoice();

            // Bank transfer - invoice and reminders must be sent in advance
            if (product.method == PaymentMethod.BANK_TRANSFER) {

                logger.debug("spendCreditSaas: bank transfer");

                // If credit will suffice only for 14 days - make new invoice
                if (invoice == null && days < 14) {

                    logger.debug("spendCreditSaas: bank transfer: It is time to send an invoice");

                    if (!product.isBillingReady()) {

                        if (days < -20) {
                            product.active = false;
                            product.update();

                            product.notificationDeactivation("Fill in your payment details and buy a credit.");

                            new Email()
                                    .text("Dear customer,")
                                    .text("We are sorry to inform you, that your product was deactivated. Fill in your payment details and buy a credit.")
                                    .text("Best regards, Byzance Team")
                                    .send(product.customer, "Product Deactivation" );
                        }

                        return;
                    }

                    invoice = new Model_Invoice();
                    invoice.method = product.method;
                    invoice.product = product;
                    invoice.setItems();

                    invoice = fakturoid.create_proforma(invoice);
                    if (invoice == null) return;

                    invoice.notificationInvoiceNew();

                    fakturoid.sendInvoiceEmail(invoice, null);

                    return;
                }

                if (invoice == null) {
                    logger.debug("spendCreditSaas: bank transfer: The financial reserves are sufficient.");
                    return;
                }

                if (invoice.warning == PaymentWarning.ZERO_BALANCE && product.credit < 0 && days < -20) {
                    logger.debug("spendCreditSaas: bank transfer: The product is in minus 20 times the average spending");

                    invoice.warning = PaymentWarning.DEACTIVATION;
                    invoice.update();

                    invoice.notificationInvoiceReminder("You have reached negative credit limit. Your product is deactivated.");

                    fakturoid.sendInvoiceReminderEmail(invoice,
                            "We are sorry to inform you, that you have reached negative credit limit. Your services are no longer supported. We activate your product again, when we receive payment for your invoice.");

                    product.active = false;
                    product.update();
                    return;
                }

                if (invoice.warning == PaymentWarning.FIRST && product.credit < 0) {
                    logger.debug("spendCreditSaas: bank transfer: The product is in negative credit balance");

                    invoice.warning = PaymentWarning.ZERO_BALANCE;
                    invoice.update();

                    invoice.notificationInvoiceReminder("You have reached zero credit balance. Your services will be supported for next 20 days.");

                    fakturoid.sendInvoiceReminderEmail(invoice,
                            "You have reached zero credit balance. Your services will be supported for next 20 days. If we do not receive your payment till then, we will have to deactivate your services.");

                    return;
                }

                if (invoice.warning == PaymentWarning.NONE && days < 7 ) {
                    logger.debug("spendCreditSaas: bank transfer:  The Product is close to zero in financial balance");

                    invoice.warning = PaymentWarning.FIRST;
                    invoice.update();

                    invoice.notificationInvoiceReminder("You will reach zero credit balance soon.");

                    fakturoid.sendInvoiceReminderEmail(invoice,
                            "We did not receive your payment. You will reach zero credit balance soon.");

                }

            } else if (product.method == PaymentMethod.CREDIT_CARD) {

                logger.debug("spendCreditSaas: credit card");

                if (invoice == null && days < 5) {

                    logger.debug("spendCreditSaas: credit card: The Product is close to zero in financial balance. Credit will suffice for {}", days);

                    if (!product.isBillingReady()) {

                        if (days < -10) {
                            product.active = false;
                            product.update();

                            product.notificationDeactivation("Fill in your payment details and buy a credit.");

                            new Email()
                                    .text("Dear customer,")
                                    .text("We are sorry to inform you, that your product was deactivated. Fill in your payment details and buy a credit.")
                                    .text("Best regards, Byzance Team")
                                    .send(product.customer, "Product Deactivation" );
                        }
                    }

                    invoice = new Model_Invoice();
                    invoice.method = product.method;
                    invoice.product = product;
                    invoice.setItems();

                    invoice = fakturoid.create_proforma(invoice);
                    if (invoice == null) return;

                    if (product.on_demand && product.gopay_id != null) {
                        try {

                            goPay.onDemandPayment(invoice);

                        } catch (Exception e) {
                            logger.internalServerError(e);

                            invoice = goPay.singlePayment("Substitute payment", product, invoice);

                            invoice.notificationInvoiceReminder("Failed to take money from your credit card, resolve it manually.");

                            fakturoid.sendInvoiceReminderEmail(invoice,
                                    "We could not take money from your credit card due to some problems. Please use the manual substitute payment through financial section of your Byzance account.");
                        }
                    } else if (product.on_demand) {

                        invoice = goPay.singlePayment("First Payment", product, invoice);

                        invoice.notificationInvoiceReminder("You have to authorize the first payment.");

                        fakturoid.sendInvoiceEmail(invoice, null);

                    } else {

                        invoice = goPay.singlePayment("Single Payment", product, invoice);

                        invoice.notificationInvoiceNew();

                        fakturoid.sendInvoiceEmail(invoice, null);
                    }

                    return;
                }

                if (invoice == null) {
                    logger.debug("spendCreditSaas: credit card: The financial reserves are sufficient.");
                    return;
                }

                if (invoice.warning == PaymentWarning.ZERO_BALANCE && product.credit < 0 && days < -10 ) {
                    logger.debug("spendCreditSaas: credit card: The product is in the minus 10 times the average spending");

                    invoice.warning = PaymentWarning.DEACTIVATION;
                    invoice.update();

                    invoice.notificationInvoiceReminder("Your product is deactivated.");

                    fakturoid.sendInvoiceReminderEmail(invoice,
                            "We are sorry to inform you, that you have reached negative credit limit. Your services are no longer supported. We activate your product again, when we receive payment for your invoice.");

                    product.active = false;
                    product.update();
                    return;
                }

                if (invoice.warning == PaymentWarning.NONE && product.credit < 0) {
                    logger.debug("spendCreditSaas: credit card: The account is in the minus");

                    invoice.warning = PaymentWarning.ZERO_BALANCE;
                    invoice.update();

                    invoice.notificationInvoiceReminder("You have 10 days before your product will be deactivated.");

                    fakturoid.sendInvoiceReminderEmail(invoice,
                            "You have reached zero credit balance. Your services will be supported for next 10 days. If we do not receive your payment till then, we will have to deactivate your services.");
                }
            }
        }

        private void spendCreditFee(Model_Product product) {

            // TODO
        }

        private void spendCreditCal(Model_Product product) {

            // TODO
        }
    };
}