package utilities.goPay;

import com.fasterxml.jackson.databind.JsonNode;
import models.Model_Invoice;
import play.Logger;
import play.api.Play;
import play.data.Form;
import play.libs.F;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.enums.Enum_Payment_status;
import utilities.fakturoid.Utilities_Fakturoid_Controller;
import utilities.goPay.helps_objects.GoPay_Result;
import utilities.loggy.Loggy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GoPay_PaymentCheck {

    // Logger
    private static Logger.ALogger logger = Logger.of("Loggy");

    private static List<Long> payments = new ArrayList<>(); // Tady se hromadí id plateb, které je potřeba zkontrolovat

    public static void startPaymentCheckThread(){
        logger.info("GoPay_PaymentCheck:: startPaymentCheckThread: starting");
        if(!check_payment_thread.isAlive()) check_payment_thread.start();
    }

    public static void addToQueue(Long payment){

        logger.info("GoPay_PaymentCheck:: addToQueue: adding payment to queue");

        payments.add(payment);

        if(check_payment_thread.getState() == Thread.State.TIMED_WAITING) {
            logger.debug("GoPay_PaymentCheck:: addToQueue: thread is sleeping, waiting for interruption!");
            check_payment_thread.interrupt();
        }
    }

    private static Thread check_payment_thread = new Thread(){

        @Override
        public void run(){

            while(true){
                try{

                    if(!payments.isEmpty()) {

                        logger.debug("GoPay_PaymentCheck:: check_payment_thread: checking {} payments ", payments.size());

                        Long payment = payments.get(0);

                        checkPayment(payment);

                        payments.remove(payment);

                    } else {

                        logger.debug("GoPay_PaymentCheck:: check_payment_thread: no payments, thread is going to sleep");
                        sleep(2100000000);
                    }
                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    Loggy.internalServerError("GoPay_PaymentCheck:: check_payment_thread:", e);
                }
            }
        }
    };

    private static void checkPayment(Long id) {
        try {

            String local_token = Utilities_GoPay_Controller.getToken();

            logger.debug("GoPay_PaymentCheck:: checkPayment: Asking for payment state: gopay_id - {}", id);

            logger.debug("GoPay_PaymentCheck:: checkPayment: Getting invoice");
            Model_Invoice invoice = Model_Invoice.find.where().eq("gopay_id", id).findUnique();
            if (invoice == null) throw new NullPointerException("Invoice is null. Cannot find it in database. Gopay ID was: " + id);

            WSClient ws = Play.current().injector().instanceOf(WSClient.class);
            F.Promise<WSResponse> responsePromise = ws.url(Server.GoPay_api_url +  "/payments/payment/" + id)
                    .setContentType("application/x-www-form-urlencoded")
                    .setHeader("Accept", "application/json")
                    .setHeader("Authorization" , "Bearer " + local_token)
                    .setRequestTimeout(5000)
                    .get();

            WSResponse response = responsePromise.get(5000);

            JsonNode result = response.asJson();

            logger.debug("GoPay_PaymentCheck:: checkPayment: Status: " + response.getStatus() + " Response " + result);

            if (response.getStatus() == 200) {
                final Form<GoPay_Result> form = Form.form(GoPay_Result.class).bind(result);
                if (form.hasErrors()) throw new Exception("Error while binding Json: " + form.errorsAsJson().toString());
                GoPay_Result help = form.get();

                switch (help.state){

                    case "PAID":{

                        logger.debug("GoPay_PaymentCheck:: checkPayment: state - PAID");

                        if (invoice.status != Enum_Payment_status.paid) {

                            /*
                            if (!Utilities_Fakturoid_Controller.fakturoid_delete("/invoices/" + invoice.fakturoid_id + ".json"))
                                Loggy.internalServerError("GoPay_PaymentCheck:: checkPayment:", new Exception("Error removing proforma from Fakturoid"));

                            invoice = Utilities_Fakturoid_Controller.create_paid_invoice(invoice);
                            */

                            if (!Utilities_Fakturoid_Controller.fakturoid_post("/invoices/" + invoice.fakturoid_id + "/fire.json?event=pay_proforma"))
                                Loggy.internalServerError("GoPay_PaymentCheck:: checkPayment:", new Exception("Error changing status to paid on Fakturoid. Inconsistent state."));

                            invoice.getProduct().credit_upload(help.amount * 10);
                            invoice.status = Enum_Payment_status.paid;
                            invoice.paid = new Date();
                            invoice.update();

                            invoice.getProduct().archiveEvent("Payment", "GoPay payment number: " + invoice.gopay_id + " was successful", invoice.id);

                            // TODO notifikace
                        }

                        break;
                    }

                    case "PAYMENT_METHOD_CHOSEN":{

                        logger.debug("GoPay_PaymentCheck:: checkPayment: state - PAYMENT_METHOD_CHOSEN");

                        // TODO notifikace - zdá se že nedokončil platbu

                        break;
                    }

                    case  "REFUNDED":{

                        logger.debug("GoPay_PaymentCheck:: checkPayment: state - REFUNDED");

                        invoice.getProduct().credit_remove(help.amount * 10);
                        invoice.status = Enum_Payment_status.canceled;
                        invoice.update();

                        invoice.getProduct().archiveEvent("Refund Payment", "GoPay payment number: " + invoice.gopay_id + " was successfully refunded", invoice.id);

                        // TODO úprava ve fakturoidu

                        break;
                    }

                    case  "PARTIALLY_REFUNDED":{

                        logger.debug("GoPay_PaymentCheck:: checkPayment: state - PARTIALLY_REFUNDED");

                        // TODO úprava ve fakturoidu

                        invoice.getProduct().credit_remove(help.amount * 10);

                        break;
                    }

                    case  "CANCELED":{

                        logger.debug("GoPay_PaymentCheck:: checkPayment: state - CANCELED");

                        invoice.status = Enum_Payment_status.canceled;
                        invoice.gw_url = null;
                        invoice.update();

                        break;
                    }

                    case  "TIMEOUTED":{

                        logger.debug("GoPay_PaymentCheck:: checkPayment: state - TIMEOUTED");

                        // TODO notifikace

                        break;
                    }

                    case  "CREATED":{

                        logger.debug("GoPay_PaymentCheck:: checkPayment: state - CREATED");

                        // TODO notifikace

                        break;
                    }

                    default: throw new Exception("Payment state could not be handled. State: " + help.state);
                }



            } else {
                // TODO notifikace a náhrada platby?

                Utilities_Fakturoid_Controller.sendInvoiceReminderEmail(invoice, "We were unable to take money from your credit card. Please check your payment credentials or contact our support if anything is unclear. You can also pay this invoice manually through your Byzance account.");

                throw new Exception("Cannot obtain payment. Response from GoPay is: " + result);
            }
        } catch (Exception e){

            Loggy.internalServerError("GoPay_PaymentCheck:: checkPayment:", e);
        }
    }
}
