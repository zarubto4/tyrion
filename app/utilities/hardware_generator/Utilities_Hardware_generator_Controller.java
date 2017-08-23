package utilities.hardware_generator;

import io.swagger.annotations.*;
import models.*;
import play.Configuration;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.Enum_Garfield_burning_state;
import utilities.enums.Enum_Cloud_HomerServer_type;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_Admin;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_NotFound;
import utilities.response.response_objects.Result_Forbidden;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_Ok;
import utilities.swagger.documentationClass.Swagger_Hardware_New_Hardware_Request;
import utilities.swagger.documentationClass.Swagger_Hardware_New_Settings_Request;
import utilities.swagger.outboundClass.Swagger_Hardware_New_Settings_Result;

import java.util.Date;


@Api(value = "Not Documented API - InProgress or Stuck")
public class Utilities_Hardware_generator_Controller extends Controller {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Utilities_Hardware_generator_Controller.class);

// - Oblužné metody - primárně pro Wiev Tyriona ------------------------------------------------------------------------

    // Generátor - respektive přirazovač nových MacAdress z Rozsahu nastaveným v konfiguračním serveru
    public static Long get_new_MacAddress(){
        try {

            String mac_address = Model_Board.find.where().orderBy().desc("mac_address").setMaxRows(1).findUnique().mac_address;
            Long highest_mac = Long.parseLong(mac_address.replace(":", ""), 16);

            long mac = highest_mac + 1;

            return mac;

        }catch (NullPointerException e) {
           return 0xAABBCCDD0000L;
        }
    }

    //Konvertor Long na ISO normu Mac addressy
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

    // Get Počátek koupeného rozsahu pro Byzance
    public static String get_Range_From(){
       return Configuration.root().getString("MacAddressForBoards.beginning");
    }

    // Get Konec  koupeného rozsahu pro Byzance
    public static String get_Range_To(){
        return  Configuration.root().getString("MacAddressForBoards.ending");
    }

    // Get Začátek Typu Desky z konfiguračního souboru
    public static String get_macAddress_type_of_board_from(String targetName){
        try {

            String address = Configuration.root().getString("MacAddressForBoards." + targetName + ".mac_address");
            if(address.equals("") || address== null) throw new NullPointerException();
            return address;

        }catch (NullPointerException e){
            terminal_logger.internalServerError(new Exception("TargetName is not set in configuration file!",e));
            return "ERROR! targetName is not set in configuration file!!";
        }
    }

// ------------------------------------------------------------------------------------------------------------------------------------
// API - PRO Garfielda ----------------------------------------------------------------------------------------------------------------
// ------------------------------------------------------------------------------------------------------------------------------------

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
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_Admin.class)
    public Result new_hardware_request(){
        try{

            final Form<Swagger_Hardware_New_Settings_Request> form = Form.form(Swagger_Hardware_New_Settings_Request.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Hardware_New_Settings_Request help = form.get();

            // Ověřím Typ desky!!!
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.where().eq("compiler_target_name", help.compiler_target_name).findUnique();
            if(typeOfBoard == null) return GlobalResult.result_notFound("Type Of Board - compiler_target_name not found");


            Model_MacAddressRegisterRecord record = Model_MacAddressRegisterRecord.find.byId(help.uuid_request_number);
            if(record != null){
                return GlobalResult.result_notFound("You are using same - UUID twice");
            }

            // Vytvořím Záznam o vypalovací proceduře
            record = new Model_MacAddressRegisterRecord();
            record.uuid_request_number = help.uuid_request_number;  // TODO - zanořit do objektu jako UUID objekt a autogenerated
            if(help.mac_address != null && help.mac_address.length() > 0) record.mac_address =  help.mac_address;
            if(help.full_id != null && help.full_id.length() > 0)         record.full_id     =  help.full_id;
            record.type_of_board = typeOfBoard.compiler_target_name;
            record.state = Enum_Garfield_burning_state.in_progress;
            record.save();

            // Ověřím full_id - pokud už existuje a je shodný s MacAdressou - pak zašlu jen novou konfiguraci
            if( Model_Board.get_byId(help.full_id) != null){
                terminal_logger.debug("HardwareGeneratorController:: new_hardware_request:: Full_Id is used - Just new Configuration!");
            }


            if(help.full_id != null){
                if(help.full_id.length() != 24) return GlobalResult.result_badRequest("Full_Id is not correct!");
            }


            // Postavím odpověď Pro Yodu ::
            if(typeOfBoard.connectible_to_internet) {

                // Najdu backup_server
                Model_HomerServer backup_server = Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.backup_server).findUnique();
                if (backup_server == null) return GlobalResult.result_notFound("Backup server not found!!!");

                // Najdu Main_server
                Model_HomerServer main_server = Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.main_server).findUnique();
                if (main_server == null) return GlobalResult.result_notFound("Main server not found!!!");

                // Najdu Firmware
                Model_FileRecord firmware = Model_FileRecord.find.where().eq("c_compilations_binary_file.version_object.default_version_program.default_program_type_of_board.id", typeOfBoard.id).eq("file_name", "compilation.bin").findUnique();
                if (firmware == null) return GlobalResult.result_notFound("firmware not found - Set Main Firmware in Tyrion first!!!");

                // Najdu Bootloader
                Model_FileRecord bootloader = Model_FileRecord.find.where().eq("boot_loader.main_type_of_board.id", typeOfBoard.id).eq("file_name", "bootloader.bin").findUnique();
                if (bootloader == null) return GlobalResult.result_notFound("bootloader not found - Set bootloader in Tyrion first!!!!");

                Swagger_Hardware_New_Settings_Result result = new Swagger_Hardware_New_Settings_Result();
                result.full_id                              = record.full_id;
                result.normal_mqtt_hostname                 = main_server.server_url;
                result.normal_mqtt_port                     = main_server.mqtt_port;
                result.normal_mqtt_username                 = main_server.mqtt_username;
                result.normal_mqtt_password                 = main_server.mqtt_password;

                result.backup_mqtt_hostname                 = backup_server.server_url;
                result.backup_mqtt_port                     = backup_server.mqtt_port;
                result.backup_mqtt_username                 = backup_server.mqtt_username;
                result.backup_mqtt_password                 = backup_server.mqtt_password;

                result.wifi_password            = null;
                result.wifi_ssid                = null;
                result.wifi_username            = null;
                result.devlist_counter          = 0;
                result.bootloader_report        = false;
                result.autobackup               = Configuration.root().getBoolean( "MacAddressForBoards." + typeOfBoard.compiler_target_name + ".autobackup" );
                result.features                 = typeOfBoard.features;
                result.mac_address              = record.mac_address;

                result.type_of_board            = typeOfBoard.compiler_target_name;

                result.firmware_version_id      = firmware.c_compilations_binary_file.version_object.id;
                result.bootloader_id            = bootloader.boot_loader.id.toString();
                // Vložím Programy v base64
                result.firmware_base64          = firmware.get_fileRecord_from_Azure_inString();
                result.bootloader_base64        = bootloader.get_fileRecord_from_Azure_inString();

                return GlobalResult.result_ok(Json.toJson(result));
            }
            else {

                // Najdu Firmware
                Model_FileRecord firmware = Model_FileRecord.find.where().eq("c_compilations_binary_file.version_object.default_version_program.default_program_type_of_board.id", typeOfBoard.id).eq("file_name", "compilation.bin").findUnique();
                if (firmware == null) return GlobalResult.result_notFound("firmware not found - Set Main Firmware in Tyrion first!!!");

                // Najdu Bootloader
                Model_FileRecord bootloader = Model_FileRecord.find.where().eq("boot_loader.main_type_of_board.id", typeOfBoard.id).eq("file_name", "bootloader.bin").findUnique();
                if (bootloader == null) return GlobalResult.result_notFound("bootloader not found - Set bootloader in Tyrion first!!!!");

                Swagger_Hardware_New_Settings_Result result = new Swagger_Hardware_New_Settings_Result();
                result.autobackup               = Configuration.root().getBoolean( "MacAddressForBoards." + typeOfBoard.compiler_target_name + ".autobackup" );
                result.full_id                  = record.full_id;

                // Vložím Programy v base64
                result.firmware_base64          = firmware.get_fileRecord_from_Azure_inString();
                result.bootloader_base64        = bootloader.get_fileRecord_from_Azure_inString();

                return GlobalResult.result_ok(Json.toJson(result));
            }

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Hardware_New_Hardware_Request",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_Admin.class)
    public Result new_hardware_result() {
        try{

            final Form<Swagger_Hardware_New_Hardware_Request> form = Form.form(Swagger_Hardware_New_Hardware_Request.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Hardware_New_Hardware_Request help = form.get();

            // Ověřím Record
            Model_MacAddressRegisterRecord record = Model_MacAddressRegisterRecord.find.byId(help.uuid_request_number);
            if(record == null) return GlobalResult.result_notFound("MacAddressRegisterRecord not exist");

            // OVěřím stav
            Enum_Garfield_burning_state state = Enum_Garfield_burning_state.get_state(help.status);
            if(state == null) return GlobalResult.result_notFound("Status not recognize! Use only [complete, in_progress, broken_device,unknown_error] ");

            // Ověřím program
            Model_VersionObject firmware_version = Model_VersionObject.find.byId(help.firmware_version_id);
            if(firmware_version == null) return GlobalResult.result_notFound("Firmware version not exist");

            // Ověřím bootloader
            Model_BootLoader bootLoader = Model_BootLoader.find.byId(help.bootloader_id);
            if(bootLoader == null) return GlobalResult.result_notFound("bootLoader not exist");





            if(state == Enum_Garfield_burning_state.broken_device){

                record.state = state;
                record.update();

            }

            if(state == Enum_Garfield_burning_state.unknown_error){

                record.state = state;
                record.update();

            }

            if(state == Enum_Garfield_burning_state.complete) {

                record.state = state;
                record.bootloader_id = bootLoader.id.toString();
                record.firmware_version_id = firmware_version.id;
                record.full_id = help.full_id;
                record.mac_address = help.mac_address;
                record.update();

                Model_Board board = Model_Board.find.where().eq("mac_address", help.mac_address).findUnique();

                // Úplně nově vypálená deska
                if (board == null) {

                    // Ověřím Typ desky!!!
                    Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.where().eq("compiler_target_name", help.compiler_target_name).findUnique();
                    if (typeOfBoard == null)
                        return GlobalResult.result_notFound("Type Of Board - compiler_target_name not found");

                    board = new Model_Board();
                    board.id = help.full_id;
                    board.type_of_board = typeOfBoard;
                    board.actual_boot_loader = bootLoader;
                    board.actual_c_program_version = firmware_version;
                    board.backup_mode = false;
                    board.date_of_create = new Date();
                    board.mac_address = convert_to_MAC_ISO(get_new_MacAddress());
                    board.is_active = false;
                    board.save();

                    // Oprava vypálení
                } else {
                    board.actual_boot_loader = bootLoader;
                    board.actual_c_program_version = firmware_version;
                    board.update();
                }


                if (help.status.equals("success")) {
                    board.is_active = true;
                    board.update();
                }

            }

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }
}
