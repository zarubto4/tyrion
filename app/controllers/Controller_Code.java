package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.ebean.Ebean;
import io.ebean.ExpressionList;
import io.ebean.Junction;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.*;
import play.Environment;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.emails.Email;
import utilities.enums.*;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.scheduler.SchedulerController;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_Compilation_Build_Error;
import utilities.swagger.output.Swagger_Compilation_Ok;
import utilities.swagger.output.filter_results.Swagger_C_Program_List;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Help_Hardware_Pair;

import java.util.*;

@Security.Authenticated(Authentication.class)
@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Code extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Code.class);

// CONTROLLER CONFIGURATION ############################################################################################

    @javax.inject.Inject
    public Controller_Code(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerController scheduler) {
        super(environment, ws, formFactory, youTrack, config, scheduler);
    }

// CONTROLLER CONTENT ##################################################################################################

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
            Swagger_C_Program_Version_Update help  = formFromRequestWithValidation(Swagger_C_Program_Version_Update.class);

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

                if(help.immediately_hardware_update && !help.hardware_ids.isEmpty()) {
                    Model_Compilation compilation = Model_Compilation.make_a_individual_compilation(compilation_result, help.library_compilation_version);
                    System.out.println("Success -  we have compilation file!");

                    Model_UpdateProcedure procedure = new Model_UpdateProcedure();
                    procedure.type_of_update = UpdateType.MANUALLY_RELEASE_MANAGER;

                    procedure.date_of_planing = new Date();


                    for(UUID hardware_id : help.hardware_ids) {

                        Model_Hardware hardware = Model_Hardware.getById(hardware_id);
                        procedure.project_id = hardware.get_project_id();

                        Model_HardwareUpdate plan = new Model_HardwareUpdate();
                        plan.hardware = hardware;
                        plan.firmware_type = FirmwareType.FIRMWARE;
                        plan.state = HardwareUpdateState.NOT_YET_STARTED;
                        plan.binary_file = compilation.blob;
                        procedure.updates.add(plan);

                    }

                    procedure.save();
                }

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
            Swagger_C_Program_New help  = formFromRequestWithValidation(Swagger_C_Program_New.class);

            // Ověření Typu Desky
            Model_HardwareType hardwareType = Model_HardwareType.getById(help.hardware_type_id);

            System.out.println("Model_HardwareType ok");

            // Tvorba programu
            Model_CProgram c_program        = new Model_CProgram();
            c_program.name                  = help.name;
            c_program.description           = help.description;
            c_program.hardware_type         = hardwareType;
            c_program.publish_type          = ProgramType.PRIVATE;

            if (help.project_id != null) {
                c_program.project = Model_Project.getById(help.project_id);
            }

            // Uložení C++ Programu
            c_program.save();

            // Set Tags
            c_program.setTags(help.tags);

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

            return created(Model_CProgram.getById(c_program.id));

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
            Swagger_C_Program_Copy help = formFromRequestWithValidation(Swagger_C_Program_Copy.class);

            // Vyhledám Objekt
            Model_CProgram c_program_old = Model_CProgram.getById(help.c_program_id);

            // Vyhledám Objekt
            Model_Project project = Model_Project.getById(help.project_id);

            Model_CProgram c_program_new =  new Model_CProgram();
            c_program_new.name = help.name;
            c_program_new.description = help.description;
            c_program_new.hardware_type = c_program_old.getHardwareType();
            c_program_new.publish_type  = ProgramType.PRIVATE;
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
            Swagger_C_Program_Filter help = formFromRequestWithValidation(Swagger_C_Program_Filter.class);


            // Musí být splněna alespoň jedna podmínka, aby mohl být Junction aktivní. V opačném případě by totiž způsobil bychu
            // která vypadá nějak takto:  where t0.deleted = false and and .... KDE máme 2x end!!!!!
            if (!(help.project_id != null || help.public_programs || help.pending_programs)) {
                return ok(new Swagger_C_Program_List());
            }


            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_CProgram> query = Ebean.find(Model_CProgram.class);
            query.orderBy("UPPER(name) ASC");
            query.where().ne("deleted", true);

            // Ovlivňuje všechny
            if (!help.hardware_type_ids.isEmpty()) {
                query.where().in("hardware_type.id", help.hardware_type_ids);
            }


            ExpressionList<Model_CProgram> list = query.where();
            Junction<Model_CProgram> disjunction = list.disjunction();

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if (help.project_id != null) {
                Model_Project.getById(help.project_id);
                disjunction
                        .conjunction()
                            .eq("project.id", help.project_id)
                        .endJunction();
            }

            if (help.public_programs) {
                disjunction
                        .conjunction()
                            .eq("publish_type", ProgramType.PUBLIC.name())
                        .endJunction();
            }

            if (help.pending_programs) {
                if (!person().has_permission(Model_CProgram.Permission.C_Program_community_publishing_permission.name())) return forbidden();
                disjunction
                        .conjunction()
                            .eq("versions.approval_state", Approval.PENDING.name())
                            .ne("publish_type", ProgramType.DEFAULT_MAIN)
                        .endJunction();
            }

            disjunction.endJunction();




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
            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_CProgram c_program = Model_CProgram.getById(c_program_id);

            // Úprava objektu
            c_program.name = help.name;
            c_program.description = help.description;

            // Uložení změn
            c_program.update();

            // Set Tags
            c_program.setTags(help.tags);

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
            Swagger_Tags help = formFromRequestWithValidation(Swagger_Tags.class);

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
            Swagger_Tags help = formFromRequestWithValidation(Swagger_Tags.class);

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

    @ApiOperation(value = "create C_Program_Version SaveAs",
            tags = {"C_Program"},
            notes = "New and database tracked version of C_Program",
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


            System.out.println("c_program_version_create");

            // Get and Validate Object
            Swagger_C_Program_Version_New help = formFromRequestWithValidation(Swagger_C_Program_Version_New.class);

            // Ověření objektu
            Model_CProgram c_program = Model_CProgram.getById(c_program_id);

            // Zkontroluji oprávnění
            c_program.check_update_permission();

            UUID working_copy_version_id = Model_CProgramVersion.find.query().where().eq("c_program.id", c_program_id).ne("deleted", true).eq("working_copy", true).select("id").findSingleAttribute();

            // If the is not working copy - make it
            if(working_copy_version_id != null) {
                Model_CProgramVersion version = Model_CProgramVersion.getById(working_copy_version_id);
                version.delete();
            }

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

    @ApiOperation(value = "working_copy_save C_Program_Version",
            tags = {"C_Program"},
            notes = "Just override last version",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_C_Program_Version_Refresh",
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
    public Result c_program_version_working_copy(@ApiParam(value = "version_id String query", required = true)  UUID c_program_id) {
        try {

            System.out.println("c_program_version_working_copy");

            // Get and Validate Object
            Swagger_C_Program_Version_Refresh help = formFromRequestWithValidation(Swagger_C_Program_Version_Refresh.class);

            System.out.println("Sem to ani nedošlo :(");

            // Find Last working copy
            UUID version_id = Model_CProgramVersion.find.query().where().eq("c_program.id", c_program_id).ne("deleted", true).eq("working_copy", true).select("id").findSingleAttribute();

            Model_CProgramVersion version = null;

            // If the is not working copy - make it
            if(version_id == null) {

                System.out.println("Verze Neexistuje a tak jí vytvořím");


                // Ověření objektu
                Model_CProgram c_program = Model_CProgram.getById(c_program_id);

                // Zkontroluji oprávnění
                c_program.check_update_permission();

                version = new Model_CProgramVersion();
                version.name            = "Working Copy";
                version.description     = "Save As for a databased version";
                version.c_program       = c_program;
                version.publish_type    = ProgramType.PRIVATE;
                version.working_copy    = true;
                version.save();

            }else  {

                version = Model_CProgramVersion.getById(version_id);

                if(version.file != null) {
                    version.file.delete();
                }
            }


            System.out.println("Vytvářím Soubor");

            // Content se nahraje na Azure
            version.file = Model_Blob.upload(Json.toJson(help).toString(), "code.json" , Model_Blob.get_path_for_bin());
            version.update();

            // Start with asynchronous ccompilation
            version.compile_program_thread(help.library_compilation_version);

            // Vracím vytvořený objekt
            return created(version);

        } catch (Exception e) {
            e.printStackTrace();
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
            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

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
                    .eq("author_id", _BaseController.personId())
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
            Swagger_Community_Version_Publish_Response help = formFromRequestWithValidation(Swagger_Community_Version_Publish_Response.class);

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
                                .send(version_old.get_c_program().getProject().getProduct().owner, "Publishing your program" );

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
                                .send(version_old.get_c_program().getProject().getProduct().owner, "Publishing your program" );

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
                            .send(version_old.get_c_program().getProject().getProduct().owner, "Publishing your program");

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
