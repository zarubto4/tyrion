package utilities.financial.fakturoid;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import controllers._BaseController;
import controllers._BaseFormFactory;
import models.*;
import play.Environment;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Result;
import utilities.Server;
import utilities.emails.Email;
import utilities.enums.Currency;
import utilities.enums.InvoiceStatus;
import utilities.enums.PaymentWarning;
import utilities.enums.ProductEventType;
import utilities.financial.fakturoid.helps_objects.Fakturoid_Invoice;
import utilities.financial.fakturoid.helps_objects.Fakturoid_InvoiceItem;
import utilities.financial.fakturoid.helps_objects.Fakturoid_Subject;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.permission.PermissionService;
import utilities.scheduler.SchedulerService;
import utilities.swagger.input.Swagger_Fakturoid_Callback;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CompletionStage;

/**
 * This class is used to interact with Fakturoid or sending emails with invoices.
 * (Creating invoices and subjects or callbacks from Fakturoid.)
 */
@Singleton
public class FakturoidService extends _BaseController {

    // Logger
    private static final Logger logger = new Logger(FakturoidService.class);

    @javax.inject.Inject
    public FakturoidService(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerService scheduler, PermissionService permissionService) {
        super(environment, ws, formFactory, youTrack, config, scheduler, permissionService);
    }

// PUBLIC CONTROLLERS METHODS ##########################################################################################

    /**
     * RestApi callback notification from Fakturoid is received here.
     * If invoice was paid, we mark the proforma as paid and read the real
     * invoice (tax document) from Fakturoid.
     *
     * @return Result ok is returned every time, errors are only logged.
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result fakturoid_callback() {
        try {
            // Get and Validate Object
            Swagger_Fakturoid_Callback help = formFactory.formFromRequestWithValidation(Swagger_Fakturoid_Callback.class);

            logger.info("fakturoid_callback: Body: {}", getBodyAsJson());

            // Finding in DB
            Model_Invoice invoice = Model_Invoice.find.query().where().eq("proforma_id", help.invoice_id).findOne();
            if (invoice == null) {
                throw new NullPointerException("Invoice is null. Cannot find it in database.");
            }

            switch (help.status) {
                case "paid":
                    // Set status as paid in order to avoid the confusion of the user,
                    // if we are not able to get details about the invoice immediately.

                    if(invoice.status == InvoiceStatus.PAID) {
                        logger.warn("Invoice {} is already paid!", invoice.id);
                        return ok();
                    }

                    invoice.status = InvoiceStatus.PAID;
                    invoice.paid = help.paid_at;
                    invoice.update();

                    invoice.saveEvent(invoice.updated, ProductEventType.INVOICE_PAYMENT_RECEIVED);

                    new Thread(() -> {
                        try {
                            this.checkPaidProforma(invoice);
                        }
                        catch(Exception e) {
                            logger.error("We receive paid event from Fakturoid, " +
                                    "but we were not able to retrieve the invoice details from Fakturoid!", e);
                        }
                    });
                    break;
                case "overdue":
                    invoice.status = InvoiceStatus.OVERDUE;
                    invoice.overdue = new Date();
                    invoice.update();

                    invoice.notificationInvoiceOverdue();
                    sendInvoiceReminderEmail(invoice, "Invoice for your product is overdue.");
                    break;
                default:
                    throw new Exception("Unknown invoice status. Callback payload: " + getBodyAsJson().toString());
            }

            return ok();

        } catch (Exception e) {
            logger.internalServerError(e);
            return ok();
        }
    }

    // PRIVATE API CALLS METHODS #######################################################################################

    /**
     * Get an invoice / proforma from Fakturoid.
     *
     * @param id of an invoice / proforma.
     * @return
     */
    public Fakturoid_Invoice getInvoice(Long id) {
        try {
            JsonNode result = makeRequest(RequestType.GET, "/invoices/" + id + ".json");
            if (result != null) {
                return formFactory.formFromJsonWithValidation(Fakturoid_Invoice.class, result);
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return null;
    }

    /**
     * Method creates object in Fakturoid and saves provided info into model invoice.
     * If it is a first invoice, that means the product owner is not registered in Fakturoid,
     * method tries to create subject in Fakturoid.
     *
     * @param invoice Model invoice that needs to be synchronized to Fakturoid.
     * @return invoice with details from Fakturoid or null if error occur.
     */
    private Fakturoid_Invoice createProforma(Model_Invoice invoice) {
        try {
            Fakturoid_Invoice fakturoid_invoice = new Fakturoid_Invoice();
            fakturoid_invoice.custom_id = invoice.getProduct().id.toString();
            fakturoid_invoice.currency = Currency.USD.getCode();
            fakturoid_invoice.proforma = true;
            fakturoid_invoice.partial_proforma = false;
            fakturoid_invoice.subject_id = invoice.getProduct().owner.contact.fakturoid_subject_id;

            for (Model_InvoiceItem item : invoice.invoice_items) {
                Fakturoid_InvoiceItem fakturoidItem = new Fakturoid_InvoiceItem();
                fakturoidItem.name = item.name;
                fakturoidItem.quantity = item.quantity.doubleValue();
                fakturoidItem.unit_name = item.unit_name;
                fakturoidItem.unit_price = item.unit_price.doubleValue();
                fakturoidItem.vat_rate = item.vat_rate;

                if(fakturoid_invoice.lines == null) {
                    fakturoid_invoice.lines = new ArrayList<>();
                }
                fakturoid_invoice.lines.add(fakturoidItem);
            }

            logger.debug("createProforma::  Sending Proforma to Fakturoid");
            JsonNode result = makeRequest(RequestType.POST, "/invoices.json", Json.toJson(fakturoid_invoice));

            if (result != null) {
                return formFactory.formFromJsonWithValidation(Fakturoid_Invoice.class, result);
            }


        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return null;
    }

    public boolean createAndUpdateProforma(Model_Invoice invoice) {
        if(invoice.status != InvoiceStatus.UNFINISHED) {
            logger.internalServerError(new Exception("Invoice is in a wrong state! Invoice ID = " + invoice.id + ", status: " + invoice.status + "."));
            return false;
        }

        // check if fakturoid subject is created
        Model_Contact contact = invoice.getProduct().owner.contact;
        if (contact.fakturoid_subject_id == null) {
            logger.internalServerError(new NullPointerException("Fakturoid subject id was null. Should not happen. Product ID = " + invoice.getProduct().id));

            contact.fakturoid_subject_id = createSubject(contact).id;
            if (contact.fakturoid_subject_id == null) {
                return false;
            }
            contact.update();
        }

        Fakturoid_Invoice fakturoidProforma = createProforma(invoice);
        if (fakturoidProforma != null) {
            logger.debug("Invoice {} created in Fakturoid. Update and inform the user.", invoice.id);

            invoice.proforma_id = fakturoidProforma.id;
            invoice.proforma_pdf_url = fakturoidProforma.pdf_url;
            invoice.invoice_number = fakturoidProforma.number;
            invoice.proforma = true;
            invoice.warning = PaymentWarning.NONE;
            invoice.status = InvoiceStatus.PENDING;
            invoice.total_price_with_vat = fakturoidProforma.total;
            invoice.total_price_without_vat = fakturoidProforma.subtotal;
            invoice.status = InvoiceStatus.PENDING;
            invoice.issued = new Date();
            invoice.public_html_url = fakturoidProforma.public_html_url;
            invoice.update();

            // save event
            invoice.saveEvent(invoice.issued, ProductEventType.INVOICE_ISSUED);

            // send notification and email
            invoice.notificationInvoiceNew();
            sendInvoiceEmail(invoice, contact.invoice_email);

            if(fakturoidProforma.lines == null || fakturoidProforma.lines.size() != invoice.invoice_items.size()) {
                invoice.sendMessageToAdmin("Number items in our invoice and Fakturoid does not fit!");
            }

            return true;
        } else {
            logger.debug("Faild to create Invoice in Fakturoid. Try later.", invoice.id);
        }

        return false;
    }

    /**
     * Method creates subject in Fakturoid. Tries 5 times to get the result.
     *
     * @param paymentDetails Model PaymentDetails with info about the customer.
     * @return String id of the subject from Fakturoid. Null if some error occurs.
     */
    public Fakturoid_Subject createSubject(Model_Contact paymentDetails) {
        try {
            Fakturoid_Subject subject = createFakturoidSubjectObject(paymentDetails);

            JsonNode result = makeRequest(RequestType.POST, "/subjects.json", Json.toJson(subject));
            if (result != null) {
                return formFactory.formFromJsonWithValidation(Fakturoid_Subject.class, result);
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return null;
    }

    /**
     * Updates subject info in Fakturoid.
     *
     * @param paymentDetails Updated model PaymentDetails.
     * @return Boolean true if it succeeded or false if it failed.
     */
    public boolean updateSubject(Model_Contact paymentDetails) {
        try {
            Fakturoid_Subject subject = createFakturoidSubjectObject(paymentDetails);

            JsonNode result = makeRequest(RequestType.PATCH, "/subjects/" + paymentDetails.fakturoid_subject_id + ".json", Json.toJson(subject));
            if (result != null) {
                return true;
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return false;
    }

    /**
     * Take payment details and transform them into fakturoid subject, Can be uset to update or create the subject.
     *
     * @param paymentDetails
     * @return
     * @throws Exception
     */
    private Fakturoid_Subject createFakturoidSubjectObject(Model_Contact paymentDetails) {
        Fakturoid_Subject subject = new Fakturoid_Subject();
        subject.name = paymentDetails.name;
        subject.street = paymentDetails.street + " " + paymentDetails.street_number;
        subject.city = paymentDetails.city;
        subject.zip = paymentDetails.zip_code;
        subject.email = paymentDetails.invoice_email;

        subject.vat_no = paymentDetails.company_vat_number;
        subject.registration_no = paymentDetails.company_registration_no;
        subject.phone = paymentDetails.company_authorized_phone;
        subject.web = paymentDetails.company_web;

        return subject;
    }

    /**
     * Method gets the PDF from Fakturoid.
     * If the invoice is just created there might be some latency and PDF creation could be delayed.
     * So the method will try to get the invoice 3 times with 2,5s interval before it gives up.
     *
     * @param type    String type of a document. ("proforma", "invoice")
     * @param invoice Given model invoice to get the PDF for.
     * @return Byte array represented PDF file.
     */
    public byte[] downloadPdfInvoice(String type, Model_Invoice invoice) {

        logger.debug("downloadPdfInvoice: type: {}", type);

        int terminator = 3;
        while (terminator >= 0) {
            try {

                String url;

                if (type.equals("proforma") && invoice.proforma_pdf_url != null) url = invoice.proforma_pdf_url;
                else url = invoice.fakturoid_pdf_url;

                logger.debug("downloadPdfInvoice: Getting PDF invoice from url: {}", url);

                CompletionStage<WSResponse> responsePromise = ws.url(url)
                        .setAuth(Server.Fakturoid_secret_combo)
                        .addHeader("User-Agent", Server.Fakturoid_user_agent)
                        .setRequestTimeout(Duration.ofSeconds(5))
                        .get();

                WSResponse promise = responsePromise.toCompletableFuture().get();

                if (promise.getStatus() == 200) {
                    logger.debug("downloadPdfInvoice: Status = {}. PDF Download successfully to byte[]", promise.getStatus());
                    return promise.asByteArray();

                } else {

                    logger.warn("downloadPdfInvoice: Status = {}. PDF Download unsuccessful.", promise.getStatus());

                    --terminator;
                    Thread.sleep(2500);
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }
        throw new NullPointerException("Unable to download PDF invoice.");
    }

    // PRIVATE METHODS #################################################################################################

    /**
     * Check if given proforma was paid. If yes, update the invoice in our database.
     *
     * @param invoice
     * @return whether invoice was paid and updated
     * @throws Exception error while trying to update the invoice
     */
    public boolean checkPaidProforma(Model_Invoice invoice) throws Exception {
        if (invoice.proforma_id == null) {
            throw new Exception("Invoice: " + invoice.id + ". No proforma_id in the invoice!");
        }

        if (invoice.fakturoid_id != null) {
            throw new Exception("Invoice: " + invoice.id + ". Fakturoid_id is already filled!");
        }

        Fakturoid_Invoice fakturoidProforma = getInvoice(invoice.proforma_id);
        if (fakturoidProforma == null) {
            throw new Exception("Invoice: " + invoice.id + ". No proforma received from Fakturoid.");
        }

        if(fakturoidProforma.status == null || !fakturoidProforma.status.equals("paid")) {
            return false;
        }

        if(fakturoidProforma.related_id == null) {
            throw new Exception("Invoice: " + invoice.id + ". Proforma paid but no related_id attribute present!");
        }

        Fakturoid_Invoice fakturoidInvoice = getInvoice(fakturoidProforma.related_id);
        if (fakturoidInvoice == null) {
            return false;
        }

        // till now we did not register the payment
        if(invoice.status != InvoiceStatus.PAID) {
            invoice.saveEvent(new Date(), ProductEventType.INVOICE_PAYMENT_RECEIVED);
        }

        invoice.status = InvoiceStatus.PAID;
        invoice.paid = fakturoidInvoice.paid_at;
        invoice.fakturoid_id = fakturoidInvoice.id;
        invoice.fakturoid_pdf_url = fakturoidInvoice.pdf_url;
        invoice.proforma = false;
        invoice.invoice_number = fakturoidInvoice.number;
        invoice.update();

        invoice.notificationPaymentSuccess();
        sendInvoiceEmail(invoice, invoice.product.owner.contact.invoice_email);
        logger.debug("Invoice {} was paid and proforma was turned to invoice.", invoice.id);

        return true;
    }

    /**
     * Method tries to download the PDF file from Fakturoid and generates an email with the invoice as attachment.
     *
     * @param invoice Model invoice that is being sent.
     * @param mail    String mail that the invoice is sent to. If null default invoice_email from contact is used.
     */
    public void sendInvoiceEmail(Model_Invoice invoice, String mail) {
        try {

            logger.debug("sendInvoiceEmail: Trying send PDF Invoice to User Email");

            byte[] body = this.downloadPdfInvoice(invoice.proforma ? "proforma" : "invoice", invoice);

            if (body.length < 1) {
                logger.warn("Incoming File from Fakturoid is empty!");
                return;
            }

            if (mail == null) mail = invoice.getProduct().owner.contact.invoice_email;

            logger.debug("sendInvoiceEmail: PDF with invoice was successfully downloaded from Fakturoid");

            new Email()
                    .text("Dear customer,")
                    .text("Please find an enclosed invoice for the services you ordered.")
                    .text("State of the invoice: " + (invoice.status == InvoiceStatus.PAID ? "<b>PAID</b>" : "To be paid") +".")
                    .text("In case of questions, please contact our financial department.")
                    .text("Best regards, Byzance Team")
                    .attachmentPDF(invoice.invoice_number + ".pdf", body)
                    .send(mail, "Invoice " + invoice.issued);

            logger.debug("sendInvoiceEmail: Email was successfully sent");

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    /**
     * Method is used to send a reminder email, that some problem has happened during payment for his product.
     *
     * @param invoice Model invoice that is being sent.
     * @param message String contents of the email.
     */
    public void sendInvoiceReminderEmail(Model_Invoice invoice, String message) {
        try {

            byte[] body = this.downloadPdfInvoice(invoice.proforma ? "proforma" : "invoice", invoice);

            Model_Product product = invoice.getProduct();

            new Email()
                    .text("Dear customer,")
                    .text(message)
                    //.text("We have problems with payment of your services on this month. Log in and check your credit parameters. " + expiry + " Please contact us immediately if something is unclear.")
                    .text("See the attached invoice.")
                    .text("Best regards, Byzance Team")
                    .attachmentPDF(invoice.invoice_number + ".pdf", body)
                    .send(product.owner.contact.invoice_email, "Invoice from " + invoice.issued + " - Reminder");


        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    // PRIVATE HELPERS METHODS #########################################################################################
    private enum RequestType {
        POST, GET, PUT, PATCH, DELETE
    }

    private JsonNode makeRequest(RequestType requesTtype, String url) throws Exception {
        return makeRequest(requesTtype, url, null);
    }

    private JsonNode makeRequest(RequestType requesTtype, String url, JsonNode data) throws Exception {

        for (int trial = 5; trial > 0; trial--) {
            logger.debug("Fakturoid_request {}, {}. Number of remaining tries: {}", requesTtype, url, trial);

            WSResponse response;
            try {
                if (trial < 5) {
                    Thread.sleep(2500);
                }

                WSRequest wsRequest = ws.url(Server.Fakturoid_url + url)
                        .setAuth(Server.Fakturoid_secret_combo)
                        .setContentType("application/json")
                        .addHeader("User-Agent", Server.Fakturoid_user_agent)
                        .setRequestTimeout(Duration.ofSeconds(5));

                CompletionStage<WSResponse> responsePromise;
                switch (requesTtype) {
                    case PUT:
                        responsePromise = wsRequest.put(data == null ? Json.toJson("{}") : data);
                        break;
                    case POST:
                        responsePromise = wsRequest.post(data == null ? Json.toJson("{}") : data);
                        break;
                    case GET:
                        responsePromise = wsRequest.get();
                        break;
                    case DELETE:
                        responsePromise = wsRequest.delete();
                        break;
                    case PATCH:
                        responsePromise = wsRequest.patch(data);
                        break;
                    default:
                        logger.error("Unknown request type!");
                        return null;
                }

                response = responsePromise.toCompletableFuture().get();

            } catch (Exception e) {
                logger.internalServerError(e);
                continue;
            }

            if (response == null) {
                logger.debug("Request was not successful, we have no response.");
                continue;
            }

            System.out.println(url + " " + response.getStatus() + " " + response.getBody() + " ");
            switch (response.getStatus()) {
                case 200:
                case 201:
                    logger.debug("fakturoid_request url {}: Status: 200", url);
                    return response.asJson();
                case 400:
                    logger.debug("fakturoid_request url {}: Status: 400 - Bad Request. Response body: {}", url, response.getBody());
                case 403:
                    logger.debug("fakturoid_request url {}: Status: 403 - Forbidden. Response body: {}", url, response.getBody());
                case 422:
                    logger.debug("fakturoid_request url {}: Status: 422 - Unprocessable Entity. Response body: {}", url, response.getBody());
                default:
                    logger.debug("fakturoid_request url {}: Status: {}. Response body: {}", url, response.getStatus(), response.getBody());
            }
        }

        return null;
    }

}