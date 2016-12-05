package utilities.hardware_generator;

import io.swagger.annotations.*;
import models.compiler.Board;
import models.compiler.FileRecord;
import models.compiler.TypeOfBoard;
import models.project.b_program.servers.Cloud_Homer_Server;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.CLoud_Homer_Server_Type;
import utilities.enums.NetSource;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_Admin;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_NotFound;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_ok;
import utilities.swagger.documentationClass.Swagger_Hardware_New_Hardware_Result;
import utilities.swagger.documentationClass.Swagger_Hardware_New_Settings_Request;
import utilities.swagger.outboundClass.Swagger_Hardware_New_Settings_Result;

import java.util.Date;


@Api(value = "Not Documented API - InProgress or Stuck")
public class HardwareGeneratorController extends Controller {


    // Generátor - respektive přirazovač nových MacAdress z Rozsahu nastaveným v konfiguračním serveru
    public static Long get_new_MacAddress(){
        try {

            String mac_address = Board.find.where().orderBy().desc("mac_address").setMaxRows(1).findUnique().mac_address;
            Long highest_mac = Long.parseLong(mac_address.replace(":", ""), 16);

            long mac = highest_mac + 1;

            return mac;

        }catch (NullPointerException e) {
           return 0xAABBCCDD0000L;
        }
    }

    public static String convert_to_MAC_ISO(Long mac){

        if (mac > 0xFFFFFFFFFFFFL || mac < 0) {
            throw new IllegalArgumentException("mac out of range");
        }

        StringBuffer m = new StringBuffer(Long.toString(mac, 16));
        while (m.length() < 12) m.insert(0, "0");

        for (int j = m.length() - 2; j >= 2; j-=2) {
            m.insert(j, ":");
        }

        return m.toString().toUpperCase();
    }


    @ApiOperation(value = "Request for Details for new Board",
            hidden = false,
            tags = {"Board_Registration"},
            notes = "Required data for new Embedded Hardware for first hardware settings. Required Permission key from Tyrion Backend Web Page!",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Hardware_New_Settings_Request",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Hardware_New_Settings_Result.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_Admin.class)
    public Result new_hardware_request(){
        try{

            final Form<Swagger_Hardware_New_Settings_Request> form = Form.form(Swagger_Hardware_New_Settings_Request.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Hardware_New_Settings_Request help = form.get();


            // Ověřím full_id
            if( Board.find.where().eq("id", help.full_id).findUnique() != null){
                return GlobalResult.result_BadRequest("Full_Id is used!");
            }

            if(help.full_id.length() != 24){
                return GlobalResult.result_BadRequest("Full_Id is not correct!");
            }


            // Ověřím Typ desky!!!
            TypeOfBoard typeOfBoard = TypeOfBoard.find.where().eq("compiler_target_name", help.compiler_target_name).findUnique();
            if(typeOfBoard == null) return GlobalResult.notFoundObject("Type Of Board - compiler_target_name not found");


            // Postavím odpověď Pro Yodu ::
            if(typeOfBoard.connectible_to_internet) {

                // Najdu backup_server
                Cloud_Homer_Server backup_server = Cloud_Homer_Server.find.where().eq("server_type", CLoud_Homer_Server_Type.backup_server).findUnique();
                if (backup_server == null) return GlobalResult.notFoundObject("Backup server not found!!!");

                // Najdu Main_server
                Cloud_Homer_Server main_server = Cloud_Homer_Server.find.where().eq("server_type", CLoud_Homer_Server_Type.main_server).findUnique();
                if (main_server == null) return GlobalResult.notFoundObject("Main server not found!!!");

                // Najdu Firmware
                FileRecord firmware = FileRecord.find.where().eq("c_compilations_binary_file.version_object.default_version_program.default_program_type_of_board.id", typeOfBoard.id).eq("file_name", "compilation.bin").findUnique();
                if (firmware == null) return GlobalResult.notFoundObject("firmware not found - Set Main Firmware in Tyrion first!!!");

                // Najdu Bootloader
                FileRecord bootloader = FileRecord.find.where().eq("boot_loader.main_type_of_board.id", typeOfBoard.id).eq("file_name", "bootloader.bin").findUnique();
                if (bootloader == null) return GlobalResult.notFoundObject("bootloader not found - Set bootloader in Tyrion first!!!!");


                // Vytvořím Hardware!
                Board board = new Board();
                board.id = help.full_id;
                board.type_of_board = typeOfBoard;
                board.actual_boot_loader = typeOfBoard.main_boot_loader;
                board.actual_c_program_version = typeOfBoard.default_program.default_main_version;
                board.backup_mode = false;
                board.date_of_create = new Date();
                board.mac_address = convert_to_MAC_ISO( get_new_MacAddress() );
                board.is_active = false;
                board.save();

                Swagger_Hardware_New_Settings_Result result = new Swagger_Hardware_New_Settings_Result();
                result.full_id              =   help.full_id;
                result.normal_mqtt_hostname = main_server.destination_address;
                result.normal_mqtt_port = main_server.mqtt_port;
                result.normal_mqtt_username = main_server.mqtt_username;
                result.normal_mqtt_password = main_server.mqtt_password;

                result.backup_mqtt_hostname = backup_server.destination_address;
                result.backup_mqtt_port = backup_server.mqtt_port;
                result.backup_mqtt_username = backup_server.mqtt_username;
                result.backup_mqtt_password = backup_server.mqtt_password;


                result.wifi_password = null;
                result.wifi_ssid = null;
                result.wifi_username = null;
                result.devlist_counter = 0;
                result.bootloader_report = false;
                result.autobackup =  board.backup_mode;

                result.netsource = NetSource.ethernet;
                result.mac_address = board.mac_address;

                // Vložím Programy v base64
                result.firmware_base64 = firmware.get_fileRecord_from_Azure_inString();
                result.bootloader_base64 = bootloader.get_fileRecord_from_Azure_inString();

                return GlobalResult.result_ok(Json.toJson(result));
            }

            return GlobalResult.result_BadRequest("Type Of board not supported!");

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }




    @ApiOperation(value = "Result for Details for new Board",
            hidden = false,
            tags = {"Board_Registration"},
            notes = "Result data for new Embedded Hardware for first hardware settings. Required Permission key from Tyrion Backend Web Page!",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Hardware_New_Hardware_Result",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_Admin.class)
    public Result new_hardware_result() {
        try{

            final Form<Swagger_Hardware_New_Hardware_Result> form = Form.form(Swagger_Hardware_New_Hardware_Result.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Hardware_New_Hardware_Result help = form.get();


            Board board = Board.find.where().eq("mac_address",help.mac_address).findUnique();
            if(board == null) return GlobalResult.notFoundObject("Board with mac_address not found");


            if(help.status.equals("success")) {

                board.is_active = true;
                board.update();
            }

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }
}
