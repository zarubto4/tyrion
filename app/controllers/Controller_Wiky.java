package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.Model_Product;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
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

            ObjectNode json = Json.newObject();
            json.put("test", "test");

            return GlobalResult.result_ok(json);

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