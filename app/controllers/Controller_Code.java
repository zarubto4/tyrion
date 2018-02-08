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
import utilities.swagger.output.Swagger_C_Program_Version;
import utilities.swagger.output.Swagger_Compilation_Build_Error;
import utilities.swagger.output.Swagger_Compilation_Ok;
import utilities.swagger.output.filter_results.Swagger_C_Program_List;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;

import java.util.*;

@Security.Authenticated(Authentication.class)
@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Code extends BaseController {

// LOGGER ##############################################################################################################
    private static final Logger logger = new Logger(Controller_Hardware.class);

    private FormFactory formFactory;

    @Inject
    public Controller_Code(FormFactory formFactory) {
        this.formFactory = formFactory;
    }
    
    
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
     return okEmpty();
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
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_Program.Version.read_permission", value = Model_Version.read_permission_docs),
                    }),
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "CProgram_read"),
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
    public Result compile_c_program_version( @ApiParam(value = "version_id String query", required = true) String version_id ) {
        try {

            logger.debug("Starting compilation on version_id = " + version_id);

            // Ověření objektu
            Model_Version version_object = Model_Version.getById(version_id);
            if (version_object == null) return notFound("Version_Object version_id not found");

            // Smažu předchozí kompilaci
            if (version_object.get_c_program() == null) return badRequest("Version is not version of C_Program");

            // Kontrola oprávnění
            if (!version_object.get_c_program().read_permission()) return forbiddenEmpty();

            // Odpovím předchozí kompilací
            if (version_object.compilation != null) return ok(Json.toJson(new Swagger_Compilation_Ok()));


            Response_Interface result = version_object.compile_program_procedure();

            if (result instanceof Result_Ok) {
                return  ok(Json.toJson(new Swagger_Compilation_Ok()));
            }

            if (result instanceof Result_CompilationListError) {
                return  ok(Json.toJson(((Result_CompilationListError) result).errors));
            }

            if (result instanceof Result_ExternalServerSideError ) {
                return externalServerError(Json.toJson(result));
            }

            if (result instanceof Result_ServerOffline) {
                return externalServerOffline(((Result_ServerOffline) result).message);
            }

            // Neznámá chyba se kterou nebylo počítání
            return badRequest("unknown_error");

        } catch (Exception e) {
            return internalServerError(e);
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
                            dataType = "utilities.swagger.input.Swagger_C_Program_Version_Update",
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
            Form<Swagger_C_Program_Version_Update> form = formFactory.form(Swagger_C_Program_Version_Update.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_C_Program_Version_Update help = form.get();

            // Ověření objektu
            if (help.hardware_type_id == null) return badRequest("hardware_type_id is missing!");

            // Ověření objektu
            Model_HardwareType hardwareType = Model_HardwareType.getById(help.hardware_type_id);
            if (hardwareType == null) return notFound("HardwareType not found");

            if (!Model_CompilationServer.is_online()) return externalServerOffline("Compilation server is offline");

            List<Swagger_Library_Record> library_files = new ArrayList<>();

            for (String lib_id : help.imported_libraries) {

                logger.trace("compile_C_Program_code:: Looking for library Version Id " + lib_id);
                Model_Version lib_version = Model_Version.getById(lib_id);

                if (lib_version == null || lib_version.library == null) {

                    logger.internalServerError(new Exception("Error in reading libraries version not found! Version ID = " + lib_version));

                    ObjectNode error = Json.newObject();
                    error.put("status", "error");
                    error.put("error_message", "Error getting libraries - Library not found!");
                    error.put("error_code", 400);
                    return buildErrors(error);
                }

                if (!lib_version.files.isEmpty()) {

                    logger.trace("compile_C_Program_code:: Library contains files");

                    for (Model_Blob f : lib_version.files) {

                        JsonNode json_library = Json.parse(f.get_fileRecord_from_Azure_inString());

                        Form<Swagger_Library_File_Load> lib_form = formFactory.form(Swagger_Library_File_Load.class).bind(json_library);
                        if (lib_form.hasErrors()) {

                            logger.internalServerError(new Exception("Error reading libraries from files! Model_FileRecord ID = " + f.id));

                            ObjectNode error = Json.newObject();
                            error.put("status", "error");
                            error.put("error_message", "Error with importing libraries - Library Id: " + lib_id );
                            error.put("error_code", 400);
                            return buildErrors(error);
                        }

                        Swagger_Library_File_Load lib_file = lib_form.get();
                        library_files.addAll(lib_file.files);
                    }
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

                Swagger_Cloud_Compilation_Server_CompilationResult result = new Swagger_Cloud_Compilation_Server_CompilationResult();
                result.interface_code = compilation_result.interface_code;

                return ok(Json.toJson(result));
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
            return internalServerError(e);
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
     @ExtensionProperty(name = "Static Permission key", value = "Hardware_update"),
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
     Board board = Board.getById(board_id);
     if (board == null) return notFound("Board board_id object not found");

     if (!board.update_permission()) return forbiddenEmpty();

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

     return okEmpty();

     } catch (Exception e) {
     return Server_Logger.result_internalServerError(e, request());
     }
     }
     */
// C_ Program && Version ###############################################################################################

    @ApiOperation(value = "create C_Program",
            tags = {"C_Program"},
            notes = "If you want create new C_Program in project.id = {project_id}. Send required json values and cloud_compilation_server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_Program.create_permission", value = Model_CProgram.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "CProgram_create" ),
                    })
            }
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
    @ApiResponses(value = {
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

            // Zpracování Json
            final Form<Swagger_C_Program_New> form = formFactory.form(Swagger_C_Program_New.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_C_Program_New help = form.get();

            // Ověření Typu Desky
            Model_HardwareType hardwareType = Model_HardwareType.getById(help.hardware_type_id);
            if (hardwareType == null) return notFound("HardwareType hardware_type_id not found");

            // Tvorba programu
            Model_CProgram c_program        = new Model_CProgram();
            c_program.name                  = help.name;
            c_program.description           = help.description;
            c_program.hardware_type = hardwareType;
            c_program.publish_type          = ProgramType.PRIVATE;

            if (help.project_id != null) {
                // Ověření projektu
                Model_Project project = Model_Project.getById(help.project_id);
                if (project == null) return notFound("Project not found");
                c_program.project = project;
            }

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if (!c_program.create_permission()) return forbiddenEmpty();

            // Uložení C++ Programu
            c_program.save();
            c_program.refresh();

            // Přiřadím první verzi!
            if (hardwareType.get_main_c_program() != null && hardwareType.get_main_c_program().default_main_version != null) {

                Model_Version version = new Model_Version();
                version.name = "1.0.1";
                version.description = hardwareType.get_main_c_program().description;
                version.author = person();
                version.c_program = c_program;
                version.public_version = help.c_program_public_admin_create;

                // Zkontroluji oprávnění
                if (!c_program.update_permission()) return forbiddenEmpty();

                version.save();

                for (Model_Blob file : hardwareType.get_main_c_program().default_main_version.files) {

                    JsonNode json = Json.parse(file.get_fileRecord_from_Azure_inString());

                    Form<Swagger_C_Program_Version_Update> scheme_form = formFactory.form(Swagger_C_Program_Version_Update.class).bind(json);
                    if (form.hasErrors()) {
                        logger.internalServerError(new Exception("Error loading first default version of CProgram."));
                        break;
                    }
                    Swagger_C_Program_Version_Update scheme_load_form = scheme_form.get();

                    // Nahraje do Azure a připojí do verze soubor
                    ObjectNode content = Json.newObject();
                    content.put("main", scheme_load_form.main);
                    content.set("files", Json.toJson(scheme_load_form.files));
                    content.set("imported_libraries", Json.toJson(scheme_load_form.imported_libraries));

                    // Content se nahraje na Azure
                    Model_Blob.uploadAzure_Version(content.toString(), "code.json", c_program.get_path(), version);
                    version.update();
                }

                version.compile_program_thread(hardwareType.get_main_c_program().default_main_version.compilation.firmware_version_lib);
            }

            c_program.refresh();

            return created(Json.toJson(c_program));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "make_Clone C_Program",
            tags = {"C_Program"},
            notes = "clone C_Program for private",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CProgram.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_clone() {
        try {

            // Zpracování Json
            final Form<Swagger_C_Program_Copy> form = formFactory.form(Swagger_C_Program_Copy.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_C_Program_Copy help = form.get();

            // Vyhledám Objekt
            Model_CProgram c_program_old = Model_CProgram.getById(help.c_program_id);
            if (c_program_old == null) return notFound("C_Program c_program not found");

            // Zkontroluji oprávnění
            if (!c_program_old.read_permission())  return forbiddenEmpty();

            // Vyhledám Objekt
            Model_Project project = Model_Project.getById(help.project_id);
            if (project == null) return notFound("Project project_id not found");

            // Zkontroluji oprávnění
            if (!project.update_permission())  return forbiddenEmpty();

            Model_CProgram c_program_new =  new Model_CProgram();
            c_program_new.name = help.name;
            c_program_new.description = help.description;
            c_program_new.hardware_type = c_program_old.getHardwareType();
            c_program_new.project = project;
            c_program_new.save();

            c_program_new.refresh();

            for (Model_Version version : c_program_old.getVersions()) {

                Model_Version copy_object = new Model_Version();
                copy_object.name            = version.name;
                copy_object.description     = version.description;
                copy_object.c_program       = c_program_new;
                copy_object.public_version  = false;
                copy_object.author          = version.author;

                // Zkontroluji oprávnění
                copy_object.save();

                // Překopíruji veškerý obsah
                Model_Blob fileRecord = version.files.get(0);

                Model_Blob.uploadAzure_Version(fileRecord.get_fileRecord_from_Azure_inString(), "code.json" , c_program_new.get_path() ,  copy_object);
                copy_object.update();

                copy_object.compile_program_thread(version.compilation.firmware_version_lib);
            }

            c_program_new.refresh();

            // Vracím Objekt
            return ok(Json.toJson(c_program_new));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }
    
    @ApiOperation(value = "get C_Program",
            tags = {"C_Program"},
            notes = "get C_Program by query = c_program_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_Program.read_permission", value = Model_CProgram.read_permission_docs),
                    }),
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "CProgram_read"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CProgram.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_get(String c_program_id) {
        try {

            // Vyhledám Objekt
            Model_CProgram c_program = Model_CProgram.getById(c_program_id);
            if (c_program == null) return notFound("C_Program c_program not found");

            // Zkontroluji oprávnění
            if (! c_program.read_permission())  return forbiddenEmpty();

            // Vracím Objekt
            return ok(Json.toJson(c_program));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get C_Program List by Filter",
            tags = {"C_Program"},
            notes = "get all C_Programs that belong to logged person",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_Program.read_permission", value = "Tyrion only returns C_Programs which person owns, there is no need to check permissions"),
                    }),
            }
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_C_Program_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n. For first call, use 1 (first page of list)", required = true)  int page_number) {
        try {

            // Získání JSON
            final Form<Swagger_C_Program_Filter> form = formFactory.form(Swagger_C_Program_Filter.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_C_Program_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_CProgram> query = Ebean.find(Model_CProgram.class);

            query.orderBy("UPPER(name) ASC");

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if (help.project_id != null) {

                Model_Project project = Model_Project.getById(help.project_id);
                if (project == null) return notFound("Project not found");
                if (!project.read_permission()) return forbiddenEmpty();

                query.where().eq("project.id", help.project_id).eq("deleted", false);
            }

            if (!help.hardware_type_ids.isEmpty()) {
               query.where().in("hardware_type.id", help.hardware_type_ids);
            }

            if (help.public_programs) {
                query.where().isNull("project").eq("deleted", false).eq("publish_type", ProgramType.PUBLIC.name());
            }

            if (help.pending_programs) {
                if (!person().has_permission(Model_CProgram.Permission.C_Program_community_publishing_permission.name())) return forbiddenEmpty();
                query.where().eq("version_objects.approval_state", Approval.PENDING.name());
            }

            // Vyvoření odchozího JSON
            Swagger_C_Program_List result = new Swagger_C_Program_List(query,page_number);

            // Vrácení výsledku
            return ok(Json.toJson(result));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit C_Program",
            tags = {"C_Program"},
            notes = "If you want edit base information about C_Program by  query = c_program_id. Send required json values and cloud_compilation_server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "CProgram_edit"),
                    })
            }
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_CProgram.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_edit(String c_program_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_NameAndDescription help = form.get();

            // Ověření objektu
            Model_CProgram c_program = Model_CProgram.getById(c_program_id);
            if (c_program == null ) return notFound("C_Program not found");

            // Úprava objektu
            c_program.name = help.name;
            c_program.description = help.description;

            // Zkontroluji oprávnění
            if (!c_program.edit_permission())  return forbiddenEmpty();

            // Uložení změn
            c_program.update();

            // Vrácení objektu
            return ok(Json.toJson(c_program));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete C_Program",
            tags = {"C_Program"},
            notes = "delete C_Program by query = c_program_id, query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "CProgram_delete"),
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
    public Result c_program_delete( String c_program_id) {
        try {

            // Ověření objektu
            Model_CProgram c_program = Model_CProgram.getById(c_program_id);
            if (c_program == null ) return notFound("C_Program c_program_id not found");

            // Kontrola oprávnění
            if (!c_program.delete_permission()) return forbiddenEmpty();

            // Vyhledání PRoduct pro získání kontejneru
            //Model_Product product = Model_Product.find.query().where().eq("projects.c_programs.id", c_program_id).findOne();

            // Smazání objektu
            c_program.delete();

            // Vrácení potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "create C_Program_Version",
            tags = {"C_Program"},
            notes = "If you want add new code to C_Program by query = c_program_id. Send required json values and cloud_compilation_server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "CProgram_update"),
                    })
            }
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
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Swagger_C_Program_Version.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_version_create(@ApiParam(value = "version_id String query", required = true)  String c_program_id) {
        try {

            // Zpracování Json
            Form<Swagger_C_Program_Version_New> form = formFactory.form(Swagger_C_Program_Version_New.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_C_Program_Version_New help = form.get();

            // Ověření objektu
            Model_CProgram c_program = Model_CProgram.getById(c_program_id);
            if (c_program == null) return notFound("C_Program c_program_id not found");

            // Zkontroluji oprávnění
            if (!c_program.update_permission()) return forbiddenEmpty();

            // První nová Verze
            Model_Version version = new Model_Version();
            version.name            = help.name;
            version.description     = help.description;
            version.author          = person();
            version.c_program       = c_program;
            version.public_version  = false;

            // Zkontroluji oprávnění
            if (!c_program.update_permission()) return forbiddenEmpty();

            version.save();

            // Content se nahraje na Azure
            Model_Blob.uploadAzure_Version(Json.toJson(help).toString(), "code.json" , c_program.get_path() ,  version);
            version.update();

            version.compile_program_thread(help.library_compilation_version);

            // Vracím vytvořený objekt
            return created(Json.toJson(c_program.program_version(version)));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get C_Program_Version",
            tags = {"C_Program"},
            notes = "get Version of C_Program by query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "C_Program.Version.read_permission", value = Model_Version.read_permission_docs),
                    }),
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "CProgram_read"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_C_Program_Version.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_version_get(@ApiParam(value = "version_id String query", required = true)  String version_id) {
        try {

            // Vyhledám Objekt
            Model_Version version_object = Model_Version.getById(version_id);
            if (version_object == null) return notFound("Version_Object version not found");

            //Zkontroluji validitu Verze zda sedí k C_Programu
            if (version_object.get_c_program() == null) return badRequest("Version_Object its not version of C_Program");

            // Zkontroluji oprávnění
            if (!version_object.get_c_program().read_permission())  return forbiddenEmpty();

            // Vracím Objekt
            return ok(Json.toJson(version_object.get_c_program().program_version(version_object)));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit C_Program_Version information",
            tags = {"C_Program"},
            notes = "For update basic (name and description) information in Version of C_Program. If you want update code. You have to create new version. " +
                    "And after that you can delete previous version",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "CProgram_edit"),
                    })
            }
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Version.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result c_program_version_edit(@ApiParam(value = "version_id String query",   required = true)  String version_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_NameAndDescription help = form.get();

            // Ověření objektu
            Model_Version version = Model_Version.getById(version_id);
            if (version == null) return notFound("Version not found");

            // Kontrola oprávnění
            if (!version.get_c_program().edit_permission()) return forbiddenEmpty();

            //Uprava objektu
            version.name        = help.name;
            version.description = help.description;

            // Uložení změn
            version.update();

            // Vrácení objektu
            return ok(Json.toJson(version));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete C_Program_Version",
            tags = {"C_Program"},
            notes = "delete Version.id = version_id in C_Program by query = c_program_id, query = version_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "CProgram_delete"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response =  Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_version_delete(@ApiParam(value = "version_id String query",   required = true)    String version_id) {
        try {

            // Ověření objektu
            Model_Version version_object = Model_Version.getById(version_id);
            if (version_object == null) return notFound("Version version_id not found");

            // Zkontroluji validitu Verze zda sedí k C_Programu
            if (version_object.get_c_program() == null) return badRequest("Version_Object its not version of C_Program");

            // Kontrola oprávnění
            if (!version_object.get_c_program().delete_permission()) return forbiddenEmpty();

            // Smažu zástupný objekt
            version_object.delete();

            // Vracím potvrzení o smazání
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }


    @ApiOperation(value = "make C_Program_Version public",
            tags = {"C_Program"},
            notes = "Make C_Program public, so other users can see it and use it. Attention! Attention! Attention! A user can publish only three programs at the stage waiting for approval.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "C_Program.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "CProgram_edit"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Bad Request",               response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_version_make_public(@ApiParam(value = "version_id String query", required = true)  String version_id) {
        try {

            // Kontrola objektu
            Model_Version version = Model_Version.getById(version_id);
            if (version == null) return notFound("Version not found");

            if (version.get_c_program()  == null )return notFound("Version not found");


            if (Model_Version.find.query().where().eq("approval_state", Approval.PENDING.name())
                    .eq("c_program.project.participants.person.id", BaseController.personId())
                    .findList().size() > 3) {
                // TODO Notifikace uživatelovi
                return badRequest("You can publish only 3 programs. Wait until the previous ones approved by the administrator. Thanks.");
            }

            if (version.approval_state != null)  return badRequest("You cannot publish same program twice!");

            // Úprava objektu
            version.approval_state = Approval.PENDING;

            // Kontrola oprávnění
            if (!(version.get_c_program().edit_permission())) return forbiddenEmpty();

            // Uložení změn
            version.update();

            // Vrácení potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit C_Program_Version Response publication",
            tags = {"Admin-C_Program"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            protocols = "https",
            code = 200
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result c_program_public_response() {
        try {

            // Získání Json
            final Form<Swagger_Community_Version_Publish_Response> form = formFactory.form(Swagger_Community_Version_Publish_Response.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Community_Version_Publish_Response help = form.get();

            // Kontrola objektu
            Model_Version version_old = Model_Version.getById(help.version_id);
            if (version_old == null) return notFound("Version not found");

            if (version_old.get_c_program() == null) {
                return notFound("C_Program c_program_id not found");
            }

            // Ověření objektu
            Model_CProgram c_program_old = Model_CProgram.getById(version_old.get_c_program().id);


            // Zkontroluji oprávnění
            if (!c_program_old.community_publishing_permission()) {
                return forbiddenEmpty();
            }

            if (help.decision) {

                // Odkomentuj až odzkoušíš že emaily jsou hezky naformátované - můžeš totiž Verzi hodnotit pořád dokola!!
                version_old.approval_state = Approval.APPROVED;
                version_old.update();


                Model_CProgram c_program = Model_CProgram.find.byId(c_program_old.id); // + "_public_copy"); // TODO

                if (c_program == null) {
                    c_program = new Model_CProgram();
                    // c_program.id = c_program_old.id + "_public_copy"; TODO
                    c_program.name = help.program_name;
                    c_program.description = help.program_description;
                    c_program.hardware_type = c_program_old.hardware_type;
                    c_program.publish_type  = ProgramType.PUBLIC;
                    c_program.save();
                }

                Model_Version version_object = new Model_Version();
                version_object.name             = help.version_name;
                version_object.description      = help.version_description;
                version_object.c_program        = c_program;
                version_object.public_version   = true;
                version_object.author           = version_old.author;

                // Zkontroluji oprávnění
                version_object.save();

                c_program.refresh();

                // Překopíruji veškerý obsah
                Model_Blob fileRecord = version_old.files.get(0);

                Model_Blob.uploadAzure_Version(fileRecord.get_fileRecord_from_Azure_inString(), "code.json" , c_program.get_path() ,  version_object);
                version_object.update();

                version_object.compile_program_thread(version_old.compilation.firmware_version_lib);

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
                                .send(version_old.get_c_program().get_project().getProduct().customer, "Publishing your program" );

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
                                .send(version_old.get_c_program().get_project().getProduct().customer, "Publishing your program" );

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
                            .send(version_old.c_program.get_project().getProduct().customer, "Publishing your program");

                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }

            // Potvrzení
            return  okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "set_c_program_version_as_main HardwareType",
            tags = {"Admin-C_Program, HardwareType"},
            notes = "set C_Program version as Main for This Type of Device. Version must be from Main or Test C Program of this version",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Empty.class)
    public Result c_program_markScheme(@ApiParam(value = "version_id", required = true) String version_id) {
        try {

            Model_Version version = Model_Version.getById(version_id);
            if (version == null) return notFound("Version not found");

            if (version.get_c_program() == null || (version.get_c_program().hardware_type_default == null && version.get_c_program().hardware_type_test == null)) return badRequest("Version_object is not version of c_program or is not default firmware");

            // Kontrola oprávnění
            if (!version.get_c_program().edit_permission()) return forbiddenEmpty();

            Model_Version previous_main_version_not_cached = Model_Version.find.query().where().eq("c_program.id", version.get_c_program().id).isNotNull("default_program").select("id").findOne();
            if (previous_main_version_not_cached != null) {

                Model_Version previous_main_version = Model_Version.getById(previous_main_version_not_cached.id);
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
            return ok(Json.toJson(version.get_c_program()));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }
}
