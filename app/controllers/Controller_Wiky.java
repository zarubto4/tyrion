package controllers;


import io.swagger.annotations.Api;
import models.Model_Product;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.logger.Class_Logger;
import utilities.response.GlobalResult;

import static utilities.scheduler.jobs.Job_SpendingCredit.spend;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Wiky extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_WebSocket.class);

     public Result test1(){
         try {

             Model_Product product = Model_Product.get_byId(request().body().asJson().get("id").asText());
             if (product == null) return GlobalResult.notFoundObject("Product not found");

             spend(product);

             return ok();
         }catch (Exception e){
             e.printStackTrace();
             return badRequest();
         }

     }





}
