package controllers;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import com.microsoft.azure.documentdb.*;
import com.tc.util.UUID;
import io.swagger.annotations.Api;
import models.Model_Board;
import models.Model_Product;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Server;
import utilities.logger.Class_Logger;
import utilities.response.GlobalResult;

import java.util.Date;
import java.util.List;

import static utilities.scheduler.jobs.Job_SpendingCredit.spend;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Wiky extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Wiky.class);

     public Result test1(){
         try {

             Model_Product product = Model_Product.get_byId(request().body().asJson().get("id").asText());
             if (product == null) return GlobalResult.notFoundObject("Product not found");

             spend(product);

             return ok();
         }catch (Exception e){
             terminal_logger.internalServerError(e);
             return badRequest();
         }

     }

    public static final String COLLECTION_MAME = Model_Board.class.getSimpleName();
    public static DocumentCollection collection = null;

    public Result test2(){
        try {

            new Model_Board().make_log_connect();

            return ok();

        }catch (Exception e){
            e.printStackTrace();
            return badRequest();
        }

    }





}
