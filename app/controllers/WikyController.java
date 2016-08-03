package controllers;


import io.swagger.annotations.Api;
import models.project.global.financial.Invoice;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.emails.EmailTool;
import utilities.fakturoid.Fakturoid_Controller;
import utilities.goPay.GoPay_Controller;

import java.util.Calendar;

@Api(value = "Not Documented API - InProgress or Stuck")
public class WikyController extends Controller {

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

     public Result test1(){
         try {

              GoPay_Controller.do_on_Demand_payment();

             return ok();

         }catch (Exception e){
             e.printStackTrace();
             System.out.println("Došlo k chybě");
             return badRequest();
         }

     }

    public Result test2(){

        Invoice invoice = Invoice.find.byId(142L);
        byte[] body = Fakturoid_Controller.download_PDF_invoice(invoice);

        if(body.length < 1){
            logger.warn("Incoming File from Facturoid is empty!");
            return ok();
        }

        logger.debug("PDF with invoice was successfully downloaded from Facturoid");

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
                .addAttachment_PDF("Invoice", body)
                .sendEmail("tomas.zaruba@byzance.cz","Invoice");



        logger.debug("Email was successfully sanded");

        return ok();
    }



}
