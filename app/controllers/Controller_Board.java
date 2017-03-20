package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;
import utilities.enums.Enum_Compile_status;
import utilities.enums.Enum_Update_type_of_update;
import utilities.enums.Enum_Board_registration_status;
import utilities.loggy.Loggy;
import utilities.login_entities.Secured_API;
import utilities.login_entities.Secured_Admin;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Filter_List.Swagger_Board_List;
import utilities.swagger.outboundClass.*;
import web_socket.message_objects.compilatorServer_with_tyrion.WS_Message_Make_compilation;
import web_socket.message_objects.homer_instance.WS_Message_Board_set_autobackup;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Api(value = "Not Documented API - InProgress or Stuck")  // Záměrně takto zapsané - Aby ve swaggru nezdokumentované API byly v jedné sekci
@Security.Authenticated(Secured_API.class)
public class Controller_Board extends Controller {

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");



///###################################################################################################################*/

    @ApiOperation(value = "compile C_program Version",
            tags = {"C_Program"},
            notes = "Compile specific version of C_program - before compilation - you have to update (save) version code",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_program.Version.read_permission", value = Model_VersionObject.read_permission_docs),
                    }),
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_program_read"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Compilation successful",    response = Swagger_Compilation_Ok.class),
            @ApiResponse(code = 477, message = "External server is offline",response = Result_BadRequest.class),
            @ApiResponse(code = 422, message = "Compilation unsuccessful",  response = Swagger_Compilation_Build_Error.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 478, message = "External server side Error",response = Result_BadRequest.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result compile_C_Program_version( @ApiParam(value = "version_id String query", required = true) String version_id ){
        try{

            logger.debug("CompilationController:: Starting compilation on version_id = " + version_id);

            // Ověření objektu
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if(version_object == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Smažu předchozí kompilaci
            if(version_object.c_program == null) return GlobalResult.result_BadRequest("Version is not version of C_Program");

            // Kontrola oprávnění
            if(!version_object.c_program.read_permission()) return GlobalResult.forbidden_Permission();

            // Smažu předchozí kompilaci
            if(version_object.c_compilation != null) return GlobalResult.result_ok(Json.toJson( new Swagger_Compilation_Ok()));


            JsonNode result = version_object.compile_program_procedure();

            if(result.has("status") && result.get("status").asText().equals("success")) return  GlobalResult.result_ok(result);

            if(result.has("error_code") && result.get("error_code").asInt() == 400) return GlobalResult.badRequest(result);
            if(result.has("error_code") && result.get("error_code").asInt() == 477) return GlobalResult.external_server_is_offline();

            // Neznámá chyba se kterou nebylo počítání
           return GlobalResult.result_BadRequest("unknown_error");

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "compile C_program with Code",
            tags = {"C_Program"},
            notes = "Compile code",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_C_Program_Version_Update",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Compilation successful",    response = Swagger_Cloud_Compilation_Server_CompilationResult.class),
            @ApiResponse(code = 477, message = "External server is offline",response = Result_BadRequest.class),
            @ApiResponse(code = 422, message = "Compilation unsuccessful",  response = Swagger_Compilation_Build_Error.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 478, message = "External server side Error",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result compile_C_Program_code() {
        try {

            // Zpracování Json
            Form<Swagger_C_Program_Version_Update> form = Form.form(Swagger_C_Program_Version_Update.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.formExcepting(form.errorsAsJson());
            Swagger_C_Program_Version_Update help = form.get();

            // Ověření objektu
            if (help.type_of_board_id.isEmpty()) return GlobalResult.result_BadRequest("type_of_board_id is missing!");

            // Ověření objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.byId(help.type_of_board_id);
            if (typeOfBoard == null) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            if(!Model_CompilationServer.is_online()) return GlobalResult.result_external_server_is_offline("Compilation Server offilne");


            List<Swagger_C_Program_Version_New.Library_File> library_files = new ArrayList<>();

            for (String lib_id : help.library_files) {

                Model_VersionObject lib_version = Model_VersionObject.find.byId(lib_id);
                if (lib_version == null || lib_version.library == null){

                    ObjectNode error = Json.newObject();
                    error.put("status", "error");
                    error.put("error", "Error getting libraries");
                    error.put("error_code", 400);
                    return GlobalResult.result_BadRequest(error);
                }

                if (!lib_version.files.isEmpty()){
                    for (Model_FileRecord f : lib_version.files) {

                        JsonNode j = Json.parse(f.get_fileRecord_from_Azure_inString());

                        Form<Swagger_C_Program_Version_New.Library_File> lib_form = Form.form(Swagger_C_Program_Version_New.Library_File.class).bind(j);
                        if (lib_form.hasErrors()){

                            ObjectNode error = Json.newObject();
                            error.put("status", "error");
                            error.put("error", "Error importing libraries");
                            error.put("error_code", 400);
                            return GlobalResult.result_BadRequest(error);
                        }

                        Swagger_C_Program_Version_New.Library_File lib_file = lib_form.get();

                        for (Swagger_C_Program_Version_Update.User_File user_file : help.user_files){

                            if (lib_file.file_name.equals(user_file.file_name))break;
                            if (!library_files.contains(lib_file)) library_files.add(lib_file);

                        }
                    }
                }
            }

            ObjectNode includes = Json.newObject();

            for(Swagger_C_Program_Version_New.Library_File file_lib : library_files){
                includes.put(file_lib.file_name , file_lib.content);
            }

            if(help.user_files != null)
                for(Swagger_C_Program_Version_Update.User_File user_file : help.user_files){
                    includes.put(user_file.file_name , user_file.code);
                }


            if (Controller_WebSocket.compiler_cloud_servers.isEmpty()) {
                return GlobalResult.result_external_server_is_offline("Compilation cloud_compilation_server is offline!");
            }


            // Odesílám na compilační cloud_compilation_server
            WS_Message_Make_compilation compilation_result = Model_CompilationServer.make_Compilation(new WS_Message_Make_compilation().make_request( typeOfBoard ,"", help.main, includes ));


            // V případě úspěšného buildu obsahuje příchozí JsonNode buildUrl
            if (compilation_result.buildUrl != null && compilation_result.status.equals("success")) {

                Swagger_Cloud_Compilation_Server_CompilationResult result = new Swagger_Cloud_Compilation_Server_CompilationResult();
                result.interface_code = compilation_result.interface_code;

                return GlobalResult.result_ok(Json.toJson(result));
            }

            // Kompilace nebyla úspěšná a tak vracím obsah neuspěšné kompilace
            if (!compilation_result.buildErrors.isEmpty()) {

                return GlobalResult.result_buildErrors(Json.toJson(compilation_result.buildErrors));
            }

            // Nebylo úspěšné ani odeslání requestu - Chyba v konfiguraci a tak vracím defaulní chybz
            if (compilation_result.error != null) {

                ObjectNode result_json = Json.newObject();
                result_json.put("error", compilation_result.error);

                return GlobalResult.result_external_server_error(result_json);
            }

            // Neznámá chyba se kterou nebylo počítání
            return GlobalResult.result_BadRequest("Unknown error");
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

    }

    /**
    @ApiOperation(value = "update Embedded Hardware with  binary file",
            tags = {"C_Program", "Actualization"},
            notes = "Upload Binary file and choose hardware_id for update. Result (HTML code) will be every time 200. - Its because upload, restart, etc.. operation need more than ++30 second " +
                    "There is also problem / chance that Tyrion didn't find where Embedded hardware is. So you have to listening Server Sent Events (SSE) and show \"future\" message to the user!",
            produces = "application/json",
            protocols = "https",
            consumes = "application/octet-stream",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Board.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_update"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Result_ok.class),
            @ApiResponse(code = 477, message = "External Cloud_Homer_server where is hardware is offline", response = Result_serverIsOffline.class),
            @ApiResponse(code = 404, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result uploadBinaryFileToBoard(@ApiParam(value = "version_id ", required = true) String board_id, @ApiParam(value = "version_id ", required = true) String firmware_type_string) {
        try {

            System.out.println("Body " + request().body().asText());

            // Vyhledání objektů
            Board board = Board.find.byId(board_id);
            if (board == null) return GlobalResult.notFoundObject("Board board_id object not found");

            if (!board.update_permission()) return GlobalResult.forbidden_Permission();

            Firmware_type firmware_type = Firmware_type.getFirmwareType(firmware_type_string);
            if (firmware_type == null) return GlobalResult.notFoundObject("FirmwareType not found!");

            // Přijmu soubor
            Http.MultipartFormData body = request().body().asMultipartFormData();

            List<Http.MultipartFormData.FilePart> files_from_request = body.getFiles();

            if (files_from_request == null || files_from_request.isEmpty())return GlobalResult.notFoundObject("Bin File not found!");
            if (files_from_request.size() > 1)return GlobalResult.result_BadRequest("More than one File is not allowed!");

            File file = files_from_request.get(0).getFile();
            if (file == null) return GlobalResult.result_BadRequest("File not found!");
            if (file.length() < 1) return GlobalResult.result_BadRequest("File is Empty!");


            int dot = files_from_request.get(0).getFilename().lastIndexOf(".");
            String file_type = files_from_request.get(0).getFilename().substring(dot);
            String file_name = files_from_request.get(0).getFilename().substring(0, dot);

            // Zkontroluji soubor
            if (!file_type.equals(".bin"))return GlobalResult.result_BadRequest("Wrong type of File - \"Bin\" required! ");
            if ((file.length() / 1024) > 500)return GlobalResult.result_BadRequest("File is bigger than 500K b");

            // Existuje Homer?

             String binary_file = FileRecord.get_encoded_binary_string_from_File(file);
             FileRecord fileRecord = FileRecord.create_Binary_file("byzance-private/binaryfiles", binary_file, file_name);
             Controller_Actualization.add_new_actualization_request_with_user_file(board.project, firmware_type, board, fileRecord);

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }
    */

    /**
    @ApiOperation(value = "only for Tyrion Front End", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result uploadBinaryFileToBoard_fake_board(String instance_id, String board_id, String build_id,  String firmware_type_string){
        try {

            // Slouží k nahrávání firmwaru do deviců, které jsou ve fakce instnaci pro testování
            // nejsou databázovaný a tedy nejde spustit regulérní update procedura na kterou jsme zvyklé - viz metoda nad tímto
            // Slouží jen pro Admin rozhraní Tyriona

            Firmware_type firmware_type = Firmware_type.getFirmwareType(firmware_type_string);
            if (firmware_type == null) return GlobalResult.notFoundObject("FirmwareType not found!");

            List<String> list = new ArrayList<>();
            list.add(board_id);

            // Přijmu soubor
            Http.MultipartFormData body = request().body().asMultipartFormData();

            List<Http.MultipartFormData.FilePart> files_from_request = body.getFiles();

            if (files_from_request == null || files_from_request.isEmpty())return GlobalResult.notFoundObject("Bin File not found!");
            if (files_from_request.size() > 1)return GlobalResult.result_BadRequest("More than one File is not allowed!");

            File file = files_from_request.get(0).getFile();
            if (file == null) return GlobalResult.result_BadRequest("File not found!");
            if (file.length() < 1) return GlobalResult.result_BadRequest("File is Empty!");


            int dot = files_from_request.get(0).getFilename().lastIndexOf(".");
            String file_type = files_from_request.get(0).getFilename().substring(dot);
            String file_name = files_from_request.get(0).getFilename().substring(0, dot);

            // Zkontroluji soubor
            if (!file_type.equals(".bin"))return GlobalResult.result_BadRequest("Wrong type of File - \"Bin\" required! ");
            if ((file.length() / 1024) > 500)return GlobalResult.result_BadRequest("File is bigger than 500K b");


            ObjectNode request = Json.newObject();
            request.put("messageChannel", "tyrion");
            request.put("instanceId", instance_id);
            request.put("messageType", "updateDevice");
            request.put("firmware_type", firmware_type.get_firmwareType());
            request.set("targetIds",  Json.toJson(list));
            request.put("build_id", build_id);
            request.put("program", Model_FileRecord.get_encoded_binary_string_from_File(file));

            // TODO - tohle nejde nějak domylset
            // ObjectNode result =  Controller_WebSocket.incomingConnections_homers.get(instance_id).write_with_confirmation(request, 1000*30, 0, 3);

            if(request.get("status").asText().equals("success")) {
                return GlobalResult.result_ok();
            }
            else {
                return GlobalResult.result_BadRequest(request);
            }

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }
    */

    @ApiOperation(value = "update Embedded Hardware with C_program compilation",
            tags = {"C_Program", "Actualization"},
            notes = "Upload compilation to list of hardware. Compilation is on Version oc C_program. And before uplouding compilation, you must succesfuly compile required version before! " +
                    "Result (HTML code) will be every time 200. - Its because upload, restart, etc.. operation need more than ++30 second " +
                    "There is also problem / chance that Tyrion didn't find where Embedded hardware is. So you have to listening Server Sent Events (SSE) and show \"future\" message to the user!",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Board.update_permission", value = "true"),
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_update"),
                    })
            }

    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_UploadBinaryFileToBoard",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result uploadCompilationToBoard() {
        try {

            // Zpracování Json
            Form<Swagger_UploadBinaryFileToBoard> form = Form.form(Swagger_UploadBinaryFileToBoard.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_UploadBinaryFileToBoard help = form.get();


            List<Model_BPair> b_pairs = new ArrayList<>();

            if(help.board_pairs.isEmpty()) return GlobalResult.badRequest("List is Empty");

            for(Swagger_UploadBinaryFileToBoard.Board_pair board_update_pair : help.board_pairs) {

                // Ověření objektu
                Model_VersionObject c_program_version = Model_VersionObject.find.byId(board_update_pair.c_program_version_id);
                if (c_program_version == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

                //Zkontroluji validitu Verze zda sedí k C_Programu
                if (c_program_version.c_program == null) return GlobalResult.result_BadRequest("Version_Object its not version of C_Program");

                // Zkontroluji oprávnění
                if (!c_program_version.c_program.read_permission()) return GlobalResult.forbidden_Permission();

                //Zkontroluji validitu Verze zda sedí k C_Programu
                if (c_program_version.c_compilation == null) return GlobalResult.result_BadRequest("Version_Object its not version of C_Program - Missing compilation File");

                // Ověření zda je kompilovatelná verze a nebo zda kompilace stále neběží
                if (c_program_version.c_compilation.status != Enum_Compile_status.successfully_compiled_and_restored) return GlobalResult.result_BadRequest("You cannot upload code in state:: " + c_program_version.c_compilation.status.name());

                //Zkontroluji zda byla verze už zkompilována
                if (!c_program_version.c_compilation.status.name().equals(Enum_Compile_status.successfully_compiled_and_restored.name())) return GlobalResult.result_BadRequest("The program is not yet compiled & Restored");

                // Kotrola objektu
                Model_Board board = Model_Board.find.byId(board_update_pair.board_id);
                if (board == null) return GlobalResult.notFoundObject("Board board_id not found");

                // Kontrola oprávnění
                if (!board.edit_permission()) return GlobalResult.forbidden_Permission();


                Model_BPair b_pair = new Model_BPair();
                b_pair.board = board;
                b_pair.c_program_version = c_program_version;

                b_pairs.add(b_pair);

            }


            Model_Board.update_firmware(Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL, b_pairs);

            // Vracím odpověď
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }
    
///###################################################################################################################*/

    @ApiOperation(value = "Create new Compilation Server",
            hidden = true,
            tags = {"External Server"},
            notes = "Create new Gate for Compilation Server",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Compilation_Server_create" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Cloud_Compilation_Server_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = Model_CompilationServer.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result create_Compilation_Server(){
        try{

            // Zpracování Json
            Form<Swagger_Cloud_Compilation_Server_New> form = Form.form(Swagger_Cloud_Compilation_Server_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Cloud_Compilation_Server_New help = form.get();


            // Vytvářím objekt
            Model_CompilationServer server = new Model_CompilationServer();
            server.personal_server_name = help.personal_server_name;

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! server.create_permission())  return GlobalResult.forbidden_Permission();

            // Ukládám objekt
            server.save();

            // Vracím objekt
            return GlobalResult.created(Json.toJson(server));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Compilation Server",
            hidden = true,
            tags = {"External Server"},
            notes = "Edit basic information Compilation Server",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Compilation_Server_edit" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Cloud_Compilation_Server_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Update successfuly",        response = Model_CompilationServer.class),
            @ApiResponse(code = 400, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing",   response = Result_JsonValueMissing.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Compilation_Server( @ApiParam(value = "server_id ", required = true) String server_id ){
        try{

            // Zpracování Json
            Form<Swagger_Cloud_Compilation_Server_New> form = Form.form(Swagger_Cloud_Compilation_Server_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Cloud_Compilation_Server_New help = form.get();

            //Zkontroluji validitu
            Model_CompilationServer server = Model_CompilationServer.find.byId(server_id);
            if (server == null) return GlobalResult.notFoundObject("Cloud_Compilation_Server server_id not found");

            // Zkontroluji oprávnění
            if(!server.edit_permission()) return GlobalResult.forbidden_Permission();

            // Upravím objekt
            server.personal_server_name = help.personal_server_name;

            // Uložím objekt
            server.update();

            // Vrátím objekt
            return GlobalResult.result_ok(Json.toJson(server));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Compilation Servers",
            hidden = true,
            tags = {"External Server"},
            notes = "get Compilation Servers",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Model_CompilationServer.class, responseContainer = "List "),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_All_Compilation_Server(){
        try{

            // Vyhledám všechny objekty
            List<Model_CompilationServer> servers = Model_CompilationServer.find.all();

            // Vracím Objekty
            return GlobalResult.result_ok(Json.toJson(servers));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove Compilation Servers",
            hidden = true,
            tags = {"External Server"},
            notes = "remove Compilation Servers",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Compilation_Server_delete" ),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 400, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_Compilation_Server( @ApiParam(value = "server_id ", required = true) String server_id ){
        try{

            //Zkontroluji validitu
            Model_CompilationServer server = Model_CompilationServer.find.byId(server_id);
            if (server == null) return GlobalResult.notFoundObject("Cloud_Compilation_Server server_id not found");

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! server.delete_permission())  return GlobalResult.forbidden_Permission();

            // Smažu objekt
            server.delete();

            // Vracím odpověď
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "Create new Processor",
            hidden = true,
            tags = {"Processor"},
            notes = "If you want create new Processor. Send required json values and server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Processor_create" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Processor_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = Model_Processor.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result processor_create() {
        try {

            // Zpracování Json
            final Form<Swagger_Processor_New> form = Form.form(Swagger_Processor_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Processor_New help = form.get();

            // Vytvářím objekt
            Model_Processor processor = new Model_Processor();
            processor.description    = help.description;
            processor.processor_code = help.processor_code;
            processor.processor_name = help.processor_name;
            processor.speed          = help.speed;

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! processor.create_permission())  return GlobalResult.forbidden_Permission();

            // Ukládám objekt
            processor.save();

            // Vracím objekt
            return GlobalResult.created(Json.toJson(processor));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Processor",
            tags = {"Processor"},
            notes = "If you get Processor by query processor_id.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",             response = Model_Processor.class),
            @ApiResponse(code = 400, message = "Objects not found",     response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result processor_get(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            //Zkontroluji validitu
            Model_Processor processor = Model_Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject("Processor processor_id not found");

            // Vracím objekt
            return GlobalResult.result_ok(Json.toJson(processor));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Processors",
            tags = {"Processor"},
            notes = "If you want get Processor by query processor_id.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Processor.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result processor_getAll() {
        try {

            //Vyhledám objekty
           List<Model_Processor> processors = Model_Processor.find.all();

            // Vracím seznam objektů
           return GlobalResult.result_ok(Json.toJson(processors));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update Processor",
            tags = {"Processor"},
            notes = "If you want update Processor.id by query = processor_id . Send required json values and server respond with update object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Processor_edit" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Processor_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Processor.class),
            @ApiResponse(code = 400, message = "Some Json value Missing",   response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result processor_update(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            // Zpracování Json
            Form<Swagger_Processor_New> form = Form.form(Swagger_Processor_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Processor_New help = form.get();

            // Kontroluji validitu
            Model_Processor processor = Model_Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject("Processor processor_id not found");

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! processor.edit_permission())  return GlobalResult.forbidden_Permission();

            // Upravuji objekt
            processor.description    = help.description;
            processor.processor_code = help.processor_code;
            processor.processor_name = help.processor_name;
            processor.speed          = help.speed;

            // Ukládám do databáze
            processor.update();

            // Vracím upravený objekt
            return GlobalResult.result_ok(Json.toJson(processor));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Processor",
            hidden = true,
            tags = {"Processor"},
            notes = "If you want delete Processor by query processor_id.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Processor_delete" ),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result processor_delete(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            // Kontroluji validitu
            Model_Processor processor = Model_Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject("Processor processor_id not found");

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! processor.delete_permission())  return GlobalResult.forbidden_Permission();

            // Mažu z databáze
            processor.delete();

            // Vracím objekt
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "get FileRecord",
            tags = {"File"},
            notes = "if you want create new SingleLibrary for C_program compilation",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            hidden = true
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_File_Content.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result fileRecord(@ApiParam(value = "file_record_id String query", required = true)  String file_record_id){
        try {

            // Kontrola validity objektu
            Model_FileRecord fileRecord = Model_FileRecord.find.fetch("version_object").where().eq("id", file_record_id).findUnique();
            if (fileRecord == null) return GlobalResult.notFoundObject("FileRecord file_record_id not found");

            // Swagger_File_Content - Zástupný dokumentační objekt

            // Vracím content
            return GlobalResult.result_ok(Json.toJson( fileRecord.get_fileRecord_from_Azure_inString()));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

///###################################################################################################################*/

///###################################################################################################################*/

    @ApiOperation(value = "create new Producer",
            hidden = true,
            tags = {"Producer"},
            notes = "if you want create new Producer. Its company owned physical boards and we used that for filtering",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Producer_create" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Producer_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_Producer.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_Producer() {
        try {

            // Zpracování Json
            final Form<Swagger_Producer_New> form = Form.form(Swagger_Producer_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Producer_New help = form.get();

            //Vytvářím objekt
            Model_Producer producer = new Model_Producer();
            producer.name = help.name;
            producer.description = help.description;

            // Kontorluji oprávnění těsně před uložením
            if(! producer.create_permission()) return GlobalResult.forbidden_Permission();

            //Ukládám objekt
            producer.save();

            // Vracím objekt
            return GlobalResult.created(Json.toJson(producer));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Producer",
            hidden = true,
            tags = {"Producer"},
            notes = "if you want edit information about Producer. Its company owned physical boards and we used that for filtering",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Producer.edit_permission", value =  "true" ),
                            @ExtensionProperty(name = "Static Permission key", value =  "Producer_edit" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Producer_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Producer.class),
            @ApiResponse(code = 400, message = "Objects not found",       response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Producer(@ApiParam(required = true) String producer_id) {
        try {

            // Zpracování Json
            final Form<Swagger_Producer_New> form = Form.form(Swagger_Producer_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Producer_New help = form.get();

            // Kontrola objektu
            Model_Producer producer = Model_Producer.find.byId(producer_id);
            if(producer == null ) return GlobalResult.notFoundObject("Producer producer_id not found");

            // Kontorluji oprávnění těsně před uložením
            if(! producer.edit_permission()) return GlobalResult.forbidden_Permission();

            // Úprava objektu
            producer.name = help.name;
            producer.description = help.description;

            // Uložení změn objektu
            producer.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(producer));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Producers",
            tags = {"Producer"},
            notes = "if you want get list of Producers. Its list of companies owned physical boards and we used that for filtering",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Producer.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Producers() {
        try {

            // Získání seznamu
            List<Model_Producer> producers = Model_Producer.find.all();

            // Vrácení seznamu
            return GlobalResult.result_ok(Json.toJson(producers));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Producer",
            tags = {"Producer"},
            notes = "if you want get Producer. Its company owned physical boards and we used that for filtering",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Producer.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Producer(@ApiParam(required = true)  String producer_id) {
        try {

            // Kontrola objektu
            Model_Producer producer = Model_Producer.find.byId(producer_id);
            if(producer == null ) return GlobalResult.notFoundObject("Producer producer_id not found");

            // Vrácneí objektu
            return GlobalResult.result_ok(Json.toJson(producer));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Producer",
            hidden = true,
            tags = {"Producer"},
            notes = "if you want delete Producer",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Producer.delete_permission", value =  "true" ),
                            @ExtensionProperty(name = "Static Permission key", value =  "Producer_delete" ),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_Producer(@ApiParam(required = true) String producer_id) {
        try {

            // Kontrola objektu
            Model_Producer producer = Model_Producer.find.byId(producer_id);
            if(producer == null ) return GlobalResult.notFoundObject("Producer producer_id not found");

            // Kontorluji oprávnění
            if(! producer.delete_permission()) return GlobalResult.forbidden_Permission();

            // Smazání objektu
            producer.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "The TypeOfBoard is category for IoT. Like Raspberry2, Arduino-Uno etc. \n\n" +
                    "We using that for compilation, sorting libraries, filtres and more..",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value = "TypeOfBoard_create"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfBoard_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_TypeOfBoard.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBoard_create() {
        try {

            // Zpracování Json
            final Form<Swagger_TypeOfBoard_New> form = Form.form(Swagger_TypeOfBoard_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBoard_New help = form.get();

            // Kontrola objektu
            Model_Producer producer = Model_Producer.find.byId(help.producer_id);
            if(producer == null ) return GlobalResult.notFoundObject("Producer producer_id not found");

            // Kontrola objektu
            Model_Processor processor = Model_Processor.find.byId(help.processor_id);
            if(processor == null ) return GlobalResult.notFoundObject("Processor processor_id not found");

            // Tvorba objektu
            Model_TypeOfBoard typeOfBoard = new Model_TypeOfBoard();
            typeOfBoard.name = help.name;
            typeOfBoard.description = help.description;
            typeOfBoard.processor = processor;
            typeOfBoard.producer = producer;
            typeOfBoard.connectible_to_internet = help.connectible_to_internet;

            // Kontorluji oprávnění
            if(!typeOfBoard.create_permission()) return GlobalResult.forbidden_Permission();

            // Uložení objektu do DB
            typeOfBoard.save();

            return GlobalResult.created(Json.toJson(typeOfBoard));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want edit base TypeOfBoard information",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBoard.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "TypeOfBoard_edit"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfBoard_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_TypeOfBoard.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBoard_update(@ApiParam(required = true)  String type_of_board_id) {
        try {

            // Zpracování Json
            final Form<Swagger_TypeOfBoard_New> form = Form.form(Swagger_TypeOfBoard_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBoard_New help = form.get();

            // Kontrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.byId(type_of_board_id);
            if (typeOfBoard == null) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            // Kontrola objektu
            Model_Producer producer = Model_Producer.find.byId(help.producer_id);
            if(producer == null ) return GlobalResult.notFoundObject("Producer producer_id not found");

            // Kontrola objektu
            Model_Processor processor = Model_Processor.find.byId(help.processor_id);
            if(processor == null ) return GlobalResult.notFoundObject("Processor processor_id not found");

            // Kontorluji oprávnění
            if(! typeOfBoard.edit_permission()) return GlobalResult.forbidden_Permission();

            // Uprava objektu
            typeOfBoard.name = help.name;
            typeOfBoard.description = help.description;
            typeOfBoard.compiler_target_name = help.compiler_target_name;
            typeOfBoard.processor = processor;
            typeOfBoard.producer = producer;
            typeOfBoard.connectible_to_internet = help.connectible_to_internet;

            // Uložení do DB
            typeOfBoard.update();

            // Vrácení změny
            return GlobalResult.result_ok(Json.toJson(typeOfBoard));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "delete TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want delete TypeOfBoard object by query = type_of_board_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBoard.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "TypeOfBoard_delete"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result typeOfBoard_delete(@ApiParam(required = true)  String type_of_board_id) {
        try {

            // Kontrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.byId(type_of_board_id);
            if(typeOfBoard == null ) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found") ;

            // Kontorluji oprávnění
            if(! typeOfBoard.delete_permission()) return GlobalResult.forbidden_Permission();

            // Smazání objektu
            typeOfBoard.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get list of all TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want get all TypeOfBoard objects",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_TypeOfBoard.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result typeOfBoard_getAll() {
        try {

            // Získání seznamu
            List<Model_TypeOfBoard> typeOfBoards = Model_TypeOfBoard.find.all();

            // Vrácení seznamu
            return  GlobalResult.result_ok(Json.toJson(typeOfBoards));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want get TypeOfBoard object by query = type_of_board_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_TypeOfBoard.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result typeOfBoard_get(@ApiParam(required = true)  String type_of_board_id) {
        try {

            // Kontrola validity objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.byId(type_of_board_id);
            if(typeOfBoard == null ) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            // Kontorluji oprávnění
            if(! typeOfBoard.read_permission()) return GlobalResult.forbidden_Permission();

            // Vrácení validity objektu
            return GlobalResult.result_ok(Json.toJson(typeOfBoard));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Upload TypeOfBoard picture", hidden = true)
    public Result typeOfBoard_uploadPicture(@ApiParam(required = true) String type_of_board_id){
        try {

            Model_TypeOfBoard type_of_board = Model_TypeOfBoard.find.byId(type_of_board_id);
            if (type_of_board == null) return GlobalResult.notFoundObject("Type of board does not exist");

            // Přijmu soubor
            Http.MultipartFormData body = request().body().asMultipartFormData();

            if (body == null) return GlobalResult.notFoundObject("Missing picture!");

            Http.MultipartFormData.FilePart file_from_request = body.getFile("file");

            if (file_from_request == null) return GlobalResult.notFoundObject("Missing picture!");

            File file = file_from_request.getFile();

            int dot = file_from_request.getFilename().lastIndexOf(".");
            String file_type = file_from_request.getFilename().substring(dot);

            // Zkontroluji soubor - formát, velikost, rozměry
            if((!file_type.equals(".jpg"))&&(!file_type.equals(".png"))) return GlobalResult.result_BadRequest("Wrong type of File - '.jpg' or '.png' required! ");
            if( (file.length() / 1024) > 500) return GlobalResult.result_BadRequest("Picture is bigger than 500 KB");
            BufferedImage bimg = ImageIO.read(file);
            if((bimg.getWidth() < 50)||(bimg.getWidth() > 400)||(bimg.getHeight() < 50)||(bimg.getHeight() > 400)) return GlobalResult.result_BadRequest("Picture height or width is not between 50 and 400 pixels.");

            // Odebrání předchozího obrázku
            if(!(type_of_board.picture == null)){
                Model_FileRecord fileRecord = type_of_board.picture;
                type_of_board.picture = null;
                type_of_board.update();
                fileRecord.delete();
            }

            // Pokud link není, vygeneruje se nový, unikátní
            if(type_of_board.azure_picture_link == null){
                while(true){ // I need Unique Value
                    String azure_picture_link = type_of_board.get_Container().getName() + "/" + UUID.randomUUID().toString() + file_type;
                    if (Model_TypeOfBoard.find.where().eq("azure_picture_link", azure_picture_link ).findUnique() == null) {
                        type_of_board.azure_picture_link = azure_picture_link;
                        type_of_board.update();
                        break;
                    }
                }
            }

            String file_path = type_of_board.azure_picture_link;

            int slash = file_path.indexOf("/");
            String file_name = file_path.substring(slash+1);

            type_of_board.picture = Model_FileRecord.uploadAzure_File(file, file_name, file_path);
            type_of_board.update();


            return GlobalResult.result_ok(Json.toJson(type_of_board));
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Remove TypeOfBoard picture", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result typeOfBoard_removePicture(@ApiParam(required = true) String type_of_board_id){
        try {

            Model_TypeOfBoard type_of_board = Model_TypeOfBoard.find.byId(type_of_board_id);
            if (type_of_board == null) return GlobalResult.notFoundObject("Type of Board does not exist");

            if(!(type_of_board.picture == null)) {
                Model_FileRecord fileRecord = type_of_board.picture;
                type_of_board.azure_picture_link = null;
                type_of_board.picture = null;
                type_of_board.update();
                fileRecord.delete();
            }else{
                return GlobalResult.result_BadRequest("There is no picture to remove.");
            }

            return GlobalResult.result_ok("Picture successfully removed");
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    // BootLoader ---------------------------------------------------------------------------------------------------------------------

    @ApiOperation(value = "new_boot_loader", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result boot_loader_create(@ApiParam(value = "type_of_board_id", required = true) String type_of_board_id) {
        try {

            // Zpracování Json
            final Form<Swagger_BootLoader_New> form = Form.form(Swagger_BootLoader_New.class).bindFromRequest();
            if(form.hasErrors()){return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BootLoader_New help = form.get();

            Model_TypeOfBoard type_of_board = Model_TypeOfBoard.find.byId(type_of_board_id);
            if(type_of_board == null) return GlobalResult.notFoundObject("Type_of_board_not_found");

            if(Model_BootLoader.find.where().eq("version_identificator", help.version_identificator ).eq("type_of_board.id", type_of_board.id).findUnique() != null) return GlobalResult.result_BadRequest("Version format is not unique!");

            Model_BootLoader boot_loader = new Model_BootLoader();
            boot_loader.date_of_create = new Date();
            boot_loader.name = help.name;
            boot_loader.changing_note =  help.changing_notes;
            boot_loader.description = help.description;
            boot_loader.version_identificator = help.version_identificator;
            boot_loader.type_of_board = type_of_board;

            if(!boot_loader.create_permission()) return GlobalResult.forbidden_Permission();
            boot_loader.save();

            // Vracím seznam
            return GlobalResult.result_ok(Json.toJson(boot_loader));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Uploud bootloader file", hidden = true)
    @BodyParser.Of(BodyParser.MultipartFormData.class)
    public Result boot_loader_upload_file(@ApiParam(value = "boot_loader_id", required = true) String boot_loader_id) {
        try {

            Model_BootLoader boot_loader = Model_BootLoader.find.byId(boot_loader_id);
            if(boot_loader == null) return GlobalResult.notFoundObject("BootLoader boot_loader_id not found");

            if(!boot_loader.edit_permission()) return GlobalResult.forbidden_Permission();

            if(boot_loader.file != null) return GlobalResult.result_BadRequest("You cannot upload file twice!");

            Http.MultipartFormData body = request().body().asMultipartFormData();
            List<Http.MultipartFormData.FilePart> files_from_request = body.getFiles();

            //Bin FILE
            File file = files_from_request.get(0).getFile();
            if (file == null) return GlobalResult.result_BadRequest("File not found!");
            if (file.length() < 1) return GlobalResult.result_BadRequest("File is Empty!");


            int dot = files_from_request.get(0).getFilename().lastIndexOf(".");
            String file_type = files_from_request.get(0).getFilename().substring(dot);
            String file_name = files_from_request.get(0).getFilename().substring(0, dot);

            // Zkontroluji soubor
            if (!file_type.equals(".bin")) return GlobalResult.result_BadRequest("Wrong type of File - \"Bin\" required! ");
            if ((file.length() / 1024) > 500) return GlobalResult.result_BadRequest("File is bigger than 500Kb");

            String binary_file = Model_FileRecord.get_encoded_binary_string_from_File(file);
            Model_FileRecord filerecord  = Model_FileRecord.create_Binary_file( boot_loader.get_path(), binary_file, "bootloader.bin");

            boot_loader.file = filerecord;
            filerecord.boot_loader = boot_loader;
            filerecord.update();
            boot_loader.update();


            // Vracím seznam
            return GlobalResult.result_ok(Json.toJson(boot_loader));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Mark as main", hidden = true)
    @BodyParser.Of(BodyParser.Empty.class)
    public Result boot_loader_mark_as_main(@ApiParam(value = "boot_loader_id", required = true) String boot_loader_id) {
        try {

            Model_BootLoader boot_loader = Model_BootLoader.find.byId(boot_loader_id);
            if(boot_loader == null) return GlobalResult.notFoundObject("BootLoader boot_loader_id not found");

            if(!boot_loader.edit_permission()) return GlobalResult.forbidden_Permission();
            if(boot_loader.file == null) return GlobalResult.result_BadRequest("Required bootloader object with file");

            if(boot_loader.main_type_of_board != null) return GlobalResult.result_BadRequest("Bootloader is Already Main");


            Model_BootLoader old_main = Model_BootLoader.find.where().eq("main_type_of_board.id", boot_loader.type_of_board.id).findUnique();
            if(old_main != null){

                old_main.main_type_of_board = null;
                old_main.update();

            }

            boot_loader.main_type_of_board = boot_loader.type_of_board;
            boot_loader.update();

            // Vracím Json
            return GlobalResult.result_ok(Json.toJson(boot_loader));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Update bootloader on device list", hidden = true)
    public Result boot_loader_manual_update(){
        try {

            // Zpracování Json
            final Form<Swagger_Board_Bootloader_Update > form = Form.form(Swagger_Board_Bootloader_Update.class).bindFromRequest();
            if(form.hasErrors()){return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Board_Bootloader_Update help = form.get();


            List<Model_Board> boards = Model_Board.find.where().in("id", help.device_ids).findList();
            if(boards.isEmpty()) return GlobalResult.notFoundObject("Board not found");

            if(help.bootloader_id != null) {

                Model_BootLoader bootLoader = Model_BootLoader.find.byId(help.bootloader_id);
                if (bootLoader == null) return GlobalResult.notFoundObject("BootLoader not found");

                for(Model_Board board : boards) {
                    if (!board.read_permission()) return GlobalResult.forbidden_Permission();
                }

                Model_Board.update_bootloader(Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL, boards, bootLoader);

            }else {

                for(Model_Board board : boards) {
                    if (!board.read_permission()) return GlobalResult.forbidden_Permission();
                }

                Model_Board.update_bootloader(Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL, boards, null);
            }


            // Vracím Json
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    ///###################################################################################################################*/

    @ApiOperation(value = "create Board",
            hidden =  true,
            tags = { "Board"},
            notes = "This Api is using only for developing mode, for registration of our Board - in future it will be used only by machine in factory or " +
                    "boards themselves with \"registration procedure\". Its not allowed to delete that! Only deactivate. Classic User can registed that to own " +
                    "project or own account",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                 @Extension( name = "permission_required", properties = {
                         @ExtensionProperty(name = "TypeOfBoard.register_new_device_permission", value = "true"),
                         @ExtensionProperty(name = "Static Permission key", value = "Board_create"),
                 }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = Model_Board.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_create() {
        try {

            // Zpracování Json
            final Form<Swagger_Board_New> form = Form.form(Swagger_Board_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Board_New help = form.get();

            // Kotrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.byId( help.type_of_board_id  );
            if(typeOfBoard == null ) return GlobalResult.notFoundObject("TypeOfBoard type_of_board_id not found");

            // Kontorluji oprávnění
            if(! typeOfBoard.register_new_device_permission()) return GlobalResult.forbidden_Permission();


                Model_Board board = new Model_Board();
                board.id = help.hardware_unique_id;
                board.is_active = false;
                board.date_of_create = new Date();
                board.type_of_board = typeOfBoard;

                // Uložení desky do DB
                board.save();

            // Vracím seznam zařízení k registraci
            return GlobalResult.created(Json.toJson(board));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get List of Boards for Firmware Upload",
            tags = { "Board"},
            notes = "List of boards under Project for fast upload of Firmware to Board from Web IDE",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Board.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_edit"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Board_for_fast_upload_detail.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result board_get_for_fast_upload(@ApiParam(required = true)  String project_id){
        try {

            // Kotrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if(project == null ) return GlobalResult.notFoundObject("Project project not found");

            // Kontrola oprávnění
            if(!project.edit_permission()) return GlobalResult.forbidden_Permission();

            // Vyhledání seznamu desek na které lze nahrát firmware - okamžitě
            List<Model_Board> boards = Model_Board.find.where().eq("type_of_board.connectible_to_internet", true).eq("project.id", project_id).findList();

            List<Swagger_Board_for_fast_upload_detail> list = new ArrayList<>();

            for(Model_Board board : boards ){
                list.add(board.get_short_board_for_fast_upload());
            }


            // Vrácení upravenéh objektu
            return GlobalResult.result_ok(Json.toJson(list));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Board - update personal description",
            tags = { "Board"},
            notes = "Used for add descriptions by owners. \"Persons\" who registred \"Board\" to own \"Projec\" ",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Board.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_edit"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_Personal",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Board.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_update_description(@ApiParam(required = true)  String board_id){
        try {

            // Zpracování Json
            final Form<Swagger_Board_Personal> form = Form.form(Swagger_Board_Personal.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Board_Personal help = form.get();

            // Kotrola objektu
            Model_Board board = Model_Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject("Board board_id not found");

            // Kontrola oprávnění
            if(!board.edit_permission()) return GlobalResult.forbidden_Permission();

            // Uprava desky
            board.personal_description = help.personal_description;

            // Uprava objektu v databázi
            board.update();

            // Vrácení upravenéh objektu
            return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


    @ApiOperation(value = "update Board - update Backup settiong",
            tags = { "Board"},
            notes = "",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Board.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_edit"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_Backup_settings",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Board.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_update_backup(){
        try {

            // Zpracování Json
            final Form<Swagger_Board_Backup_settings> form = Form.form(Swagger_Board_Backup_settings.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Board_Backup_settings help = form.get();

            if( help.board_backup_pair_list.isEmpty()) return GlobalResult.notFoundObject("List is Empty");

            for(Swagger_Board_Backup_settings.Board_backup_pair board_backup_pair : help.board_backup_pair_list) {

                // Kotrola objektu
                Model_Board board = Model_Board.find.byId(board_backup_pair.board_id);
                if (board == null) return GlobalResult.notFoundObject("Board board_id not found");

                // Kontrola oprávnění
                if (!board.edit_permission()) return GlobalResult.forbidden_Permission();

                // Uprava desky


                logger.debug("Controller_Board:: board_update_backup:: Board has own Static Backup - Removing static backup procedure required");

                WS_Message_Board_set_autobackup result =  Model_Board.set_auto_backup(board);
                if(result.status.equals("success")){

                    board.actual_backup_c_program_version = null;
                    board.backup_mode = true;
                    board.save();

                }else {
                    logger.warn("Controller_Board:: board_update_backup:: Something is wrong in message:: Error:: " + result.error + " ErrorCode:: " + result.errorCode);
                }



            }

            // Vrácení upravenéh objektu
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update Board - update Backup settiong",
            tags = { "Board"},
            notes = "",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Board.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_edit"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_Backup_settings",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Board.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result  board_set_backup_c_program_version(){
        try {

            // Zpracování Json
            final Form<Swagger_Board_SetBackup> form = Form.form(Swagger_Board_SetBackup.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Board_SetBackup help = form.get();

            List<Model_BPair> board_pairs = new ArrayList<>();

            for(Swagger_Board_SetBackup.Board_backup_pair board_backup_pair : help.board_backup_pair_list) {

                // Kotrola objektu
                Model_Board board = Model_Board.find.byId(board_backup_pair.board_id);
                if (board == null) return GlobalResult.notFoundObject("Board board_id not found");

                // Kontrola oprávnění
                if (!board.edit_permission()) return GlobalResult.forbidden_Permission();

                // Uprava desky
                Model_VersionObject c_program_version = Model_VersionObject.find.byId(board_backup_pair.c_program_version_id);
                if (c_program_version == null) return GlobalResult.notFoundObject("Version_Object c_program_version_id not found");

                //Zkontroluji validitu Verze zda sedí k C_Programu
                if (c_program_version.c_program == null) return GlobalResult.result_BadRequest("Version_Object its not version of C_Program");

                // Zkontroluji oprávnění
                if (!c_program_version.c_program.read_permission()) return GlobalResult.forbidden_Permission();

                //Zkontroluji validitu Verze zda sedí k C_Programu
                if (c_program_version.c_compilation == null) return GlobalResult.result_BadRequest("Version_Object its not version of C_Program - Missing compilation File");

                // Ověření zda je kompilovatelná verze a nebo zda kompilace stále neběží
                if (c_program_version.c_compilation.status != Enum_Compile_status.successfully_compiled_and_restored) return GlobalResult.result_BadRequest("You cannot upload code in state:: " + c_program_version.c_compilation.status.name());

                //Zkontroluji zda byla verze už zkompilována
                if (!c_program_version.c_compilation.status.name().equals(Enum_Compile_status.successfully_compiled_and_restored.name())) return GlobalResult.result_BadRequest("The program is not yet compiled & Restored");

                Model_BPair b_pair = new Model_BPair();
                b_pair.board = board;
                b_pair.c_program_version = c_program_version;

                board_pairs.add(b_pair);

            }

            Model_Board.update_backup(Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL, board_pairs);

            // Vrácení upravenéh objektu
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Boards with filter parameters",
            tags = { "Board"},
            notes = "Get List of boards. Acording by permission - system return only hardware from project, where is user owner or" +
                    " all boards if user have static Permission key",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_read"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Board_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_getByFilter() {
        try {

            // Zpracování Json
            final Form<Swagger_Board_Filter> form = Form.form(Swagger_Board_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Board_Filter help = form.get();

            // Tvorba parametru dotazu
            Query<Model_Board> query = Ebean.find(Model_Board.class);


            // If Json contains TypeOfBoards list of id's
            if(help.type_of_board_ids != null ){
                query.where().in("type_of_board.id", help.type_of_board_ids);
            }

            // If contains confirms
            if(help.active != null){
                query.where().eq("is_active", help.active.equals("true"));
            }

            if(help.projects != null){
                query.where().in("projects.id", help.projects);
            }

            if(help.producers != null){
                query.where().in("type_of_board.producer.id", help.producers);
            }

            if(help.processors != null){
                query.where().in("type_of_board.processor.id", help.processors);
            }

            // From date
            if(help.start_time != null){
                query.where().ge("date_of_create", help.start_time);
            }

            // To date
            if(help.end_time != null){
                query.where().le("date_of_create", help.end_time);
            }

            // Vytvářím seznam podle stránky
            Swagger_Board_List result = new Swagger_Board_List(query, help.page_number);

            // Vracím seznam
            return GlobalResult.result_ok(Json.toJson(result));

        } catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "deactivate Board",
            tags = { "Board"},
            notes = "Permanent exclusion from the system - for some reason it is not allowed to remove the Board from database",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_update"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Board.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result board_deactivate(@ApiParam(required = true)  String board_id) {
        try {

            // Kotrola objektu
            Model_Board board = Model_Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject("Board board_id not found");

            // Kontrola oprávnění
            if(board.update_permission()) return GlobalResult.forbidden_Permission();

            // Úprava stavu
            board.is_active = false;

            // Uložení do databáze
            board.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "get Board",
            tags = { "Board"},
            notes = "if you want get Board object by query = board_id. User can get only boards from project, whitch " +
                    "user owning or user need Permission key \"Board_rea\".",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_read"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Board.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result board_get(@ApiParam(required = true) String board_id) {
        try {

            // Kotrola objektu
            Model_Board board = Model_Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject("Board board_id not found");

            // Kontrola oprávnění
            if(!board.read_permission()) return GlobalResult.forbidden_Permission();

            // vrácení objektu
            return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            e.printStackTrace();
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "check Board during registration",
            tags = {"Board"},
            notes = "Check Board state for new Registration. Types of responses in JSON state value" +
                    "[CAN_REGISTER, NOT_EXIST, ALREADY_REGISTERED_IN_YOUR_ACCOUNT, ALREADY_REGISTERED, PERMANENTLY_DISABLED, BROKEN_DEVICE]... \n " +
                    "PERMANENTLY_DISABLED - device was removed by Byzance. \n" +
                    "BROKEN_DEVICE - device exist - but its not possible to registered that. Damaged during manufacturing. ",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_read"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Board_Registration_Status.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result board_check(@ApiParam(required = true) String hash_for_adding) {
        try {

            // Kotrola objektu
            Model_Board board = Model_Board.find.where().eq("hash_for_adding", hash_for_adding).findUnique();


            Swagger_Board_Registration_Status status = new Swagger_Board_Registration_Status();

            if(board == null ){
                status.status = Enum_Board_registration_status.NOT_EXIST;
            }else if(board.project_id() == null){
                status.status = Enum_Board_registration_status.CAN_REGISTER;
            }else if(board.project_id() != null && board.read_permission()){
                status.status = Enum_Board_registration_status.ALREADY_REGISTERED_IN_YOUR_ACCOUNT;
            }else{
                status.status = Enum_Board_registration_status.ALREADY_REGISTERED;
            }


            return GlobalResult.result_ok(Json.toJson(status));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }
    @ApiOperation(value = "connect Board with Project",
            tags = { "Board"},
            notes = "This Api is used by Users for connection of Board with their Project",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Board_Connection", value = Model_Board.connection_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Board.first_connect_permission", value = "true"),
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_update"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Board.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result board_connectProject(@ApiParam(required = true) String hash_for_adding, @ApiParam(required = true) String project_id){
        try {

            logger.debug("CompilationControler:: Registrace nového zařízení ");
            // Kotrola objektu
            Model_Board board = Model_Board.find.where().eq("hash_for_adding", hash_for_adding).findUnique();
            if(board == null ) return GlobalResult.notFoundObject("Board board_id not found");

            // Kotrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if(!board.first_connect_permission()) return GlobalResult.result_BadRequest("Board is already registered");

            // Kontrola oprávnění
            if(!project.update_permission()) return GlobalResult.forbidden_Permission();

            // uprava desky
            board.project = project;
            project.boards.add(board);
            board.update();
            project.update();


            if(board.type_of_board.connectible_to_internet){

                logger.debug("CompilationController:: board_connectProject:: Deska je připojitelná k internetu");

                Model_HomerInstance instance = project.private_instance;
                instance.boards_in_virtual_instance.add(board);
                board.virtual_instance_under_project = instance;
                instance.update();
                board.update();
                instance.add_Yoda_to_instance(board.id);
            }


             // vrácení objektu
             return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "disconnect Board from Project",
            tags = { "Board"},
            notes = "This Api is used by Users for disconnection of Board from their Project, its not meaning that Board is removed from system, only disconnect " +
                    "and another user can registred that (connect that with different account/project etc..)",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Board_Disconnection", value = Model_Board.disconnection_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_update"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Board.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result board_disconnectProject(@ApiParam(required = true)   String board_id){
        try {

            // Kontrola objektu
            Model_Board board = Model_Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject("Board board_id not found");

            // Kontrola oprávnění
            if(!board.update_permission()) return GlobalResult.forbidden_Permission();

            // Odstraním vazbu
            board.project = null;

            // uložím do databáze
            board.update();

            // vracím upravenou hodnotu
            return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Boards details for integration to Blocko program",
            tags = {"Blocko", "B_Program"},
            notes = "get all boards that user can integrate to Blocko program",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Project_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response =  Swagger_Boards_For_Blocko.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result board_allDetailsForBlocko(@ApiParam(required = true)   String project_id){
        try {

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (! project.read_permission()) return GlobalResult.forbidden_Permission();

            // Získání objektu
            Swagger_Boards_For_Blocko boards_for_blocko = new Swagger_Boards_For_Blocko();
            boards_for_blocko.add_M_Projects(project.m_projects);
            boards_for_blocko.add_C_Programs(project.c_programs);

            for (Model_Board board : project.boards)              boards_for_blocko.boards.add(board.get_short_board());


            boards_for_blocko.type_of_boards = Model_TypeOfBoard.find.where().eq("boards.project.id", project.id).findList();


            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(boards_for_blocko));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    // LIBRARIES #######################################################################################################
}
