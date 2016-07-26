package utilities.facturoid;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.project.global.Product;
import models.project.global.financial.Invoice;
import play.api.libs.mailer.MailerClient;
import play.libs.F;
import play.libs.Json;
import play.libs.mailer.Email;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import utilities.Server;
import utilities.emails.EmailTool;

import java.util.Calendar;

public class Fakturoid_Controller extends Controller {

    // Rest Api call client
    @Inject WSClient ws;
    @javax.inject.Inject MailerClient mailerClient;

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");


    public JsonNode put(String url, ObjectNode node){

        F.Promise<WSResponse> responsePromise = ws.url(Server.Fakturoid_url + url)
                .setContentType("application/json")
                .setHeader("User-Agent", Server.Fakturoid_user_agent)
                .setRequestTimeout(5000)
                .put(node);

        return responsePromise.get(5000).asJson();
    }

    public JsonNode post(String url, ObjectNode node){

        F.Promise<WSResponse> responsePromise = ws.url(Server.Fakturoid_url + url)
                .setContentType("application/json")
                .setHeader("User-Agent", Server.Fakturoid_user_agent)
                .setRequestTimeout(5000)
                .post(node);

        return responsePromise.get(5000).asJson();
    }


    public void create_Invoice(Product product, Invoice invoice){

        ObjectNode request = Json.newObject();
        request.put("custom_id", product.id);

        if(product.subject_id == null) { create_subject_in_fakturoid(product); product.refresh();}

        request.put("subject_id", product.subject_id);

        request.put("number", invoice.invoice_number );
        request.put("currency", String.valueOf(product.currency_type));
        request.put("payment_method", String.valueOf(invoice.payment_method));
        request.put("due", 10);

        request.set("lines", Json.toJson(invoice.invoice_items));

        JsonNode result = put("/account.json", request);
        invoice.facturoid_invoice_id = request.get("id").asLong();
        invoice.update();
    }


    public void send_Invoice(Invoice invoice, Product product){
        try {

            logger.debug("Trying send PDF Invoice to User Email");

            F.Promise<WSResponse> responsePromise = ws.url(Server.Fakturoid_url + "invoices/" + invoice.facturoid_invoice_id + "/download.pdf")
                    .setContentType("application/pdf")
                    .setHeader("User-Agent", Server.Fakturoid_user_agent)
                    .setRequestTimeout(50000)
                    .get();

            byte[] body = responsePromise.get(50000).asByteArray();

            if(body.length < 1){
                logger.warn("Incoming File from Facturoid is empty!");
                return;
            }

            logger.debug("PDF with invoice was successfully downloaded from Facturoid");

            EmailTool emailTool = new EmailTool()
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
                    .addAttachment(invoice.invoice_number, body);


            Email email = emailTool.sendEmail(product.payment_details.company_invoice_email != null ? product.payment_details.company_invoice_email : product.payment_details.person.mail , "Invoice " + Calendar.getInstance().get(Calendar.MONTH), emailTool.getEmailContent());
            mailerClient.send(email);

            logger.debug("Email was successfully sanded");

        }catch (Exception e){
            logger.error("Error while sending invoice", e);
        }
    }



    public void create_subject_in_fakturoid(Product product){
        ObjectNode request = Json.newObject();
        request.put("custom_id", product.id);

        if(product.payment_details.company_account) {
            request.put("name", product.payment_details.company_name);
            request.put("street", product.payment_details.street + " " + product.payment_details.street_number);
            request.put("city", product.payment_details.city);
            request.put("zip", product.payment_details.zip_code);
            request.put("country", product.payment_details.country);
            request.put("zip", product.payment_details.city);
            request.put("vat_no", product.payment_details.VAT_number);
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

        JsonNode result = post("subjects.json", request);
        product.subject_id = result.get("id").asText();
        product.update();
    }

    public void edit_subject(Product product){
        // TODO http://docs.fakturoid.apiary.io/#reference/subjects/subject/uprava-kontaktu
    }




}
