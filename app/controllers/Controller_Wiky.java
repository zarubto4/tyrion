package controllers;

import io.swagger.annotations.Api;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.logger.Class_Logger;
import utilities.scheduler.jobs.Job_OldFloatingTokenRemoval;
import utilities.scheduler.jobs.Job_SpendingCredit;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Wiky extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Wiky.class);

     public Result test1(){
         try {

             new Job_SpendingCredit().execute(null);

             return ok();

         }catch (Exception e){
             e.printStackTrace();
             return badRequest();
         }
     }

    public Result test2(){
        try {

            terminal_logger.error("Test error hahaha dsdsdsdad");

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