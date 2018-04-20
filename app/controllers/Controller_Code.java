package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import io.ebean.Ebean;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.emails.Email;
import utilities.enums.Approval;
import utilities.enums.ProgramType;
import utilities.logger.Logger;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_Compilation_Build_Error;
import utilities.swagger.output.Swagger_Compilation_Ok;
import utilities.swagger.output.filter_results.Swagger_C_Program_List;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;

import java.util.*;

@Security.Authenticated(Authentication.class)
@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Code extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Code.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private _BaseFormFactory baseFormFactory;

    @Inject public Controller_Code(_BaseFormFactory formFactory) {
        this.baseFormFactory = formFactory;
    }

// CONTROLLER CONTENT ##################################################################################################
    
    /**
     @ApiOperation(value = "only for Tyrion Front End", hidden = true)
     @Security.Authenticated(Secured_Admin.class)
     public Result uploadBinaryFileToBoard_fake_board(String instance_id, String board_id, String build_id,  String firmware_type_string) {
     try {

     // Slouží k nahrávání firmwaru do deviců, které jsou ve fakce instnaci pro testování
     // nejsou databázovaný a tedy nejde spustit regulérní update procedura na kterou jsme zvyklé - viz metoda nad tímto
     // Slouží jen pro Admin rozhraní Tyriona

     Firmware_type firmware_type = Firmware_type.getFirmwareType(firmware_type_string);
     if (firmware_type == null) return notFound("FirmwareType not found!");

     List<String> list = new ArrayList<>();
     list.add(board_id);

     // Přijmu soubor
     Http.MultipartFormData body = request().body().asMultipartFormData();

     List<Http.MultipartFormData.FilePart> files_from_request = body.getFiles();

     if (files_from_request == null || files_from_request.isEmpty())return notFound("Bin File not found!");
     if (files_from_request.size() > 1)return badRequest("More than one File is not allowed!");

     File file = files_from_request.get(0).getFile();
     if (file == null) return badRequest("File not found!");
     if (file.length() < 1) return badRequest("File is Empty!");


     int dot = files_from_request.get(0).getFilename().lastIndexOf(".");
     String file_type = files_from_request.get(0).getFilename().substring(dot);
     String file_name = files_from_request.get(0).getFilename().substring(0, dot);

     // Zkontroluji soubor
     if (!file_type.equals(".bin"))return badRequest("Wrong type of File - \"Bin\" required! ");
     if ((file.length() / 1024) > 500)return badRequest("File is bigger than 500K b");


     ObjectNode request = Json.newObject();
     request.put("message_channel", "tyrion");
     request.put("instance_id", instance_id);
     request.put("message_type", "updateDevice");
     request.put("firmware_type", firmware_type.get_firmwareType());
     request.set("targetIds",  Json.toJson(list));
     request.put("build_id", build_id);
     request.put("program", Model_FileRecord.get_encoded_binary_string_from_File(file));

     // ObjectNode result =  Controller_WebSocket.incomingConnections_homers.get(instance_id).write_with_confirmation(request, 1000*30, 0, 3);

     if (request.get("status").asText().equals("success")) {
     return ok();
     }
     else {
     return badRequest(request);
     }

     } catch (Exception e) {
     return Server_Logger.result_internalServerError(e, request());
     }
     }
     */

    @ApiOperation(value = "compile C_Program_Version",
            hidden = true,
            tags = {"Admin-C_Program"},
            notes = "Compile specific version of C_Program - before compilation - you have to update (save) version code" +
                    "This appi is udes by Tyrion Calling on own API",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Compilation successful",    response = Swagger_Compilation_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 422, message = "Compilation unsuccessful",  response = Swagger_Compilation_Build_Error.class, responseContainer = "List"),
            @ApiResponse(code = 477, message = "External server is offline",response = Result_ServerOffline.class),
            @ApiResponse(code = 478, message = "External server side Error",response = Result_ExternalServerSideError.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result compile_c_program_version( @ApiParam(value = "version_id String query", required = true) UUID version_id ) {
        try {

            // Ověření objektu
            Model_CProgramVersion version = Model_CProgramVersion.getById(version_id);

            // Odpovím předchozí kompilací
            if (version.compilation != null) return ok(new Swagger_Compilation_Ok());

            return version.compile_program_procedure();

        } catch (Exception e) {
            return controllerServerError(e);
        }

    }

    @ApiOperation(value = "compile C_Program",
            tags = {"C_Program"},
            notes = "Compile code",
            produces = "application/json",
            protocols = "https"

    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_C_Program_Version_Update",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Compilation successful",    response = Swagger_Compilation_Server_CompilationResult.class),
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

            // Get and Validate Object
            Swagger_C_Program_Version_Update help  = baseFormFactory.formFromRequestWithValidation(Swagger_C_Program_Version_Update.class);

            // Ověření objektu
            Model_HardwareType hardwareType = Model_HardwareType.getById(help.hardware_type_id);

            if (!Model_CompilationServer.is_online()) return externalServerOffline("Compilation server is offline");

            List<Swagger_Library_Record> library_files = new ArrayList<>();

            for (String lib_id : help.imported_libraries) {

                logger.trace("compile_C_Program_code:: Looking for library Version Id " + lib_id);
                Model_LibraryVersion lib_version = Model_LibraryVersion.getById(lib_id);
                if (lib_version.file != null) {

                    logger.trace("compile_C_Program_code:: Library contains files");

                    Swagger_Library_File_Load lib_file = baseFormFactory.formFromJsonWithValidation(Swagger_Library_File_Load.class, Json.parse(lib_version.file.get_fileRecord_from_Azure_inString()));
                    library_files.addAll(lib_file.files);

                }
            }

            ObjectNode includes = Json.newObject();

            for (Swagger_Library_Record file_lib : library_files) {
                if (file_lib.file_name.equals("README.md") || file_lib.file_name.equals("readme.md")) continue;
                includes.put(file_lib.file_name, file_lib.content);
            }

            if (help.files != null) {
                for (Swagger_Library_Record user_file : help.files) {
                    includes.put(user_file.file_name, user_file.content);
                }
            }

            if (Controller_WebSocket.compilers.isEmpty()) {
                return externalServerOffline("Compilation cloud_compilation_server is offline!");
            }

            WS_Message_Make_compilation compilation_result = Model_CompilationServer.make_Compilation(new WS_Message_Make_compilation().make_request( hardwareType , help.library_compilation_version, UUID.randomUUID(), help.main, includes ));

            // V případě úspěšného buildu obsahuje příchozí JsonNode build_url
            if (compilation_result.build_url != null && compilation_result.status.equals("success")) {

                Swagger_Compilation_Server_CompilationResult result = new Swagger_Compilation_Server_CompilationResult();
                result.interface_code = compilation_result.interface_code;

                return ok(result);
            }

            // Kompilace nebyla úspěšná a tak vracím obsah neuspěšné kompilace
            if (!compilation_result.build_errors.isEmpty()) {
                return buildErrors(Json.toJson(compilation_result.build_errors));
            }

            // Nebylo úspěšné ani odeslání requestu - Chyba v konfiguraci a tak vracím defaulní chybz
            if (compilation_result.error_message != null) {

                ObjectNode result_json = Json.newObject();
                result_json.put("error_message", compilation_result.error_message);

                return externalServerError(result_json);
            }

            // Neznámá chyba se kterou nebylo počítání
            return badRequest("Unknown error");
        } catch (Exception e) {
            return controllerServerError(e);
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
     )
     @ApiResponses({
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
     Board board = Board.getById(board_id);
     if (board == null) return notFound("Board board_id object not found");

     if (!board.update_permission()) return forbidden();

     Firmware_type firmware_type = Firmware_type.getFirmwareType(firmware_type_string);
     if (firmware_type == null) return notFound("FirmwareType not found!");

     // Přijmu soubor
     Http.MultipartFormData body = request().body().asMultipartFormData();

     List<Http.MultipartFormData.FilePart> files_from_request = body.getFiles();

     if (files_from_request == null || files_from_request.isEmpty())return notFound("Bin File not found!");
     if (files_from_request.size() > 1)return badRequest("More than one File is not allowed!");

     File file = files_from_request.get(0).getFile();
     if (file == null) return badRequest("File not found!");
     if (file.length() < 1) return badRequest("File is Empty!");


     int dot = files_from_request.get(0).getFilename().lastIndexOf(".");
     String file_type = files_from_request.get(0).getFilename().substring(dot);
     String file_name = files_from_request.get(0).getFilename().substring(0, dot);

     // Zkontroluji soubor
     if (!file_type.equals(".bin"))return badRequest("Wrong type of File - \"Bin\" required! ");
     if ((file.length() / 1024) > 500)return badRequest("File is bigger than 500K b");

     // Existuje Homer?

     String binary_file = FileRecord.get_encoded_binary_string_from_File(file);
     FileRecord fileRecord = FileRecord.create_Binary_file("byzance-private/binaryfiles", binary_file, file_name);
     Controller_Actualization.add_new_actualization_request_with_user_file(board.project, firmware_type, board, fileRecord);

     return ok();

     } catch (Exception e) {
     return Server_Logger.result_internalServerError(e, request());
     }
     }
     */

// C_PROGRAM AND VERSION  ###############################################################################################

    @ApiOperation(value = "create C_Program",
            tags = {"C_Program"},
            notes = "If you want create new C_Program in project.id = {project_id}. Send required json values and cloud_compilation_server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_C_Program_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_CProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_create() {
        try {

            // Get and Validate Object
            Swagger_C_Program_New help  = baseFormFactory.formFromRequestWithValidation(Swagger_C_Program_New.class);

            // Ověření Typu Desky
            Model_HardwareType hardwareType = Model_HardwareType.getById(help.hardware_type_id);

            // Tvorba programu
            Model_CProgram c_program        = new Model_CProgram();
            c_program.name                  = help.name;
            c_program.description           = help.description;
            c_program.hardware_type = hardwareType;
            c_program.publish_type          = ProgramType.PRIVATE;
            c_program.setTags(help.tags);

            if (help.project_id != null) {
                c_program.project = Model_Project.getById(help.project_id);
            }

            // Uložení C++ Programu
            c_program.save();

            // Přiřadím první verzi!
            if (hardwareType.get_main_c_program() != null && hardwareType.get_main_c_program().default_main_version != null) {

                Model_CProgramVersion version = new Model_CProgramVersion();
                version.name = "1.0.1";
                version.description = hardwareType.get_main_c_program().description;
                version.c_program = c_program;
                version.publish_type = help.c_program_public_admin_create ? ProgramType.PUBLIC : ProgramType.PRIVATE;

                version.save();

                // Content se nahraje na Azure
                version.file = Model_Blob.upload(hardwareType.get_main_c_program().default_main_version.file.get_fileRecord_from_Azure_inString(), "code.json", c_program.get_path());
                version.update();


                version.compile_program_thread(hardwareType.get_main_c_program().default_main_version.compilation.firmware_version_lib);
            }

            return created(c_program);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "make_Clone C_Program",
            tags = {"C_Program"},
            notes = "clone C_Program for private",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_C_Program_Copy",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CProgram.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_clone() {
        try {

            // Get and Validate Object
            Swagger_C_Program_Copy help = baseFormFactory.formFromRequestWithValidation(Swagger_C_Program_Copy.class);

            // Vyhledám Objekt
            Model_CProgram c_program_old = Model_CProgram.getById(help.c_program_id);

            // Vyhledám Objekt
            Model_Project project = Model_Project.getById(help.project_id);

            Model_CProgram c_program_new =  new Model_CProgram();
            c_program_new.name = help.name;
            c_program_new.description = help.description;
            c_program_new.hardware_type = c_program_old.getHardwareType();
            c_program_new.project = project;

            c_program_new.save();

            for (Model_CProgramVersion version : c_program_old.getVersions()) {

                Model_CProgramVersion copy_object = new Model_CProgramVersion();
                copy_object.name            = version.name;
                copy_object.description     = version.description;
                copy_object.c_program       = c_program_new;
                copy_object.publish_type    = ProgramType.PRIVATE;

                // Zkontroluji oprávnění
                copy_object.save();

                // Překopíruji veškerý obsah
                Model_Blob fileRecord = version.file;

                copy_object.file = Model_Blob.upload(fileRecord.get_fileRecord_from_Azure_inString(), "code.json" , c_program_new.get_path());
                copy_object.update();

                copy_object.compile_program_thread(version.compilation.firmware_version_lib);
            }

            c_program_new.refresh();

            // Vracím Objekt
            return ok(c_program_new);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
    
    @ApiOperation(value = "get C_Program",
            tags = {"C_Program"},
            notes = "get C_Program by query = c_program_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CProgram.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_get(UUID c_program_id) {
        try {

            // Vyhledám Objekt
            Model_CProgram c_program = Model_CProgram.getById(c_program_id);

            // Vracím Objekt
            return ok(c_program);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get C_Program List by Filter",
            tags = {"C_Program"},
            notes = "get all C_Programs that belong to logged person",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_C_Program_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_C_Program_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n. For first call, use 1 (first page of list)", required = true)  int page_number) {
        try {

            // Get and Validate Object
            Swagger_C_Program_Filter help = baseFormFactory.formFromRequestWithValidation(Swagger_C_Program_Filter.class);

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_CProgram> query = Ebean.find(Model_CProgram.class);
            query.orderBy("UPPER(name) ASC");
            query.where().eq("deleted", false);

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if (help.project_id != null) {
                Model_Project.getById(help.project_id);
                query.where().eq("project.id", help.project_id);
            }

            if (!help.hardware_type_ids.isEmpty()) {
               query.where().in("hardware_type.id", help.hardware_type_ids);
            }

            if (help.public_programs) {
                query.where().isNull("project").eq("publish_type", ProgramType.PUBLIC.name());
            }

            if (help.pending_programs) {
                if (!person().has_permission(Model_CProgram.Permission.C_Program_community_publishing_permission.name())) return forbidden();
                query.where().eq("versions.approval_state", Approval.PENDING.name());
            }

            // Vyvoření odchozího JSON
            Swagger_C_Program_List result = new Swagger_C_Program_List(query,page_number,help);

            // Vrácení výsledku
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit C_Program",
            tags = {"C_Program"},
            notes = "If you want edit base information about C_Program by  query = c_program_id. Send required json values and cloud_compilation_server respond with new object",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_NameAndDescription",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CProgram.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_edit(UUID c_program_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_CProgram c_program = Model_CProgram.getById(c_program_id);

            // Úprava objektu
            c_program.name = help.name;
            c_program.description = help.description;

            // Uložení změn
            c_program.update();

            // Vrácení objektu
            return ok(c_program);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "tag CProgram",
            tags = {"C_Program"},
            notes = "",     //TODO
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Tags",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_addTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help = baseFormFactory.formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_CProgram cProgram = Model_CProgram.getById(help.object_id);

            // Add Tags
            cProgram.addTags(help.tags);

            // Vrácení objektu
            return ok(cProgram);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "untag CProgram",
            tags = {"C_Program"},
            notes = "",     //TODO
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Tags",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_removeTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help = baseFormFactory.formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_CProgram cProgram = Model_CProgram.getById(help.object_id);

            // Remove Tags
            cProgram.removeTags(help.tags);

            // Vrácení objektu
            return ok(cProgram);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete C_Program",
            tags = {"C_Program"},
            notes = "delete C_Program by query = c_program_id, query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_delete(UUID c_program_id) {
        try {

            // Ověření objektu
            Model_CProgram c_program = Model_CProgram.getById(c_program_id);

            // Smazání objektu
            c_program.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "create C_Program_Version",
            tags = {"C_Program"},
            notes = "If you want add new code to C_Program by query = c_program_id. Send required json values and cloud_compilation_server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_C_Program_Version_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_CProgramVersion.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_version_create(@ApiParam(value = "version_id String query", required = true)  UUID c_program_id) {
        try {

            // Get and Validate Object
            Swagger_C_Program_Version_New help = baseFormFactory.formFromRequestWithValidation(Swagger_C_Program_Version_New.class);

            // Ověření objektu
            Model_CProgram c_program = Model_CProgram.getById(c_program_id);

            // Zkontroluji oprávnění
            c_program.check_update_permission();

            // První nová Verze
            Model_CProgramVersion version = new Model_CProgramVersion();
            version.name            = help.name;
            version.description     = help.description;
            version.c_program       = c_program;
            version.publish_type    = ProgramType.PRIVATE;

            version.save();

            // Content se nahraje na Azure
            version.file =  Model_Blob.upload(Json.toJson(help).toString(), "code.json" , c_program.get_path());
            version.update();

            // Start with asynchronous ccompilation
            version.compile_program_thread(help.library_compilation_version);

            // Vracím vytvořený objekt
            return created(version);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get C_Program_Version",
            tags = {"C_Program"},
            notes = "get Version of C_Program by query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CProgramVersion.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_version_get(@ApiParam(value = "version_id String query", required = true)  UUID version_id) {
        try {

            // Kontrola objekt
            Model_CProgramVersion version = Model_CProgramVersion.getById(version_id);

            // Vracím Objekt
            return ok(version);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit C_Program_Version information",
            tags = {"C_Program"},
            notes = "For update basic (name and description) information in Version of C_Program. If you want update code. You have to create new version. " +
                    "And after that you can delete previous version",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_NameAndDescription",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CProgramVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_version_edit(@ApiParam(value = "version_id String query",   required = true)  UUID version_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Ověření objektu
            Model_CProgramVersion version = Model_CProgramVersion.getById(version_id);

            //Uprava objektu
            version.name        = help.name;
            version.description = help.description;

            // Uložení změn
            version.update();

            // Vrácení objektu
            return ok(version);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete C_Program_Version",
            tags = {"C_Program"},
            notes = "delete Version.id = version_id in C_Program by query = c_program_id, query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_version_delete(@ApiParam(value = "version_id String query",   required = true)  UUID version_id) {
        try {

            // Ověření objektu
            Model_CProgramVersion version = Model_CProgramVersion.getById(version_id);

            // Smažu zástupný objekt
            version.delete();

            // Vracím potvrzení o smazání
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }


    @ApiOperation(value = "make C_Program_Version public",
            tags = {"C_Program"},
            notes = "Make C_Program public, so other users can see it and use it. Attention! Attention! Attention! A user can publish only three programs at the stage waiting for approval.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Bad Request",               response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_version_make_public(@ApiParam(value = "version_id String query", required = true)  UUID version_id) {
        try {

            // Kontrola objektu
            Model_CProgramVersion version = Model_CProgramVersion.getById(version_id);

            if (Model_CProgramVersion.find.query().where().eq("approval_state", Approval.PENDING.name())
                    .eq("c_program.project.participants.person.id", _BaseController.personId())
                    .findList().size() > 3) {
                // TODO Notifikace uživatelovi
                return badRequest("You can publish only 3 programs. Wait until the previous ones approved by the administrator. Thanks.");
            }

            if (version.approval_state != null)  return badRequest("You cannot publish same program twice!");

            // Úprava objektu
            version.approval_state = Approval.PENDING;

            // Uložení změn
            version.update();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit C_Program_Version Response publication",
            tags = {"Admin-C_Program"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Community_Version_Publish_Response",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_public_response() {
        try {

            // Get and Validate Object
            Swagger_Community_Version_Publish_Response help = baseFormFactory.formFromRequestWithValidation(Swagger_Community_Version_Publish_Response.class);

            // Kontrola objektu
            Model_CProgramVersion version_old = Model_CProgramVersion.getById(help.version_id);

            // Kontrola objektu
            Model_CProgram c_program_old = Model_CProgram.getById(version_old.get_c_program().id);

            // Zkontroluji oprávnění
            if (!c_program_old.community_publishing_permission()) {
                return forbidden();
            }

            if (help.decision) {

                // Odkomentuj až odzkoušíš že emaily jsou hezky naformátované - můžeš totiž Verzi hodnotit pořád dokola!!
                version_old.approval_state = Approval.APPROVED;
                version_old.update();


                UUID c_program_previous_id = Model_CProgram.find.query().where().eq("original_id", c_program_old.id).select("id").findSingleAttribute();

                Model_CProgram c_program = null;

                if (c_program_previous_id == null) {
                    c_program = new Model_CProgram();
                    c_program.original_id = c_program_old.id;
                    c_program.name = help.program_name;
                    c_program.description = help.program_description;
                    c_program.hardware_type = c_program_old.hardware_type;
                    c_program.publish_type  = ProgramType.PUBLIC;
                    c_program.save();
                }else {
                    c_program = Model_CProgram.getById(c_program_previous_id);
                }

                Model_CProgramVersion version = new Model_CProgramVersion();
                version.name             = help.version_name;
                version.description      = help.version_description;
                version.c_program        = c_program;
                version.publish_type     = ProgramType.PUBLIC;
                version.author_id        = version_old.author_id;

                // Zkontroluji oprávnění
                version.save();

                c_program.refresh();

                // Překopíruji veškerý obsah
                Model_Blob fileRecord = version_old.file;

                version.file = Model_Blob.upload(fileRecord.get_fileRecord_from_Azure_inString(), "code.json" , c_program.get_path());
                version.update();

                version.compile_program_thread(version_old.compilation.firmware_version_lib);

                // Admin to schválil bez dalších keců
                if ((help.reason == null || help.reason.length() < 4) ) {
                    try {

                        new Email()
                                .text("Thank you for publishing your program!")
                                .text(  Email.bold("C Program Name: ") +        c_program_old.name + Email.newLine() +
                                        Email.bold("C Program Description: ") + c_program_old.name + Email.newLine() +
                                        Email.bold("Version Name: ") +          c_program_old.name + Email.newLine() +
                                        Email.bold("Version Description: ") +   c_program_old.name + Email.newLine() )
                                .divider()
                                .text("We will publish it as soon as possible.")
                                .text(Email.bold("Thanks!") + Email.newLine() + person().full_name())
                                .send(version_old.get_c_program().getProject().getProduct().customer, "Publishing your program" );

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }

                // Admin to schválil ale měl nějaký keci k tomu
                } else {
                    try {

                        new Email()
                                .text("Thank you for publishing your program!")
                                .text(  Email.bold("C Program Name: ") +        c_program_old.name + Email.newLine() +
                                        Email.bold("C Program Description: ") + c_program_old.name + Email.newLine() +
                                        Email.bold("Version Name: ") +          c_program_old.name + Email.newLine() +
                                        Email.bold("Version Description: ") +   c_program_old.name + Email.newLine() )
                                .divider()
                                .text("We will publish it as soon as possible. We also had to make some changes to your program or rename something.")
                                .text(Email.bold("Reason: ") + Email.newLine() + help.reason)
                                .text(Email.bold("Thanks!") + Email.newLine() + person().full_name())
                                .send(version_old.get_c_program().getProject().getProduct().customer, "Publishing your program" );

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }

            } else {

                version_old.approval_state = Approval.DISAPPROVED;
                version_old.update();

                try {

                    new Email()
                            .text("First! Thank you for publishing your program!")
                            .text(Email.bold("C Program Name: ") + c_program_old.name + Email.newLine() +
                                    Email.bold("C Program Description: ") + c_program_old.name + Email.newLine() +
                                    Email.bold("Version Name: ") + c_program_old.name + Email.newLine() +
                                    Email.bold("Version Description: ") + c_program_old.name + Email.newLine())
                            .divider()
                            .text("We are sorry, but we found some problems in your program, so we did not publish it. But do not worry and do not give up! " +
                                    "We are glad that you want to contribute to our public libraries. Here are some tips what to improve, so you can try it again.")
                            .text(Email.bold("Reason: ") + Email.newLine() + help.reason)
                            .text(Email.bold("Thanks!") + Email.newLine() + person().full_name())
                            .send(version_old.get_c_program().getProject().getProduct().customer, "Publishing your program");

                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }

            // Potvrzení
            return  ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "set_c_program_version_as_main HardwareType",
            tags = {"Admin-C_Program, HardwareType"},
            notes = "set C_Program version as Main for This Type of Device. Version must be from Main or Test C Program of this version",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Empty.class)
    public Result c_program_markScheme(@ApiParam(value = "version_id", required = true) UUID version_id) {
        try {

            Model_CProgramVersion version = Model_CProgramVersion.getById(version_id);

            if (version.get_c_program().hardware_type_default == null && version.get_c_program().hardware_type_test == null) return badRequest("Version_object is not version of c_program or is not default firmware");


            Model_CProgramVersion previous_main_version_not_cached = Model_CProgramVersion.find.query().where().eq("c_program.id", version.get_c_program().id).isNotNull("default_program").select("id").findOne();
            if (previous_main_version_not_cached != null) {

                Model_CProgramVersion previous_main_version = Model_CProgramVersion.getById(previous_main_version_not_cached.id);
                if (previous_main_version != null) {
                    previous_main_version.default_program = null;
                    version.get_c_program().default_main_version = null;
                    previous_main_version.update();
                    version.get_c_program().update();
                }
            }

            version.default_program = version.get_c_program();
            version.update();

            version.get_c_program().refresh();

            // Vracím Json
            return ok(version.get_c_program());

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

}
