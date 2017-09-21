package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.*;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.demo_data.Utilities_Demo_data_Controller;
import utilities.enums.Enum_Publishing_type;
import utilities.lablel_printer_service.Printer_Api;
import utilities.lablel_printer_service.labels.Label_12_mm;
import utilities.lablel_printer_service.printNodeModels.PrinterOption;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.response.GlobalResult;
import utilities.scheduler.jobs.Job_SpendingCredit;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Online_Change_status;

import java.util.UUID;

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
            board.hash_for_adding = UUID.randomUUID().toString();

            PrinterOption option = new PrinterOption();
            option.media = "label";
            option.dpi = "100800";

            // Test of printer
            Label_12_mm label_12_mm = new Label_12_mm(board);

           // api.printFile(279215, 1, "Garfield Print QR Hash", label_12_mm.get_label());
              api.printFile(279214, 1, "Garfield Print QR Hash", label_12_mm.get_label(), option);
           // api.printFile(279213, 1, "Garfield Print QR Hash", label_12_mm.get_label());


            return GlobalResult.result_ok();
        }catch (Exception e){
            e.printStackTrace();
            return Server_Logger.result_internalServerError(e, request());
        }
    }

}