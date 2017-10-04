package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.*;
import org.bson.Document;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.enums.Enum_Publishing_type;
import utilities.hardware_registration_auhtority.document_objects.DM_Board_Registration_Central_Authority;
import utilities.lablel_printer_service.Printer_Api;
import utilities.lablel_printer_service.labels.Label_62_mm_package;
import utilities.lablel_printer_service.labels.Label_62_split_mm_Details;
import utilities.lablel_printer_service.labels.Label_12_mm_QR_code;
import utilities.lablel_printer_service.printNodeModels.PrinterOption;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.response.GlobalResult;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Online_Change_status;

import java.util.Arrays;
import java.util.UUID;

import static com.mongodb.client.model.Sorts.descending;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_ZZZ_Tester extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_ZZZ_Tester.class);

    @ApiOperation(value = "Hidden test Method", hidden = true)
     public Result test1(){
         try {

             // String id = request().body().asJson().get("id").asText();
             // if (id == null) return badRequest("id is null");

            // Model_Product product = Model_Product.get_byId(id);
             // Job_SpendingCredit.spend(product);

             Model_GridWidget gridWidget = new Model_GridWidget();
             gridWidget.id                  = UUID.fromString("00000000-0000-0000-0000-000000000001");
             gridWidget.description         = "Default Widget";
             gridWidget.name                = "Default Widget";
             gridWidget.type_of_widget      = null;
             gridWidget.publish_type        = Enum_Publishing_type.default_main_program;
             gridWidget.save();

             return ok("Credit was spent");

         }catch (Exception e){
             terminal_logger.internalServerError(e);
             return badRequest();
         }
     }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test2(){
        try {

            // terminal_logger.error(BCrypt.hashpw("password", BCrypt.gensalt(12)));
            // Test online change stavů
            JsonNode json = request().body().asJson();

            String type = json.get("type").asText();
            String id = json.get("id").asText();
            String project_id = json.get("project_id").asText();
            boolean online_state = json.get("online_state").asBoolean();

            if(type.equals("board")) {
                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Board.class, id, online_state, project_id);
            }

            if(type.equals("instance")) {
                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_HomerInstance.class, id, online_state, project_id);
            }

            if(type.equals("server")) {
                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_HomerServer.class, id, online_state, project_id);
            }

            return GlobalResult.result_ok(json);

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test3(){
        try {

            System.out.println("Testuji Apu Printer");

            Printer_Api api = new Printer_Api();


            Model_Board board = new Model_Board();
            board.id = "123456789123456789121234";
            board.hash_for_adding = "HW" + UUID.randomUUID().toString().replaceAll("[-]","").substring(0, 24);
            board.mac_address = "AA:QF:NN:MM:WW";

            Model_TypeOfBoard_Batch batch = Model_TypeOfBoard_Batch.find.findList().get(0);
            Model_Garfield garfield = Model_Garfield.find.findList().get(0);

            // Test of printer
            //Label_62_mm_package label_12_package  = new Label_62_mm_package(board, batch_id, garfield);
            Label_62_split_mm_Details label_12_mm_details = new Label_62_split_mm_Details(board);

            //api.printFile(279211, 1, "Garfield Print QR Hash", label_12_mm_qr_code.get_label(), null);
            //api.printFile(279211, 1, "Garfield Print QR Hash", label_12_package.get_label(), null);
            api.printFile(279211, 1, "Garfield Print QR Hash", label_12_mm_details.get_label(), null);


            return GlobalResult.result_ok();
        }catch (Exception e){
            e.printStackTrace();
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test4(){
        try {

            MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://production-byzance-cosmos:PbimpRkWXhUrGBwRtLaR19B6NbffCgzklSfSVtHThFzMn6keUENJ9Hm50TZZgtqVOGesgbtCWLaC3yd6ENhoew==@production-byzance-cosmos.documents.azure.com:10255/?ssl=true&replicaSet=globaldb"));
            MongoDatabase database = mongoClient.getDatabase("hardware-registration-authority-database");
            MongoCollection<Document> collection = database.getCollection(DM_Board_Registration_Central_Authority.COLLECTION_NAME);

            Model_TypeOfBoard_Batch batch = Model_TypeOfBoard_Batch.find.where().eq("removed_by_user", false).findUnique();

            if(batch == null){
                System.out.println("Batch nenalezen!!");
                return GlobalResult.badRequest();
            }

            BasicDBObject whereQuery_mac = new BasicDBObject();
            whereQuery_mac.put("revision", batch.revision);
            whereQuery_mac.put("production_batch", batch.production_batch);

            Document mac_address_already_registered = collection.find(whereQuery_mac).sort(descending("mac_address")).first();

            System.out.println("Addresa kterou to vyflusnulo:: " + mac_address_already_registered.get("mac_address"));

            Long latest_from_database = Long.parseLong(mac_address_already_registered.get("mac_address").toString().replace(":",""),16);

            System.out.println("Adresa kterou to vyflusnulo v Longu:: " + latest_from_database);

            /*
                if(mac_address_already_registered != null){
                    String latest_used_mac_address = (String) mac_address_already_registered.get("mac_address");

                    Long latest_from_database = Long.parseLong(latest_used_mac_address.replace(":",""),16);

                    if(batch_id.latest_used_mac_address != latest_from_database) {
                        batch_id.latest_used_mac_address = latest_from_database;
                        batch_id.refresh();
                    }
                }
            */

            return GlobalResult.result_ok();

        }catch (Exception e){
            e.printStackTrace();
            return Server_Logger.result_internalServerError(e, request());
        }
    }




}