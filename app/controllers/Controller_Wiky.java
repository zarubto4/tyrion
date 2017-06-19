package controllers;

import io.swagger.annotations.Api;
import models.Model_Board;
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

             new Job_SpendingCredit().execute(null);

             return ok();

         }catch (Exception e){
             e.printStackTrace();
             return badRequest();
         }
     }

    public Result test2(){
        try {

            Model_Board board = Model_Board.find.where().eq("personal_description", "[G]").findUnique();

            Date time = board.last_online();

            System.out.println(time.toString());

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