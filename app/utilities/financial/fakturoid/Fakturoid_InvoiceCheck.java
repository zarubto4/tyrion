package utilities.financial.fakturoid;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.Model_Invoice;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Lang;
import play.libs.F;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.enums.PaymentMethod;
import utilities.enums.PaymentStatus;
import utilities.logger.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * This class is used to check status of invoices from Fakturoid.
 */
public class Fakturoid_InvoiceCheck {
/*
    // Logger
    private static final Logger logger = new Logger(Fakturoid_InvoiceCheck.class);

    private WSClient ws;
    private FormFactory formFactory;
    private Fakturoid fakturoid;

    @Inject
    public Fakturoid_InvoiceCheck(WSClient ws, FormFactory formFactory, Fakturoid fakturoid) {
        this.ws = ws;
        this.formFactory = formFactory;
        this.fakturoid = fakturoid;
    }

    /**
     * List of invoices, that needs to be checked. This is the queue
     *//*
    private static List<Model_Invoice> invoices = new ArrayList<>();

    /**
     * Method starts the concurrent thread.
     *//*
    public void startThread() {
        logger.info("startThread: starting");
        if (!check_invoice_thread.isAlive()) check_invoice_thread.start();
    }

    /**
     * Method adds invoice to queue and interrupts thread if it is sleeping.
     * @param invoice Model invoice that needs to be checked
     *//*
    public void addToQueue(Model_Invoice invoice) {

        logger.info("addToQueue: adding payment to queue");

        invoices.add(invoice);

        if (check_invoice_thread.getState() == Thread.State.TIMED_WAITING) {
            logger.debug("GoPay_PaymentCheck:: addToQueue: thread is sleeping, waiting for interruption!");
            check_invoice_thread.interrupt();
        }
    }

    /**
     * Thread with infinite loop inside. If there are not any invoices in the queue, thread goes to sleep.
     *//*
    private static Thread check_invoice_thread = new Thread() {

        @Override
        public void run() {

            while(true) {
                try {

                    if (!invoices.isEmpty()) {

                        logger.debug("check_invoice_thread: checking {} invoices ", invoices.size());

                        Model_Invoice invoice = invoices.get(0);

                        //checkInvoice(invoice);

                        invoices.remove(invoice);

                    } else {

                        logger.debug("check_invoice_thread: no invoices, thread is going to sleep");
                        sleep(2100000000);
                    }
                } catch (InterruptedException i) {
                    // Do nothing
                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }
        }
    };

    /**
     * Method gets the invoice from Fakturoid and checks its status.
     * If it is paid this method transforms it to a tax document (non-proforma).
     * If payment method is "BANK_TRANSFER" the appropriate amount of credit will be added to product.
     * Method tries 5 times to get the result.
     * @param invoice Given invoice that is being checked.
     *//*
    public void checkInvoice(Model_Invoice invoice) {
        try {

            // Some operations require more tries
            for (int trial = 5; trial > 0; trial--) {

                WSResponse response;

                JsonNode result;

                // Get proforma and check if it has a related_id of new invoice
                try {

                    CompletionStage<WSResponse> responsePromise = ws.url(Server.Fakturoid_url + "/invoices/" + (invoice.proforma ? invoice.proforma_id : invoice.fakturoid_id) + ".json")
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .addHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(Duration.ofSeconds(5))
                            .get();

                    response = responsePromise.toCompletableFuture().get();

                    result = response.asJson();

                } catch (Exception e) {
                    logger.internalServerError(e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 200: {

                        logger.debug("checkInvoice: GET: Result: {}", result.toString());

                        // Binding Json with help object
                        final Form<Fakturoid_ResponseInvoice> form = formFactory.form(Fakturoid_ResponseInvoice.class).bind(result);
                        if (form.hasErrors()) throw new Exception("Error binding Json from Fakturoid: " + form.errorsAsJson(Lang.forCode("en-US")).toString());
                        Fakturoid_ResponseInvoice help = form.get();

                        // If it has related_id of new invoice, get the new invoice and update our DB
                        if (invoice.proforma && help.related_id != null) {
                            try {

                                for (int trial2 = 5; trial2 > 0; trial2--) {

                                    WSResponse response2;

                                    JsonNode result2;

                                    try {

                                        CompletionStage<WSResponse> responsePromise2 = ws.url(Server.Fakturoid_url + "/invoices/" + help.related_id + ".json")
                                                .setAuth(Server.Fakturoid_secret_combo)
                                                .setContentType("application/json")
                                                .addHeader("User-Agent", Server.Fakturoid_user_agent)
                                                .setRequestTimeout(Duration.ofSeconds(5))
                                                .get();

                                        response2 = responsePromise2.toCompletableFuture().get();

                                        logger.debug("checkInvoice: response statust for related_id {} is {}", help.related_id, response2.getStatus());

                                        result2 = response2.asJson();

                                    } catch (Exception e) {
                                        logger.internalServerError(e);
                                        Thread.sleep(2500);
                                        continue;
                                    }

                                    switch (response2.getStatus()) {

                                        case 200: {

                                            // Binding Json with help object
                                            final Form<Fakturoid_ResponseInvoice> form2 = formFactory.form(Fakturoid_ResponseInvoice.class).bind(result2);
                                            if (form2.hasErrors()) throw new Exception("Error binding Json from Fakturoid: " + form2.errorsAsJson().toString());
                                            Fakturoid_ResponseInvoice help2 = form2.get();

                                            logger.debug("checkInvoice: local proforma id: {}, from request proforma id: {}", invoice.proforma_id, help2.related_id);
                                            logger.debug("checkInvoice: local invoice id: {}, from request invoice id: {}", help.related_id, help2.id);

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
                                logger.internalServerError(e);
                                invoice.getProduct().archiveEvent("Proforma paid", "System marked proforma as paid, but cannot transform it to invoice.", invoice.id);
                            }
                        }

                        // Security condition because of artificial callbacks in dev mode
                        if (!help.status.equals("paid")) break;

                        // If bank transfer then upload credit
                        if (invoice.method == PaymentMethod.BANK_TRANSFER) {

                            invoice.getProduct().credit_upload(invoice.total_price());
                            invoice.paid = new Date();
                        }

                        // If credit card then credit was already uploaded when payment was received, this is just sync with Fakturoid
                        if (invoice.method == PaymentMethod.CREDIT_CARD) {

                            invoice.notificationInvoiceNew();
                            fakturoid.sendInvoiceEmail(invoice, null);
                        }

                        logger.debug("checkInvoice: set status to 'paid'");

                        invoice.status = PaymentStatus.PAID;
                        invoice.update();

                        break;
                    }

                    default:
                        throw new Exception("Fakturoid returned unhandled status. Response: " + result);
                }

                break;
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }*/
}