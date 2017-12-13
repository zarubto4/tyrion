package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.*;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.hardware_registration_auhtority.Hardware_Registration_Authority;
import utilities.logger.Class_Logger;
import utilities.logger.ServerLogger;
import utilities.response.GlobalResult;
import utilities.slack.Slack;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Online_Change_status;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Sorts.descending;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_ZZZ_Tester extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_ZZZ_Tester.class);

    @ApiOperation(value = "Hidden test Method", hidden = true)
     public Result test1(){
         try {

             JsonNode json = request().body().asJson();

             String tag = json.get("tag").asText();

             switch (json.get("mode").asText()) {
                 case "developer": {
                     Pattern pattern = Pattern.compile("^(v)(\\d+\\.)(\\d+\\.)(\\d+)(-(.)*)?$");
                     Matcher matcher = pattern.matcher(tag);
                     if (!matcher.find()) {
                         return GlobalResult.result_badRequest("Invalid version");
                     }
                     break;
                 }
                 case "stage": {
                     Pattern pattern = Pattern.compile("^(v)(\\d+\\.)(\\d+\\.)(\\d+)(-beta(.)*)?$");
                     Matcher matcher = pattern.matcher(tag);
                     if (!matcher.find()) {
                         return GlobalResult.result_badRequest("Invalid version");
                     }
                     break;
                 }
                 case "production": {
                     Pattern pattern = Pattern.compile("^(v)(\\d+\\.)(\\d+\\.)(\\d+)$");
                     Matcher matcher = pattern.matcher(tag);
                     if (!matcher.find()) {
                         return GlobalResult.result_badRequest("Invalid version");
                     }
                     break;
                 }
                 default: // empty
             }

             return ok("Valid version for mode");

         } catch (Exception e) {
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

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test3(){
        try {

            new Slack().post_invalid_release("v1.1.4-alpha");

            /*
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

            */

            return GlobalResult.result_ok();
        }catch (Exception e){
            e.printStackTrace();
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test4(){
        try {


            Model_TypeOfBoard_Batch batch = Model_TypeOfBoard_Batch.find.where().eq("revision", "Test Private Collection").findUnique();
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.where().eq("name","IODA G3" ).findUnique();

            /*
            Model_Board board1 = new Model_Board();
            board1.id = "004B00313435510E30353932";
            board1.name = "[G31]";
            board1.is_active = false;
            board1.date_of_create = new Date();
            board1.type_of_board = typeOfBoard;
            board1.batch_id = batch.id.toString();
            board1.mac_address = Model_TypeOfBoard_Batch.convert_to_MAC_ISO(210006720901120L);
            board1.hash_for_adding = Model_Board.generate_hash();
            Hardware_Registration_Authority.register_device(board1, typeOfBoard, batch);



            Model_Board board2 = new Model_Board();
            board2.id = "002F00323435510E30353932";
            board2.name = "[G32]";
            board2.is_active = false;
            board2.date_of_create = new Date();
            board2.type_of_board = typeOfBoard;
            board2.batch_id = batch.id.toString();
            board2.mac_address = Model_TypeOfBoard_Batch.convert_to_MAC_ISO(210006720901121L);
            board2.hash_for_adding = Model_Board.generate_hash();
            Hardware_Registration_Authority.register_device(board2, typeOfBoard, batch);


            Model_Board board3 = new Model_Board();
            board3.id = "001E00323435510E30353932";
            board3.name = "[G33]";
            board3.is_active = false;
            board3.date_of_create = new Date();
            board3.type_of_board = typeOfBoard;
            board3.batch_id = batch.id.toString();
            board3.mac_address = Model_TypeOfBoard_Batch.convert_to_MAC_ISO(210006720901122L);
            board3.hash_for_adding = Model_Board.generate_hash();

            Hardware_Registration_Authority.register_device(board3, typeOfBoard, batch);


            Model_Board board4 = new Model_Board();
            board4.id = "004100323435510E30353932";
            board4.name = "[G34]";
            board4.is_active = false;
            board4.date_of_create = new Date();
            board4.type_of_board = typeOfBoard;
            board4.batch_id = batch.id.toString();
            board4.mac_address = Model_TypeOfBoard_Batch.convert_to_MAC_ISO(210006720901123L);
            board4.hash_for_adding = Model_Board.generate_hash();
            Hardware_Registration_Authority.register_device(board4, typeOfBoard, batch);

            Model_Board board5 = new Model_Board();
            board5.id = "002400323435510E30353932";
            board5.name = "[G35]";
            board5.is_active = false;
            board5.date_of_create = new Date();
            board5.type_of_board = typeOfBoard;
            board5.batch_id = batch.id.toString();
            board5.mac_address = Model_TypeOfBoard_Batch.convert_to_MAC_ISO(210006720901124L);
            board5.hash_for_adding = Model_Board.generate_hash();
            Hardware_Registration_Authority.register_device(board5, typeOfBoard, batch);

            */
            //--------------------------------------------------------------------------------------------------------------
            Model_TypeOfBoard_Batch batch_1 = Model_TypeOfBoard_Batch.find.where().eq("production_batch", "1000001 - Test Collection").findUnique();

             /*
            Model_Board board7 = new Model_Board();
            board7.id = "002E00273136510236363332";
            board7.name = "[G41]";
            board7.is_active = false;
            board7.date_of_create = new Date();
            board7.type_of_board = typeOfBoard;
            board7.batch_id = batch.id.toString();
            board7.mac_address = Model_TypeOfBoard_Batch.convert_to_MAC_ISO(210006720901136L);
            board7.hash_for_adding = Model_Board.generate_hash();
            Hardware_Registration_Authority.register_device(board7, typeOfBoard, batch_1);

            Model_Board board8 = new Model_Board();
            board8.id = "002E00273136510236363332";
            board8.name = "[G42]";
            board8.is_active = false;
            board8.date_of_create = new Date();
            board8.type_of_board = typeOfBoard;
            board8.batch_id = batch.id.toString();
            board8.mac_address = Model_TypeOfBoard_Batch.convert_to_MAC_ISO(210006720901137L);
            board8.hash_for_adding = Model_Board.generate_hash();
            Hardware_Registration_Authority.register_device(board8, typeOfBoard, batch_1);

            Model_Board board9 = new Model_Board();
            board9.id = "003600453136510236363332";
            board9.name = "[G43]";
            board9.is_active = false;
            board9.date_of_create = new Date();
            board9.type_of_board = typeOfBoard;
            board9.batch_id = batch.id.toString();
            board9.mac_address = Model_TypeOfBoard_Batch.convert_to_MAC_ISO(210006720901141L);
            board9.hash_for_adding = Model_Board.generate_hash();
            Hardware_Registration_Authority.register_device(board9, typeOfBoard, batch_1);

            Model_Board board9_2 = new Model_Board();
            board9_2.id = "004200363136510236363332";
            board9_2.name = "[G44]";
            board9_2.is_active = false;
            board9_2.date_of_create = new Date();
            board9_2.type_of_board = typeOfBoard;
            board9_2.batch_id = batch.id.toString();
            board9_2.mac_address = Model_TypeOfBoard_Batch.convert_to_MAC_ISO(210006720901138L);
            board9_2.hash_for_adding = Model_Board.generate_hash();
            Hardware_Registration_Authority.register_device(board9_2, typeOfBoard, batch_1);

            Model_Board board10 = new Model_Board();
            board10.id = "005000263136510236363332";
            board10.name = "[G45]";
            board10.is_active = false;
            board10.date_of_create = new Date();
            board10.type_of_board = typeOfBoard;
            board10.batch_id = batch.id.toString();
            board10.mac_address = Model_TypeOfBoard_Batch.convert_to_MAC_ISO(210006720901139L);
            board10.hash_for_adding = Model_Board.generate_hash();
            Hardware_Registration_Authority.register_device(board10, typeOfBoard, batch_1);


            Model_Board board11 = new Model_Board();
            board11.id = "002800363136510236363332";
            board11.name = "[G46]";
            board11.is_active = false;
            board11.date_of_create = new Date();
            board11.type_of_board = typeOfBoard;
            board11.batch_id = batch.id.toString();
            board11.mac_address = Model_TypeOfBoard_Batch.convert_to_MAC_ISO(210006720901141L);
            board11.hash_for_adding = Model_Board.generate_hash();
            Hardware_Registration_Authority.register_device(board11, typeOfBoard, batch_1);


            Model_Board board12 = new Model_Board();
            board12.id = "004000273136510236363332";
            board12.name = "[G47]";
            board12.is_active = false;
            board12.date_of_create = new Date();
            board12.type_of_board = typeOfBoard;
            board12.batch_id = batch.id.toString();
            board12.mac_address = Model_TypeOfBoard_Batch.convert_to_MAC_ISO(210006720901142L);
            board12.hash_for_adding = Model_Board.generate_hash();
            Hardware_Registration_Authority.register_device(board12, typeOfBoard, batch_1);

            */

            Model_Board board_g49 = new Model_Board();
            board_g49.id = "003A00463136510236363332";
            board_g49.name = "[G49]";
            board_g49.is_active = false;
            board_g49.date_of_create = new Date();
            board_g49.type_of_board = typeOfBoard;
            board_g49.batch_id = batch.id.toString();
            board_g49.mac_address = Model_TypeOfBoard_Batch.convert_to_MAC_ISO(210006720901143L);
            board_g49.hash_for_adding = Model_Board.generate_hash();
            Hardware_Registration_Authority.register_device(board_g49, typeOfBoard, batch_1);

            Model_Board board_g50 = new Model_Board();
            board_g50.id = "003D00443136510236363332";
            board_g50.name = "[G50]";
            board_g50.is_active = false;
            board_g50.date_of_create = new Date();
            board_g50.type_of_board = typeOfBoard;
            board_g50.batch_id = batch.id.toString();
            board_g50.mac_address = Model_TypeOfBoard_Batch.convert_to_MAC_ISO(210006720901144L);
            board_g50.hash_for_adding = Model_Board.generate_hash();
            Hardware_Registration_Authority.register_device(board_g50, typeOfBoard, batch_1);

            return GlobalResult.result_ok();

        }catch (Exception e){
            e.printStackTrace();
            return ServerLogger.result_internalServerError(e, request());
        }
    }




}