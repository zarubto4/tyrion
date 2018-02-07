package utilities.financial.fakturoid;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.Model_PaymentDetails;
import models.Model_Product;
import models.Model_Invoice;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Lang;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Server;
import utilities.emails.Email;
import utilities.enums.Currency;
import utilities.enums.PaymentMethod;
import utilities.enums.PaymentStatus;
import utilities.enums.PaymentWarning;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_Fakturoid_Callback;

import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CompletionStage;

/**
 * This class is used to interact with Fakturoid or sending emails with invoices.
 * (Creating invoices and subjects or callbacks from Fakturoid.)
 */
public class Fakturoid extends Controller {

    // Logger
    private static final Logger logger = new Logger(Fakturoid.class);

    private FormFactory formFactory;
    private WSClient ws;

    @Inject
    public Fakturoid(FormFactory formFactory, WSClient ws, Fakturoid_InvoiceCheck invoiceCheck) {
        this.formFactory = formFactory;
        this.ws = ws;
    }

// PUBLIC CONTROLLERS METHODS ##########################################################################################

    /**
     * RestApi callback notification from Fakturoid is received here.
     * Invoice is passed to Fakturoid_InvoiceCheck.class if the status is paid,
     * so it could be checked and transformed to a tax document.
     * @return Result ok is returned every time, errors are only logged.
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result fakturoid_callback() {
        try {

            // Binding Json with help object
            final Form<Swagger_Fakturoid_Callback> form = formFactory.form(Swagger_Fakturoid_Callback.class).bindFromRequest();
            if (form.hasErrors()) throw new Exception("Error binding Json from Fakturoid: " + form.errorsAsJson().toString());
            Swagger_Fakturoid_Callback help = form.get();

            logger.warn("fakturoid_callback: Body: {}", request().body().asJson());

            // Finding in DB
            Model_Invoice invoice = Model_Invoice.find.query().where().eq("proforma_id", help.invoice_id).findOne();
            if (invoice == null) throw new NullPointerException("Invoice is null. Cannot find it in database.");

            switch (help.status) {

                case "paid": {

                    new Thread(() -> this.checkInvoice(invoice));

                    break;
                }

                case "overdue":{

                    invoice.status = PaymentStatus.OVERDUE;
                    invoice.overdue = new Date();
                    invoice.update();

                    invoice.notificationInvoiceOverdue();
                    sendInvoiceReminderEmail(invoice,
                            "Invoice for your product is overdue.");

                    break;
                }

                default: throw new Exception("Unknown invoice status. Callback payload: " + request().body().asJson().toString());
            }

            return ok();

        } catch (Exception e) {
            logger.internalServerError(e);
            return ok();
        }
    }

// PRIVATE EXECUTIVE METHODS ###########################################################################################

    /**
     * Method creates object in Fakturoid and saves provided info into model invoice.
     * If it is a first invoice, that means the product owner is not registered in Fakturoid,
     * method tries to create subject in Fakturoid.
     * @param invoice Model invoice that needs to be synchronized to Fakturoid.
     * @return invoice with details from Fakturoid or null if error occur.
     */
    public Model_Invoice create_proforma(Model_Invoice invoice) {
        try {

            Fakturoid_Invoice fakturoid_invoice = new Fakturoid_Invoice();
            fakturoid_invoice.custom_id = invoice.getProduct().id;
            fakturoid_invoice.client_name = invoice.getProduct().payment_details.company_name;
            fakturoid_invoice.currency = Currency.USD;
            fakturoid_invoice.lines = invoice.invoice_items();
            fakturoid_invoice.proforma = true;
            fakturoid_invoice.partial_proforma = false;

            if (invoice.product.fakturoid_subject_id == null) {

                logger.internalServerError(new NullPointerException("Fakturoid subject id was null. Should not happen. Product ID = " + invoice.getProduct().id));

                // Pokud ne tak ho vytvořím
                invoice.getProduct().fakturoid_subject_id = create_subject(invoice.getProduct().payment_details);
                if (invoice.product.fakturoid_subject_id == null) return null;
                invoice.getProduct().update();
            }

            fakturoid_invoice.subject_id = invoice.getProduct().fakturoid_subject_id;

            logger.debug("create_proforma::  Sending Proforma to Fakturoid");

            for (int trial = 5; trial > 0; trial--) {

                WSResponse response;

                JsonNode result;

                try {

                    CompletionStage<WSResponse> responsePromise = ws.url(Server.Fakturoid_url + "/invoices.json")
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .addHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(Duration.ofSeconds(5))
                            .post(Json.toJson(fakturoid_invoice));

                    response = responsePromise.toCompletableFuture().get();

                    result = response.asJson();

                } catch (Exception e) {
                    logger.internalServerError(e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 201: {

                        logger.debug("create_proforma: POST: Result: {}", result.toString());

                        if (!result.has("id")) {
                            throw new NullPointerException("Invoice from Fakturoid does not contain ID.");
                        }

                        invoice.proforma_id = result.get("id").asLong();
                        invoice.proforma_pdf_url = result.get("pdf_url").asText();
                        invoice.invoice_number = result.get("number").asText();
                        invoice.proforma = true;
                        invoice.warning = PaymentWarning.NONE;
                        invoice.status = PaymentStatus.PENDING;
                        invoice.save();

                        invoice.getProduct().archiveEvent("Proforma created", "System created proforma", invoice.id);

                        logger.debug("create_proforma: Saving Invoice");

                        return invoice;
                    }

                    case 422: {

                        logger.debug("create_proforma: Status: 422");

                        invoice.getProduct().archiveEvent("Proforma failed", "Failed to create proforma, something was wrong.", null);

                        throw new Exception("Fakturoid returned 422 - Unprocessable Entity. Response: " + result);
                    }

                    default:
                        throw new Exception("Fakturoid returned unhandled status. Response: " + result);
                }
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return null;
    }

    @Deprecated
    public Model_Invoice create_paid_invoice(Model_Invoice invoice) {
        try {

            Model_Product product = invoice.getProduct();

            Fakturoid_Invoice fakturoid_invoice = new Fakturoid_Invoice();
            fakturoid_invoice.custom_id = product.id;
            fakturoid_invoice.client_name = product.payment_details.company_name;
            fakturoid_invoice.currency = Currency.USD;
            fakturoid_invoice.lines = invoice.invoice_items;
            fakturoid_invoice.proforma = false;
            fakturoid_invoice.partial_proforma = false;
            fakturoid_invoice.subject_id = product.fakturoid_subject_id;

            WSResponse response;

            JsonNode result;

            for (int trial = 5; trial > 0; trial--) {

                try {

                    CompletionStage<WSResponse> responsePromise = ws.url(Server.Fakturoid_url + "/invoices.json")
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .addHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(Duration.ofSeconds(5))
                            .post(Json.toJson(fakturoid_invoice));

                    response = responsePromise.toCompletableFuture().get();

                    result = response.asJson();

                } catch (Exception e) {
                    logger.internalServerError(e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 201: {

                        logger.debug("create_paid_invoice: Status: 201");

                        if (result != null && result.has("id")) {
                            invoice.fakturoid_id = result.get("id").asLong();
                            invoice.fakturoid_pdf_url = result.get("pdf_url").asText();
                            invoice.invoice_number = result.get("number").asText();
                            invoice.status = PaymentStatus.PAID;
                            invoice.paid = new Date();
                            invoice.proforma = false;
                            invoice.gw_url = null;
                            invoice.update();

                            invoice.getProduct().archiveEvent("Paid invoice created", "System created paid invoice in Fakturoid", invoice.id);

                            return invoice;
                        } else throw new NullPointerException("Response from Fakturoid does not contain ID.");
                    }

                    case 400: {

                        logger.debug("create_paid_invoice: Status: 400");
                        throw new Exception("Fakturoid returned 400 - Bad Request. Response: " + result);
                    }

                    case 403: {

                        logger.debug("create_paid_invoice: Status: 403");
                        throw new Exception("Fakturoid returned 403 - Forbidden. Response: " + result);
                    }

                    case 422: {

                        logger.debug("create_paid_invoice: Status: 422");

                        throw new Exception("Fakturoid returned 422 - Unprocessable Entity. Response: " + result);
                    }

                    default:
                        throw new Exception("Fakturoid returned unhandled. Response: " + result);
                }
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return invoice;
    }

    /**
     * Method tries to download the PDF file from Fakturoid and generates an email with the invoice as attachment.
     * @param invoice Model invoice that is being sent.
     * @param mail String mail that the invoice is sent to. If null default invoice_email from payment_details is used.
     */
    public void sendInvoiceEmail(Model_Invoice invoice, String mail) {
        try {

            logger.debug("sendInvoiceEmail: Trying send PDF Invoice to User Email");

            byte[] body = this.download_PDF_invoice(invoice.proforma ? "proforma" : "invoice", invoice);

            if (body.length < 1) {
                logger.warn("Incoming File from Fakturoid is empty!");
                return;
            }

            if (mail == null) mail = invoice.getProduct().payment_details.invoice_email;

            logger.debug("sendInvoiceEmail: PDF with invoice was successfully downloaded from Fakturoid");

            String[] monthNames_en = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

            new Email()
                    .text("Dear customer,")
                    .text("Please find an enclosed invoice for the services you ordered for " + monthNames_en[Calendar.getInstance().get(Calendar.MONTH)] + ".")
                    .text("In case of questions, please contact our financial department.")
                    .text("Best regards, Byzance Team")
                    .attachmentPDF(invoice.invoice_number + ".pdf", body)
                    .send(mail, "Invoice " + monthNames_en[Calendar.getInstance().get(Calendar.MONTH)]);

                logger.debug("sendInvoiceEmail: Email was successfully sent");

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    /**
     * Method is used to send a reminder email, that some problem has happened during payment for his product.
     * @param invoice Model invoice that is being sent.
     * @param message String contents of the email.
     */
    public void sendInvoiceReminderEmail(Model_Invoice invoice, String message) {
        try {

            byte[] body = this.download_PDF_invoice(invoice.proforma ? "proforma" : "invoice", invoice);

            String[] monthNames_en = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

            Model_Product product = invoice.getProduct();

            new Email()
                    .text("Dear customer,")
                    .text(message)
                    //.text("We have problems with payment of your services on this month. Log in and check your credit parameters. " + expiry + " Please contact us immediately if something is unclear.")
                    .text("See the attached invoice.")
                    .text("Best regards, Byzance Team")
                    .attachmentPDF(invoice.invoice_number + ".pdf", body)
                    .send(product.payment_details.invoice_email, "Invoice for " + monthNames_en[Calendar.getInstance().get(Calendar.MONTH)] + " - Reminder" );


        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    /**
     * Method creates subject in Fakturoid. Tries 5 times to get the result.
     * @param details Model PaymentDetails with info about the customer.
     * @return String id of the subject from Fakturoid. Null if some error occurs.
     */
    public String create_subject(Model_PaymentDetails details) {
        try {

            ObjectNode request = Json.newObject();
            request.put("street", details.street + " " + details.street_number);
            request.put("city", details.city);
            request.put("zip", details.zip_code);
            request.put("email", details.invoice_email);

            // request.put("country", product.payment_details.country); (vyžaduje ISO code země - to zatím Tyrion nemá implementováno)

            if (details.company_vat_number != null && details.company_vat_number.length() > 0)               request.put("vat_no", details.company_vat_number);
            if (details.company_registration_no != null  && details.company_registration_no.length() > 0)    request.put("registration_no", details.company_registration_no);
            if (details.company_authorized_phone != null && details.company_authorized_phone.length() > 0)   request.put("phone", details.company_authorized_phone);
            if (details.company_web != null && details.company_web.length() > 0)                             request.put("web", details.company_web);
            if (details.company_name != null && details.company_name.length() > 0)                           request.put("name", details.company_name);
            if (details.full_name != null && details.full_name.length() > 0)                                 request.put("full_name", details.full_name);

            for (int trial = 5; trial > 0; trial--) {

                WSResponse response;

                JsonNode result;

                try {

                    CompletionStage<WSResponse> responsePromise = ws.url(Server.Fakturoid_url + "/subjects.json")
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .addHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(Duration.ofSeconds(5))
                            .post(Json.toJson(request));

                    response = responsePromise.toCompletableFuture().get();

                } catch (Exception e) {
                    logger.internalServerError(e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 201: {

                        logger.debug("create_subject: Status: 201");

                        if (details.product != null) // Produkt někdy ještě nemusí být v tuhle chvíli přiřazen
                            details.product.archiveEvent("Subject created", "System created subject in Fakturoid", null);

                        result = response.asJson();

                        if (result != null && result.has("id")) return result.get("id").asText();
                        else throw new NullPointerException("Response from Fakturoid does not contain ID.");
                    }

                    case 400: {

                        result = response.asJson();

                        logger.debug("create_subject: Status: 400");
                        throw new Exception("Fakturoid returned 400 - Bad Request. Response: " + result);
                    }

                    case 403: {

                        logger.debug("create_subject: Status: 403");
                        throw new Exception("Fakturoid returned 403 - Forbidden. Probably too many clients." );
                    }

                    case 422: {

                        result = response.asJson();

                        logger.debug("create_subject: Status: 422");

                        throw new IllegalArgumentException("Fakturoid returned 422 - Unprocessable Entity. Response: " + result);
                    }

                    default: throw new Exception("Fakturoid returned unhandled status: " + response.getStatus() + ".");
                }
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return null;
    }

    /**
     * Updates subject info in Fakturoid.
     * @param details Updated model PaymentDetails.
     * @return Boolean true if it succeeded or false if it failed.
     */
    public boolean update_subject(Model_PaymentDetails details) {
        try {

            Model_PaymentDetails old_details = Model_PaymentDetails.getById(details.id);

            ObjectNode request = Json.newObject();

            if (!details.street.equals(old_details.street) || !details.street_number.equals(old_details.street_number))
                request.put("street", details.street + " " + details.street_number);

            if (!details.city.equals(old_details.city))
                request.put("city", details.city);

            if (!details.zip_code.equals(old_details.zip_code))
                request.put("zip", details.zip_code);

            if (!details.invoice_email.equals(old_details.invoice_email))
                request.put("email", details.invoice_email);

            if (details.company_account && !old_details.company_account) {

                if (details.company_vat_number != null)
                    request.put("vat_no", details.company_vat_number);

                if (details.company_registration_no != null)
                    request.put("registration_no", details.company_registration_no);

                request.put("phone", details.company_authorized_phone);
                request.put("web", details.company_web);
                request.put("name", details.company_name);
                request.put("full_name", details.full_name);

            } else {
                request.put("name", details.full_name);
            }

            if (details.company_account && old_details.company_account) {

                if (details.company_vat_number != null)
                    if (old_details.company_vat_number == null || !details.company_vat_number.equals(old_details.company_vat_number))
                        request.put("vat_no", details.company_vat_number);

                if (details.company_registration_no != null)
                    if (old_details.company_registration_no == null || !details.company_registration_no.equals(old_details.company_registration_no))
                        request.put("registration_no", details.company_registration_no);

                if (!details.company_authorized_phone.equals(old_details.company_authorized_phone))
                    request.put("phone", details.company_authorized_phone);

                if (!details.company_web.equals(old_details.company_web))
                    request.put("web", details.company_web);

                if (!details.company_name.equals(old_details.company_name))
                    request.put("name", details.company_name);

                if (!details.full_name.equals(old_details.full_name))
                    request.put("full_name", details.full_name);
            }

            if (!details.company_account && old_details.company_account) {

                request.put("vat_no", "");
                request.put("phone", "");
                request.put("web", "");
                request.put("name", details.full_name);
            }

            if (!details.company_account && !old_details.company_account) {

                if (!details.full_name.equals(old_details.full_name))
                    request.put("name", details.full_name);
            }

            for (int trial = 5; trial > 0; trial--) {

                WSResponse response;

                JsonNode result;

                try {

                    CompletionStage<WSResponse> responsePromise = ws.url(Server.Fakturoid_url + "/subjects/" + details.product.fakturoid_subject_id + ".json")
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .addHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(Duration.ofSeconds(5))
                            .patch(Json.toJson(request));

                    response = responsePromise.toCompletableFuture().get();

                    result = response.asJson();

                } catch (Exception e) {
                    logger.internalServerError(e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 200: {

                        logger.debug("update_subject: Status: 201");

                        details.product.archiveEvent("Subject updated", "System updated subject in Fakturoid", null);

                        if (result != null && result.has("id")) {
                            details.update();
                            return true;
                        }
                        else throw new NullPointerException("Response from Fakturoid does not contain ID.");
                    }

                    case 400: {

                        logger.debug("update_subject: Status: 400");
                        throw new Exception("Fakturoid returned 400 - Bad Request. Response: " + result);
                    }

                    case 403: {

                        logger.debug("update_subject: Status: 403");
                        throw new Exception("Fakturoid returned 403 - Forbidden. Response: " + result);
                    }

                    case 422: {

                        logger.debug("update_subject: Status: 422");

                        throw new Exception("Fakturoid returned 422 - Unprocessable Entity. Response: " + result);
                    }

                    default: throw new Exception("Fakturoid returned unhandled status: " + response.getStatus() + ". Response: " + result);
                }
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return false;
    }

// PRIVATE HELPERS METHODS #####################################################################################################

    public JsonNode fakturoid_put(String url, JsonNode node) {

        logger.debug("fakturoid_put:: PUT: URL: " + Server.Fakturoid_url + url + "  Json: " + node.toString());
        CompletionStage<WSResponse> responsePromise = ws.url(Server.Fakturoid_url + url)
                .setContentType("application/json")
                .addHeader("User-Agent", Server.Fakturoid_user_agent)
                .setRequestTimeout(Duration.ofSeconds(5))
                .put(node);

        try {

            JsonNode response = responsePromise.toCompletableFuture().get().asJson();
            logger.debug("fakturoid_put:: Result: " + response.toString() );
            return response;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    /**
     * Method is used to fire specific events in Fakturoid. (e.g. "pay_proforma")
     * @param url String url action to perform.
     * @return Boolean true if it succeeded or false if it failed.
     */
    public boolean fakturoid_post (String url) {

        // Slouží ke změnám faktury - například na změnu stavu na "zaplaceno"
        logger.debug("fakturoid_post: URL = " + Server.Fakturoid_url + url);

        try {

            WSResponse response;

            JsonNode result;

            for (int trial = 5; trial > 0; trial--) {

                logger.debug("fakturoid_post: Number of remaining tries: {}", trial);

                try {

                    CompletionStage<WSResponse> responsePromise = ws.url(Server.Fakturoid_url + url)
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .addHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(Duration.ofSeconds(5))
                            .post("{}");

                    response = responsePromise.toCompletableFuture().get();

                } catch (Exception e) {
                    logger.internalServerError(e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 200: {

                        logger.debug("fakturoid_post: Status: 200");
                        return true;
                    }

                    case 422: {

                        result = response.asJson();

                        logger.debug("fakturoid_post: Status: 422");

                        throw new Exception("Fakturoid returned 422 - Unprocessable Entity. Response: "+ result);
                    }

                    default: {

                        result = response.asJson();

                        throw new Exception("Fakturoid returned unhandled. Response: "+ result);
                    }
                }
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return false;
    }

    public boolean fakturoid_delete(String url) {
        // Slouží například k mazáním proformy a transfromace na fakturu

        logger.debug("fakturoid_delete::  URL: " + Server.Fakturoid_url + url);
        try {

            for (int trial = 5; trial > 0; trial--) {

                logger.debug("Fakturoid_Controller: fakturoid_delete: Number of remaining tries: {}", trial);

                WSResponse response;

                try {

                    CompletionStage<WSResponse> responsePromise = ws.url(Server.Fakturoid_url + url)
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .addHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(Duration.ofSeconds(5))
                            .delete();

                    response = responsePromise.toCompletableFuture().get();

                } catch (Exception e) {
                    logger.internalServerError(e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 204: {

                        logger.debug("fakturoid_delete: Status: 204");
                        return true;
                    }

                    case 404: {

                        logger.debug("create_subject: Status: 404");
                        throw new Exception("Fakturoid returned 404 - Not Found. Invoice is probably already deleted or does not exist.");
                    }

                    default: throw new Exception("Fakturoid returned unhandled status: " + response.getStatus());
                }
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return false;
    }

    /**
     * Method gets the PDF from Fakturoid.
     * If the invoice is just created there might be some latency and PDF creation could be delayed.
     * So the method will try to get the invoice 3 times with 2,5s interval before it gives up.
     * @param type String type of a document. ("proforma", "invoice")
     * @param invoice Given model invoice to get the PDF for.
     * @return Byte array represented PDF file.
     */
    public byte[] download_PDF_invoice(String type, Model_Invoice invoice) {

        logger.debug("download_PDF_invoice: type: {}", type);

            int terminator = 3;
            while (terminator >= 0) {
                try {

                    String url;

                    if (type.equals("proforma") && invoice.proforma_pdf_url != null) url = invoice.proforma_pdf_url;
                    else url = invoice.fakturoid_pdf_url;

                    logger.debug("download_PDF_invoice: Getting PDF invoice from url: {}", url);

                    CompletionStage<WSResponse> responsePromise = ws.url(url)
                            .setAuth(Server.Fakturoid_secret_combo)
                            .addHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(Duration.ofSeconds(5))
                            .get();

                    WSResponse promise = responsePromise.toCompletableFuture().get();

                    if (promise.getStatus() == 200) {
                        logger.debug("download_PDF_invoice: Status = {}. PDF Download successfully to byte[]", promise.getStatus());
                        return promise.asByteArray();

                    } else {

                        logger.warn("download_PDF_invoice: Status = {}. PDF Download unsuccessful.", promise.getStatus());

                        --terminator;
                        Thread.sleep(2500);
                    }

                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }
        throw new NullPointerException("Unable to download PDF invoice.");
    }

    /**
     * Method gets the invoice from Fakturoid and checks its status.
     * If it is paid this method transforms it to a tax document (non-proforma).
     * If payment method is "BANK_TRANSFER" the appropriate amount of credit will be added to product.
     * Method tries 5 times to get the result.
     * @param invoice Given invoice that is being checked.
     */
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
                            this.sendInvoiceEmail(invoice, null);
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
    }
}