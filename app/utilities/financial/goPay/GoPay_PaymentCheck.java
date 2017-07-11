package utilities.financial.goPay;

import com.fasterxml.jackson.databind.JsonNode;
import models.Model_Invoice;
import play.api.Play;
import play.data.Form;
import play.libs.F;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.enums.Enum_Payment_status;
import utilities.financial.fakturoid.Fakturoid_Controller;
import utilities.financial.goPay.helps_objects.GoPay_Result;
import utilities.logger.Class_Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class is used to check GoPay payments when notification is received.
 */
public class GoPay_PaymentCheck {

    // Logger
    private static final Class_Logger terminal_logger = new Class_Logger(GoPay_PaymentCheck.class);

    /**
     * List of ids of payments that needs to be checked.
     * This is the queue.
     */
    private static List<Long> payments = new ArrayList<>();

    /**
     * Method starts the concurrent thread.
     */
    public static void startPaymentCheckThread(){
        terminal_logger.info("startPaymentCheckThread: starting");
        if(!check_payment_thread.isAlive()) check_payment_thread.start();
    }

    /**
     * Method adds payment to the queue and interrupts the thread if it is needed
     * @param payment Long id of payment (invoice.gopay_id)
     */
    public static void addToQueue(Long payment){

        terminal_logger.info("addToQueue: adding payment to queue");

        payments.add(payment);

        if(check_payment_thread.getState() == Thread.State.TIMED_WAITING) {
            terminal_logger.debug("addToQueue: thread is sleeping, waiting for interruption!");
            check_payment_thread.interrupt();
        }
    }

    /**
     * Thread with an infinite loop inside. The thread goes to sleep when there is no payment to check.
     */
    private static Thread check_payment_thread = new Thread(){

        @Override
        public void run(){

            while(true){
                try{

                    if(!payments.isEmpty()) {

                        terminal_logger.debug("check_payment_thread: checking {} payments ", payments.size());

                        Long payment = payments.get(0);

                        checkPayment(payment);

                        payments.remove(payment);

                    } else {

                        terminal_logger.debug("check_payment_thread: no payments, thread is going to sleep");
                        sleep(2100000000);
                    }
                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    terminal_logger.internalServerError("check_payment_thread:", e);
                }
            }
        }
    };

    /**
     * Method gets the payment from GoPay and checks its status.
     * Adds credit to a product if it is paid and fire Fakturoid event "pay_proforma".
     * @param id Id of the given payment.
     */
    private static void checkPayment(Long id) {
        try {

            String local_token = GoPay_Controller.getToken();

            terminal_logger.debug("checkPayment: Asking for payment state: gopay_id - {}", id);

            terminal_logger.debug("checkPayment: Getting invoice");
            Model_Invoice invoice = Model_Invoice.find.where().eq("gopay_id", id).findUnique();
            if (invoice == null) throw new NullPointerException("Invoice is null. Cannot find it in database. Gopay ID was: " + id);

            try {

                WSClient ws = Play.current().injector().instanceOf(WSClient.class);

                // Some operations require more tries
                for (int trial = 5; trial > 0; trial--) {

                    WSResponse response;

                    JsonNode result;

                    try {

                        F.Promise<WSResponse> responsePromise = ws.url(Server.GoPay_api_url + "/payments/payment/" + id)
                                .setContentType("application/x-www-form-urlencoded")
                                .setHeader("Accept", "application/json")
                                .setHeader("Authorization", "Bearer " + local_token)
                                .setRequestTimeout(5000)
                                .get();

                        response = responsePromise.get(5000);

                        result = response.asJson();

                    } catch (Exception e) {
                        terminal_logger.internalServerError("checkPayment:", e);
                        Thread.sleep(2500);
                        continue;
                    }

                    terminal_logger.debug("checkPayment: Status: " + response.getStatus() + " Response " + result);

                    if (response.getStatus() == 200) {
                        final Form<GoPay_Result> form = Form.form(GoPay_Result.class).bind(result);
                        if (form.hasErrors()) throw new Exception("Error while binding Json: " + form.errorsAsJson().toString());
                        GoPay_Result help = form.get();

                        switch (help.state) {

                            case "PAID": {

                                terminal_logger.debug("checkPayment: state - PAID");

                                if (invoice.status != Enum_Payment_status.paid) {

                                    if (!Fakturoid_Controller.fakturoid_post("/invoices/" + invoice.proforma_id + "/fire.json?event=pay_proforma"))
                                        terminal_logger.internalServerError("checkPayment:", new Exception("Error changing status to paid on Fakturoid. Inconsistent state."));

                                    invoice.getProduct().credit_upload(help.amount * 10);
                                    invoice.status = Enum_Payment_status.paid;
                                    invoice.paid = new Date();
                                    invoice.update();

                                    invoice.getProduct().archiveEvent("Payment", "GoPay payment number: " + invoice.gopay_id + " was successful", invoice.id);
                                    invoice.notificationPaymentSuccess(((double) help.amount) / 100);
                                }

                                break;
                            }

                            case "PAYMENT_METHOD_CHOSEN": {

                                terminal_logger.debug("checkPayment: state - PAYMENT_METHOD_CHOSEN");

                                invoice.notificationPaymentIncomplete();

                                break;
                            }

                            case "REFUNDED": {

                                terminal_logger.debug("checkPayment: state - REFUNDED");

                                invoice.getProduct().credit_remove(help.amount * 10);
                                invoice.status = Enum_Payment_status.canceled;
                                invoice.update();

                                invoice.getProduct().archiveEvent("Refund Payment", "GoPay payment number: " + invoice.gopay_id + " was successfully refunded", invoice.id);
                                invoice.getProduct().notificationRefundPaymentSuccess(((double) help.amount) / 100);

                                // TODO úprava ve fakturoidu

                                break;
                            }

                            case "PARTIALLY_REFUNDED": {

                                terminal_logger.debug("checkPayment: state - PARTIALLY_REFUNDED");

                                // TODO úprava ve fakturoidu

                                invoice.getProduct().credit_remove(help.amount * 10);

                                invoice.getProduct().archiveEvent("Refund Payment", "GoPay payment number: " + invoice.gopay_id + " was successfully partially refunded", invoice.id);
                                invoice.getProduct().notificationRefundPaymentSuccess(((double) help.amount) / 100);

                                break;
                            }

                            case "CANCELED": {

                                terminal_logger.debug("checkPayment: state - CANCELED");

                                invoice.status = Enum_Payment_status.canceled;
                                invoice.gw_url = null;
                                invoice.update();

                                if (!Fakturoid_Controller.fakturoid_post("/invoices/" + (invoice.proforma ? invoice.proforma_id : invoice.fakturoid_id) + "/fire.json?event=cancel"))
                                    terminal_logger.internalServerError(new Exception("Error changing status to canceled on Fakturoid. Inconsistent state."));

                                break;
                            }

                            case "TIMEOUTED": {

                                terminal_logger.debug("checkPayment: state - TIMEOUTED");

                                // TODO notifikace

                                break;
                            }

                            case "CREATED": {

                                terminal_logger.debug("checkPayment: state - CREATED");

                                // TODO notifikace

                                break;
                            }

                            default:
                                throw new Exception("Payment state could not be handled. State: " + help.state);
                        }

                        break;

                    } else {

                        throw new Exception("Cannot obtain payment. Response from GoPay was: " + result);
                    }
                }
            } catch (Exception e) {
                // TODO náhrada platby?

                invoice.notificationPaymentFail();

                Fakturoid_Controller.sendInvoiceReminderEmail(invoice, "We were unable to take money from your credit card. " +
                        "Please check your payment credentials or contact our support if anything is unclear. " +
                        "You can also pay this invoice manually through your Byzance account.");
            }
        } catch (Exception e){

            terminal_logger.internalServerError("checkPayment:", e);
        }
    }
}
