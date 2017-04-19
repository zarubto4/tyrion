package utilities.fakturoid;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import models.Model_PaymentDetails;
import models.Model_Product;
import models.Model_Invoice;
import play.Logger;
import play.api.Play;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.emails.Email;
import utilities.enums.Enum_Currency;
import utilities.enums.Enum_Payment_status;
import utilities.enums.Enum_Payment_warning;
import utilities.loggy.Loggy;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_BadRequest;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.swagger.documentationClass.Swagger_Fakturoid_Callback;
import utilities.swagger.outboundClass.Swagger_Invoice_FullDetails;

import java.util.Calendar;
import java.util.Date;

public class Utilities_Fakturoid_Controller extends Controller {

    // Logger
    private static Logger.ALogger logger = Logger.of("Loggy");

// PUBLIC CONTROLLERS METHODS ##########################################################################################

    @ApiOperation(value = "get Invoice PDF file",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get PDF invoice file",
            produces = "multipartFormData",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Invoice_FullDetails.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result invoice_get_pdf(String invoice_id){

        try {
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            if(!invoice.read_permission()) return GlobalResult.forbidden_Permission();

            byte[] pdf_in_array = Utilities_Fakturoid_Controller.download_PDF_invoice(invoice);

            return GlobalResult.result_pdf_file(pdf_in_array, invoice.invoice_number + ".pdf");

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(hidden = true, value = "fakturoid callback endpoint")
    @BodyParser.Of(BodyParser.Json.class)
    public Result fakturoid_callback(){
        try {

            // Binding Json with help object
            final Form<Swagger_Fakturoid_Callback> form = Form.form(Swagger_Fakturoid_Callback.class).bindFromRequest();
            if(form.hasErrors()) throw new Exception("Error binding Json from Fakturoid: " + form.errorsAsJson().toString());
            Swagger_Fakturoid_Callback help = form.get();

            logger.warn("Fakturoid_Controller:: fakturoid_callback: Body: {}", request().body().asJson());

            // Finding in DB
            Model_Invoice invoice = Model_Invoice.find.where().eq("fakturoid_id", help.related_id != null ? help.related_id : help.invoice_id).findUnique();
            if (invoice == null) throw new NullPointerException("Invoice is null. Cannot find it in database.");

            switch (help.status){

                case "paid": {

                    invoice.status = Enum_Payment_status.paid;
                    invoice.invoice_number = help.number;
                    invoice.proforma = false;
                    invoice.paid = new Date();
                    invoice.update();

                    // TODO

                    invoice.getProduct().credit_upload(invoice.total_price());

                    break;
                }

                case "overdue":{

                    invoice.status = Enum_Payment_status.overdue;
                    invoice.overdue = new Date();
                    invoice.update();

                    // TODO notifikace

                    break;
                }

                default: throw new Exception("Unknown invoice status. Callback payload: " + request().body().asJson().toString());
            }

            return ok();

        }catch (Exception e){
            Loggy.internalServerError("Fakturoid_Controller:: fakturoid_callback:", e);
            return ok();
        }
    }

// PRIVATE EXECUTIVE METHODS ###########################################################################################

    public static Model_Invoice create_proforma(Model_Invoice invoice){

        for (int trial = 5; trial > 0; trial--) {
            try {

                Utilities_Fakturoid_Invoice fakturoid_invoice = new Utilities_Fakturoid_Invoice();
                fakturoid_invoice.custom_id = invoice.product.id;
                fakturoid_invoice.client_name = invoice.product.payment_details.company_account ? invoice.product.payment_details.company_name : invoice.product.payment_details.person.full_name;
                fakturoid_invoice.currency = Enum_Currency.USD;
                fakturoid_invoice.lines = invoice.getInvoiceItems();
                fakturoid_invoice.proforma = true;
                fakturoid_invoice.partial_proforma = false;

                if (invoice.product.fakturoid_subject_id == null) {

                    logger.debug("Fakturoid_Controller:: create_proforma:: Client has not registration object in Fakturoid");
                    // Ověřím zda tam je - a jestli ano - tak ho jen vytvořím v lokální DB

                    // Pokud ne tak ho vytvořím
                    invoice.product.fakturoid_subject_id = create_subject(invoice.product.payment_details);
                    if (invoice.product.fakturoid_subject_id == null) return null;
                    invoice.product.update();

                    logger.debug("Fakturoid_Controller:: create_proforma:: New Client Id in Fakturoid is " + invoice.product.fakturoid_subject_id);
                }

                fakturoid_invoice.subject_id = invoice.product.fakturoid_subject_id;

                logger.debug("Fakturoid_Controller:: create_proforma::  Sending Proforma to Fakturoid");

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
                    Loggy.internalServerError("Fakturoid_Controller:: create_proforma:", e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 201: {

                        logger.debug("Fakturoid_Controller:: create_proforma:: POST: Result: {}", result.toString());

                        if (!result.has("id")) {
                            logger.error("Fakturoid_Controller:: create_proforma:: Invoice From fakturoid does not contains ID");
                            throw new NullPointerException("Invoice From fakturoid does not contains ID");
                        }


                        invoice.fakturoid_id = result.get("id").asLong();
                        invoice.fakturoid_pdf_url = result.get("pdf_url").asText();
                        invoice.invoice_number = result.get("number").asText();
                        invoice.proforma = true;
                        invoice.warning = Enum_Payment_warning.none;
                        invoice.status = Enum_Payment_status.pending;
                        invoice.save();

                        invoice.getProduct().archiveEvent("Proforma created", "System created proforma", invoice.id);

                        logger.debug("Fakturoid_Controller:: create_proforma:: Saving Invoice");

                        return invoice;
                    }

                    case 422: {

                        logger.debug("Fakturoid_Controller:: create_proforma: Status: 422");

                        // TODO notifikace

                        Loggy.internalServerError("Fakturoid_Controller:: create_proforma:", new Exception("Fakturoid returned 422 - Unprocessable Entity. Response: " + result));
                        return null;
                    }

                    default:
                        throw new Exception("Fakturoid returned unhandled status. Response: " + result);
                }
            } catch (Exception e) {
                Loggy.internalServerError("Fakturoid_Controller:: create_proforma:", e);
            }
        }
        return null;
    }

    public static Model_Invoice create_paid_invoice(Model_Invoice invoice){

        for (int trial = 5; trial > 0; trial--) {
            try {

                Model_Product product = invoice.getProduct();

                Utilities_Fakturoid_Invoice fakturoid_invoice = new Utilities_Fakturoid_Invoice();
                fakturoid_invoice.custom_id = product.id;
                fakturoid_invoice.client_name = product.payment_details.company_account ? product.payment_details.company_name : product.payment_details.person.full_name;
                fakturoid_invoice.currency = Enum_Currency.USD;
                fakturoid_invoice.lines = invoice.invoice_items;
                fakturoid_invoice.proforma = false;
                fakturoid_invoice.partial_proforma = false;
                fakturoid_invoice.subject_id = product.fakturoid_subject_id;

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
                    Loggy.internalServerError("Fakturoid_Controller:: create_subject:", e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 201: {

                        logger.debug("Fakturoid_Controller:: create_paid_invoice: Status: 201");

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
                        }
                        else throw new NullPointerException("Response from Fakturoid does not contain ID.");
                    }

                    case 400: {

                        logger.debug("Fakturoid_Controller:: create_paid_invoice: Status: 400");
                        throw new Exception("Fakturoid returned 400 - Bad Request. Response: "+ result);
                    }

                    case 403: {

                        logger.debug("Fakturoid_Controller:: create_paid_invoice: Status: 403");
                        throw new Exception("Fakturoid returned 403 - Forbidden. Response: "+ result);
                    }

                    case 422: {

                        logger.debug("Fakturoid_Controller:: create_paid_invoice: Status: 422");

                        // TODO notifikace

                        Loggy.internalServerError("Fakturoid_Controller:: create_paid_invoice:", new Exception("Fakturoid returned 422 - Unprocessable Entity. Response: "+ result));
                        return null;
                    }

                    default: throw new Exception("Fakturoid returned unhandled. Response: "+ result);
                }

            } catch (Exception e) {
                Loggy.internalServerError("Fakturoid_Controller:: create_paid_invoice:", e);
            }
        }
        return null;
    }

    public static void sendInvoiceEmail(Model_Invoice invoice, String mail){
        try {

            logger.debug("Fakturoid_Controller:: sendInvoiceEmail:: Trying send PDF Invoice to User Email");

            byte[] body = download_PDF_invoice(invoice);

            if(body.length < 1){
                logger.warn("Incoming File from Fakturoid is empty!");
                return;
            }

            if (mail == null) mail = invoice.product.payment_details.invoice_email;

            logger.debug("Fakturoid_Controller:: sendInvoiceEmail:: PDF with invoice was successfully downloaded from Fakturoid");

            String[] monthNames_en = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

            new Email()
                    .text("Dear customer,")
                    .text("Please find an enclosed invoice for the services you ordered for " + monthNames_en[Calendar.getInstance().get(Calendar.MONTH)] + ".")
                    .text("In case of questions, please contact our financial department.")
                    .text("Best regards, Byzance Team")
                    .attachmentPDF(invoice.invoice_number + ".pdf", body)
                    .send(mail, "Invoice " + monthNames_en[Calendar.getInstance().get(Calendar.MONTH)]);

                logger.debug("Fakturoid_Controller:: sendInvoiceEmail:: Email was successfully sent");

        }catch (Exception e){
            Loggy.internalServerError("Fakturoid_Controller:: sendInvoiceEmail:", e);
        }
    }

    public static void sendInvoiceReminderEmail(Model_Invoice invoice, String message){
        try{

            byte[] body = Utilities_Fakturoid_Controller.download_PDF_invoice(invoice);

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
            Loggy.internalServerError("Fakturoid_Controller:: sendInvoiceReminderEmail:", e);
        }
    }

    private static String create_subject(Model_PaymentDetails details){

        for (int trial = 5; trial > 0; trial--) {
            try {

                ObjectNode request = Json.newObject();
                request.put("street", details.street + " " + details.street_number);
                request.put("city", details.city);
                request.put("zip", details.zip_code);
                request.put("email", details.invoice_email);

                if (details.company_account) {
                    // request.put("country", product.payment_details.country); (vyžaduje ISO code země - to zatím Tyrion nemá implementováno)
                    request.put("vat_no", details.company_vat_number);
                    request.put("phone", details.company_authorized_phone);
                    request.put("web", details.company_web);
                    request.put("name", details.company_name);
                } else {
                    request.put("name", details.full_name);
                }

                WSResponse response;

                JsonNode result;

                try {

                    F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + "/subjects.json")
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .setHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(5000)
                            .post(Json.toJson(request));

                    response = responsePromise.get(5000);

                    result = response.asJson();

                } catch (Exception e) {
                    Loggy.internalServerError("Fakturoid_Controller:: create_subject:", e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 201: {

                        logger.debug("Fakturoid_Controller:: create_subject: Status: 201");

                        details.product.archiveEvent("Subject created", "System created subject in Fakturoid", null);

                        if (result != null && result.has("id")) return result.get("id").asText();
                        else throw new NullPointerException("Response from Fakturoid does not contain ID.");
                    }

                    case 400: {

                        logger.debug("Fakturoid_Controller:: create_subject: Status: 400");
                        throw new Exception("Fakturoid returned 400 - Bad Request. Response: "+ result);
                    }

                    case 403: {

                        logger.debug("Fakturoid_Controller:: create_subject: Status: 403");
                        throw new Exception("Fakturoid returned 403 - Forbidden. Response: "+ result);
                    }

                    case 422: {

                        logger.debug("Fakturoid_Controller:: create_subject: Status: 422");

                        // TODO notifikace

                        Loggy.internalServerError("Fakturoid_Controller:: create_subject:", new Exception("Fakturoid returned 422 - Unprocessable Entity. Response: "+ result));
                        return null;
                    }

                    default: throw new Exception("Fakturoid returned unhandled. Response: "+ result);
                }

            } catch (Exception e) {
                Loggy.internalServerError("Fakturoid_Controller:: create_subject:", e);
            }
        }
        return null;
    }

    public static void edit_subject(Model_Product product){
        // TODO http://docs.fakturoid.apiary.io/#reference/subjects/subject/uprava-kontaktu
    }

// PRIVATE HELPERS METHODS #####################################################################################################

    public static JsonNode fakturoid_put(String url, JsonNode node){

        logger.debug("Fakturoid_Controller:: fakturoid_put:: PUT: URL: " + Server.Fakturoid_url + url + "  Json: " + node.toString());
        F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + url)
                .setContentType("application/json")
                .setHeader("User-Agent", Server.Fakturoid_user_agent)
                .setRequestTimeout(5000)
                .put(node);

        try {

            JsonNode response = responsePromise.get(5000).asJson();
            logger.debug("Fakturoid_Controller:: fakturoid_put:: Result: " + response.toString() );
            return response;

        }catch(Exception e){
            logger.error("Fakturoid_Controller:: fakturoid_put::  Error: " + responsePromise.get(5000).toString() );
            throw new NullPointerException();
        }
    }

    public static boolean fakturoid_post (String url){
        // Slouží ke změnám faktury - například na změnu stavu na "zaplaceno"
        logger.debug("Fakturoid_Controller: fakturoid_post: URL: " + Server.Fakturoid_url + url);

        for (int trial = 5; trial > 0; trial--){
            try{

                logger.debug("Fakturoid_Controller: fakturoid_post: Number of remaining tries: {}", trial);

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
                    Loggy.internalServerError("Fakturoid_Controller:: fakturoid_post:", e);
                    Thread.sleep(2500);
                    continue;
                }

                switch (response.getStatus()) {

                    case 200: {

                        logger.debug("Fakturoid_Controller:: fakturoid_post: Status: 200");
                        return true;
                    }

                    case 422: {

                        result = response.asJson();

                        logger.debug("Fakturoid_Controller:: fakturoid_post: Status: 422");

                        // TODO notifikace

                        Loggy.internalServerError("Fakturoid_Controller:: fakturoid_post:", new Exception("Fakturoid returned 422 - Unprocessable Entity. Response: "+ result));
                        return false;
                    }

                    default: {

                        result = response.asJson();

                        throw new Exception("Fakturoid returned unhandled. Response: "+ result);
                    }
                }

            } catch (Exception e) {
                Loggy.internalServerError("Fakturoid_Controller:: fakturoid_post:", e);
            }
        }
        return false;
    }

    public static boolean fakturoid_delete(String url){
        // Slouží například k mazáním proformy a transfromace na fakturu

        logger.debug("Fakturoid_Controller:: fakturoid_delete::  URL: " + Server.Fakturoid_url + url);

        for (int trial = 5; trial > 0; trial--){
            try {

                logger.debug("Fakturoid_Controller: fakturoid_delete: Number of remaining tries: {}", trial);

                WSResponse response;
                try {

                    F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + url)
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setContentType("application/json")
                            .setHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(5000)
                            .delete();

                    response = responsePromise.get(5000);

                    JsonNode result = response.asJson();

                    logger.debug("Fakturoid_Controller: fakturoid_delete: Response - {}", result);

                } catch (Exception e) {
                    Loggy.internalServerError("Fakturoid_Controller:: fakturoid_delete:", e);
                    Thread.sleep(2500);
                    continue;
                }

                if (response.getStatus() < 203) return true;

            } catch (Exception e) {
                Loggy.internalServerError("Fakturoid_Controller:: fakturoid_delete:", e);
            }
        }
        return  false;
    }

    public static byte[] download_PDF_invoice(Model_Invoice invoice){

            // Tuto metodu volám když ve fakturoidu pomocí API vytvořím fakturu.
            // U nich může dojít k latenci serveru a zpoždění vytvoření faktury - proto je zde while - který usíná na 2,5s a dává tomu 3x šanci než se rozhodne to zahodit úplně.

            int terminator = 3;
            while (terminator >= 0) {
                try {

                    logger.debug("Fakturoid_Controller:: download_PDF_invoice::  Trying download PDF invoice from Fakturoid on url: " + invoice.fakturoid_pdf_url);

                    F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(invoice.fakturoid_pdf_url)
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(5000)
                            .get();

                    WSResponse promise = responsePromise.get(5000);

                    logger.debug("Fakturoid_Controller:: download_PDF_invoice:: download_PDF_invoice:: promise status " + promise.getStatus());

                    if (promise.getStatus() == 200) {
                        logger.debug("Fakturoid_Controller:: download_PDF_invoice:: PDF Download successfully to byte[]");
                        return promise.asByteArray();

                    } else {

                        logger.warn("Fakturoid_Controller:: download_PDF_invoice:: promise status" + promise.getStatus());
                        logger.warn("Fakturoid_Controller:: download_PDF_invoice:: PDF Download un-successfully to byte[]");

                        --terminator;
                        Thread.sleep(2500);

                    }

                } catch (InterruptedException e) {
                    logger.error("Fakturoid_Controller:: download_PDF_invoice::  Error:: Interupted exception", e);
                }
            }

        logger.error("Fakturoid_Controller:: download_PDF_invoice:: Error:: PDF Download un-successfully to byte[]");
        throw new NullPointerException("File not found");

    }

}
