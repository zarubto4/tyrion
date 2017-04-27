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
import utilities.logger.Class_Logger;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Job_SpendingCredit implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Job_SpendingCredit.class);

//**********************************************************************************************************************

    public Job_SpendingCredit(){}

    public void execute(JobExecutionContext context) throws JobExecutionException {

        terminal_logger.info("execute: Executing Job_SpendingCredit");

        if(!spend_credit_thread.isAlive()) spend_credit_thread.start();
    }

    private Thread spend_credit_thread = new Thread(){

        @Override
        public void run() {

            terminal_logger.debug("spend_credit_thread: concurrent thread started on {}", new Date());

            try {

                Date created = new Date(new Date().getTime() - TimeUnit.HOURS.toMillis(16));

                int total =  Model_Product.find.where().lt("created", created)
                        .isNotNull("extensions.id").eq("active", true)
                        .findRowCount();

                if (total != 0) {

                    int page_total = total / 25;

                    if (total % 25 > 0) page_total++;

                    for (int page = 0; page <= page_total - 1; page++) {

                        terminal_logger.debug("spend_credit_thread: procedure for page {} from {}", (page + 1), page_total);

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
                terminal_logger.internalServerError("spend_credit_thread:", e);
            }

            terminal_logger.debug("spend_credit_thread: thread stopped on {}", new Date());
        }
    };

    /**
     * Vezmou se všechny připojené Extensions sečte se jejich cena a odečte se z účtu.
     */
    public static void spend(Model_Product product){
        try {

            terminal_logger.info("spend: product ID: {}", product.id );
            Long total_spending = (long) 0;
            int daily = 1; //The number "1" determines how many times on one day is credit spent.

            for(Model_ProductExtension extension : product.extensions){
                total_spending += extension.getPrice();
            }

            terminal_logger.debug("spend: total spending: {}", total_spending);
            terminal_logger.debug("spend: state before: {}", product.credit);

            product.credit -= total_spending;
            product.update();

            terminal_logger.debug("spend: actual state: {}", product.credit);

            Long daily_spending = daily * total_spending;

            double double_days = (double)product.credit / (double)daily_spending;

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
            terminal_logger.internalServerError("spend:", e);
        }

    }

    private static void spendCreditSaas(Model_Product product, Long daily_spending, int days){

        terminal_logger.debug("spendCreditSaas: daily_spending: {}, days: {}", daily_spending, days);

        Model_Invoice invoice = product.pending_invoice();

        // Bank transfer - invoice and reminders must be sent in advance
        if(product.method == Enum_Payment_method.bank_transfer) {

            terminal_logger.debug("spendCreditSaas: bank transfer");

            // If credit will suffice only for 14 days - make new invoice
            if (invoice == null && days < 14){

                terminal_logger.debug("spendCreditSaas: bank transfer: It is time to send an invoice");

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
                if (invoice == null) return;

                invoice.notificationInvoiceNew();

                Utilities_Fakturoid_Controller.sendInvoiceEmail(invoice, null);

                return;
            }

            if (invoice == null) {
                terminal_logger.debug("spendCreditSaas: bank transfer: The financial reserves are sufficient.");
                return;
            }

            if(invoice.warning == Enum_Payment_warning.zero_balance && product.credit < 0 && days < -20) {
                terminal_logger.debug("spendCreditSaas: bank transfer: The product is in minus 20 times the average spending");

                invoice.warning = Enum_Payment_warning.deactivation;
                invoice.update();

                invoice.notificationInvoiceReminder("You have reached negative credit limit. Your product is deactivated.");

                Utilities_Fakturoid_Controller.sendInvoiceReminderEmail(invoice,
                        "We are sorry to inform you, that you have reached negative credit limit. Your services are no longer supported. We activate your product again, when we receive payment for your invoice.");

                product.active = false;
                product.update();
                return;
            }

            if(invoice.warning == Enum_Payment_warning.first && product.credit < 0){
                terminal_logger.debug("spendCreditSaas: bank transfer: The product is in negative credit balance");

                invoice.warning = Enum_Payment_warning.zero_balance;
                invoice.update();

                invoice.notificationInvoiceReminder("You have reached zero credit balance. Your services will be supported for next 20 days.");

                Utilities_Fakturoid_Controller.sendInvoiceReminderEmail(invoice,
                        "You have reached zero credit balance. Your services will be supported for next 20 days. If we do not receive your payment till then, we will have to deactivate your services.");

                return;
            }

            if(invoice.warning == Enum_Payment_warning.none && days < 7 ){
                terminal_logger.debug("spendCreditSaas: bank transfer:  The Product is close to zero in financial balance");

                invoice.warning = Enum_Payment_warning.first;
                invoice.update();

                invoice.notificationInvoiceReminder("You will reach zero credit balance soon.");

                Utilities_Fakturoid_Controller.sendInvoiceReminderEmail(invoice,
                        "We did not receive your payment. You will reach zero credit balance soon.");

            }

        }else if(product.method == Enum_Payment_method.credit_card){

            terminal_logger.debug("spendCreditSaas: credit card");

            if(invoice == null && days < 5){

                terminal_logger.debug("spendCreditSaas: credit card: The Product is close to zero in financial balance. Credit will suffice for {}", days);

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
                if (invoice == null) return;

                if (product.on_demand) {
                    try {

                        Utilities_GoPay_Controller.onDemandPayment(invoice);

                    } catch (Exception e) {
                        terminal_logger.internalServerError("spendCreditSaas:", e);

                        invoice = Utilities_GoPay_Controller.singlePayment("Substitute payment", product, invoice);

                        invoice.notificationInvoiceReminder("Failed to take money from your credit card, resolve it manually.");

                        Utilities_Fakturoid_Controller.sendInvoiceReminderEmail(invoice,
                                "We could not take money from your credit card due to some problems. Please use the manual substitute payment through financial section of your Byzance account.");
                    }
                } else {

                    invoice = Utilities_GoPay_Controller.singlePayment("First Payment", product, invoice);

                    invoice.notificationInvoiceReminder("You have to authorize the first payment.");

                    Utilities_Fakturoid_Controller.sendInvoiceEmail(invoice, null);
                }

                return;
            }

            if (invoice == null){
                terminal_logger.debug("spendCreditSaas: credit card: The financial reserves are sufficient.");
                return;
            }

            if(invoice.warning == Enum_Payment_warning.zero_balance && product.credit < 0 && days < -10 ){
                terminal_logger.debug("spendCreditSaas: credit card: The product is in the minus 10 times the average spending");

                invoice.warning = Enum_Payment_warning.deactivation;
                invoice.update();

                invoice.notificationInvoiceReminder("Your product is deactivated.");

                Utilities_Fakturoid_Controller.sendInvoiceReminderEmail(invoice,
                        "We are sorry to inform you, that you have reached negative credit limit. Your services are no longer supported. We activate your product again, when we receive payment for your invoice.");

                product.active = false;
                product.update();
                return;
            }

            if(invoice.warning == Enum_Payment_warning.none && product.credit < 0){
                terminal_logger.debug("spendCreditSaas: credit card: The account is in the minus");

                invoice.warning = Enum_Payment_warning.zero_balance;
                invoice.update();

                invoice.notificationInvoiceReminder("You have 10 days before your product will be deactivated.");

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