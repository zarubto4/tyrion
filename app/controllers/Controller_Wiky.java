package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;


import org.json.JSONObject;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import utilities.logger.Class_Logger;
import utilities.scheduler.jobs.Job_OldFloatingTokenRemoval;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Wiky extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Wiky.class);

     public Result test1(){
         try {


             new Job_OldFloatingTokenRemoval().execute(null);

             return ok(  );

         }catch (Exception e){
             e.printStackTrace();
             return badRequest();
         }
     }


    public Result test2(){
        try {

            throw new Exception("Test error hahaha dsdsdsdad");

        }catch (Exception e){
            terminal_logger.internalServerError("test2", e);
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
