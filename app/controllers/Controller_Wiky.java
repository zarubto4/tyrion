package controllers;

import io.swagger.annotations.Api;
import models.Model_Board;
import models.Model_Product;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.logger.Class_Logger;
import utilities.scheduler.jobs.Job_OldFloatingTokenRemoval;
import utilities.scheduler.jobs.Job_SpendingCredit;

import java.util.Date;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Wiky extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Wiky.class);

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

    public Result test2(){
        try {

            new Job_SpendingCredit().execute(null);

            return ok();

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return badRequest();
        }
    }

    public Result test3(){
        try {

            return ok();
        }catch (Exception e){
            e.printStackTrace();
            return badRequest();
        }
    }
}