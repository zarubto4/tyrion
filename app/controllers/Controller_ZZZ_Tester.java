package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.*;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import play.libs.Json;
import play.mvc.Result;
import utilities.hardware_registration_auhtority.DM_Board_Registration_Central_Authority;
import utilities.hardware_registration_auhtority.Enum_Hardware_Registration_DB_Key;
import utilities.homer_auto_update.DigitalOceanTyrionService;
import utilities.logger.Logger;
import utilities.scheduler.jobs.Job_CheckCompilationLibraries;
import websocket.messages.tyrion_with_becki.WS_Message_Online_Change_status;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
            System.out.println("Jdu do toho");

             MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://production-byzance-cosmos:PbimpRkWXhUrGBwRtLaR19B6NbffCgzklSfSVtHThFzMn6keUENJ9Hm50TZZgtqVOGesgbtCWLaC3yd6ENhoew==@production-byzance-cosmos.documents.azure.com:10255/?ssl=true&replicaSet=globaldb"));
             MongoDatabase database = mongoClient.getDatabase("hardware-registration-authority-database");
             MongoCollection<Document> collection = database.getCollection(DM_Board_Registration_Central_Authority.COLLECTION_NAME);

            // BasicDBObject query = new BasicDBObject();
            // query.put("type_of_board_compiler_target_name", "BYZANCE_IODAG3E");

            Document document_nothing = new Document();

            Document document = new Document();
            document.put("bodyAsJson", "1");
            document.put("board_id", "1");
            document.put("type_of_board_revision_name", "1");
            document.put("pcb_manufacture_name", "1");
            document.put("pcb_manufacture_id", "1");
            document.put("assembly_manufacture_name", "1");
            document.put("assembly_manufacture_id", "1");
            document.put("type_of_board_compiler_target_name", "1");
            document.put("revision", "1");
            document.put("production_batch", "1");


            collection.updateMany(document_nothing, new Document("$unset", document));

            //MongoCursor<Document> cursor = collection.find(query).iterator();

            System.out.println("Iterator done");
            /**
            while (cursor.hasNext()) {

                System.out.println("nexte");
                Document document = cursor.next();


                String string_json = document.toJson();

                ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);
                System.out.println("Hardware: Json \n" + string_json +" \n");

                if(!json.has("board_id")) {
                    throw new NullPointerException("Chybí board_id");
                }
                if(!json.has("mac_address")) {
                    throw new NullPointerException("Chybí mac_address");
                }
                if(!json.has("hash_for_adding")) {
                    throw new NullPointerException("Chybí hash_for_adding");
                }
                if(!json.has("type_of_board_compiler_target_name")) {
                    throw new NullPointerException("Chybí type_of_board_compiler_target_name");
                }


                DM_Board_Registration_Central_Authority hw = new DM_Board_Registration_Central_Authority();
                hw.full_id = json.get("board_id").asText();
                hw.mac_address = json.get("mac_address").asText();
                hw.hash_for_adding = json.get("hash_for_adding").asText();

                if(json.has("personal_name")) {
                    hw.personal_name = json.get("personal_name").asText();
                }else {
                    hw.personal_name = json.get("board_id").asText();
                }

                hw.created = json.get("date_of_create").asText();
                hw.production_batch_id = "abd218dc-14ca-4d2e-a731-66f71ed41245";

                if(json.has("mqtt_password")) {
                    hw.mqtt_password = json.get("mqtt_password").asText();
                    hw.mqtt_username = json.get("mqtt_username").asText();
                }else {
                    hw.mqtt_password = "pass";
                    hw.mqtt_username = "user";
                }
                hw.hardware_type_compiler_target_name = "BYZANCE_IODAG3E";
                hw.state = "CAN_REGISTER";

                System.out.println("Updatuji full_id:: " + hw.full_id);



                Document newdocument = Document.parse(Json.toJson(hw).toString());
                collection.updateOne( eq("board_id", hw.full_id), new Document("$set", newdocument));
            }
             */


           return ok();


        } catch (Exception e) {
            logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test3() {
        try {

            DigitalOceanTyrionService oceanTyrionService = new DigitalOceanTyrionService();

            Model_HomerServer server = new Model_HomerServer();
            server.id = UUID.randomUUID();
            DigitalOceanTyrionService.create_server(server);


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