package utilities.fakturoid;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.project.global.Product;
import models.project.global.financial.Invoice;
import play.api.Play;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Server;
import utilities.emails.EmailTool;
import utilities.enums.Payment_method;
import utilities.enums.Payment_status;
import utilities.fakturoid.helps_objects.Fakturoid_Invoice;
import utilities.loggy.Loggy;
import utilities.response.GlobalResult;

import java.util.Calendar;

public class Fakturoid_Controller extends Controller {


    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");



// PUBLIC CONTROLLERS METHODS ##########################################################################################


    public Result get_PDF_Invoice(Long invoice_id){

        try {

            Invoice invoice = Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            byte[] pdf_in_array = download_PDF_invoice(invoice);
            return GlobalResult.result_pdf_file(pdf_in_array);

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }


// PRIVATE EXECUTIVE METHODS ###########################################################################################

    public static Invoice create_proforma(Product product, Invoice invoice){

        Fakturoid_Invoice fakturoid_invoice = new Fakturoid_Invoice();
        fakturoid_invoice.custom_id         = product.id;
        fakturoid_invoice.client_name       = product.payment_details.company_account ? product.payment_details.company_name : product.payment_details.person.full_name;
        fakturoid_invoice.currency          = product.currency;
        fakturoid_invoice.payment_method    = Payment_method.bank_transfer.name();
        fakturoid_invoice.lines             = invoice.invoice_items;
        fakturoid_invoice.proforma          = true;
        fakturoid_invoice.partial_proforma  = true;

        if(product.fakturoid_subject_id == null) {
            logger.debug("Client has not registration object in Fakturoid");
            String fakturoid_subject_id = create_subject_in_fakturoid(product);
            product.update();

            logger.debug("New Client Id in Fakturoid is " + fakturoid_subject_id);
            fakturoid_invoice.subject_id = fakturoid_subject_id;

        }else {
            logger.debug("Client has already registration object in Fakturoid");
            fakturoid_invoice.subject_id = product.fakturoid_subject_id;
        }

        logger.debug("Sending Proforma to Fakturoid");
        JsonNode result = fakturoid_post("/invoices.json", Json.toJson(fakturoid_invoice));

        if(!result.has("id")) throw new NullPointerException("Invoice From fakturoid does not contains ID");


        invoice.facturoid_invoice_id = result.get("id").asLong();
        invoice.facturoid_pdf_url    = result.get("pdf_url").asText();
        invoice.invoice_number       = result.get("number").asText();
        invoice.update();

        return invoice;
    }

    public static Invoice create_paid_invoice(Product product, Invoice invoice){

        Fakturoid_Invoice fakturoid_invoice = new Fakturoid_Invoice();
        fakturoid_invoice.custom_id         = product.id;
        fakturoid_invoice.client_name       = product.payment_details.company_account ? product.payment_details.company_name : product.payment_details.person.full_name;
        fakturoid_invoice.currency          = product.currency;
        fakturoid_invoice.payment_method    = Payment_method.bank_transfer.name();
        fakturoid_invoice.lines             = invoice.invoice_items;
        fakturoid_invoice.proforma          = false;
        fakturoid_invoice.partial_proforma  = false;
        fakturoid_invoice.subject_id        = product.fakturoid_subject_id;


        logger.debug("Sending Invoice to Fakturoid");
        JsonNode result = fakturoid_post("/invoices.json", Json.toJson(fakturoid_invoice));

        if(!result.has("id")) throw new NullPointerException("Invoice From fakturoid does not contain ID");

        invoice.facturoid_invoice_id = result.get("id").asLong();
        invoice.facturoid_pdf_url    = result.get("pdf_url").asText();
        invoice.invoice_number       = result.get("number").asText();
        invoice.status               = Payment_status.paid;
        invoice.update();

        return invoice;
    }

    public static void send_Invoice_to_Email(Invoice invoice){
        try {

            logger.debug("Trying send PDF Invoice to User Email");


                byte[] body = download_PDF_invoice(invoice);

                if(body.length < 1){
                    logger.warn("Incoming File from Facturoid is empty!");
                    return;
                }

                logger.debug("PDF with invoice was successfully downloaded from Facturoid");

                String[] monthNames_en = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

                        new EmailTool()
                        .addEmptyLineSpace()
                        .startParagraph("14")
                        .addText("Dear customer,")
                        .endParagraph()
                        .startParagraph("13")
                        .addText("Please find enclosed an invoice for the services you order." + Calendar.getInstance().get(Calendar.MONTH) )
                        .endParagraph()
                        .startParagraph("10")
                        .addText("In case of questions, please contact our financial department.")
                        .addText("Have a nice day")
                        .addEmptyLineSpace()
                        .addAttachment_PDF(invoice.invoice_number + ".pdf", body)
                        .sendEmail( invoice.product.payment_details.company_invoice_email != null ? invoice.product.payment_details.company_invoice_email : invoice.product.payment_details.person.mail  , "Invoice " + monthNames_en[Calendar.getInstance().get(Calendar.MONTH)] );


                logger.debug("Email was successfully sanded");



        }catch (Exception e){
            logger.error("Error while sending invoice", e);
        }
    }

    public static  void send_UnPaidInvoice_to_Email(Invoice invoice){

        try{

            byte[] body = Fakturoid_Controller.download_PDF_invoice(invoice);

            String[] monthNames_en = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

                     new EmailTool()
                    .addEmptyLineSpace()

                    .startParagraph("13")
                    .addText("Hello,")
                    .endParagraph()

                    .startParagraph("11")
                    .addText("We have problems with payment services fo actual month. ")
                    .addText("Log in and check your credit parameters. " +
                            "Your services will be supported by a further 30 days after the time expires. " +
                            "Please contact us immediately if something is not clear to you.")
                    .addEmptyLineSpace()

                    .startParagraph("11")
                    .addText("I attachment you have invoice.")
                    .endParagraph()

                    .startParagraph("11")
                    .addText("Best regard, Byzance Team")
                    .endParagraph()


                    .addEmptyLineSpace()
                    .endParagraph()

                    .addAttachment_PDF(invoice.invoice_number + ".pdf", body)
                    .sendEmail( invoice.product.payment_details.company_invoice_email != null ? invoice.product.payment_details.company_invoice_email : invoice.product.payment_details.person.mail , "Invoice for " + monthNames_en[Calendar.getInstance().get(Calendar.MONTH)] + ". Problems with payment" );


        }catch (Exception e){
            logger.error("Error while sending invoice", e);
        }
    }

    public static String create_subject_in_fakturoid(Product product){
        ObjectNode request = Json.newObject();

        if(product.payment_details.company_account) {
            request.put("name", product.payment_details.company_name);
            request.put("street", product.payment_details.street + " " + product.payment_details.street_number);
            request.put("city", product.payment_details.city);
            request.put("zip", product.payment_details.zip_code);
            request.put("country", product.payment_details.country);
            request.put("zip", product.payment_details.city);
            request.put("vat_no", product.payment_details.company_vat_number);
            request.put("email", product.payment_details.company_authorized_email);
            request.put("phone", product.payment_details.company_authorized_phone);
            request.put("web", product.payment_details.company_web);

        }else {
            request.put("name", product.payment_details.person.full_name);
            request.put("street", product.payment_details.street + " " + product.payment_details.street_number);
            request.put("city", product.payment_details.city);
            request.put("zip", product.payment_details.zip_code);
            request.put("country", product.payment_details.country);
            request.put("zip", product.payment_details.city);
            request.put("email", product.payment_details.person.mail);
        }

        JsonNode result = fakturoid_post("/subjects.json", request);
        product.fakturoid_subject_id = result.get("id").asText();
        return product.fakturoid_subject_id;
    }


    public static void edit_subject(Product product){
        // TODO http://docs.fakturoid.apiary.io/#reference/subjects/subject/uprava-kontaktu
    }

// PRIVATE HELPERS METHODS #####################################################################################################

    public static JsonNode fakturoid_put(String url, JsonNode node){

        logger.debug("Fakturoid controller: PUT: URL: " + Server.Fakturoid_url + url + "  Json: " + node.toString());
        F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + url)
                .setContentType("application/json")
                .setHeader("User-Agent", Server.Fakturoid_user_agent)
                .setRequestTimeout(5000)
                .put(node);


        try {

            JsonNode response = responsePromise.get(5000).asJson();
            logger.debug("Fakturoid controller: PUT: Result: " + response.toString() );
            return response;

        }catch(Exception e){
            logger.error("Put Failed: " + responsePromise.get(5000).toString() );
            throw new NullPointerException();
        }
    }

    public static JsonNode fakturoid_post(String url, JsonNode node){

        logger.debug("Fakturoid controller: POST: URL: " + Server.Fakturoid_url + url + "  Json: " + node);

        F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + url)
                .setAuth(Server.Fakturoid_secret_combo)
                .setContentType("application/json")
                .setHeader("User-Agent", Server.Fakturoid_user_agent)
                .setRequestTimeout(5000)
                .post(node);

        try {

            WSResponse response = responsePromise.get(5000);


            logger.debug("Incoming status: " + response.getStatus());
            logger.debug("Incoming message: " + Json.toJson(response.getBody()).toString());


            if( response.getStatus() == 201) {
                JsonNode json = response.asJson();
                logger.debug("Fakturoid controller: POST: Result: " + json.toString());
                return json;

            }else if( response.getStatus() == 401){
                logger.error("Fakturoid!!!!!!!!!!!!!");
                logger.error("Fakturoid Unauthorized");
                logger.error("Fakturoid!!!!!!!!!!!!!");

                throw new NullPointerException();

            }else if( response.getStatus() == 403){
                logger.error("Fakturoid!!!!!!!!!!!!!");
                logger.error("Fakturoid you have maximum of customers!!!");
                logger.error("Fakturoid!!!!!!!!!!!!!");

                throw new NullPointerException();
            }else if( response.getStatus() == 422 ){

                logger.error("Fakturoid!!!!!!!!!!!!!");
                logger.error("Customer s Id je již vytvořen!");
                logger.error("Fakturoid!!!!!!!!!!!!!");

                throw new NullPointerException();
            }



            throw new NullPointerException();

        }catch(Exception e){
            e.printStackTrace();
            logger.error("POST Failed: " + responsePromise.get(5000).toString() );
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

        logger.debug("Fakturoid controller: DELETE: URL: " + Server.Fakturoid_url + url);

        F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.Fakturoid_url + url)
                .setAuth(Server.Fakturoid_secret_combo)
                .setContentType("application/json")
                .setHeader("User-Agent", Server.Fakturoid_user_agent)
                .setRequestTimeout(5000)
                .delete();

        int status = responsePromise.get(5000).getStatus();
        return  (status < 205);
    }

    public static byte[] download_PDF_invoice(Invoice invoice){

            // Tuto metodu volám když ve fakturoidu pomocí API vytvořím fakturu.
            // U nich může dojít k latenci serveru a zpoždění vytvoření faktury - proto je zde while - který usíná na 2,5s a dává tomu 3x šanci než se rozhodne to zahodit úplně.

            int terminator = 3;
            while (terminator >= 0) {
                try {

                    logger.debug("Trying download PDF invoice from Fakturoid on url: " + invoice.facturoid_pdf_url);

                    F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(invoice.facturoid_pdf_url)
                            .setAuth(Server.Fakturoid_secret_combo)
                            .setHeader("User-Agent", Server.Fakturoid_user_agent)
                            .setRequestTimeout(5000)
                            .get();

                    WSResponse promise = responsePromise.get(5000);

                    System.out.println("promise status " + promise.getStatus());

                    if (promise.getStatus() == 200) {
                        logger.debug("PDF Download successfully to byte[]");
                        return promise.asByteArray();

                    } else {

                        logger.warn("promise status" + promise.getStatus());
                        logger.warn("PDF Download un-successfully to byte[]");

                        --terminator;
                        Thread.sleep(2500);

                    }

                } catch (InterruptedException e) {
                    logger.error("Interupted exception", e);
                }
            }

        logger.error("PDF Download un-successfully to byte[]");
        throw new NullPointerException("File not found");

    }

}
