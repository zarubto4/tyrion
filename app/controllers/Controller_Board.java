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
import utilities.hardware_registration_auhtority.Hardware_Registration_Authority;
import utilities.lablel_printer_service.Printer_Api;
import utilities.lablel_printer_service.labels.Label_62_mm_package;
import utilities.enums.*;
import utilities.lablel_printer_service.labels.Label_62_split_mm_Details;
import utilities.logger.Class_Logger;
import utilities.logger.ServerLogger;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Filter_List.Swagger_Board_List;
import utilities.swagger.outboundClass.*;
import web_socket.message_objects.compilator_with_tyrion.WS_Message_Make_compilation;
import web_socket.message_objects.homer_hardware_with_tyrion.WS_Message_Hardware_set_settings;
import web_socket.message_objects.homer_hardware_with_tyrion.helps_objects.WS_Help_Hardware_Pair;

import java.nio.charset.IllegalCharsetNameException;
import java.util.*;


@Api(value = "Not Documented API - InProgress or Stuck")  // Záměrně takto zapsané - Aby ve swaggru nezdokumentované API byly v jedné sekci
@Security.Authenticated(Secured_API.class)
public class Controller_Board extends Controller {

// LOGGER ##############################################################################################################
    
    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Board.class);
    
///###################################################################################################################*/

    @ApiOperation(value = "compile C_Program_Version",
            hidden = true,
            tags = {"Admin-C_Program"},
            notes = "Compile specific version of C_Program - before compilation - you have to update (save) version code" +
                    "This appi is udes by Tyrion Calling on own API",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_Program.Version.read_permission", value = Model_VersionObject.read_permission_docs),
                    }),
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "C_Program_read"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Compilation successful",    response = Swagger_Compilation_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 422, message = "Compilation unsuccessful",  response = Swagger_Compilation_Build_Error.class, responseContainer = "List"),
            @ApiResponse(code = 477, message = "External server is offline",response = Result_ServerOffline.class),
            @ApiResponse(code = 478, message = "External server side Error",response = Result_ExternalServerSideError.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result compile_c_program_version( @ApiParam(value = "version_id String query", required = true) String version_id ){
        try{

            terminal_logger.debug("Starting compilation on version_id = " + version_id);

            // Ověření objektu
            Model_VersionObject version_object = Model_VersionObject.get_byId(version_id);
            if(version_object == null) return GlobalResult.result_notFound("Version_Object version_id not found");

            // Smažu předchozí kompilaci
            if(version_object.c_program == null) return GlobalResult.result_badRequest("Version is not version of C_Program");

            // Kontrola oprávnění
            if(!version_object.c_program.read_permission()) return GlobalResult.result_forbidden();

            // Odpovím předchozí kompilací
            if(version_object.c_compilation != null) return GlobalResult.result_ok(Json.toJson( new Swagger_Compilation_Ok()));


            Response_Interface result = version_object.compile_program_procedure();

            if(result instanceof Result_Ok){
               return  GlobalResult.result_ok(Json.toJson(new Swagger_Compilation_Ok()));
            }

            if(result instanceof Result_CompilationListError){
                return  GlobalResult.result_ok(Json.toJson(((Result_CompilationListError) result).errors));
            }

            if(result instanceof Result_ExternalServerSideError ){
                return GlobalResult.result_externalServerError(Json.toJson(result));
            }

            if(result instanceof Result_ServerOffline){
                return GlobalResult.result_externalServerIsOffline(((Result_ServerOffline) result).message);
            }

            // Neznámá chyba se kterou nebylo počítání
           return GlobalResult.result_badRequest("unknown_error");

        }catch (Exception e){
            return ServerLogger.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "compile C_Program",
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
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 422, message = "Compilation unsuccessful",  response = Swagger_Compilation_Build_Error.class, responseContainer = "List"),
            @ApiResponse(code = 477, message = "External server is offline",response = Result_ServerOffline.class),
            @ApiResponse(code = 478, message = "External server side Error",response = Result_ExternalServerSideError.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result compile_c_program_code() {
        try {

            // Zpracování Json
            Form<Swagger_C_Program_Version_Update> form = Form.form(Swagger_C_Program_Version_Update.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_C_Program_Version_Update help = form.get();

            // Ověření objektu
            if (help.type_of_board_id.isEmpty()) return GlobalResult.result_badRequest("type_of_board_id is missing!");

            // Ověření objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.get_byId(help.type_of_board_id);
            if (typeOfBoard == null) return GlobalResult.result_notFound("TypeOfBoard type_of_board_id not found");

            if(!Model_CompilationServer.is_online()) return GlobalResult.result_externalServerIsOffline("Compilation Server offilne");


            List<Swagger_Library_Record> library_files = new ArrayList<>();

            for (String lib_id : help.imported_libraries) {

                terminal_logger.trace("compile_C_Program_code:: Looking for library Version Id " + lib_id);
                Model_VersionObject lib_version = Model_VersionObject.get_byId(lib_id);

                if (lib_version == null || lib_version.library == null){

                    terminal_logger.internalServerError(new Exception("Error in reading libraries version not found! Version ID = " + lib_version));

                    ObjectNode error = Json.newObject();
                    error.put("status", "error");
                    error.put("error_message", "Error getting libraries - Library not found!");
                    error.put("error_code", 400);
                    return GlobalResult.result_buildErrors(error);


                }

                if (!lib_version.files.isEmpty()){

                    System.out.println("Files nejsou Empty");

                    for (Model_FileRecord f : lib_version.files) {

                        JsonNode json_library = Json.parse(f.get_fileRecord_from_Azure_inString());

                        Form<Swagger_Library_File_Load> lib_form = Form.form(Swagger_Library_File_Load.class).bind(json_library);
                        if (lib_form.hasErrors()){

                            terminal_logger.internalServerError(new Exception("Error reading libraries from files! Model_FileRecord ID = " + f.id));

                            ObjectNode error = Json.newObject();
                            error.put("status", "error");
                            error.put("error_message", "Error with importing libraries - Library Id: " + lib_id );
                            error.put("error_code", 400);
                            return GlobalResult.result_buildErrors(error);
                        }

                        Swagger_Library_File_Load lib_file = lib_form.get();

                        for (Swagger_Library_Record file : lib_file.files){
                           library_files.add(file);
                        }
                    }
                }
            }

            ObjectNode includes = Json.newObject();

            for(Swagger_Library_Record file_lib : library_files){
                if(file_lib.file_name.equals("README.md") || file_lib.file_name.equals("readme.md")) continue;
                includes.put(file_lib.file_name, file_lib.content);
            }

            if(help.files != null) {
                for (Swagger_Library_Record user_file : help.files) {
                    includes.put(user_file.file_name, user_file.content);
                }
            }

            if (Controller_WebSocket.compiler_cloud_servers.isEmpty()) {
                return GlobalResult.result_externalServerIsOffline("Compilation cloud_compilation_server is offline!");
            }


            WS_Message_Make_compilation compilation_result = Model_CompilationServer.make_Compilation(new WS_Message_Make_compilation().make_request( typeOfBoard , help.library_compilation_version, "only_for_compilation", help.main, includes ));

            // V případě úspěšného buildu obsahuje příchozí JsonNode build_url
            if (compilation_result.build_url != null && compilation_result.status.equals("success")) {

                Swagger_Cloud_Compilation_Server_CompilationResult result = new Swagger_Cloud_Compilation_Server_CompilationResult();
                result.interface_code = compilation_result.interface_code;

                return GlobalResult.result_ok(Json.toJson(result));
            }

            // Kompilace nebyla úspěšná a tak vracím obsah neuspěšné kompilace
            if (!compilation_result.build_errors.isEmpty()) {

                return GlobalResult.result_buildErrors(Json.toJson(compilation_result.build_errors));
            }

            // Nebylo úspěšné ani odeslání requestu - Chyba v konfiguraci a tak vracím defaulní chybz
            if (compilation_result.error_message != null) {

                ObjectNode result_json = Json.newObject();
                result_json.put("error_message", compilation_result.error_message);

                return GlobalResult.result_externalServerError(result_json);
            }

            // Neznámá chyba se kterou nebylo počítání
            return GlobalResult.result_badRequest("Unknown error");
        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }

    }

    /**
     * TODO http://youtrack.byzance.cz/youtrack/issue/TYRION-503
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
            @ApiResponse(code = 477, message = "External Cloud_Homer_server where is hardware is offline", response = Result_ServerOffline.class),
            @ApiResponse(code = 404, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result uploadBinaryFileToBoard(@ApiParam(value = "version_id ", required = true) String board_id, @ApiParam(value = "version_id ", required = true) String firmware_type_string) {
        try {

            System.out.println("Body " + request().body().asText());

            // Vyhledání objektů
            Board board = Board.get_byId(board_id);
            if (board == null) return GlobalResult.result_notFound("Board board_id object not found");

            if (!board.update_permission()) return GlobalResult.result_forbidden();

            Firmware_type firmware_type = Firmware_type.getFirmwareType(firmware_type_string);
            if (firmware_type == null) return GlobalResult.result_notFound("FirmwareType not found!");

            // Přijmu soubor
            Http.MultipartFormData body = request().body().asMultipartFormData();

            List<Http.MultipartFormData.FilePart> files_from_request = body.getFiles();

            if (files_from_request == null || files_from_request.isEmpty())return GlobalResult.result_notFound("Bin File not found!");
            if (files_from_request.size() > 1)return GlobalResult.result_badRequest("More than one File is not allowed!");

            File file = files_from_request.get(0).getFile();
            if (file == null) return GlobalResult.result_badRequest("File not found!");
            if (file.length() < 1) return GlobalResult.result_badRequest("File is Empty!");


            int dot = files_from_request.get(0).getFilename().lastIndexOf(".");
            String file_type = files_from_request.get(0).getFilename().substring(dot);
            String file_name = files_from_request.get(0).getFilename().substring(0, dot);

            // Zkontroluji soubor
            if (!file_type.equals(".bin"))return GlobalResult.result_badRequest("Wrong type of File - \"Bin\" required! ");
            if ((file.length() / 1024) > 500)return GlobalResult.result_badRequest("File is bigger than 500K b");

            // Existuje Homer?

             String binary_file = FileRecord.get_encoded_binary_string_from_File(file);
             FileRecord fileRecord = FileRecord.create_Binary_file("byzance-private/binaryfiles", binary_file, file_name);
             Controller_Actualization.add_new_actualization_request_with_user_file(board.project, firmware_type, board, fileRecord);

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
            if (firmware_type == null) return GlobalResult.result_notFound("FirmwareType not found!");

            List<String> list = new ArrayList<>();
            list.add(board_id);

            // Přijmu soubor
            Http.MultipartFormData body = request().body().asMultipartFormData();

            List<Http.MultipartFormData.FilePart> files_from_request = body.getFiles();

            if (files_from_request == null || files_from_request.isEmpty())return GlobalResult.result_notFound("Bin File not found!");
            if (files_from_request.size() > 1)return GlobalResult.result_badRequest("More than one File is not allowed!");

            File file = files_from_request.get(0).getFile();
            if (file == null) return GlobalResult.result_badRequest("File not found!");
            if (file.length() < 1) return GlobalResult.result_badRequest("File is Empty!");


            int dot = files_from_request.get(0).getFilename().lastIndexOf(".");
            String file_type = files_from_request.get(0).getFilename().substring(dot);
            String file_name = files_from_request.get(0).getFilename().substring(0, dot);

            // Zkontroluji soubor
            if (!file_type.equals(".bin"))return GlobalResult.result_badRequest("Wrong type of File - \"Bin\" required! ");
            if ((file.length() / 1024) > 500)return GlobalResult.result_badRequest("File is bigger than 500K b");


            ObjectNode request = Json.newObject();
            request.put("message_channel", "tyrion");
            request.put("instance_id", instance_id);
            request.put("message_type", "updateDevice");
            request.put("firmware_type", firmware_type.get_firmwareType());
            request.set("targetIds",  Json.toJson(list));
            request.put("build_id", build_id);
            request.put("program", Model_FileRecord.get_encoded_binary_string_from_File(file));

            // ObjectNode result =  Controller_WebSocket.incomingConnections_homers.get(instance_id).write_with_confirmation(request, 1000*30, 0, 3);

            if(request.get("status").asText().equals("success")) {
                return GlobalResult.result_ok();
            }
            else {
                return GlobalResult.result_badRequest(request);
            }

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }
    */


    

///###################################################################################################################*/

    @ApiOperation(value = "create Processor",
            tags = {"Admin-Processor"},
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
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Processor.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result processor_create() {
        try {

            // Zpracování Json
            final Form<Swagger_Processor_New> form = Form.form(Swagger_Processor_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Processor_New help = form.get();

            // Vytvářím objekt
            Model_Processor processor = new Model_Processor();
            processor.description    = help.description;
            processor.processor_code = help.processor_code;
            processor.processor_name = help.processor_name;
            processor.speed          = help.speed;

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! processor.create_permission())  return GlobalResult.result_forbidden();

            // Ukládám objekt
            processor.save();

            // Vracím objekt
            return GlobalResult.result_created(Json.toJson(processor));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Processor",
            tags = {"Processor"},
            notes = "If you get Processor by query processor_id.",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",         response = Model_Processor.class),
            @ApiResponse(code = 404, message = "Object not found",  response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result processor_get(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            //Zkontroluji validitu
            Model_Processor processor = Model_Processor.get_byId(processor_id);
            if(processor == null ) return GlobalResult.result_notFound("Processor processor_id not found");

            // Vracím objekt
            return GlobalResult.result_ok(Json.toJson(processor));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Processor All",
            tags = {"Processor"},
            notes = "Get list of all Processor by query",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",         response = Model_Processor.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result processor_getAll() {
        try {

            //Vyhledám objekty
           List<Model_Processor> processors = Model_Processor.find.where().eq("removed_by_user", false).order().asc("processor_name").findList();

            // Vracím seznam objektů
           return GlobalResult.result_ok(Json.toJson(processors));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Processor",
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
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result processor_edit(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            // Zpracování Json
            Form<Swagger_Processor_New> form = Form.form(Swagger_Processor_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Processor_New help = form.get();

            // Kontroluji validitu
            Model_Processor processor = Model_Processor.get_byId(processor_id);
            if(processor == null ) return GlobalResult.result_notFound("Processor processor_id not found");

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if(! processor.edit_permission())  return GlobalResult.result_forbidden();

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
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Processor",
            tags = {"Admin-Processor"},
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result processor_delete(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            // Kontroluji validitu
            Model_Processor processor = Model_Processor.get_byId(processor_id);
            if (processor == null ) return GlobalResult.result_notFound("Processor not found");

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if (!processor.delete_permission()) return GlobalResult.result_forbidden();

            if (processor.type_of_boards.size() > 0) return GlobalResult.result_badRequest("Processor is assigned to some type of board, so cannot be deleted");

            // Mažu z databáze
            processor.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "get Bootloader FileRecord",
            tags = {"File", "Garfield"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_File_Content.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result fileRecord_get_bootLoader(@ApiParam(value = "file_record_id String query", required = true)  String bootloader_id){
        try {

            Model_BootLoader boot_loader = Model_BootLoader.find.byId(bootloader_id);
            if (boot_loader == null) return GlobalResult.result_notFound("BootLoader not found");

            if (!boot_loader.read_permission()) return GlobalResult.result_forbidden();

            // Swagger_File_Content - Zástupný dokumentační objekt
            Swagger_File_Content content = new Swagger_File_Content();
            content.file_in_base64 = boot_loader.file.get_fileRecord_from_Azure_inString();

            // Vracím content
            return GlobalResult.result_ok(Json.toJson(content));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get CProgram_Version FileRecord",
            tags = { "File" , "Garfield"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_File_Content.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result fileRecord_get_firmware(@ApiParam(value = "file_record_id String query", required = true)  String version_id){
        try {

            // Kontrola validity objektu
            Model_VersionObject versionObject = Model_VersionObject.find.byId(version_id);
            if (versionObject == null) return GlobalResult.result_notFound("FileRecord file_record_id not found");

            // Swagger_File_Content - Zástupný dokumentační objekt
            if (versionObject.c_program == null) return GlobalResult.result_badRequest();

            // Kontrola oprávnění
            if (!versionObject.c_program.read_permission()) return GlobalResult.result_badRequest();

            // Swagger_File_Content - Zástupný dokumentační objekt
            Swagger_File_Content content = new Swagger_File_Content();
            content.file_in_base64 = versionObject.c_compilation.bin_compilation_file.get_fileRecord_from_Azure_inString();

            // Vracím content
            return GlobalResult.result_ok(Json.toJson(content));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create Producer",
            tags = {"Admin-Producer"},
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
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Producer.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result producer_create() {
        try {

            // Zpracování Json
            final Form<Swagger_Producer_New> form = Form.form(Swagger_Producer_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Producer_New help = form.get();

            //Vytvářím objekt
            Model_Producer producer = new Model_Producer();
            producer.name = help.name;
            producer.description = help.description;

            // Kontorluji oprávnění těsně před uložením
            if(! producer.create_permission()) return GlobalResult.result_forbidden();

            //Ukládám objekt
            producer.save();

            // Vracím objekt
            return GlobalResult.result_created(Json.toJson(producer));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Producer",
            tags = {"Admin-Producer"},
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Producer.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result producer_update(String producer_id) {
        try {

            // Zpracování Json
            final Form<Swagger_Producer_New> form = Form.form(Swagger_Producer_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Producer_New help = form.get();

            // Kontrola objektu
            Model_Producer producer = Model_Producer.get_byId(producer_id);
            if(producer == null ) return GlobalResult.result_notFound("Producer producer_id not found");

            // Kontorluji oprávnění těsně před uložením
            if(! producer.edit_permission()) return GlobalResult.result_forbidden();

            // Úprava objektu
            producer.name = help.name;
            producer.description = help.description;

            // Uložení změn objektu
            producer.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(producer));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Producers All",
            tags = {"Producer"},
            notes = "if you want get list of Producers. Its list of companies owned physical boards and we used that for filtering",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Producer.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result producer_getAll() {
        try {

            // Získání seznamu
            List<Model_Producer> producers = Model_Producer.find.where().eq("removed_by_user", false).order().asc("name").findList();

            // Vrácení seznamu
            return GlobalResult.result_ok(Json.toJson(producers));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Producer",
            tags = {"Producer"},
            notes = "if you want get Producer. Its company owned physical boards and we used that for filtering",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Producer.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result producer_get( String producer_id) {
        try {

            // Kontrola objektu
            Model_Producer producer = Model_Producer.get_byId(producer_id);
            if(producer == null ) return GlobalResult.result_notFound("Producer producer_id not found");

            // Vrácneí objektu
            return GlobalResult.result_ok(Json.toJson(producer));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Producer",
            tags = {"Admin-Producer"},
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result producer_delete(String producer_id) {
        try {

            // Kontrola objektu
            Model_Producer producer = Model_Producer.get_byId(producer_id);
            if (producer == null) return GlobalResult.result_notFound("Producer not found");

            // Kontorluji oprávnění
            if (!producer.delete_permission()) return GlobalResult.result_forbidden();

            if (producer.type_of_boards.size() > 0 || producer.blocko_blocks.size() > 0 || producer.grid_widgets.size() > 0)
                return GlobalResult.result_badRequest("Producer is assigned to some objects, so cannot be deleted.");

            // Smazání objektu
            producer.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "The TypeOfBoard is category for IoT. Like Raspberry2, Arduino-Uno etc. \n\n" +
                    "We using that for compilation, sorting libraries, filtres and more..",
            produces = "application/json",
            protocols = "https",
            code = 201
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
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_TypeOfBoard.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBoard_create() {
        try {

            // Zpracování Json
            final Form<Swagger_TypeOfBoard_New> form = Form.form(Swagger_TypeOfBoard_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_TypeOfBoard_New help = form.get();

            // Kontrola objektu
            Model_Producer producer = Model_Producer.get_byId(help.producer_id);
            if(producer == null ) return GlobalResult.result_notFound("Producer producer_id not found");

            // Kontrola objektu
            Model_Processor processor = Model_Processor.get_byId(help.processor_id);
            if(processor == null ) return GlobalResult.result_notFound("Processor processor_id not found");

            // Tvorba objektu
            Model_TypeOfBoard typeOfBoard = new Model_TypeOfBoard();
            typeOfBoard.name = help.name;
            typeOfBoard.description = help.description;
            typeOfBoard.compiler_target_name = help.compiler_target_name;
            typeOfBoard.processor = processor;
            typeOfBoard.producer = producer;
            typeOfBoard.connectible_to_internet = help.connectible_to_internet;

            // Kontorluji oprávnění
            if(!typeOfBoard.create_permission()) return GlobalResult.result_forbidden();

            // Uložení objektu do DB
            typeOfBoard.save();

            // Vytvoříme defaultní C_Program pro snížení počtu kroků pro nastavení desky
            Model_CProgram c_program = new Model_CProgram();
            c_program.name =  typeOfBoard.name + " default program";
            c_program.description = "Default program for this device type";
            c_program.type_of_board_default = typeOfBoard;
            c_program.type_of_board =  typeOfBoard;
            c_program.publish_type  = Enum_Publishing_type.default_main_program;
            c_program.save();

            typeOfBoard.refresh();

            // Vytvoříme testovací C_Program pro snížení počtu kroků pro nastavení desky
            Model_CProgram c_program_test = new Model_CProgram();
            c_program_test.name =  typeOfBoard.name + " test program";
            c_program_test.description = "Test program for this device type";
            c_program_test.type_of_board_test = typeOfBoard;
            c_program_test.type_of_board =  typeOfBoard;
            c_program_test.publish_type  = Enum_Publishing_type.default_test_program;
            c_program_test.save();

            typeOfBoard.refresh();

            // TODO přidat do cache

            return GlobalResult.result_created(Json.toJson(typeOfBoard));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_TypeOfBoard.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBoard_update( String type_of_board_id) {
        try {

            // Zpracování Json
            final Form<Swagger_TypeOfBoard_New> form = Form.form(Swagger_TypeOfBoard_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_TypeOfBoard_New help = form.get();

            // Kontrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.get_byId(type_of_board_id);
            if (typeOfBoard == null) return GlobalResult.result_notFound("TypeOfBoard type_of_board_id not found");

            // Kontrola objektu
            Model_Producer producer = Model_Producer.get_byId(help.producer_id);
            if(producer == null ) return GlobalResult.result_notFound("Producer producer_id not found");

            // Kontrola objektu
            Model_Processor processor = Model_Processor.get_byId(help.processor_id);
            if(processor == null ) return GlobalResult.result_notFound("Processor processor_id not found");

            // Kontorluji oprávnění
            if(! typeOfBoard.edit_permission()) return GlobalResult.result_forbidden();

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
            return ServerLogger.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBoard_delete( String type_of_board_id) {
        try {

            // Kontrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.get_byId(type_of_board_id);
            if(typeOfBoard == null ) return GlobalResult.result_notFound("TypeOfBoard type_of_board_id not found") ;

            // Kontorluji oprávnění
            if(! typeOfBoard.delete_permission()) return GlobalResult.result_forbidden();

            // Smazání objektu
            typeOfBoard.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get TypeOfBoards All",
            tags = { "Type-Of-Board"},
            notes = "if you want get all TypeOfBoard objects",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_TypeOfBoard.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBoard_getAll() {
        try {

            // TODO dá se cachovat - Pozor stejný seznam se nachází i Job_CheckCompilationLibraries
            // Získání seznamu
            // To co jsem tady napsal jen filtruje tahá ručně desky z cache pojendom - možná by šlo někde mít statické pole ID třeba
            // přímo v objektu Model_TypeOfBoard DB ignor a to používat a aktualizovat a statické pole nechat na samotné jave, aby si ji uchavaala v pam,ěti
            List<Model_TypeOfBoard> typeOfBoards_not_cached = Model_TypeOfBoard.find.where().eq("removed_by_user", false).orderBy("UPPER(name) ASC").select("id").findList();

            List<Model_TypeOfBoard> typeOfBoards = new ArrayList<>();

            for(Model_TypeOfBoard typeOfBoard_not_cached : typeOfBoards_not_cached ) {
                typeOfBoards.add(Model_TypeOfBoard.get_byId(typeOfBoard_not_cached.id));
            }


            // Vrácení seznamu
            return  GlobalResult.result_ok(Json.toJson(typeOfBoards));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want get TypeOfBoard object by query = type_of_board_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_TypeOfBoard.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBoard_get( String type_of_board_id) {
        try {

            // Kontrola validity objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.get_byId(type_of_board_id);
            if(typeOfBoard == null ) return GlobalResult.result_notFound("TypeOfBoard type_of_board_id not found");

            // Kontorluji oprávnění
            if(! typeOfBoard.read_permission()) return GlobalResult.result_forbidden();

            // Vrácení validity objektu
            return GlobalResult.result_ok(Json.toJson(typeOfBoard));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "upload TypeOfBoard picture",
            tags = { "Admin-Type-Of-Board"},
            notes = "Upload TypeOfBoard picture",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.documentationClass.Swagger_BASE64_FILE",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(value = BodyParser.Json.class, maxLength = 1024 * 1024 * 10)
    public Result typeOfBoard_uploadPicture(String type_of_board_id){
        try {

            // Získání JSON
            final Form<Swagger_BASE64_FILE> form = Form.form(Swagger_BASE64_FILE.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_BASE64_FILE help = form.get();

            Model_TypeOfBoard type_of_board = Model_TypeOfBoard.get_byId(type_of_board_id);
            if (type_of_board == null) return GlobalResult.result_notFound("Type of board does not exist");


            if(!type_of_board.edit_permission()) return GlobalResult.result_forbidden();

            terminal_logger.debug("typeOfBoard_uploadPicture update picture ");

            type_of_board.cache_value_picture_link = null;

            // Odebrání předchozího obrázku
            if(!(type_of_board.picture == null)){

                terminal_logger.debug("typeOfBoard_uploadPicture picture is already there - system remove previous photo");
                Model_FileRecord fileRecord = type_of_board.picture;
                type_of_board.picture = null;
                type_of_board.update();
                fileRecord.delete();
            }

            //  data:image/png;base64,
            String[] parts = help.file.split(",");
            String[] type = parts[0].split(":");
            String[] dataType = type[1].split(";");

            terminal_logger.debug("typeOfBoard_uploadPicture:: Type     :: " + dataType[0]);
            terminal_logger.debug("typeOfBoard_uploadPicture:: Data     :: " + parts[1].substring(0, 10) + "......");

            String file_name =  UUID.randomUUID().toString() + ".png";
            String file_path =  type_of_board.get_Container().getName() + "/" + file_name;

            terminal_logger.debug("typeOfBoard_uploadPicture:: File Name:: " + file_name );
            terminal_logger.debug("typeOfBoard_uploadPicture:: File Path:: " + file_path );

            type_of_board.picture  = Model_FileRecord.uploadAzure_File( parts[1], dataType[0], file_name , file_path);
            type_of_board.update();


            return GlobalResult.result_ok("Picture successfully uploaded");
        }catch (Exception e){
            return ServerLogger.result_internalServerError(e, request());
        }
    }

// Type Of Board - Batch ###############################################################################################

    @ApiOperation(value = "create TypeOfBoardBatch",
            tags = { "Type-Of-Board"},
            notes = "Create new Production Batch for Type Of Board",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfBoardBatch_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_TypeOfBoard_Batch.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBoardBatch_create(String type_of_board_id) {
        try {

            // Zpracování Json
            final Form<Swagger_TypeOfBoardBatch_New> form = Form.form(Swagger_TypeOfBoardBatch_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_TypeOfBoardBatch_New help = form.get();

            // Kontrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.get_byId(type_of_board_id);
            if(typeOfBoard == null ) return GlobalResult.result_notFound("Model_TypeOfBoard type_of_board_id not found");

            // Tvorba objektu
            Model_TypeOfBoard_Batch batch = new Model_TypeOfBoard_Batch();
            batch.type_of_board = typeOfBoard;

            batch.revision = help.revision;
            batch.production_batch = help.production_batch;

            batch.date_of_assembly = help.date_of_assembly;

            batch.pcb_manufacture_name = help.pcb_manufacture_name;
            batch.pcb_manufacture_id = help.pcb_manufacture_id;

            batch.assembly_manufacture_name = help.assembly_manufacture_name;
            batch.assembly_manufacture_id = help.assembly_manufacture_id;

            batch.customer_product_name = help.customer_product_name;

            batch.customer_company_name = help.customer_company_name;
            batch.customer_company_made_description = help.customer_company_made_description;

            batch.mac_address_start = help.mac_address_start;
            batch.mac_address_end = help.mac_address_end;

            batch.ean_number = help.ean_number;

            // Kontorluji oprávnění
            if(!batch.create_permission()) return GlobalResult.result_forbidden();

            // Uložení objektu do DB
            batch.save();

            return GlobalResult.result_created(Json.toJson(batch));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete TypeOfBoardBatch",
            tags = { "Type-Of-Board"},
            notes = "if you want delete TypeOfBoard Batch object by query = type_of_board_id",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBoardBatch_delete( String type_of_board_batch_id) {
        try {

            // Kontrola objektu
            Model_TypeOfBoard_Batch batch = Model_TypeOfBoard_Batch.get_byId(type_of_board_batch_id);
            if(batch == null ) return GlobalResult.result_notFound("Model_TypeOfBoard_Batch type_of_board_batch_id not found") ;

            // Kontorluji oprávnění
            if(! batch.delete_permission()) return GlobalResult.result_forbidden();

            // Smazání objektu
            batch.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit TypeOfBoardBatch",
            tags = { "Type-Of-Board"},
            notes = "Create new Production Batch for Type Of Board",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfBoardBatch_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_TypeOfBoard_Batch.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBoardBatch_edit(String type_of_board_id) {
        try {

            // Zpracování Json
            final Form<Swagger_TypeOfBoardBatch_New> form = Form.form(Swagger_TypeOfBoardBatch_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_TypeOfBoardBatch_New help = form.get();

            // Kontrola objektu
            Model_TypeOfBoard_Batch batch = Model_TypeOfBoard_Batch.get_byId(type_of_board_id);
            if(batch == null ) return GlobalResult.result_notFound("Model_TypeOfBoard type_of_board_id not found");

            // Tvorba objektu
            batch.revision = help.revision;
            batch.production_batch = help.production_batch;

            batch.date_of_assembly = help.date_of_assembly;

            batch.pcb_manufacture_name = help.pcb_manufacture_name;
            batch.pcb_manufacture_id = help.pcb_manufacture_id;

            batch.assembly_manufacture_name = help.assembly_manufacture_name;
            batch.assembly_manufacture_id = help.assembly_manufacture_id;

            batch.customer_product_name = help.customer_product_name;

            batch.customer_company_name = help.customer_company_name;
            batch.customer_company_made_description = help.customer_company_made_description;

            batch.mac_address_start = help.mac_address_start;
            batch.mac_address_end = help.mac_address_end;

            batch.ean_number = help.ean_number;

            // Kontorluji oprávnění
            if(!batch.create_permission()) return GlobalResult.result_forbidden();

            // Uložení objektu do DB
            batch.save();

            return GlobalResult.result_created(Json.toJson(batch));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

// BootLoader ##########################################################################################################

    @ApiOperation(value = "create Bootloader",
            tags = { "Admin-Type-Of-Board"},
            notes = "Create picture from TypeOfBoard",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.documentationClass.Swagger_BootLoader_New",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_BootLoader.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bootLoader_create(@ApiParam(value = "type_of_board_id", required = true) String type_of_board_id) {
        try {

            // Zpracování Json
            final Form<Swagger_BootLoader_New> form = Form.form(Swagger_BootLoader_New.class).bindFromRequest();
            if(form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_BootLoader_New help = form.get();

            Model_TypeOfBoard type_of_board = Model_TypeOfBoard.get_byId(type_of_board_id);
            if(type_of_board == null) return GlobalResult.result_notFound("Type_of_board_not_found");

            String identifier = help.version_identificator.replaceAll("\\s+", "");

            if (Model_BootLoader.find.where().eq("version_identificator", identifier).eq("type_of_board.id", type_of_board.id).findUnique() != null)
                return GlobalResult.result_badRequest("Version format is not unique!");

            Model_BootLoader boot_loader = new Model_BootLoader();
            boot_loader.date_of_create = new Date();
            boot_loader.name = help.name;
            boot_loader.changing_note =  help.changing_note;
            boot_loader.description = help.description;
            boot_loader.version_identificator = identifier;
            boot_loader.type_of_board = type_of_board;

            if(!boot_loader.create_permission()) return GlobalResult.result_forbidden();
            boot_loader.save();

            // Vracím seznam
            return GlobalResult.result_ok(Json.toJson(boot_loader));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Bootloader",
            tags = { "Admin-Type-Of-Board"},
            notes = "Create picture from TypeOfBoard",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.documentationClass.Swagger_BootLoader_Edit",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_BootLoader.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bootLoader_update(@ApiParam(value = "boot_loader_id", required = true) String boot_loader_id) {
        try {

            // Zpracování Json
            final Form<Swagger_BootLoader_Edit> form = Form.form(Swagger_BootLoader_Edit.class).bindFromRequest();
            if(form.hasErrors())return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_BootLoader_Edit help = form.get();

            Model_BootLoader boot_loader = Model_BootLoader.find.byId(boot_loader_id);
            if (boot_loader == null) return GlobalResult.result_notFound("BootLoader not found");

            if (!boot_loader.edit_permission()) return GlobalResult.result_forbidden();

            boot_loader.name = help.name;
            boot_loader.changing_note = help.changing_note;
            boot_loader.description = help.description;
            boot_loader.version_identificator = help.version_identificator;

            boot_loader.update();

            return GlobalResult.result_ok(Json.toJson(boot_loader));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Bootloader",
            tags = { "Admin-Type-Of-Board"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bootLoader_delete(String boot_loader_id) {
        try {

            Model_BootLoader boot_loader = Model_BootLoader.find.byId(boot_loader_id);
            if (boot_loader == null) return GlobalResult.result_notFound("BootLoader not found");

            if (!boot_loader.delete_permission()) return GlobalResult.result_forbidden();

            if (!boot_loader.boards.isEmpty()) return GlobalResult.result_badRequest("Bootloader is already used on some Board. Cannot be deleted.");

            boot_loader.delete();

            return GlobalResult.result_ok(Json.toJson(boot_loader));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "upload Bootloader File",
            tags = {"Admin-Bootloader"},
            notes = "",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.documentationClass.Swagger_BASE64_FILE",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body", response = Result_InvalidBody.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(value = BodyParser.Json.class, maxLength = 1024 * 1024 * 5)
    public Result bootLoader_uploadFile(String boot_loader_id) {
        try {

            // Získání JSON
            final Form<Swagger_BASE64_FILE> form = Form.form(Swagger_BASE64_FILE.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_BASE64_FILE help = form.get();

            Model_BootLoader boot_loader = Model_BootLoader.get_byId(boot_loader_id);
            if(boot_loader == null) return GlobalResult.result_notFound("BootLoader boot_loader_id not found");

            if (!boot_loader.edit_permission()) return GlobalResult.result_forbidden();

            //  data:image/png;base64,
            String[] parts = help.file.split(",");
            String[] type = parts[0].split(":");
            String[] content_type = type[1].split(";");
            String dataType = content_type[0].split("/")[1];

            terminal_logger.debug("bootLoader_uploadFile:: Cont Type:" + content_type[0]);
            terminal_logger.debug("bootLoader_uploadFile:: Data Type:" + dataType);
            terminal_logger.debug("bootLoader_uploadFile:: Data: " + parts[1].substring(0, 10) + "......");

            if (boot_loader.file != null) {
                boot_loader.file.delete();
            }

            String file_name =  UUID.randomUUID().toString() + "." + "bin";
            String file_path =  boot_loader.get_Container().getName() + "/" +file_name;

            terminal_logger.debug("bootLoader_uploadFile::  File Name " + file_name );
            terminal_logger.debug("bootLoader_uploadFile::  File Path " + file_path );

            boot_loader.file = Model_FileRecord.uploadAzure_File( parts[1], content_type[0], file_name, file_path);
            boot_loader.update();

            // Nefungovalo to korektně občas - tak se to ukládá oboustraně!
            boot_loader.file.boot_loader = boot_loader;
            boot_loader.file.update();

            boot_loader.refresh();

            // Vracím seznam
            return GlobalResult.result_ok(Json.toJson(boot_loader));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Bootloader Set as Main",
                tags = {"Admin-Bootloader"},
                notes = "List of Hardware Id for update on latest bootloader version (system used latest bootloader for type of hardware)",
                produces = "application/json",
                protocols = "https",
                code = 200
            )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body", response = Result_InvalidBody.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Empty.class)
    public Result bootLoader_markAsMain(String boot_loader_id) {
        try {

            Model_BootLoader boot_loader = Model_BootLoader.get_byId(boot_loader_id);
            if(boot_loader == null) return GlobalResult.result_notFound("BootLoader boot_loader_id not found");

            if(!boot_loader.edit_permission()) return GlobalResult.result_forbidden();
            if(boot_loader.file == null) return GlobalResult.result_badRequest("Required bootloader object with file");

            if(boot_loader.get_main_type_of_board() != null) return GlobalResult.result_badRequest("Bootloader is Already Main");


            Model_BootLoader old_main = Model_BootLoader.find.where().eq("main_type_of_board.id", boot_loader.type_of_board.id).findUnique();
            if(old_main != null){

                old_main.main_type_of_board = null;
                old_main.cache_value_main_type_of_board_id = null;
                old_main.update();

            }

            boot_loader.main_type_of_board = boot_loader.get_type_of_board();
            boot_loader.cache_value_main_type_of_board_id =  boot_loader.main_type_of_board.id;
            boot_loader.update();

            // Vymažu Device Cache
            Model_Board.cache.clear();


            // Vracím Json
            return GlobalResult.result_ok(Json.toJson(boot_loader));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update Hardware Bootloader",
            tags = {"Bootloader"},
            notes = "List of Hardware Id for update on latest bootloader version (system used latest bootloader for type of hardware)",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_Bootloader_Update",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bootLoader_manualUpdate(){
        try {

            // Zpracování Json
            final Form<Swagger_Board_Bootloader_Update > form = Form.form(Swagger_Board_Bootloader_Update.class).bindFromRequest();
            if(form.hasErrors()){return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Board_Bootloader_Update help = form.get();


            List<Model_Board> boards = Model_Board.find.where().in("id", help.device_ids).findList();
            if(boards.isEmpty()) return GlobalResult.result_notFound("Board not found");



            List<WS_Help_Hardware_Pair> board_for_update = new ArrayList<>();

            for(Model_Board board : boards) {

                if (!board.read_permission()) return GlobalResult.result_forbidden("You have no permission for Device " + board.id);

                WS_Help_Hardware_Pair pair = new WS_Help_Hardware_Pair();
                pair.board = board;

                if(help.bootloader_id != null) {

                    pair.bootLoader = Model_BootLoader.get_byId(help.bootloader_id);
                    if (pair.bootLoader == null) return GlobalResult.result_notFound("BootLoader not found");

                } else{
                    pair.bootLoader = Model_BootLoader.find.where().eq("main_type_of_board.boards.id", board.id).findUnique();
                }

                board_for_update.add(pair);
            }

            System.out.println("Velikost pole:: "+ board_for_update.size());

            if(!board_for_update.isEmpty()){
                new Thread( () -> {
                    try {

                        Model_ActualizationProcedure procedure = Model_Board.create_update_procedure(Enum_Firmware_type.BOOTLOADER, Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL, board_for_update);
                        procedure.execute_update_procedure();

                    } catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }).start();
            }


            // Vracím Json
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    ///###################################################################################################################*/

    @ApiOperation(value = "create Board manual Registration",
            tags = { "Admin-Board"},
            notes = "This Api is using only for developing mode, for registration of our Board - in future it will be used only by machine in factory or " +
                    "boards themselves with \"registration procedure\". Hardware is not allowed to delete! Only deactivate. Classic User can only register that to own " +
                    "project or own to account",
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_New_Manual",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Board.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_create_manual() {
        try {

            // Zpracování Json
            final Form<Swagger_Board_New_Manual> form = Form.form(Swagger_Board_New_Manual.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_Board_New_Manual help = form.get();

            // Kotrola objektu
            if (Model_Board.find.byId(help.full_id) != null) return GlobalResult.result_badRequest("Board is already registered");

            // Kotrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.get_byId( help.type_of_board_id  );
            if(typeOfBoard == null ) return GlobalResult.result_notFound("TypeOfBoard type_of_board_id not found");

            // Kontorluji oprávnění
            if (!typeOfBoard.register_new_device_permission()) return GlobalResult.result_forbidden();

            Model_Board board = new Model_Board();
            board.id = help.full_id;
            board.is_active = false;
            board.date_of_create = new Date();
            board.type_of_board = typeOfBoard;

            // Uložení desky do DB
            board.save();

            // Vracím seznam zařízení k registraci
            return GlobalResult.result_created(Json.toJson(board));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "create Board automatic Garfield",
            tags = { "Admin-Board"},
            notes = "This Api is using for Board automatic registration adn Testing. Hardware is not allowed to delete! Only deactivate. Classic User can only register that to own " +
                    "project or own to account",
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_New_Garfield",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Swagger_Hardware_New_Settings_Result.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_create_garfield() {
        try {

            // Zpracování Json
            final Form<Swagger_Board_New_Garfield> form = Form.form(Swagger_Board_New_Garfield.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_Board_New_Garfield help = form.get();

            // Kotrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.get_byId(help.type_of_board_id);
            if (typeOfBoard == null) return GlobalResult.result_notFound("TypeOfBoard type_of_board_id not found");

            // Kontorluji oprávnění
            if (!typeOfBoard.register_new_device_permission()) return GlobalResult.result_forbidden();

            Model_TypeOfBoard_Batch batch = Model_TypeOfBoard_Batch.get_byId(help.type_of_board_batch_id);
            if (batch == null) return GlobalResult.result_notFound("TypeOfBoard_Batch type_of_board_batch_id not found");

            // Kontrola Objektu
            Model_Garfield garfiled = Model_Garfield.get_byId(help.garfield_station_id);
            if (garfiled == null) return GlobalResult.result_notFound("Garfield Station not found");

            Model_Board board = Model_Board.find.byId(help.full_id);

            // Pokud neexistuje vytvořím
            if (board == null) {
                board = new Model_Board();
                board.id = help.full_id;
                board.is_active = false;
                board.date_of_create = new Date();
                board.type_of_board = typeOfBoard;
                board.batch_id = batch.id.toString();
                board.mac_address = batch.get_new_MacAddress();
                board.hash_for_adding = Model_Board.generate_hash();


                if(Hardware_Registration_Authority.register_device(board, typeOfBoard, batch)){
                    board.save();
                }else {
                    Model_Board board_repair_from_authority = Model_Board.find.byId(help.full_id);
                    if(board_repair_from_authority != null) {
                        board = board_repair_from_authority;
                    }else {
                       return GlobalResult.result_notFound("Registration Authority Fail!!");
                    }
                }

                board.refresh();
            }

            // Vytisknu štítky

            Printer_Api api = new Printer_Api();

            // Label 62 mm
            try{
                // Test for creating - Controlling all prerequisites and requirements
                new Label_62_mm_package(board, batch, garfiled);
            }catch (IllegalArgumentException e){
                return GlobalResult.badRequest("Something is wrong: " + e.getMessage());
            }

            Label_62_mm_package label_62_mmPackage = new Label_62_mm_package(board, batch, garfiled);
            api.printFile(garfiled.print_sticker_id, 1, "Garfield Print Label", label_62_mmPackage.get_label(), null);

            // Label qith QR kode on Ethernet connector
            Label_62_split_mm_Details label_12_mm_details = new Label_62_split_mm_Details(board);
            api.printFile(garfiled.print_label_id_1, 1, "Garfield Print QR Hash", label_12_mm_details.get_label(), null);


            if (typeOfBoard.connectible_to_internet) {

                // Najdu backup_server
                Model_HomerServer backup_server = Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.backup_server).findUnique();
                if (backup_server == null) return GlobalResult.result_notFound("Backup server not found!!!");

                // Najdu Main_server
                Model_HomerServer main_server = Model_HomerServer.find.where().eq("server_type", Enum_Cloud_HomerServer_type.main_server).findUnique();
                if (main_server == null) return GlobalResult.result_notFound("Main server not found!!!");

                Swagger_Hardware_New_Settings_Result result = new Swagger_Hardware_New_Settings_Result();
                result.full_id = board.id;
                result.normal_mqtt_hostname = main_server.server_url;
                result.normal_mqtt_port = main_server.mqtt_port;
                result.normal_mqtt_username = main_server.mqtt_username;
                result.normal_mqtt_password = main_server.mqtt_password;

                result.backup_mqtt_hostname = backup_server.server_url;
                result.backup_mqtt_port = backup_server.mqtt_port;
                result.backup_mqtt_username = backup_server.mqtt_username;
                result.backup_mqtt_password = backup_server.mqtt_password;

                result.mac_address = board.mac_address;

                return GlobalResult.result_created(Json.toJson(result));
            }

            // Vracím seznam zařízení k registraci
            return GlobalResult.result_created(Json.toJson(board));
        } catch (IllegalCharsetNameException e) {
            return GlobalResult.result_badRequest("All Mac Address used");
        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Boards for Ide Operation",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Board_for_fast_upload_detail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result board_getForFastUpload( String project_id){
        try {

            // Kotrola objektu
            Model_Project project = Model_Project.get_byId(project_id);
            if(project == null ) return GlobalResult.result_notFound("Project project not found");

            // Kontrola oprávnění
            if(!project.edit_permission()) return GlobalResult.result_forbidden();

            // Vyhledání seznamu desek na které lze nahrát firmware - okamžitě
            List<Model_Board> boards = Model_Board.find.where().eq("type_of_board.connectible_to_internet", true).eq("project.id", project_id).findList();

            List<Swagger_Board_for_fast_upload_detail> list = new ArrayList<>();

            for(Model_Board board : boards ){
                list.add(board.get_short_board_for_fast_upload());
            }


            // Vrácení upravenéh objektu
            return GlobalResult.result_ok(Json.toJson(list));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Board personal description",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Board.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_update_description( String board_id){
        try {

            // Zpracování Json
            final Form<Swagger_Board_Personal> form = Form.form(Swagger_Board_Personal.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Board_Personal help = form.get();

            // Kotrola objektu
            Model_Board board = Model_Board.get_byId(board_id);
            if(board == null ) return GlobalResult.result_notFound("Board board_id not found");

            // Kontrola oprávnění
            if(!board.edit_permission()) return GlobalResult.result_forbidden();

            // Uprava desky
            board.name = help.name;
            board.description = help.description;

            // Uprava objektu v databázi
            board.update();

            // Synchronizace s Homer serverem
            board.set_alias(board.name);

            // Vrácení upravenéh objektu
            return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Board developers parameters",
            tags = { "Board"},
            notes = "Edit Developers parameters [developer_kit, database_synchronize, web_view, web_port]",
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_Developer_parameters",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Board.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_update_parameters( String board_id){
        try {

            // Zpracování Json
            final Form<Swagger_Board_Developer_parameters> form = Form.form(Swagger_Board_Developer_parameters.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Board_Developer_parameters help = form.get();

            // Kotrola objektu
            Model_Board board = Model_Board.get_byId(board_id);
            if(board == null ) return GlobalResult.result_notFound("Board board_id not found");

            // Kontrola oprávnění
            if(!board.edit_permission()) return GlobalResult.result_forbidden();


            switch (help.parameter_type){

                case "developer_kit" :{

                    // Synchronizace s Homer serverem a databází
                    board.developer_kit = help.boolean_value;
                    board.update();
                    break;
                }

                case "database_synchronize" :{
                    // Synchronizace s Homer serverem a databází
                    board.set_database_synchronize(help.boolean_value);
                    break;
                }

                case "web_view" :{
                    // Synchronizace s Homer serverem a databázíw
                    board.set_web_view(help.boolean_value);
                    break;
                }

                case "web_port" :{
                    // Synchronizace s Homer serverem a databází
                    if(help.integer_value < 2001 && help.integer_value > 9999) return GlobalResult.result_badRequest("The port must be between 2001 and 9999. We also recommend not using commonly used ports such as Postgres 5432 and etc ..");
                    if(help.integer_value == 8502 ) return GlobalResult.result_badRequest("The port is used by some other entity in the system."); // Zde hlídáme aby nedošlo ke kolizím na portech, které má homer server
                    if(help.integer_value == 8501 ) return GlobalResult.result_badRequest("The port is used by some other entity in the system."); // Zde hlídáme aby nedošlo ke kolizím na portech, které má homer server
                    board.set_web_port(help.integer_value);
                    break;
                }

                default: {
                    terminal_logger.warn("parameter_type" + help.parameter_type + "not recognized");
                    return GlobalResult.result_notFound("parameter_type not recognized");
                }

            }

            // Vrácení upravenéh objektu
            return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "upload C_Program into Hardware",
            tags = {"C_Program", "Board", "Actualization"},
            notes = "Upload compilation to list of hardware. Compilation is on Version oc C_Program. And before uplouding compilation, you must succesfuly compile required version before! " +
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result uploadCompilationToBoard() {
        try {

            // Zpracování Json
            Form<Swagger_UploadBinaryFileToBoard> form = Form.form(Swagger_UploadBinaryFileToBoard.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_UploadBinaryFileToBoard help = form.get();


            List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

            if(help.board_pairs.isEmpty()) return GlobalResult.result_badRequest("List is Empty");

            for(Swagger_Board_CProgram_Pair board_update_pair : help.board_pairs) {

                // Ověření objektu
                Model_VersionObject c_program_version = Model_VersionObject.get_byId(board_update_pair.c_program_version_id);
                if (c_program_version == null) return GlobalResult.result_notFound("Version_Object version_id not found");

                //Zkontroluji validitu Verze zda sedí k C_Programu
                if (c_program_version.c_program == null) return GlobalResult.result_badRequest("Version_Object its not version of C_Program");

                // Zkontroluji oprávnění
                if (!c_program_version.c_program.read_permission()) return GlobalResult.result_forbidden();

                //Zkontroluji validitu Verze zda sedí k C_Programu
                if (c_program_version.c_compilation == null) return GlobalResult.result_badRequest("Version_Object its not version of C_Program - Missing compilation File");

                // Ověření zda je kompilovatelná verze a nebo zda kompilace stále neběží
                if (c_program_version.c_compilation.status != Enum_Compile_status.successfully_compiled_and_restored) return GlobalResult.result_badRequest("You cannot upload code in state:: " + c_program_version.c_compilation.status.name());

                //Zkontroluji zda byla verze už zkompilována
                if (!c_program_version.c_compilation.status.name().equals(Enum_Compile_status.successfully_compiled_and_restored.name())) return GlobalResult.result_badRequest("The program is not yet compiled & Restored");

                // Kotrola objektu
                Model_Board board = Model_Board.get_byId(board_update_pair.board_id);
                if (board == null) return GlobalResult.result_notFound("Board board_id not found");

                // Kontrola oprávnění
                if (!board.edit_permission()) return GlobalResult.result_forbidden();


                WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                b_pair.board = board;
                b_pair.c_program_version = c_program_version;

                b_pairs.add(b_pair);

            }

            if(!b_pairs.isEmpty()){
                new Thread( () -> {
                    try {

                        Model_ActualizationProcedure procedure = Model_Board.create_update_procedure(Enum_Firmware_type.FIRMWARE, Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL, b_pairs);
                        procedure.execute_update_procedure();

                    } catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }).start();
            }


            // Vracím odpověď
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update Board Backup",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_updateBackup(){
        try {

            // Zpracování Json
            final Form<Swagger_Board_Backup_settings> form = Form.form(Swagger_Board_Backup_settings.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Board_Backup_settings help = form.get();

            if(help.board_backup_pair_list.isEmpty()) return GlobalResult.result_notFound("List is Empty");


            // Seznam Hardwaru k updatu
            List<WS_Help_Hardware_Pair> board_pairs = new ArrayList<>();

            for(Swagger_Board_Backup_settings.Board_backup_pair board_backup_pair : help.board_backup_pair_list) {

                // Kotrola objektu
                Model_Board board = Model_Board.get_byId(board_backup_pair.board_id);
                if (board == null) return GlobalResult.result_notFound("Board board_id not found");

                // Kontrola oprávnění
                if (!board.edit_permission()) return GlobalResult.result_forbidden();

                // Pokud je nastaven autobackup na true
                if(board_backup_pair.backup_mode) {


                    // Na devicu byla nastavená statická - Proto je potřeba jí odstranit a nahradit autobackupem
                    if(!board.backup_mode) {
                        terminal_logger.debug("Controller_Board:: board_update_backup:: To TRUE:: Board Id: {} has own Static Backup - Removing static backup procedure required", board_backup_pair.board_id);

                        board.actual_backup_c_program_version = null;
                        board.backup_mode = true;
                        board.update();

                        WS_Message_Hardware_set_settings result = board.set_auto_backup();

                    // Na devicu už autobackup zapnutý byl - nic nedělám jen překokontroluji???
                    }else {

                        terminal_logger.debug("Controller_Board:: board_update_backup:: To TRUE:: Board Id: {} has already sat as a dynamic Backup", board_backup_pair.board_id);

                        WS_Message_Hardware_set_settings result = board.set_auto_backup();
                        if (result.status.equals("success")) {
                            terminal_logger.debug("Controller_Board:: board_update_backup:: To TRUE:: Board Id: {} Success of setting of dynamic backup", board_backup_pair.board_id);

                            // Toto je pro výjmečné případy - kdy při průběhu updatu padne tyrion a transakce není komplentí
                            if( board.actual_backup_c_program_version != null){
                                board.actual_backup_c_program_version = null;
                                board.update();
                            }

                        }

                    }

                // Autobacku je statický
                }else{

                    if(board_backup_pair.c_program_version_id == null || board_backup_pair.c_program_version_id.equals("")) return GlobalResult.result_badRequest("If backup_mode is set to false, c_program_version_id is required");

                    terminal_logger.debug("Controller_Board:: board_update_backup:: To FALSE:: Board Id: {} has dynamic Backup or already set static backup", board_backup_pair.board_id);

                    // Uprava desky na statický backup
                    Model_VersionObject c_program_version = Model_VersionObject.get_byId(board_backup_pair.c_program_version_id);
                    if (c_program_version == null) return GlobalResult.result_notFound("Version_Object c_program_version_id not found");

                    //Zkontroluji validitu Verze zda sedí k C_Programu
                    if (c_program_version.c_program == null) return GlobalResult.result_badRequest("Version_Object its not version of C_Program");

                    // Zkontroluji oprávnění
                    if (!c_program_version.c_program.read_permission()) return GlobalResult.result_forbidden();

                    //Zkontroluji validitu Verze zda sedí k C_Programu
                    if (c_program_version.c_compilation == null) return GlobalResult.result_badRequest("Version_Object its not version of C_Program - Missing compilation File");

                    // Ověření zda je kompilovatelná verze a nebo zda kompilace stále neběží
                    if (c_program_version.c_compilation.status != Enum_Compile_status.successfully_compiled_and_restored) return GlobalResult.result_badRequest("You cannot upload code in state:: " + c_program_version.c_compilation.status.name());

                    //Zkontroluji zda byla verze už zkompilována
                    if (!c_program_version.c_compilation.status.name().equals(Enum_Compile_status.successfully_compiled_and_restored.name())) return GlobalResult.result_badRequest("The program is not yet compiled & Restored");

                    WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                    b_pair.board = board;
                    b_pair.c_program_version = c_program_version;

                    board_pairs.add(b_pair);

                    if(!board.backup_mode){
                        board.actual_backup_c_program_version = null;
                        board.backup_mode = false;
                        board.update();
                    }
                }

            }


            if(!board_pairs.isEmpty()){
                new Thread( () -> {

                    try {
                        Model_ActualizationProcedure procedure = Model_Board.create_update_procedure(Enum_Firmware_type.BACKUP, Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL, board_pairs);
                        procedure.execute_update_procedure();

                    } catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }).start();
            }


            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Board_List.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_getByFilter(Integer page_number) {
        try {

            // Zpracování Json
            final Form<Swagger_Board_Filter> form = Form.form(Swagger_Board_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Board_Filter help = form.get();

            // Tvorba parametru dotazu
            Query<Model_Board> query = Ebean.find(Model_Board.class);


            // If Json contains TypeOfBoards list of id's
            if(help.type_of_board_ids != null && !help.type_of_board_ids.isEmpty()){
                query.where().in("type_of_board.id", help.type_of_board_ids);
            }

            // If contains confirms
            if(help.active != null){
                query.where().eq("is_active", help.active.equals("true"));
            }

            if(help.projects != null && !help.projects.isEmpty()){
                query.where().in("project.id", help.projects);
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
            Swagger_Board_List result = new Swagger_Board_List(query, page_number);

            // Vracím seznam
            return GlobalResult.result_ok(Json.toJson(result));

        } catch (Exception e){
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "upload Board picture",
            tags = { "Board"},
            notes = "Upload Board file",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.documentationClass.Swagger_BASE64_FILE",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Board.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(value = BodyParser.Json.class, maxLength = 1024 * 1024 * 10)
    public Result board_uploadPicture(String board_id){
        try {

            // Získání JSON
            final Form<Swagger_BASE64_FILE> form = Form.form(Swagger_BASE64_FILE.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_BASE64_FILE help = form.get();

            Model_Board board = Model_Board.get_byId(board_id);
            if(board == null) return GlobalResult.result_notFound("Board does not exist");

            if(!board.edit_permission()) return GlobalResult.result_forbidden();

            if(board.get_project() == null ) return GlobalResult.result_badRequest("Hardware is not in project!");


            // Odebrání předchozího obrázku
            if(board.picture != null){
                terminal_logger.debug("person_uploadPicture:: Removing previous picture");
                Model_FileRecord fileRecord = board.picture;
                board.picture = null;
                board.update();
                fileRecord.delete();
            }

            //  data:image/png;base64,
            String[] parts = help.file.split(",");
            String[] type = parts[0].split(":");
            String[] dataType = type[1].split(";");

            terminal_logger.debug("person_uploadPicture:: Data Type  :: " + dataType[0] + ":::");
            terminal_logger.debug("person_uploadPicture:: Data       :: " + parts[1].substring(0, 10) + "......");

            String file_name =  UUID.randomUUID().toString() + ".png";
            String file_path =  board.get_path() + "/" + file_name;


            board.picture =  Model_FileRecord.uploadAzure_File( parts[1], dataType[0], file_name , file_path);
            board.update();

            return GlobalResult.result_ok(Json.toJson(board));
        }catch (Exception e){
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Board picture",
            tags = {"Board"},
            notes = "Removes picture of logged person",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Secured_API.class)
    public Result board_removePicture(String board_id){
        try {

            Model_Board board = Model_Board.get_byId(board_id);
            if(board == null ) return GlobalResult.result_notFound("Board board_id not found");

            if(!(board.picture == null)) {
                board.picture.delete();
                board.picture = null;
                board.update();
            }else{
                return GlobalResult.result_badRequest("There is no picture to remove.");
            }

            return GlobalResult.result_ok("Picture successfully removed");
        }catch (Exception e){
            return ServerLogger.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Board.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result board_deactivate( String board_id) {
        try {

            // Kotrola objektu
            Model_Board board = Model_Board.get_byId(board_id);
            if(board == null ) return GlobalResult.result_notFound("Board board_id not found");

            // Kontrola oprávnění
            if(board.update_permission()) return GlobalResult.result_forbidden();

            // Úprava stavu
            board.is_active = false;

            // Uložení do databáze
            board.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Board.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result board_get(String board_id) {
        try {

            // Kotrola objektu
            Model_Board board = Model_Board.get_byId(board_id);
            if(board == null ) return GlobalResult.result_notFound("Board board_id not found");

            // Kontrola oprávnění
            if(!board.read_permission()) return GlobalResult.result_forbidden();

            // vrácení objektu
            return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "check Board registration status",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Board_Registration_Status.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result board_check(String hash_for_adding) {
        try {

            Swagger_Board_Registration_Status status = new Swagger_Board_Registration_Status();

            // Kotrola objektu
            Model_Board board_not_cached = Model_Board.find.where().eq("hash_for_adding", hash_for_adding).select("id").findUnique();
            if(board_not_cached == null) {
                status.status = Enum_Board_registration_status.NOT_EXIST;
                return GlobalResult.result_ok(Json.toJson(status));
            }

            Model_Board board = Model_Board.get_byId(board_not_cached.id);

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
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "connect Board with Project",
            tags = { "Board"},
            notes = "This Api is used by Users for connection of Board with their Project",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.documentationClass.Swagger_Board_Registration_To_Project",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Board.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_connectProject(){
        try {

            // Zpracování Json
            final Form<Swagger_Board_Registration_To_Project> form = Form.form(Swagger_Board_Registration_To_Project.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_Board_Registration_To_Project help = form.get();

            terminal_logger.debug("board_connectProject: registering new device with hash: {}", help.hash_for_adding);

            // Kotrola objektu - NAjdu v Databázi
            Model_Board board_not_cache = Model_Board.find.where().eq("hash_for_adding", help.hash_for_adding).select("id").findUnique();
            if (board_not_cache == null) return GlobalResult.result_notFound("Board board_id not found");

            //Vytáhnu přes Cache Manager
            Model_Board board = Model_Board.get_byId(board_not_cache.id);
            if (board == null) return GlobalResult.result_notFound("Board not found");
            if (!board.first_connect_permission()) return GlobalResult.result_badRequest("Board is already registered");

            // Kotrola objektu
            Model_Project project = Model_Project.get_byId(help.project_id);
            if (project == null) return GlobalResult.result_notFound("Project not found");
            if (!project.update_permission()) return GlobalResult.result_forbidden();

            // Pouze získání aktuálního stavu do Cache paměti ID listu
            if (board.cache_hardware_groups_id == null){
                board.get_hardware_groups();
            }

            board.date_of_user_registration = new Date();
            board.cache_value_project_id = project.id;
            board.project = project;
            board.update();

            project.cache_list_board_ids.add(board.id);

            if (!help.group_ids.isEmpty()) {

                for (String board_group_id : help.group_ids) {
                    Model_BoardGroup group = Model_BoardGroup.get_byId(board_group_id);
                    if (group == null) return GlobalResult.result_notFound("BoardGroup not found");
                    if (!group.update_permission()) return GlobalResult.result_forbidden();

                    // Přidám všechny, které nejsou už součásti cache_hardware_groups_id
                    if (!board.cache_hardware_groups_id.contains(board_group_id)) {

                        board.cache_hardware_groups_id.add(board_group_id);
                        board.board_groups.add(group);
                        group.cache_group_size += 1;
                    }
                }
            }

            System.out.println("Jsem na konci a budu ukládat");
            project.cache_list_board_ids.add(board.id);
            board.update();

            // vrácení objektu
            return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Board.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result board_disconnectProject(  String board_id){
        try {

            // Kontrola objektu
            // !!! pozor vyjímka!!!!
            Model_Board board = Model_Board.find.byId(board_id);
            if(board == null ) return GlobalResult.result_notFound("Board board_id not found");

            // Kontrola oprávnění
            if(!board.update_permission()) return GlobalResult.result_forbidden();

            if(board.get_project() == null){
                return GlobalResult.result_notFound("Board already removed");
            }

            Model_Project project = board.get_project();
            project.cache_list_board_ids.remove(board_id);

            // Odstraním vazbu
            board.project = null;

            // uložím do databáze
            board.update();

            project.refresh();

            // vracím upravenou hodnotu
            return GlobalResult.result_ok(Json.toJson(board));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get B_Program all details for integration",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Boards_For_Blocko.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result board_allDetailsForBlocko(  String project_id){
        try {

            // Kontrola objektu
            Model_Project project = Model_Project.get_byId(project_id);
            if (project == null) return GlobalResult.result_notFound("Project project_id not found");

            // Kontrola oprávnění
            if (! project.read_permission()) return GlobalResult.result_forbidden();

            // Získání objektu
            Swagger_Boards_For_Blocko boards_for_blocko = new Swagger_Boards_For_Blocko();
            boards_for_blocko.add_M_Projects(project.get_m_projects_not_deleted());
            boards_for_blocko.add_C_Programs(project.get_c_programs_not_deleted());

            for (Model_Board board : project.get_project_boards_not_deleted()) boards_for_blocko.boards.add(board.get_short_board());


            boards_for_blocko.type_of_boards = Model_TypeOfBoard.find.where().eq("boards.project.id", project.id).findList();


            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(boards_for_blocko));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Remove Hardware from Database - Only for Administrators", hidden = true)
    public Result board_delete(String board_id){
        try {

            // Kontrola objektu
            Model_Board board = Model_Board.get_byId(board_id);
            if(board == null ) return GlobalResult.result_notFound("Board not found");

            // Kontrola oprávnění
            if(!board.delete_permission()) return GlobalResult.result_forbidden();

            if (board.project != null || board.date_of_user_registration != null)
                return GlobalResult.result_badRequest("Board is already in use.");

            board.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create BoardGroup",
            tags = { "BoardGroup"},
            notes = "Create Board Group",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.documentationClass.Swagger_Hardware_Group_New",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_BoardGroup.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_group_create() {
        try {

            // Zpracování Json
            final Form<Swagger_Hardware_Group_New> form = Form.form(Swagger_Hardware_Group_New.class).bindFromRequest();
            if(form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_Hardware_Group_New help = form.get();

            Model_Project project = Model_Project.get_byId(help.project_id);
            if(project == null) return GlobalResult.result_notFound("Model_Project not found");


            if (Model_BoardGroup.find.where().eq("name", help.name).eq("project.id", project.id).findUnique() != null){
                return GlobalResult.result_badRequest("Group name must be a unique!");
            }


            Model_BoardGroup group = new Model_BoardGroup();
            group.name = help.name;
            group.description = help.description;
            group.project = project;


            if(!group.create_permission()) return GlobalResult.result_forbidden();
            group.save();

            // Vracím seznam
            return GlobalResult.result_ok(Json.toJson(group));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit BoardGroup",
            tags = { "BoardGroup"},
            notes = "update BoardGroup",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.documentationClass.Swagger_Hardware_Group_Edit",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_BootLoader.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_group_update(String board_group_id) {
        try {

            // Zpracování Json
            final Form<Swagger_Hardware_Group_New> form = Form.form(Swagger_Hardware_Group_New.class).bindFromRequest();
            if(form.hasErrors())return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_Hardware_Group_New help = form.get();

            Model_BoardGroup group = Model_BoardGroup.get_byId(board_group_id);
            if (group == null) return GlobalResult.result_notFound("BoardGroupLoader not found");

            if (!group.edit_permission()) return GlobalResult.result_forbidden();

            group.name = help.name;
            group.description = help.description;

            group.update();

            return GlobalResult.result_ok(Json.toJson(group));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update BoardGroup Device List",
            tags = { "BoardGroup"},
            notes = "update BoardGroup add or remove device list",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.documentationClass.Swagger_Hardware_Group_DeviceListEdit",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_group_update_device_list() {
        try {

            // Zpracování Json
            final Form<Swagger_Hardware_Group_DeviceListEdit> form = Form.form(Swagger_Hardware_Group_DeviceListEdit.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_Hardware_Group_DeviceListEdit help = form.get();

            if(help.device_synchro != null) {

                Model_Board board = Model_Board.get_byId(help.device_synchro.device_id);
                if (board == null) return GlobalResult.result_notFound("Board ID not found");
                if (!board.update_permission()) return GlobalResult.result_forbidden();

                terminal_logger.debug("board_group_update_device_list - board: {}", board.id);

                // Pouze získání aktuálního stavu do Cache paměti ID listu
                if(board.cache_hardware_groups_id == null){
                    terminal_logger.debug("board_group_update_device_list - loading from cache");
                    board.get_hardware_groups();
                }

                terminal_logger.debug("board_group_update_device_list - cached groups: {}", Json.toJson(board.cache_hardware_groups_id));

                // Cyklus pro přidávání
                for(String board_group_id: help.device_synchro.group_ids) {

                    // Přidám všechny, které nejsou už součásti cache_hardware_groups_id
                    if(!board.cache_hardware_groups_id.contains(board_group_id)){

                        terminal_logger.debug("board_group_update_device_list - adding group {}", board_group_id );

                        Model_BoardGroup group = Model_BoardGroup.get_byId(board_group_id);
                        if (group == null) return GlobalResult.result_notFound("BoardGroup not found");
                        if (!group.update_permission()) return GlobalResult.result_forbidden();

                        board.cache_hardware_groups_id.add(board_group_id);
                        board.board_groups.add(group);
                        group.cache_group_size +=1;
                    }
                }

                // Cyklus pro mazání java.util.ConcurrentModificationException
                for (Iterator<String> it = board.cache_hardware_groups_id.iterator(); it.hasNext(); ) {

                    String board_group_id = it.next();

                    // Není a tak mažu
                    if(!help.device_synchro.group_ids.contains(board_group_id)){

                        terminal_logger.debug("board_group_update_device_list - removing group {}", board_group_id );

                        Model_BoardGroup group = Model_BoardGroup.get_byId(board_group_id);
                        if (group == null) return GlobalResult.result_notFound("BoardGroup not found");
                        if (!group.update_permission()) return GlobalResult.result_forbidden();

                        board.board_groups.remove(group);
                        group.cache_group_size -=1;
                        it.remove();
                    }
                }

                board.update();
            }

            if(help.group_synchro != null) {

                Model_BoardGroup group = Model_BoardGroup.get_byId(help.group_synchro.group_id);
                if(!group.update_permission()) return GlobalResult.result_forbidden();

                for(String board_id: help.group_synchro.device_ids){
                    Model_Board board = Model_Board.get_byId(board_id);
                    if(!board.update_permission()) return GlobalResult.result_forbidden();

                    board.cache_hardware_groups_id.add(help.group_synchro.group_id);
                    board.board_groups.add(group);

                }

                group.refresh();
            }



            return GlobalResult.result_ok();

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete BoardGroup",
            tags = { "BoardGroup"},
            notes = "delete BoardGroup",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_group_delete(String board_group_id) {
        try {

            Model_BoardGroup group = Model_BoardGroup.find.byId(board_group_id);
            if (group == null) return GlobalResult.result_notFound("BootLoader not found");

            if (!group.delete_permission()) return GlobalResult.result_forbidden();

            group.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get_List BoardGroup From Project",
            tags = { "Type-Of-Board"},
            notes = "get List of BoardGroup from Project",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Model_BoardGroup.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result board_group_get_list_project( String project_id) {
        try {

            // Kontrola validity objektu
            Model_Project project = Model_Project.get_byId(project_id);
            if(project == null ) return GlobalResult.result_notFound("Project project_id not found");

            // Kontorluji oprávnění
            if(! project.read_permission()) return GlobalResult.result_forbidden();

            // Vrácení validity objektu
            return GlobalResult.result_ok(Json.toJson(project.get_hardware_groups_not_deleted()));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get BoardGroup",
            tags = { "Type-Of-Board"},
            notes = "get List of BoardGroup from Project",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Model_BoardGroup.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result board_group_get(String board_group_id) {
        try {

            // Kontrola validity objektu
            Model_BoardGroup group = Model_BoardGroup.find.byId(board_group_id);
            if (group == null) return GlobalResult.result_notFound("BoardGroupLoader not found");

            if (!group.read_permission()) return GlobalResult.result_forbidden();

            // Vrácení validity objektu
            return GlobalResult.result_ok(Json.toJson(group));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

}
