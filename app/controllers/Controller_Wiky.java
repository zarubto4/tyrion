package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.Model_PaymentDetails;
import models.Model_Product;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.financial.fakturoid.Fakturoid;
import utilities.logger.Class_Logger;
import utilities.response.GlobalResult;
import utilities.scheduler.jobs.Job_SpendingCredit;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Wiky extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Wiky.class);

    @ApiOperation(value = "Hidden test Method", hidden = true)
     public Result test1(){
         try {

             String id = request().body().asJson().get("id").asText();
             if (id == null) return badRequest("id is null");

             Model_Product product = Model_Product.get_byId(id);

             Job_SpendingCredit.spend(product);

             return ok("Credit was spent");

         }catch (Exception e){
             terminal_logger.internalServerError(e);
             return badRequest();
         }
     }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test2(){
        try {

            Model_PaymentDetails details = new Model_PaymentDetails();
            details.company_name = "HAHAHA";
            details.street = "s";
            details.street_number = "6";
            details.city = "a";
            details.zip_code = "654";
            details.country = "d";
            details.company_authorized_phone = "6";
            details.company_authorized_email = "a@s.cz";
            details.company_registration_no = "123";
            details.company_vat_number = "CZ28496639";
            details.company_web = "haha.cz";
            details.invoice_email = "s@s.cz";

            details.company_account = true;

            return GlobalResult.result_ok(Fakturoid.create_subject(details));

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test3(){
        try {

            return ok();
        }catch (Exception e){
            e.printStackTrace();
            return badRequest();
        }
    }
}