package controllers;


import com.microsoft.azure.documentdb.*;
import io.swagger.annotations.Api;
import models.Model_Product;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.logger.Class_Logger;
import utilities.response.GlobalResult;
import utilities.swagger.outboundClass.Swagger_B_Program_Version_Short_Detail;

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

            // Replace with your DocumentDB end point and master key.
            String END_POINT = "stagedocumentdb.documents.azure.com";
            String MASTER_KEY = "8IFUApIpk9kcY7BjKhulIHlxe77OBOyaZeh8F6UyKWqEmU81PpH7AOfGBFb08RThHhbd1UBgyFLvFlc55lRJOg==";

            // Define an id for your database and collection
            String DATABASE_ID = "stagedocumentdb";
            String COLLECTION_ID = "TestCollection";

            System.out.println(" DocumentClient");
            DocumentClient documentClient = new DocumentClient(END_POINT, MASTER_KEY, ConnectionPolicy.GetDefault(), ConsistencyLevel.Session);


            System.out.println(" Database create ");
            Database myDatabase = new Database();
            myDatabase.setId(DATABASE_ID);

            System.out.println(" RequestOptions ");
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setOfferThroughput(1000);

            DocumentCollection myCollection = new DocumentCollection();
            myCollection.setId( "TestCollection ");

            // Create a new collection.
            myCollection = documentClient.createCollection("dbs/" + DATABASE_ID, myCollection, requestOptions).getResource();
            
            Swagger_B_Program_Version_Short_Detail swagger_b_program_version_short_detail = new Swagger_B_Program_Version_Short_Detail();
            swagger_b_program_version_short_detail.version_id = "122113";



            Document allenDocument = new Document( Json.toJson(swagger_b_program_version_short_detail).toString() );


            allenDocument = documentClient.createDocument("dbs/" + DATABASE_ID + "/colls/" + "TestCollection", allenDocument, null, false).getResource();


            System.out.println( "Vypisuji dokument " + allenDocument.toString());


            return ok();
        }catch (Exception e){
            e.printStackTrace();
            return badRequest();
        }

    }


}
