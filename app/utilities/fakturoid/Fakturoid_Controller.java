package utilities.fakturoid;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import models.Model_Product;
import models.Model_Invoice;
import play.api.Play;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Server;
import utilities.emails.Email;
import utilities.enums.Enum_Currency;
import utilities.enums.Payment_status;
import utilities.fakturoid.helps_objects.Fakturoid_Invoice;
import utilities.loggy.Loggy;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_BadRequest;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.swagger.outboundClass.Swagger_Invoice_FullDetails;

import java.util.Calendar;

public class Fakturoid_Controller extends Controller {


    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");



    @ApiOperation(value = "get Invoice PDF file",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get PDF invoice file",
            produces = "multipartFormData",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Swagger_Invoice_FullDetails.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result invoice_get_pdf(String invoice_id){

        try {
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            if(!invoice.read_permission()) return GlobalResult.forbidden_Permission();

            byte[] pdf_in_array = Fakturoid_Controller.download_PDF_invoice(invoice);

            return GlobalResult.result_pdf_file(pdf_in_array, invoice.invoice_number + ".pdf");

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }


// PUBLIC CONTROLLERS METHODS ##########################################################################################



// PRIVATE EXECUTIVE METHODS ###########################################################################################

    public static Model_Invoice create_proforma(Model_Product product, Model_Invoice invoice){

        Fakturoid_Invoice fakturoid_invoice = new Fakturoid_Invoice();
        fakturoid_invoice.custom_id         = product.id;
        fakturoid_invoice.client_name       = product.payment_details.company_account ? product.payment_details.company_name : product.payment_details.person.full_name;
        fakturoid_invoice.currency          = Enum_Currency.USD;
        fakturoid_invoice.lines             = invoice.getInvoice_items();
        fakturoid_invoice.proforma          = true;
        fakturoid_invoice.partial_proforma  = true;

        if(product.fakturoid_subject_id == null) {

            logger.debug("Fakturoid_Controller:: create_proforma:: Client has not registration object in Fakturoid");
            // Ověřím zda tam je - a jestli ano - tak ho jen vytvořím v lokální DB

            // Pokud ne tak ho vytvořím
            String fakturoid_subject_id = create_subject_in_fakturoid(product);
            product.update();

            logger.debug("Fakturoid_Controller:: create_proforma:: New Client Id in Fakturoid is " + fakturoid_subject_id);
            fakturoid_invoice.subject_id = fakturoid_subject_id;

        }else {
            logger.debug("Fakturoid_Controller:: create_proforma:: Client has already registration object in Fakturoid");
            fakturoid_invoice.subject_id = product.fakturoid_subject_id;
        }

        invoice.refresh();
        logger.debug("Fakturoid_Controller:: create_proforma::  Sending Proforma to Fakturoid");

        F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + "/invoices.json")
                .setAuth(Server.Fakturoid_secret_combo)
                .setContentType("application/json")
                .setHeader("User-Agent", Server.Fakturoid_user_agent)
                .setRequestTimeout(5000)
                .post(Json.toJson(fakturoid_invoice));

            WSResponse response = responsePromise.get(5000);

            logger.debug("Fakturoid_Controller:: create_proforma:: Incoming status: " + response.getStatus());


            if( response.getStatus() == 201) {
                JsonNode result = response.asJson();
                logger.debug("Fakturoid_Controller:: create_proforma:: POST: Result: " + result.toString());

                if(!result.has("id")){
                    logger.error("Fakturoid_Controller:: create_proforma:: Invoice From fakturoid does not contains ID");
                    throw new NullPointerException("Invoice From fakturoid does not contains ID");
                }


                invoice.facturoid_invoice_id = result.get("id").asLong();
                invoice.facturoid_pdf_url    = result.get("pdf_url").asText();
                invoice.invoice_number       = result.get("number").asText();
                invoice.update();

                return invoice;

            }else if( response.getStatus() == 401){
                logger.error("Fakturoid_Controller:: create_proforma:: Fakturoid Unauthorized");
                throw new NullPointerException();
            }else if( response.getStatus() == 403){
                logger.error("Fakturoid_Controller:: create_proforma:: Fakturoid you have maximum of customers!!!");
                throw new NullPointerException();

            }else if( response.getStatus() == 422 ){

                logger.error("Fakturoid_Controller:: create_proforma::  Response"+ response.getBody());

                throw new NullPointerException();
            }

        throw new NullPointerException();
    }

    public static Model_Invoice create_paid_invoice(Model_Product product, Model_Invoice invoice){

        Fakturoid_Invoice fakturoid_invoice = new Fakturoid_Invoice();
        fakturoid_invoice.custom_id         = product.id;
        fakturoid_invoice.client_name       = product.payment_details.company_account ? product.payment_details.company_name : product.payment_details.person.full_name;
        fakturoid_invoice.currency          = Enum_Currency.USD;
        fakturoid_invoice.lines             = invoice.invoice_items;
        fakturoid_invoice.proforma          = false;
        fakturoid_invoice.partial_proforma  = false;
        fakturoid_invoice.subject_id        = product.fakturoid_subject_id;


        logger.debug("Fakturoid_Controller:: create_paid_invoice:: Sending Invoice to Fakturoid");
        JsonNode result = fakturoid_post("/invoices.json", Json.toJson(fakturoid_invoice));

        if(!result.has("id")) throw new NullPointerException("Invoice From fakturoid does not contain ID");

        invoice.facturoid_invoice_id = result.get("id").asLong();
        invoice.facturoid_pdf_url    = result.get("pdf_url").asText();
        invoice.invoice_number       = result.get("number").asText();
        invoice.status               = Payment_status.paid;
        invoice.update();

        return invoice;
    }

    public static void send_Invoice_to_Email(Model_Invoice invoice){
        try {

            logger.debug("Fakturoid_Controller:: send_Invoice_to_Email:: Trying send PDF Invoice to User Email");

            byte[] body = download_PDF_invoice(invoice);

            if(body.length < 1){
                logger.warn("Incoming File from Facturoid is empty!");
                return;
            }


            logger.debug("Fakturoid_Controller:: send_Invoice_to_Email:: PDF with invoice was successfully downloaded from Facturoid");

            String[] monthNames_en = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

            new Email()
                    .text("Dear customer,")
                    .text("Please find an enclosed invoice for the services you ordered. " + Calendar.getInstance().get(Calendar.MONTH))
                    .text("In case of questions, please contact our financial department." + Email.newLine() + "Have a nice day.")
                    .attachmentPDF(invoice.invoice_number + ".pdf", body)
                    .send( invoice.product.payment_details.invoice_email != null ? invoice.product.payment_details.invoice_email : invoice.product.payment_details.person.mail  , "Invoice " + monthNames_en[Calendar.getInstance().get(Calendar.MONTH)] );


                logger.debug("Fakturoid_Controller:: send_Invoice_to_Email:: Email was successfully sanded");



        }catch (Exception e){
            logger.error("Fakturoid_Controller:: send_Invoice_to_Email:: Error while sending invoice", e);
        }
    }

    public static  void send_UnPaidInvoice_to_Email(Model_Invoice invoice){

        try{

            byte[] body = Fakturoid_Controller.download_PDF_invoice(invoice);

            String[] monthNames_en = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

            new Email()
                    .text("Hello,")
                    .text("We have problems with payment of your services on this month. Log in and check your credit parameters. " +
                            "Your services will be supported for 30 days after expiry. Please contact us immediately if something is unclear.")
                    .text("See the attached invoice.")
                    .text("Best regards, Byzance Team")
                    .attachmentPDF(invoice.invoice_number + ".pdf", body)
                    .send( invoice.product.payment_details.invoice_email != null ? invoice.product.payment_details.invoice_email : invoice.product.payment_details.person.mail , "Invoice for " + monthNames_en[Calendar.getInstance().get(Calendar.MONTH)] + ". Problems with payment" );


        }catch (Exception e){
            logger.error("Fakturoid_Controller:: send_Invoice_to_Email:: Error while sending invoice", e);
        }
    }

    public static  void send_invoice_to_Email(Model_Invoice invoice, String email){

        try{

            byte[] body = Fakturoid_Controller.download_PDF_invoice(invoice);

            String[] monthNames_en = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

            new Email()
                    .text("Dear customer,")
                    .text("Here is your invoice number: " + invoice.invoice_number)
                    .text("In case of questions, please contact our financial department." + Email.newLine() + "Have a nice day.")
                    .attachmentPDF(invoice.invoice_number + ".pdf", body)
                    .send( email , "Invoice from Byzance :-)" );


        }catch (Exception e){
            logger.error("Fakturoid_Controller:: send_Invoice_to_Email:: Error while sending invoice", e);
        }
    }

    public static String create_subject_in_fakturoid(Model_Product product){
        ObjectNode request = Json.newObject();

        product.refresh();

        if(product.payment_details.company_account) {
            request.put("name", product.payment_details.company_name);
            request.put("street", product.payment_details.street + " " + product.payment_details.street_number);
            request.put("city", product.payment_details.city);
            request.put("zip", product.payment_details.zip_code);
            // request.put("country", product.payment_details.country); (vyžaduje ISO code země - to zatím Tyrion nemá implementováno)
            request.put("vat_no", product.payment_details.company_vat_number);
            request.put("email", product.payment_details.company_authorized_email);
            request.put("phone", product.payment_details.company_authorized_phone);
            request.put("web", product.payment_details.company_web);

        }else {
            request.put("name", product.payment_details.person.full_name);
            request.put("street", product.payment_details.street + " " + product.payment_details.street_number);
            request.put("city", product.payment_details.city);
            request.put("zip", product.payment_details.zip_code);
            // request.put("country", product.payment_details.country);  (vyžaduje ISO code země - to zatím Tyrion nemá implementováno)
            request.put("email", product.payment_details.person.mail);
        }

        JsonNode result = fakturoid_post("/subjects.json", request);
        product.fakturoid_subject_id = result.get("id").asText();
        return product.fakturoid_subject_id;
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

    public static JsonNode fakturoid_post(String url, JsonNode node){

        logger.debug("Fakturoid_Controller:: fakturoid_post:: URL: " + Server.Fakturoid_url + url + "  Json: " + node);

        F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + url)
                .setAuth(Server.Fakturoid_secret_combo)
                .setContentType("application/json")
                .setHeader("User-Agent", Server.Fakturoid_user_agent)
                .setRequestTimeout(5000)
                .post(node);

        try {

            WSResponse response = responsePromise.get(5000);

            logger.debug("Fakturoid_Controller:: fakturoid_post:: Incoming status: " + response.getStatus());
            logger.debug("Fakturoid_Controller:: fakturoid_post:: Incoming message: " + Json.toJson(response.getBody()).toString());


            if( response.getStatus() == 201) {
                JsonNode json = response.asJson();
                logger.debug("Fakturoid_Controller:: fakturoid_post::  Result: " + json.toString());
                return json;

            }else if( response.getStatus() == 401){
                logger.error("Fakturoid_Controller:: fakturoid_post:: Error:: Fakturoid Unauthorized");

                throw new NullPointerException();

            }else if( response.getStatus() == 403){

                logger.error("Fakturoid_Controller:: fakturoid_post::  Error:: Fakturoid you have maximum of customers!!!");

                throw new NullPointerException();

            }else if( response.getStatus() == 422 ){

                logger.error("Fakturoid_Controller:: fakturoid_post::  Error:: " + Json.toJson(response.getBody()).toString());

                throw new NullPointerException();
            }

            throw new NullPointerException();

        }catch(Exception e){
            e.printStackTrace();
            logger.error("Fakturoid_Controller:: fakturoid_post:: Error:: " + responsePromise.get(5000).toString() );
            throw new NullPointerException();
        }
    }

    public static boolean fakturoid_post (String url){
        // Slouží ke změnám faktury - například na změnu stavu na "zaplaceno"
        logger.debug("Fakturoid controller: POST: URL: " + Server.Fakturoid_url + url);

        F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + url)
                .setAuth(Server.Fakturoid_secret_combo)
                .setContentType("application/json")
                .setHeader("User-Agent", Server.Fakturoid_user_agent)
                .setRequestTimeout(5000)
                .post("{}");

        int status = responsePromise.get(5000).getStatus();
        return  (status < 203);
    }

    public static boolean fakturoid_delete(String url){
        // Slouží například k mazáním proformy a transfromace na fakturu

        logger.debug("Fakturoid_Controller:: fakturoid_delete::  URL: " + Server.Fakturoid_url + url);

        F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + url)
                .setAuth(Server.Fakturoid_secret_combo)
                .setContentType("application/json")
                .setHeader("User-Agent", Server.Fakturoid_user_agent)
                .setRequestTimeout(5000)
                .delete();

        int status = responsePromise.get(5000).getStatus();
        return  (status < 205);
    }

    public static byte[] download_PDF_invoice(Model_Invoice invoice){

            // Tuto metodu volám když ve fakturoidu pomocí API vytvořím fakturu.
            // U nich může dojít k latenci serveru a zpoždění vytvoření faktury - proto je zde while - který usíná na 2,5s a dává tomu 3x šanci než se rozhodne to zahodit úplně.

            int terminator = 3;
            while (terminator >= 0) {
                try {

                    logger.debug("Fakturoid_Controller:: download_PDF_invoice::  Trying download PDF invoice from Fakturoid on url: " + invoice.facturoid_pdf_url);

                    F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(invoice.facturoid_pdf_url)
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
