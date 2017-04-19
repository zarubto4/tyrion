package utilities.scheduler.jobs;

import models.Model_Product;
import models.Model_Invoice;
import models.Model_InvoiceItem;
import models.Model_ProductExtension;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.enums.Enum_Currency;
import utilities.enums.Enum_Payment_method;
import utilities.enums.Enum_Payment_warning;
import utilities.fakturoid.Utilities_Fakturoid_Controller;
import utilities.goPay.Utilities_GoPay_Controller;
import utilities.loggy.Loggy;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Job_SpendingCredit implements Job {

    public Job_SpendingCredit(){}

    private static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("Job_SpendingCredit:: execute: Executing Job_SpendingCredit");

        if(!spend_credit_thread.isAlive()) spend_credit_thread.start();
    }

    private Thread spend_credit_thread = new Thread(){

        @Override
        public void run() {

            logger.debug("Job_SpendingCredit:: spend_credit_thread: concurrent thread started on {}", new Date());

            try {

                Date created = new Date(new Date().getTime() - TimeUnit.HOURS.toMillis(16));

                int total =  Model_Product.find.where().lt("created", created)
                        .isNotNull("extensions.id").eq("active", true)
                        .findRowCount();

                if (total != 0) {

                    int page_total = total / 25;

                    if (total % 25 > 0) page_total++;

                    for (int page = 0; page <= page_total - 1; page++) {

                        logger.debug("Job_SpendingCredit:: spend_credit_thread: procedure for page {} from {}", (page + 1), page_total);

                    /*
                     * Filter slouží k hledání těch produktů, kde by mělo dojít ke stržení kreditu
                     * Kredit se strhává u všech produktů, starších než 16 hodin (Aby někdo před půlnocí nezaložil produkt a hned se mu nestrhla raketa)
                     */
                        List<Model_Product> products = Model_Product.find.where()
                                .isNotNull("extensions.id").eq("active", true)
                                .lt("created", created)
                                .order("created")
                                .findPagedList(page, 25)
                                .getList();

                        products.forEach(Job_SpendingCredit::spend);
                    }
                }
            } catch (Exception e) {
                Loggy.internalServerError("Job_SpendingCredit:: spend_credit_thread:", e);
            }

            logger.debug("Job_SpendingCredit:: spend_credit_thread: thread stopped on {}", new Date());
        }
    };

    /**
     * Vezmou se všechny připojené Extensions sečte se jejich cena a odečte se z účtu.
     */
    private static void spend(Model_Product product){
        try {

            logger.info("Job_SpendingCredit:: spend: product ID: {}", product.id );
            double total_spending = 0.0;
            int daily = 1; //The number "1" determines how many times on one day is credit spent.

            for(Model_ProductExtension extension : product.extensions){
                total_spending += extension.getPrice();
            }

            logger.debug("Job_SpendingCredit:: spend: total spending: {}", total_spending);
            logger.debug("Job_SpendingCredit:: spend: state before: {}", product.credit);

            product.credit -= total_spending;
            product.update();

            logger.debug("Job_SpendingCredit:: spend: actual state: {}", product.credit);

            double daily_spending = daily * total_spending;

            double double_days = product.credit / daily_spending;

            int days; // Determines how many days will credit suffice.

            if (double_days >= 0) days = (int) (double_days + 0.5);
            else days = (int) (double_days - 0.5);

            switch (product.business_model){
                case saas:{

                    spendCreditSaas(product, daily_spending, days);
                    break;
                }
                case fee:{

                    spendCreditFee(product, days);
                    break;
                }
                case lifelong:{

                    spendCreditLifelong(product);
                    break;
                }
                default: {
                    throw new Exception("Could not determine the business model. Fail to check credit on product: " + product.name +  " with id: " + product.id);
                }
            }
        } catch (Exception e) {
            Loggy.internalServerError("Job_SpendingCredit:: spend:", e);
        }

    }

    private static void spendCreditSaas(Model_Product product, double daily_spending, int days){

        logger.debug("Job_SpendingCredit:: spendCreditSaas: daily_spending: {}, days: {}", daily_spending, days);

        Model_Invoice invoice = product.pending_invoice();

        // Bank transfer - invoice and reminders must be sent in advance
        if(product.method == Enum_Payment_method.bank_transfer) {

            logger.debug("Job_SpendingCredit:: spendCreditSaas: bank transfer");

            // If credit will suffice only for 14 days - make new invoice
            if (invoice == null && days < 14){

                logger.warn("Job_SpendingCredit:: spendCreditSaas: bank transfer: It is time to send an invoice");

                invoice = new Model_Invoice();
                invoice.method = product.method;
                invoice.product = product;

                Model_InvoiceItem invoice_item = new Model_InvoiceItem();
                invoice_item.name = product.product_type() + " in Mode(" + product.mode.name() + ")";
                invoice_item.unit_price = daily_spending * 30;
                invoice_item.quantity = (long) 1;
                invoice_item.unit_name = "Currency";
                invoice_item.currency = Enum_Currency.USD;

                invoice.invoice_items.add(invoice_item);

                invoice = Utilities_Fakturoid_Controller.create_proforma(invoice);

                // TODO Pošlu notifikaci

                Utilities_Fakturoid_Controller.sendInvoiceEmail(invoice, null);

                return;
            }

            if (invoice == null) {
                logger.warn("Job_SpendingCredit:: spendCreditSaas: bank transfer: The financial reserves are sufficient. Just send a notification");
                return;
            }

            if(invoice.warning == Enum_Payment_warning.zero_balance && product.credit < 0 && days < -20) {
                logger.warn("Job_SpendingCredit:: spendCreditSaas: bank transfer: The product is in minus 20 times the average spending");

                invoice.warning = Enum_Payment_warning.deactivation;
                invoice.update();

                // TODO Pošlu notifikaci

                Utilities_Fakturoid_Controller.sendInvoiceReminderEmail(invoice,
                        "We are sorry to inform you, that you have reached negative credit limit. Your services are no longer supported. We activate your product again, when we receive payment for your invoice.");

                product.active = false;
                product.update();
                return;
            }

            if(invoice.warning == Enum_Payment_warning.first && product.credit < 0){
                logger.warn("Job_SpendingCredit:: spendCreditSaas: bank transfer: The product is in negative credit balance");

                invoice.warning = Enum_Payment_warning.zero_balance;
                invoice.update();

                // TODO Pošlu notifikaci

                Utilities_Fakturoid_Controller.sendInvoiceReminderEmail(invoice,
                        "You have reached zero credit balance. Your services will be supported for next 20 days. If we do not receive your payment till then, we will have to deactivate your services.");

                return;
            }

            if(invoice.warning == Enum_Payment_warning.none && days < 7 ){
                logger.warn("Job_SpendingCredit:: spendCreditSaas: bank transfer:  The Product is close to zero in financial balance");

                invoice.warning = Enum_Payment_warning.first;
                invoice.update();

                // TODO Pošlu notifikaci

                Utilities_Fakturoid_Controller.sendInvoiceEmail(invoice, null);
            }

        }else if(product.method == Enum_Payment_method.credit_card){

            logger.debug("Job_SpendingCredit:: spendCreditSaas: credit card");

            if(invoice == null && days < 5){

                logger.debug("Job_SpendingCredit:: spendCreditSaas: credit card: The Product is close to zero in financial balance. Credit will suffice for {}", days);

                invoice = new Model_Invoice();
                invoice.method = product.method;
                invoice.product = product;

                Model_InvoiceItem invoice_item = new Model_InvoiceItem();
                invoice_item.name = product.product_type() + " in Mode(" + product.mode.name() + ")";
                invoice_item.unit_price = daily_spending * 30;
                invoice_item.quantity = (long) 1;
                invoice_item.unit_name = "Currency";
                invoice_item.currency = Enum_Currency.USD;

                invoice.invoice_items.add(invoice_item);

                // TODO Pošlu notifikaci

                invoice = Utilities_Fakturoid_Controller.create_proforma(invoice);

                if (product.on_demand) {
                    try {

                        Utilities_GoPay_Controller.onDemandPayment(invoice);

                    } catch (Exception e) {
                        Loggy.internalServerError("Job_SpendingCredit:: spendCreditSaas:", e);

                        invoice = Utilities_GoPay_Controller.singlePayment("Substitute payment", product, invoice);

                        Utilities_Fakturoid_Controller.sendInvoiceReminderEmail(invoice,
                                "We could not take money from your credit card due to some problems. Please use the manual substitute payment through financial section of your Byzance account.");
                    }
                } else {

                    invoice = Utilities_GoPay_Controller.singlePayment("First Payment", product, invoice);

                    // TODO notifikace autorizace platby

                    Utilities_Fakturoid_Controller.sendInvoiceEmail(invoice, null);
                }

                return;
            }

            if (invoice == null){
                logger.warn("Job_SpendingCredit:: spendCreditSaas: credit card: The financial reserves are sufficient. Just send a notification");
                return;
            }

            if(invoice.warning == Enum_Payment_warning.zero_balance && product.credit < 0 && days < -10 ){
                logger.warn("Job_SpendingCredit:: spendCreditSaas: credit card: The product is in the minus 10 times the average spending");

                invoice.warning = Enum_Payment_warning.deactivation;
                invoice.update();

                // TODO Pošlu notifikaci

                Utilities_Fakturoid_Controller.sendInvoiceReminderEmail(invoice,
                        "We are sorry to inform you, that you have reached negative credit limit. Your services are no longer supported. We activate your product again, when we receive payment for your invoice.");

                product.active = false;
                product.update();
                return;
            }

            if(invoice.warning == Enum_Payment_warning.none && product.credit < 0){
                logger.warn("Job_SpendingCredit:: spendCreditSaas: credit card: The account is in the minus");

                invoice.warning = Enum_Payment_warning.zero_balance;
                invoice.update();

                // TODO Pošlu notifikaci

                Utilities_Fakturoid_Controller.sendInvoiceReminderEmail(invoice,
                        "You have reached zero credit balance. Your services will be supported for next 10 days. If we do not receive your payment till then, we will have to deactivate your services.");
            }
        }
    }

    private static void spendCreditFee(Model_Product product, int days){

        // TODO
    }

    private static void spendCreditLifelong(Model_Product product){

        // TODO
    }
}