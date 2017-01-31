package controllers;


import io.swagger.annotations.Api;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.schedules_activities.Sending_Invoices;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Wiky extends Controller {


     public Result test1(){

         try {

             new Sending_Invoices().execute(null);

             return ok();
         }catch (Exception e){
             e.printStackTrace();
             return badRequest();
         }

     }





}
