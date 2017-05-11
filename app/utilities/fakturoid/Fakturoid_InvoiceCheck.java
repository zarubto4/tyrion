package utilities.fakturoid;

import com.fasterxml.jackson.databind.JsonNode;
import models.Model_Invoice;
import play.api.Play;
import play.data.Form;
import play.libs.F;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.enums.Enum_Payment_method;
import utilities.enums.Enum_Payment_status;
import utilities.logger.Class_Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is used to check status of invoices from Fakturoid.
 */
public class Fakturoid_InvoiceCheck {

    // Logger
    private static final Class_Logger terminal_logger = new Class_Logger(Fakturoid_InvoiceCheck.class);

    /**
     * List of invoices, that needs to be checked. This is the queue
     */
    private static List<Model_Invoice> invoices = new ArrayList<>();

    /**
     * Method starts the concurrent thread.
     */
    public static void startInvoiceCheckThread(){
        terminal_logger.info("Fakturoid_InvoiceCheck:: startInvoiceCheckThread: starting");
        if(!check_invoice_thread.isAlive()) check_invoice_thread.start();
    }

    /**
     * Method adds invoice to queue and interrupts thread if it is sleeping.
     * @param invoice Model invoice that needs to be checked
     */
    public static void addToQueue(Model_Invoice invoice){

        terminal_logger.info("Fakturoid_InvoiceCheck:: addToQueue: adding payment to queue");

        invoices.add(invoice);

        if(check_invoice_thread.getState() == Thread.State.TIMED_WAITING) {
            terminal_logger.debug("GoPay_PaymentCheck:: addToQueue: thread is sleeping, waiting for interruption!");
            check_invoice_thread.interrupt();
        }
    }

    /**
     * Thread with infinite loop inside. If there are not any invoices in the queue, thread goes to sleep.
     */
    private static Thread check_invoice_thread = new Thread(){

        @Override
        public void run(){

            while(true){
                try{

                    if(!invoices.isEmpty()) {

                        terminal_logger.debug("Fakturoid_InvoiceCheck:: check_invoice_thread: checking {} invoices ", invoices.size());

                        Model_Invoice invoice = invoices.get(0);

                        checkInvoice(invoice);

                        invoices.remove(invoice);

                    } else {

                        terminal_logger.debug("Fakturoid_InvoiceCheck:: check_invoice_thread: no invoices, thread is going to sleep");
                        sleep(2100000000);
                    }
                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    terminal_logger.internalServerError("Fakturoid_InvoiceCheck:: check_invoice_thread:", e);
                }
            }
        }
    };

    /**
     * Method gets the invoice from Fakturoid and checks its status.
     * If it is paid this method transforms it to a tax document (non-proforma).
     * If payment method is "bank_transfer" the appropriate amount of credit will be added to product.
     * Method tries 5 times to get the result.
     * @param invoice Given invoice that is being checked.
     */
    private static void checkInvoice(Model_Invoice invoice) {
        try {

            WSClient ws = Play.current().injector().instanceOf(WSClient.class);

            // Some operations require more tries
            for (int trial = 5; trial > 0; trial--) {

                WSResponse response;

                JsonNode result;

                // Get proforma and check if it has a related_id of new invoice
                try {

                    F.Promise<WSResponse> responsePromise = ws.url(Server.Fakturoid_url + "/invoices/" + (invoice.proforma ? invoice.proforma_id : invoice.fakturoid_id) + ".json")
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .setHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(5000)
                            .get();

                    response = responsePromise.get(5000);

                    result = response.asJson();

                } catch (Exception e) {
                    terminal_logger.internalServerError("Fakturoid_InvoiceCheck:: checkInvoice:", e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 200: {

                        terminal_logger.debug("Fakturoid_InvoiceCheck:: checkInvoice: GET: Result: {}", result.toString());

                        // Binding Json with help object
                        final Form<Fakturoid_ResponseInvoice> form = Form.form(Fakturoid_ResponseInvoice.class).bind(result);
                        if(form.hasErrors()) throw new Exception("Error binding Json from Fakturoid: " + form.errorsAsJson().toString());
                        Fakturoid_ResponseInvoice help = form.get();

                        // If it has related_id of new invoice, get the new invoice and update our DB
                        if (invoice.proforma && help.related_id != null) {
                            try {

                                for (int trial2 = 5; trial2 > 0; trial2--) {

                                    WSResponse response2;

                                    JsonNode result2;

                                    try {

                                        F.Promise<WSResponse> responsePromise2 = ws.url(Server.Fakturoid_url + "/invoices/" + help.related_id + ".json")
                                                .setAuth(Server.Fakturoid_secret_combo)
                                                .setContentType("application/json")
                                                .setHeader("User-Agent", Server.Fakturoid_user_agent)
                                                .setRequestTimeout(5000)
                                                .get();

                                        response2 = responsePromise2.get(5000);

                                        terminal_logger.debug("Fakturoid_InvoiceCheck:: checkInvoice: response statust for related_id {} is {}", help.related_id, response2.getStatus());

                                        result2 = response2.asJson();

                                    } catch (Exception e) {
                                        terminal_logger.internalServerError("Fakturoid_InvoiceCheck:: checkInvoice:", e);
                                        Thread.sleep(2500);
                                        continue;
                                    }

                                    switch (response2.getStatus()) {

                                        case 200: {

                                            // Binding Json with help object
                                            final Form<Fakturoid_ResponseInvoice> form2 = Form.form(Fakturoid_ResponseInvoice.class).bind(result2);
                                            if (form2.hasErrors()) throw new Exception("Error binding Json from Fakturoid: " + form2.errorsAsJson().toString());
                                            Fakturoid_ResponseInvoice help2 = form2.get();

                                            terminal_logger.debug("Fakturoid_InvoiceCheck:: checkInvoice: local proforma id: {}, from request proforma id: {}", invoice.proforma_id, help2.related_id);
                                            terminal_logger.debug("Fakturoid_InvoiceCheck:: checkInvoice: local invoice id: {}, from request invoice id: {}", help.related_id, help2.id);

                                            invoice.fakturoid_id = help2.id;
                                            invoice.fakturoid_pdf_url = help2.pdf_url;
                                            invoice.invoice_number = help2.number;
                                            invoice.proforma = false;

                                            invoice.getProduct().archiveEvent("Proforma transformed", "System marked proforma as paid and transformed it to invoice.", invoice.id);

                                            break;
                                        }

                                        default: throw new Exception("Fakturoid returned unhandled state: " + response2.getStatus() + ", Response: " + result2);
                                    }

                                    break;
                                }
                            } catch (Exception e) {
                                terminal_logger.internalServerError("Fakturoid_InvoiceCheck:: checkInvoice:", e);
                                invoice.getProduct().archiveEvent("Proforma paid", "System marked proforma as paid, but cannot transform it to invoice.", invoice.id);
                            }
                        }

                        // Security condition because of artificial callbacks in dev mode
                        if (!help.status.equals("paid")) break;

                        // If bank transfer then upload credit
                        if (invoice.method == Enum_Payment_method.bank_transfer) {

                            invoice.getProduct().credit_upload(invoice.total_price());
                            invoice.paid = new Date();
                        }

                        // If credit card then credit was already uploaded when payment was received, this is just sync with Fakturoid
                        if (invoice.method == Enum_Payment_method.credit_card) {

                            invoice.notificationInvoiceNew();
                            Utilities_Fakturoid_Controller.sendInvoiceEmail(invoice, null);
                        }

                        terminal_logger.debug("Fakturoid_InvoiceCheck:: checkInvoice: set status to 'paid'");

                        invoice.status = Enum_Payment_status.paid;
                        invoice.update();

                        break;
                    }

                    default:
                        throw new Exception("Fakturoid returned unhandled status. Response: " + result);
                }

                break;
            }
        } catch (Exception e) {
            terminal_logger.internalServerError("Fakturoid_InvoiceCheck:: checkInvoice:", e);
        }
    }
}