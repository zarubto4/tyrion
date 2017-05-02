package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.documentdb.*;
import io.swagger.annotations.Api;
import models.Model_Board;
import models.Model_Product;
import play.api.Play;
import play.libs.F;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Server;
import utilities.logger.Class_Logger;
import utilities.response.GlobalResult;

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

    public Result test2(){
        try {


            WSClient ws = Play.current().injector().instanceOf(WSClient.class);

            F.Promise<WSResponse> responsePromise = ws.url(Server.tyrion_serverAddress + "/api-docs")
                    .setContentType("application/json")
                    .setHeader("Accept", "application/json")
                    .setRequestTimeout(15000)
                    .get();


            JsonNode result = responsePromise.get(5000).asJson();

            return GlobalResult.result_ok(result);

        }catch (Exception e){
            e.printStackTrace();
            return badRequest();
        }

    }





}
