package utilities.scheduler.jobs;


import com.google.inject.Inject;
import com.typesafe.config.Config;
import models.Model_ExtensionFinancialEvent;
import models.Model_Invoice;
import models.Model_Product;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.enums.PaymentMethod;
import utilities.enums.InvoiceStatus;
import utilities.enums.PaymentWarning;
import utilities.financial.fakturoid.FakturoidService;
import utilities.financial.goPay.GoPay;
import utilities.logger.Logger;
import utilities.scheduler.Scheduled;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is a job class that is used for spending of a product.
 */
@Scheduled("0 30 3 * * ?")
public class Job_Financial implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_Financial.class);

//**********************************************************************************************************************

    private Config config;
    private FakturoidService fakturoid;
    private GoPay goPay;

    @Inject
    public Job_Financial(Config config, FakturoidService fakturoid, GoPay goPay) {
        this.config = config;
        this.fakturoid = fakturoid;
        this.goPay = goPay;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute: Executing Job_Financial");

        if (!financial_thread.isAlive()) financial_thread.start();
    }

    /**
     * Thread that finds number of all products, that money should be spent for.
     * Product must be active, must have some extensions and must be older than 16h.
     * Then gets products in paged list (25 for page) and passes them to method spend().
     */
    private Thread financial_thread = new Thread() {

        @Override
        public void run() {

            logger.debug("financial_thread: concurrent thread started on {}", new Date());

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

                        logger.debug("financial_thread: procedure for page {} from {}", (page + 1), page_total);

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

            logger.debug("financial_thread: thread stopped on {}", new Date());
        }

        /**
         * Based on business model decides what to do with the product.
         * @param product Model product the credit is spent for.
         */
        public void spend(Model_Product product) {
            try {

                logger.info("spend: product ID: {}", product.id );

                switch (product.business_model) {
                    case SAAS:{
                        spendingSaas(product);
                        break;
                    }
                    case FEE:{
                        throw new Exception("FEE business model not implemented!");
                    }
                    case INTEGRATOR:{
                        throw new Exception("INTEGRATOR business model not implemented!");
                    }
                    case INTEGRATION:{
                        throw new Exception("INTEGRATION business model not implemented!");
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
         * This method takes all extensions of the product, calculate its resource consumption and money spend. <br />
         * Each day we
         * <ul>
         * <li>calculates and save resources consumed during last day</li>
         * <li>If billing is not ready, check if customer has enough credit. Tell him if it is running out, deactivate if there is none.</li>
         * <li>If there are some pending invoices, let the user know or deactivate the product depending on the time from its creation.</li>
         * </ul>
         * <p>
         * Each month:
         * <ul>
         * <li>Create new invoice.</li>
         * <li>Take the money from the user if credit card is set.</li>
         * </ul>
         *
         * @param product Given product for spending.
         */
        private void spendingSaas(Model_Product product) {
            logger.debug("Spending update for product {} (id: {}).", product.name, product.id);

            // calculate new spending
            List<Model_ExtensionFinancialEvent> newEvents = product.updateHistory(false);
            BigDecimal newSpending = Model_ExtensionFinancialEvent.getTotalPrice(newEvents);
            logger.debug("New spending today: {}.", newSpending);

            // billing is not ready, check if there is enough credit
            if (!product.isBillingReady()) {
                logger.debug("Product billing is not ready. Check spent credit.");
                List<Model_ExtensionFinancialEvent> notInvoiced = product.getFinancialEventsNotInvoiced(true);
                BigDecimal notInvoicedPrice = Model_ExtensionFinancialEvent.getTotalPrice(notInvoiced);

                if (notInvoicedPrice.compareTo(product.credit) > 1) {
                    logger.debug("Not enough credit, product will be deactivated.");

                    try {
                        product.setActive(false);
                        product.sendMessageToAdmin("No payment details set, no credit, product was deactivated. ");
                    } catch (Exception e) {
                        product.sendMessageToAdmin("Error while deactivating product " + product.id +"! " +
                                "Credit is gone and no payment details set, but error occurred when we tried to deactivate the product.");
                    }

                    product.notificationDeactivation("No remaining credit. Payment method missing.");
                    product.emailDeactivation();

                    return;
                }

                if (notInvoicedPrice.doubleValue() > product.credit.doubleValue() * 0.90
                        && ((notInvoicedPrice.doubleValue() - newSpending.doubleValue()) < product.credit.doubleValue() * 0.90)) {
                    logger.debug("Less than 90 % credit, warn customer.");

                    product.notificationLowCredit(notInvoicedPrice);
                    product.notificationPaymentDetails();
                    product.emailLowCredit(notInvoicedPrice);
                    return;
                }

                if (notInvoicedPrice.doubleValue() > product.credit.doubleValue() * 0.5
                        && ((notInvoicedPrice.doubleValue() - newSpending.doubleValue()) < product.credit.doubleValue() * 0.5)) {
                    logger.debug("Less than 50 % credit, warn customer.");

                    product.notificationLowCredit(notInvoicedPrice);
                    product.notificationPaymentDetails();
                    product.emailLowCredit(notInvoicedPrice);
                    return;
                }

                return;
            }

            // Billing details are ready, but user still has some credit.
            // If user is getting below 50 or 90 %, inform him/her about remaining credit.
            if (product.credit.compareTo(BigDecimal.ZERO) > 0) {
                List<Model_ExtensionFinancialEvent> notInvoiced = product.getFinancialEventsNotInvoiced(true);
                BigDecimal notInvoicedPrice = Model_ExtensionFinancialEvent.getTotalPrice(notInvoiced);

                if ((notInvoicedPrice.doubleValue() > product.credit.doubleValue() * 0.50
                        && ((notInvoicedPrice.doubleValue() - newSpending.doubleValue()) < product.credit.doubleValue() * 50))
                        || (notInvoicedPrice.doubleValue() > product.credit.doubleValue() * 0.90
                        && ((notInvoicedPrice.doubleValue() - newSpending.doubleValue()) < product.credit.doubleValue() * 0.90))) {
                    product.notificationLowCredit(notInvoicedPrice);
                }
            }

            // at the end of the month, we have to send user an invoice (if there are some unpaved events from the previous month)
            String zoneId = "UTC";
            Date startOfThisMonth = Date.from(ZonedDateTime.ofInstant(Instant.now(), ZoneId.of(zoneId)).toLocalDate().atStartOfDay().withDayOfMonth(1).toInstant(ZoneOffset.of(zoneId)));
            Model_ExtensionFinancialEvent firstUnpaid = product.getFinancialEventFirstNotInvoiced();
            if (firstUnpaid != null && firstUnpaid.event_end.before(startOfThisMonth)) {
                logger.debug("Create invoice.");
                try {
                    Instant to = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of(zoneId))
                            .toLocalDate()
                            .atStartOfDay()
                            .withDayOfMonth(1)
                            .toInstant(ZoneOffset.of(zoneId));
                    Instant from = to.minus(1, ChronoUnit.MONTHS);

                    // invoice should be sent to the user only after it is registered by Facturoid
                    Model_Invoice invoice = product.createInvoice(Date.from(from), Date.from(to));

                    // TODO try pay if GoPay is ready and payment_method == PaymentMethod.CREDIT_CARD

                    return;

                } catch (Exception ex) {
                    logger.error("Invoice was not created!", ex);
                }
            }

// TODO once GoPay is ready
//            if(product.payment_details.payment_method == PaymentMethod.CREDIT_CARD) {
//                Collection<Model_Invoice> unpaidInvoices = product.getInvoicesToBePaid();
//                if(!unpaidInvoices.isEmpty()) {
//                    // pay
//                }
//            }

            Collection<Model_Invoice> overdueInvoices = product.getInvoices(InvoiceStatus.OVERDUE);
            if (!overdueInvoices.isEmpty()) {
                int firstWarningDays = 3;
                int secondWarningDays = 10;
                int deactivateProductDays = 14;

                for (Model_Invoice invoice : overdueInvoices) {
                    Date overdue = invoice.overdue;

                    Instant firstWarning = overdue.toInstant().plus(firstWarningDays, ChronoUnit.DAYS);
                    if (invoice.warning == PaymentWarning.NONE && Instant.now().isAfter(firstWarning)) {
                        invoice.warning = PaymentWarning.FIRST;
                        invoice.update();

                        invoice.notificationInvoiceReminder("Your product will be deactivated soon.");
                        fakturoid.sendInvoiceReminderEmail(invoice, null);
                    }


                    Instant secondWarning = overdue.toInstant().plus(secondWarningDays, ChronoUnit.DAYS);
                    if (invoice.warning == PaymentWarning.FIRST && Instant.now().isAfter(secondWarning)) {
                        invoice.warning = PaymentWarning.SECOND;
                        invoice.update();

                        invoice.notificationInvoiceReminder("Your product will be deactivated soon.");
                        fakturoid.sendInvoiceReminderEmail(invoice, null);
                    }

                    Instant deactivateProduct = overdue.toInstant().plus(deactivateProductDays, ChronoUnit.DAYS);
                    if (invoice.warning == PaymentWarning.SECOND && Instant.now().isAfter(deactivateProduct)) {
                        invoice.warning = PaymentWarning.DEACTIVATION;
                        invoice.update();

                        try {
                            product.setActive(false);
                            product.sendMessageToAdmin("No payment received, product was deactivated. ");

                        } catch (Exception e) {
                            product.sendMessageToAdmin("Error while deactivating product " + product.id +"! " +
                                    "We did not receive payment for invoice " + invoice + ", but error occurred when we tried to deactivate the product.");
                        }

                        invoice.notificationInvoiceReminder("Product was deactivated.");
                        product.notificationDeactivation("Despite multiple warnings, we did not receive the payment.");
                        product.emailDeactivation();


                    }
                }
            }

            // If some invoice is not synchronized with Fakturoid after one they, there is probably some error. Inform admin.
            Collection<Model_Invoice> unfinishedInvoices = product.getInvoices(InvoiceStatus.UNFINISHED)
                    .stream()
                    .filter(invoice -> invoice.updated.before(new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli())))
                    .collect(Collectors.toList());

            for (Model_Invoice unfinished: unfinishedInvoices) {
                unfinished.sendMessageToAdmin("We have UNFINISHED invoice waiting (not register by Fakturoid)! ");
            }

            // If some invoice needs to be confirmed my admin and is older than one day, send an reminder.
            Collection<Model_Invoice> unconfirmedInvoices = product.getInvoices(InvoiceStatus.UNCONFIRMED).stream()
                    .filter(invoice -> invoice.created.before(new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli())))
                    .collect(Collectors.toList());

            for (Model_Invoice unconfirmed: unconfirmedInvoices) {
                unconfirmed.sendMessageToAdmin("We have UNCONFIRMED invoice waiting (limit of money for the invoice was reached)! ");
            }
        }
    };
}