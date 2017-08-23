package controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.Model_Product;
import org.mindrot.jbcrypt.BCrypt;
import play.mvc.Controller;
import play.mvc.Result;
import utilities._AAA_printer.Printer_Api;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.response.GlobalResult;
import utilities.scheduler.jobs.Job_SpendingCredit;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_ZZZ_Tester extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_ZZZ_Tester.class);

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

            terminal_logger.error(BCrypt.hashpw("password", BCrypt.gensalt(12)));

            return GlobalResult.result_ok();

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test3(){
        try {

            System.out.println("Testuji Apu Printer");

            Printer_Api api = new Printer_Api();

            // Test of printer
            // new Label_65_mm();


            return GlobalResult.result_ok();
        }catch (Exception e){
            e.printStackTrace();
            return Server_Logger.result_internalServerError(e, request());
        }
    }
}