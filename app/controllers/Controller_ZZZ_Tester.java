package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.Document;
import play.mvc.Result;
import utilities.logger.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_ZZZ_Tester extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_ZZZ_Tester.class);

// CONTROLLER CONFIGURATION ############################################################################################

    // Nothing
    private Config config;

// CONTROLLER CONTENT ##################################################################################################
    @ApiOperation(value = "Hidden test Method", hidden = true)
     public Result test1() {
         try {

             JsonNode json = getBodyAsJson();

             String tag = json.get("tag").asText();

             switch (json.get("mode").asText()) {
                 case "developer": {
                     Pattern pattern = Pattern.compile("^(v)(\\d+\\.)(\\d+\\.)(\\d+)(-(.)*)?$");
                     Matcher matcher = pattern.matcher(tag);
                     if (!matcher.find()) {
                         return badRequest("Invalid version");
                     }
                     break;
                 }
                 case "stage": {
                     Pattern pattern = Pattern.compile("^(v)(\\d+\\.)(\\d+\\.)(\\d+)(-beta(.)*)?$");
                     Matcher matcher = pattern.matcher(tag);
                     if (!matcher.find()) {
                         return badRequest("Invalid version");
                     }
                     break;
                 }
                 case "production": {
                     Pattern pattern = Pattern.compile("^(v)(\\d+\\.)(\\d+\\.)(\\d+)$");
                     Matcher matcher = pattern.matcher(tag);
                     if (!matcher.find()) {
                         return badRequest("Invalid version");
                     }
                     break;
                 }
                 default: // empty
             }

             return ok("Valid version for mode");

         } catch (Exception e) {
             logger.internalServerError(e);
             return badRequest();
         }
     }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test2() {
        try {



           return ok();


        } catch (Exception e) {
            logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test3() {
        try {



            return ok();
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test4() {
        try {

            return ok();

        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError(e);
        }
    }




}