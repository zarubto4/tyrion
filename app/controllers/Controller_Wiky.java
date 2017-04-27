package controllers;


import com.microsoft.azure.documentdb.*;
import io.swagger.annotations.Api;
import models.Model_Product;
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

            System.out.println(" Test 2 ");

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setOfferThroughput(1000);


            System.out.println("new DocumentCollection");
            // Define a new collection using the id above.
            DocumentCollection myCollection = new DocumentCollection();
            myCollection.setId("Model_Board_Test");

            System.out.println("new DocumentCollection Done");

            // Create a new collection.
            myCollection = Server.documentClient.createCollection("dbs/" + Server.no_sql_database.getId(), myCollection, requestOptions).getResource();

            System.out.println("Created a new collection:");
            System.out.println(myCollection.toString());

            return ok();

        }catch (Exception e){
            e.printStackTrace();
            return badRequest();
        }

    }


}
