package utilities.financial.fakturoid;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_PaymentDetails;
import models.Model_Product;
import models.Model_Invoice;
import play.api.Play;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Server;
import utilities.emails.Email;
import utilities.enums.Enum_Currency;
import utilities.enums.Enum_Payment_status;
import utilities.enums.Enum_Payment_warning;
import utilities.logger.Class_Logger;
import utilities.swagger.documentationClass.Swagger_Fakturoid_Callback;

import java.util.Calendar;
import java.util.Date;

/**
 * This class is used to interact with Fakturoid or sending emails with invoices.
 * (Creating invoices and subjects or callbacks from Fakturoid.)
 */
public class Fakturoid_Controller extends Controller {

    // Logger
    private static final Class_Logger terminal_logger = new Class_Logger(Fakturoid_Controller.class);

// PUBLIC CONTROLLERS METHODS ##########################################################################################

    /**
     * RestApi callback notification from Fakturoid is received here.
     * Invoice is passed to Fakturoid_InvoiceCheck.class if the status is paid,
     * so it could be checked and transformed to a tax document.
     * @return Result ok is returned every time, errors are only logged.
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result fakturoid_callback(){
        try {

            // Binding Json with help object
            final Form<Swagger_Fakturoid_Callback> form = Form.form(Swagger_Fakturoid_Callback.class).bindFromRequest();
            if(form.hasErrors()) throw new Exception("Error binding Json from Fakturoid: " + form.errorsAsJson().toString());
            Swagger_Fakturoid_Callback help = form.get();

            terminal_logger.warn("fakturoid_callback: Body: {}", request().body().asJson());

            // Finding in DB
            Model_Invoice invoice = Model_Invoice.find.where().eq("fakturoid_id", help.invoice_id).findUnique();
            if (invoice == null) throw new NullPointerException("Invoice is null. Cannot find it in database.");

            switch (help.status){

                case "paid": {

                    Fakturoid_InvoiceCheck.addToQueue(invoice);

                    break;
                }

                case "overdue":{

                    invoice.status = Enum_Payment_status.overdue;
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

        }catch (Exception e){
            terminal_logger.internalServerError("fakturoid_callback:", e);
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
    public static Model_Invoice create_proforma(Model_Invoice invoice){
        try {

            Fakturoid_Invoice fakturoid_invoice = new Fakturoid_Invoice();
            fakturoid_invoice.custom_id = invoice.product.id;
            fakturoid_invoice.client_name = invoice.product.payment_details.company_account ? invoice.product.payment_details.company_name : invoice.product.payment_details.person.full_name;
            fakturoid_invoice.currency = Enum_Currency.USD;
            fakturoid_invoice.lines = invoice.invoice_items();
            fakturoid_invoice.proforma = true;
            fakturoid_invoice.partial_proforma = false;

            if (invoice.product.fakturoid_subject_id == null) {

                terminal_logger.debug("create_proforma:: Client is not registered in Fakturoid");

                // Pokud ne tak ho vytvořím
                invoice.product.fakturoid_subject_id = create_subject(invoice.product.payment_details);
                if (invoice.product.fakturoid_subject_id == null) return null;
                invoice.product.update();

                terminal_logger.debug("create_proforma:: New Client Id in Fakturoid is " + invoice.product.fakturoid_subject_id);
            }

            fakturoid_invoice.subject_id = invoice.product.fakturoid_subject_id;

            terminal_logger.debug("create_proforma::  Sending Proforma to Fakturoid");

            for (int trial = 5; trial > 0; trial--) {

                WSResponse response;

                JsonNode result;

                try {

                    F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + "/invoices.json")
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .setHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(5000)
                            .post(Json.toJson(fakturoid_invoice));

                    response = responsePromise.get(5000);

                    result = response.asJson();

                } catch (Exception e) {
                    terminal_logger.internalServerError("create_proforma:", e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 201: {

                        terminal_logger.debug("create_proforma: POST: Result: {}", result.toString());

                        if (!result.has("id")) {
                            terminal_logger.error("create_proforma: Invoice From fakturoid does not contains ID");
                            throw new NullPointerException("Invoice From fakturoid does not contains ID");
                        }


                        invoice.proforma_id = result.get("id").asLong();
                        invoice.proforma_pdf_url = result.get("pdf_url").asText();
                        invoice.invoice_number = result.get("number").asText();
                        invoice.proforma = true;
                        invoice.warning = Enum_Payment_warning.none;
                        invoice.status = Enum_Payment_status.pending;
                        invoice.save();

                        invoice.getProduct().archiveEvent("Proforma created", "System created proforma", invoice.id);

                        terminal_logger.debug("create_proforma: Saving Invoice");

                        return invoice;
                    }

                    case 422: {

                        terminal_logger.debug("create_proforma: Status: 422");

                        // TODO notifikace

                        throw new Exception("Fakturoid returned 422 - Unprocessable Entity. Response: " + result);
                    }

                    default:
                        throw new Exception("Fakturoid returned unhandled status. Response: " + result);
                }
            }
        } catch (Exception e) {
            terminal_logger.internalServerError("create_proforma:", e);
        }

        return null;
    }

    @Deprecated
    public static Model_Invoice create_paid_invoice(Model_Invoice invoice){
        try {

            Model_Product product = invoice.getProduct();

            Fakturoid_Invoice fakturoid_invoice = new Fakturoid_Invoice();
            fakturoid_invoice.custom_id = product.id;
            fakturoid_invoice.client_name = product.payment_details.company_account ? product.payment_details.company_name : product.payment_details.person.full_name;
            fakturoid_invoice.currency = Enum_Currency.USD;
            fakturoid_invoice.lines = invoice.invoice_items;
            fakturoid_invoice.proforma = false;
            fakturoid_invoice.partial_proforma = false;
            fakturoid_invoice.subject_id = product.fakturoid_subject_id;

            WSResponse response;

            JsonNode result;

            for (int trial = 5; trial > 0; trial--) {

                try {

                    F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + "/invoices.json")
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .setHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(5000)
                            .post(Json.toJson(fakturoid_invoice));

                    response = responsePromise.get(5000);

                    result = response.asJson();

                } catch (Exception e) {
                    terminal_logger.internalServerError("create_subject:", e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 201: {

                        terminal_logger.debug("create_paid_invoice: Status: 201");

                        if (result != null && result.has("id")) {
                            invoice.fakturoid_id = result.get("id").asLong();
                            invoice.fakturoid_pdf_url = result.get("pdf_url").asText();
                            invoice.invoice_number = result.get("number").asText();
                            invoice.status = Enum_Payment_status.paid;
                            invoice.paid = new Date();
                            invoice.proforma = false;
                            invoice.gw_url = null;
                            invoice.update();

                            invoice.getProduct().archiveEvent("Paid invoice created", "System created paid invoice in Fakturoid", invoice.id);

                            return invoice;
                        } else throw new NullPointerException("Response from Fakturoid does not contain ID.");
                    }

                    case 400: {

                        terminal_logger.debug("create_paid_invoice: Status: 400");
                        throw new Exception("Fakturoid returned 400 - Bad Request. Response: " + result);
                    }

                    case 403: {

                        terminal_logger.debug("create_paid_invoice: Status: 403");
                        throw new Exception("Fakturoid returned 403 - Forbidden. Response: " + result);
                    }

                    case 422: {

                        terminal_logger.debug("create_paid_invoice: Status: 422");

                        // TODO notifikace

                        throw new Exception("Fakturoid returned 422 - Unprocessable Entity. Response: " + result);
                    }

                    default:
                        throw new Exception("Fakturoid returned unhandled. Response: " + result);
                }
            }

        } catch (Exception e) {
            terminal_logger.internalServerError("create_paid_invoice:", e);
        }

        return invoice;
    }

    /**
     * Method tries to download the PDF file from Fakturoid and generates an email with the invoice as attachment.
     * @param invoice Model invoice that is being sent.
     * @param mail String mail that the invoice is sent to. If null default invoice_email from payment_details is used.
     */
    public static void sendInvoiceEmail(Model_Invoice invoice, String mail){
        try {

            terminal_logger.debug("sendInvoiceEmail: Trying send PDF Invoice to User Email");

            byte[] body = download_PDF_invoice("invoice", invoice);

            if(body.length < 1){
                terminal_logger.warn("Incoming File from Fakturoid is empty!");
                return;
            }

            if (mail == null) mail = invoice.getProduct().payment_details.invoice_email;

            terminal_logger.debug("sendInvoiceEmail: PDF with invoice was successfully downloaded from Fakturoid");

            String[] monthNames_en = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

            new Email()
                    .text("Dear customer,")
                    .text("Please find an enclosed invoice for the services you ordered for " + monthNames_en[Calendar.getInstance().get(Calendar.MONTH)] + ".")
                    .text("In case of questions, please contact our financial department.")
                    .text("Best regards, Byzance Team")
                    .attachmentPDF(invoice.invoice_number + ".pdf", body)
                    .send(mail, "Invoice " + monthNames_en[Calendar.getInstance().get(Calendar.MONTH)]);

                terminal_logger.debug("sendInvoiceEmail: Email was successfully sent");

        }catch (Exception e){
            terminal_logger.internalServerError("sendInvoiceEmail:", e);
        }
    }

    /**
     * Method is used to send a reminder email, that some problem has happened during payment for his product.
     * @param invoice Model invoice that is being sent.
     * @param message String contents of the email.
     */
    public static void sendInvoiceReminderEmail(Model_Invoice invoice, String message){
        try{

            byte[] body = Fakturoid_Controller.download_PDF_invoice("invoice", invoice);

            String[] monthNames_en = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

            Model_Product product = invoice.getProduct();

            new Email()
                    .text("Dear customer,")
                    .text(message)
                    //.text("We have problems with payment of your services on this month. Log in and check your credit parameters. " + expiry + " Please contact us immediately if something is unclear.")
                    .text("See the attached invoice.")
                    .text("Best regards, Byzance Team")
                    .attachmentPDF(invoice.invoice_number + ".pdf", body)
                    .send(product.payment_details.invoice_email != null ? product.payment_details.invoice_email : product.payment_details.person.mail , "Invoice for " + monthNames_en[Calendar.getInstance().get(Calendar.MONTH)] + " - Reminder" );


        }catch (Exception e){
            terminal_logger.internalServerError("sendInvoiceReminderEmail:", e);
        }
    }

    /**
     * Method creates subject in Fakturoid. Tries 5 times to get the result.
     * @param details Model PaymentDetails with info about the customer.
     * @return String id of the subject from Fakturoid. Null if some error occurs.
     */
    public static String create_subject(Model_PaymentDetails details){
        try {

            ObjectNode request = Json.newObject();
            request.put("street", details.street + " " + details.street_number);
            request.put("city", details.city);
            request.put("zip", details.zip_code);
            request.put("email", details.invoice_email);

            if (details.company_account) {
                // request.put("country", product.payment_details.country); (vyžaduje ISO code země - to zatím Tyrion nemá implementováno)
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

            WSClient ws = Play.current().injector().instanceOf(WSClient.class);

            for (int trial = 5; trial > 0; trial--) {

                WSResponse response;

                JsonNode result;

                try {

                    F.Promise<WSResponse> responsePromise = ws.url(Server.Fakturoid_url + "/subjects.json")
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .setHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(5000)
                            .post(Json.toJson(request));

                    response = responsePromise.get(5000);

                    result = response.asJson();

                } catch (Exception e) {
                    terminal_logger.internalServerError("create_subject:", e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 201: {

                        terminal_logger.debug("create_subject: Status: 201");

                        details.product.archiveEvent("Subject created", "System created subject in Fakturoid", null);

                        if (result != null && result.has("id")) return result.get("id").asText();
                        else throw new NullPointerException("Response from Fakturoid does not contain ID.");
                    }

                    case 400: {

                        terminal_logger.debug("create_subject: Status: 400");
                        throw new Exception("Fakturoid returned 400 - Bad Request. Response: " + result);
                    }

                    case 403: {

                        terminal_logger.debug("create_subject: Status: 403");
                        throw new Exception("Fakturoid returned 403 - Forbidden. Response: " + result);
                    }

                    case 422: {

                        terminal_logger.debug("create_subject: Status: 422");

                        // TODO notifikace

                        throw new Exception("Fakturoid returned 422 - Unprocessable Entity. Response: " + result);
                    }

                    default: throw new Exception("Fakturoid returned unhandled status: " + response.getStatus() + ". Response: " + result);
                }
            }

        } catch (Exception e) {
            terminal_logger.internalServerError("create_subject:", e);
        }

        return null;
    }

    /**
     * Updates subject info in Fakturoid.
     * @param details Updated model PaymentDetails.
     * @return Boolean true if it succeeded or false if it failed.
     */
    public static boolean update_subject(Model_PaymentDetails details){
        try {

            Model_PaymentDetails old_details = Model_PaymentDetails.find.byId(details.id);

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

                    F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + "/subjects/" + details.product.fakturoid_subject_id + ".json")
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .setHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(5000)
                            .patch(Json.toJson(request));

                    response = responsePromise.get(5000);

                    result = response.asJson();

                } catch (Exception e) {
                    terminal_logger.internalServerError("update_subject:", e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 200: {

                        terminal_logger.debug("update_subject: Status: 201");

                        details.product.archiveEvent("Subject updated", "System updated subject in Fakturoid", null);

                        if (result != null && result.has("id")) {
                            details.update();
                            return true;
                        }
                        else throw new NullPointerException("Response from Fakturoid does not contain ID.");
                    }

                    case 400: {

                        terminal_logger.debug("update_subject: Status: 400");
                        throw new Exception("Fakturoid returned 400 - Bad Request. Response: " + result);
                    }

                    case 403: {

                        terminal_logger.debug("update_subject: Status: 403");
                        throw new Exception("Fakturoid returned 403 - Forbidden. Response: " + result);
                    }

                    case 422: {

                        terminal_logger.debug("update_subject: Status: 422");

                        // TODO notifikace

                        throw new Exception("Fakturoid returned 422 - Unprocessable Entity. Response: " + result);
                    }

                    default: throw new Exception("Fakturoid returned unhandled status: " + response.getStatus() + ". Response: " + result);
                }
            }

        } catch (Exception e) {
            terminal_logger.internalServerError("update_subject:", e);
        }

        return false;
    }

// PRIVATE HELPERS METHODS #####################################################################################################

    public static JsonNode fakturoid_put(String url, JsonNode node){

        terminal_logger.debug("fakturoid_put:: PUT: URL: " + Server.Fakturoid_url + url + "  Json: " + node.toString());
        F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + url)
                .setContentType("application/json")
                .setHeader("User-Agent", Server.Fakturoid_user_agent)
                .setRequestTimeout(5000)
                .put(node);

        try {

            JsonNode response = responsePromise.get(5000).asJson();
            terminal_logger.debug("fakturoid_put:: Result: " + response.toString() );
            return response;

        }catch(Exception e){
            terminal_logger.error("fakturoid_put::  Error: " + responsePromise.get(5000).toString() );
            throw new NullPointerException();
        }
    }

    /**
     * Method is used to fire specific events in Fakturoid. (e.g. "pay_proforma")
     * @param url String url action to perform.
     * @return Boolean true if it succeeded or false if it failed.
     */
    public static boolean fakturoid_post (String url){
        // Slouží ke změnám faktury - například na změnu stavu na "zaplaceno"
        terminal_logger.debug("Fakturoid_Controller: fakturoid_post: URL: " + Server.Fakturoid_url + url);

        for (int trial = 5; trial > 0; trial--){
            try{

                terminal_logger.debug("Fakturoid_Controller: fakturoid_post: Number of remaining tries: {}", trial);

                WSResponse response;

                JsonNode result;

                try {

                    F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + url)
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .setHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(5000)
                            .post("{}");

                    response = responsePromise.get(5000);

                } catch (Exception e) {
                    terminal_logger.internalServerError("fakturoid_post:", e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 200: {

                        terminal_logger.debug("fakturoid_post: Status: 200");
                        return true;
                    }

                    case 422: {

                        result = response.asJson();

                        terminal_logger.debug("fakturoid_post: Status: 422");

                        // TODO notifikace

                        terminal_logger.internalServerError("fakturoid_post:", new Exception("Fakturoid returned 422 - Unprocessable Entity. Response: "+ result));
                        return false;
                    }

                    default: {

                        result = response.asJson();

                        throw new Exception("Fakturoid returned unhandled. Response: "+ result);
                    }
                }

            } catch (Exception e) {
                terminal_logger.internalServerError("fakturoid_post:", e);
            }
        }
        return false;
    }

    public static boolean fakturoid_delete(String url){
        // Slouží například k mazáním proformy a transfromace na fakturu

        terminal_logger.debug("fakturoid_delete::  URL: " + Server.Fakturoid_url + url);
        try {

            for (int trial = 5; trial > 0; trial--) {

                terminal_logger.debug("Fakturoid_Controller: fakturoid_delete: Number of remaining tries: {}", trial);

                WSResponse response;

                try {

                    F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + url)
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .setHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(5000)
                            .delete();

                    response = responsePromise.get(5000);

                } catch (Exception e) {
                    terminal_logger.internalServerError("fakturoid_delete:", e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 204: {

                        terminal_logger.debug("fakturoid_delete: Status: 204");
                        return true;
                    }

                    case 404: {

                        terminal_logger.debug("create_subject: Status: 404");
                        throw new Exception("Fakturoid returned 404 - Not Found. Invoice is probably already deleted or does not exist.");
                    }

                    default: throw new Exception("Fakturoid returned unhandled status: " + response.getStatus());
                }
            }

        } catch (Exception e) {
            terminal_logger.internalServerError("fakturoid_delete:", e);
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
    public static byte[] download_PDF_invoice(String type, Model_Invoice invoice){

            int terminator = 3;
            while (terminator >= 0) {
                try {

                    String url;

                    if (type.equals("proforma") && invoice.proforma_pdf_url != null) url = invoice.proforma_pdf_url;
                    else url = invoice.fakturoid_pdf_url;

                    terminal_logger.debug("download_PDF_invoice::  Trying download PDF invoice from Fakturoid on url: " + url);

                    F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(url)
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(5000)
                            .get();

                    WSResponse promise = responsePromise.get(5000);

                    terminal_logger.debug("download_PDF_invoice:: download_PDF_invoice:: promise status " + promise.getStatus());

                    if (promise.getStatus() == 200) {
                        terminal_logger.debug("download_PDF_invoice:: PDF Download successfully to byte[]");
                        return promise.asByteArray();

                    } else {

                        terminal_logger.warn("download_PDF_invoice:: promise status" + promise.getStatus());
                        terminal_logger.warn("download_PDF_invoice:: PDF Download un-successfully to byte[]");

                        --terminator;
                        Thread.sleep(2500);

                    }

                } catch (InterruptedException e) {
                    terminal_logger.error("download_PDF_invoice::  Error:: Interupted exception", e);
                }
            }

        terminal_logger.error("download_PDF_invoice:: Error:: PDF Download un-successfully to byte[]");
        throw new NullPointerException("File not found");

    }
}
