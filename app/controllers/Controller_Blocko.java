package controllers;

import com.google.inject.Inject;
import io.ebean.*;
import io.swagger.annotations.*;
import models.*;
import play.data.validation.Constraints;
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
import utilities.scheduler.SchedulerController;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_Mobile_Connection_Summary;
import utilities.swagger.output.filter_results.Swagger_B_Program_List;
import utilities.swagger.output.filter_results.Swagger_Block_List;
import utilities.swagger.output.filter_results.Swagger_C_Program_List;
import utilities.swagger.output.filter_results.Swagger_Instance_List;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

@Security.Authenticated(Authentication.class)
@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Blocko extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Blocko.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private _BaseFormFactory baseFormFactory;
    private SchedulerController scheduler;

    @Inject
    public Controller_Blocko(_BaseFormFactory formFactory, SchedulerController scheduler) {
        this.baseFormFactory = formFactory;
        this.scheduler = scheduler;
    }

// CONTROLLER CONTENT ##################################################################################################

// B PROGRAM ###########################################################################################################

    @ApiOperation(value = "create B_Program",
            tags = {"B_Program"},
            notes = "create new B_Program",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https",
            code = 201
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
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_BProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bProgram_create(UUID project_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help  = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_Project project = Model_Project.getById(project_id);

            // Tvorba programu
            Model_BProgram bProgram        = new Model_BProgram();
            bProgram.description           = help.description;
            bProgram.name                  = help.name;
            bProgram.project               = project;

            // Uložení objektu
            bProgram.save();

            bProgram.setTags(help.tags);

            // Vrácení objektu
            return created(bProgram);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get B_Program",
            tags = {"B_Program"},
            notes = "get B_Program object",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BProgram.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result bProgram_get(UUID b_program_id) {
        try {

            // Kontrola objektu
            Model_BProgram bProgram = Model_BProgram.getById(b_program_id);

            return ok(bProgram);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get B_Program List by Filter",
            tags = {"B_Program"},
            notes = "get B_Program List",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_B_Program_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",             response = Swagger_B_Program_List.class),
            @ApiResponse(code = 400, message = "Invalid body",          response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",  response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",     response = Result_InternalServerError.class)
    })
    public Result bProgram_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number) {
        try {

            // Get and Validate Object
            Swagger_B_Program_Filter help  = baseFormFactory.formFromRequestWithValidation(Swagger_B_Program_Filter.class);

            // Musí být splněna alespoň jedna podmínka, aby mohl být Junction aktivní. V opačném případě by totiž způsobil bychu
            // která vypadá nějak takto:  where t0.deleted = false and and .... KDE máme 2x end!!!!!
            if (!(help.project_id != null)) {
                return ok(new Swagger_B_Program_List());
            }

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_BProgram> query = Ebean.find(Model_BProgram.class);
            query.where().eq("deleted", false);



            ExpressionList<Model_BProgram> list = query.where();
            Junction<Model_BProgram> disjunction = list.disjunction();

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if (help.project_id != null) {
                Model_Project.getById(help.project_id);
                disjunction
                        .conjunction()
                            .eq("project.id", help.project_id)
                        .endJunction();
            }

            disjunction.endJunction();

            // Vytvoření odchozího JSON
            Swagger_B_Program_List result = new Swagger_B_Program_List(query, page_number, help);

            // Vrácení výsledku
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit B_Program",
            tags = {"B_Program"},
            notes = "edit basic information in B_Program object",
            produces = "application/json",
            protocols = "https",
            code = 200
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bProgram_update(UUID b_program_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help  = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_BProgram b_program = Model_BProgram.getById(b_program_id);

            // Úprava objektu
            b_program.description = help.description;
            b_program.name        = help.name;

            // Uložení objektu
            b_program.update();

            // Vrácení objektu
            return ok(b_program);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "tag B_Program",
            tags = {"B_Program"},
            notes = "", //TODO
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Tags",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bProgram_addTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help  = baseFormFactory.formFromRequestWithValidation(Swagger_Tags.class);

            Model_BProgram bProgram = Model_BProgram.getById(help.object_id);

            bProgram.addTags(help.tags);

            // Vrácení objektu
            return ok(bProgram);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "untag B_Program",
            tags = {"B_Program"},
            notes = "", //TODO
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Tags",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bProgram_removeTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help  = baseFormFactory.formFromRequestWithValidation(Swagger_Tags.class);

            Model_BProgram bProgram = Model_BProgram.getById(help.object_id);

            bProgram.removeTags(help.tags);

            // Vrácení objektu
            return ok(bProgram);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete B_Program",
            tags = {"B_Program"},
            notes = "remove B_Program object",
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
    public Result bProgram_delete(UUID b_program_id) {
        try {

            // Kontrola objektu
            Model_BProgram program = Model_BProgram.getById(b_program_id);

            // Smazání objektu
            program.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// B PROGRAM VERSION ###################################################################################################

    @ApiOperation(value = "create B_Program_Version",
            tags = {"B_Program"},
            notes = "create new vesion in Blocko program",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_B_Program_Version_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BProgramVersion.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bProgramVersion_create(UUID b_program_id) {
        try {

            // Get and Validate Object
            Swagger_B_Program_Version_New help  = baseFormFactory.formFromRequestWithValidation(Swagger_B_Program_Version_New.class);


            // Program který budu ukládat do data Storage v Azure
            String file_content =  help.program;

            // Ověření programu
            Model_BProgram bProgram = Model_BProgram.getById(b_program_id);

            // System.out.println("bProgramVersion_create");

            // První nová Verze
            Model_BProgramVersion version = new Model_BProgramVersion();
            version.name        = help.name;
            version.description = help.description;
            version.b_program   = bProgram;

            // Uložení objektu
            version.save();

            // Vytvořím Snapshoty Verze M_Programu
            if (help.m_project_snapshots != null) {

                // System.out.println("       help.m_project_snapshots != null ");
                // System.out.println("       help.m_project_snapshots.size: " + help.m_project_snapshots.size());

                for (Swagger_B_Program_Version_New.M_Project_SnapShot help_m_project_snap : help.m_project_snapshots) {

                    // System.out.println("        help.help.m_project_snapshots.m_project_id: " +  help_m_project_snap.m_project_id);
                    // System.out.println("        help.help.m_project_snapshots.m_program_snapshots.size: " +  help_m_project_snap.m_program_snapshots.size());

                    Model_GridProject m_project = Model_GridProject.getById(help_m_project_snap.m_project_id);

                    Model_BProgramVersionSnapGridProject snap = new Model_BProgramVersionSnapGridProject();
                    snap.grid_project = m_project;

                    for (Swagger_B_Program_Version_New.M_Program_SnapShot help_m_program_snap : help_m_project_snap.m_program_snapshots) {

                        System.out.println("            Model_GridProgramVersion id: " + help_m_program_snap.version_id);
                        System.out.println("            grid_program. id: " + help_m_program_snap.m_program_id);

                        UUID m_program_version_id = Model_GridProgramVersion.find.query().where()
                                .eq("id", help_m_program_snap.version_id)
                                .eq("grid_program.id", help_m_program_snap.m_program_id)
                                .select("id")
                                .findSingleAttribute();

                        if (m_program_version_id == null) {
                            logger.error("bProgramVersion_create:: m_program_version is null!! ");
                            continue;
                        }

                        Model_GridProgramVersion grid_version = Model_GridProgramVersion.getById(m_program_version_id);

                        Model_BProgramVersionSnapGridProjectProgram snap_shot_parameter = new Model_BProgramVersionSnapGridProjectProgram();


                        // System.out.println("                    grid_program_version: " + m_program_version.id);
                        snap_shot_parameter.grid_program_version = grid_version;
                        snap_shot_parameter.grid_project_program_snapshot = snap;
                        snap_shot_parameter.save();

                        snap.grid_programs.add(snap_shot_parameter);

                    }
                    snap.b_program_version = version;
                    snap.save();

                    version.grid_project_snapshots.add(snap);
                }
            }

            // Nahrání na Azure
            version.file = Model_Blob.upload(file_content, "blocko.json", version.get_path());
            version.update();

            // Vrácení objektu
            return ok(version);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get B_Program_Version",
            tags = {"B_Program"},
            notes = "get B_Program version object",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BProgramVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result bProgramVersion_get(UUID version_id) {
        try {

            // Kontrola objektu
            Model_BProgramVersion version = Model_BProgramVersion.getById(version_id);

            // Vrácení objektu
            return ok(version);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit B_Program_Version",
            tags = {"B_Program"},
            notes = "edit Version object",
            produces = "application/json",
            consumes = "text/html",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bProgramVersion_update(UUID version_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help  = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Získání objektu
            Model_BProgramVersion version = Model_BProgramVersion.getById(version_id);

            version.name = help.name;
            version.description = help.description;

            // Smazání objektu
            version.update();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete B_Program_Version",
            tags = {"B_Program"},
            notes = "remove B_Program version object",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result bProgramVersion_delete(UUID version_id) {
        try {

            // Získání objektu
            Model_BProgramVersion version  = Model_BProgramVersion.getById(version_id);

            // Smazání objektu
            version.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// INSTANCE ############################################################################################################

    @ApiOperation(value = "create Instance",
            tags = {"Instance"},
            notes = "", //TODO
            produces = "application/json",
            consumes = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Instance_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully updated",      response = Model_Instance.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result instance_create() {
        try {

            // Get and Validate Object
            Swagger_Instance_New help  = baseFormFactory.formFromRequestWithValidation(Swagger_Instance_New.class);

            // Kontrola objektu
            Model_Project project = Model_Project.getById(help.project_id);
            Model_HomerServer main_server = Model_HomerServer.getById(help.main_server_id);

            Model_HomerServer backup_server = null;
            if(help.backup_server_id != null) backup_server = Model_HomerServer.getById(help.backup_server_id);

            Model_BProgram b_program = Model_BProgram.getById(help.b_program_id);

            // Tvorba Objektu
            Model_Instance instance = new Model_Instance();
            instance.name = help.name;
            instance.description = help.description;
            instance.project = project;
            instance.server_main = main_server;
            instance.server_backup = backup_server;
            instance.b_program = b_program;

            instance.save();

            instance.setTags(help.tags);

            return created(instance);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Instance",
            tags = {"Instance"},
            notes = "", //TODO
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Instance.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instance_get(UUID instance_id) {
        try {

            Model_Instance instance = Model_Instance.getById(instance_id);

            return ok(instance);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Instance",
            tags = {"Instance"},
            notes = "", //TODO
            produces = "application/json",
            consumes = "application/json",
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
            @ApiResponse(code = 200, message = "Successfully updated",      response = Model_Instance.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result instance_update(UUID instance_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help  = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_Instance instance = Model_Instance.getById(instance_id);

            instance.name = help.name;
            instance.description = help.description;

            // Update Objektu
            instance.update();

            return ok(instance);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "tag Instance",
            tags = {"Instance"},
            notes = "", //TODO
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Instance.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result instance_addTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help = baseFormFactory.formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_Instance instance = Model_Instance.getById(help.object_id);

            // Přidání Tagu
            instance.addTags(help.tags);

            // Vrácení objektu
            return ok(instance);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "untag Instance",
            tags = {"Instance"},
            notes = "", //TODO
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Instance.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result instance_removeTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help = baseFormFactory.formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_Instance instance = Model_Instance.getById(help.object_id);

            // Odebrání Tagu
            instance.removeTags(help.tags);

            // Vrácení objektu
            return ok(instance);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "remove Instance",
            tags = {"Instance"},
            notes = "", //TODO
            //TODO
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instance_delete(UUID instance_id) {
        try {

            Model_Instance instance = Model_Instance.getById(instance_id);
            instance.delete();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "shutdown Instance",
            tags = {"Instance"},
            notes = "stop instance of blocko and remove from cloud.",
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
    public Result instance_shutdown(UUID instance_id) {
        try {

            Model_Instance instance = Model_Instance.getById(instance_id);

            instance.stop();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "create InstanceSnapshot",
            tags = {"Instance"},
            notes = "", //TODO
            produces = "application/json",
            consumes = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_InstanceSnapshot_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully updated",      response = Model_InstanceSnapshot.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result instanceSnapshot_create(UUID instance_id) {
        try {

            // Get and Validate Object
            Swagger_InstanceSnapshot_New help = baseFormFactory.formFromRequestWithValidation(Swagger_InstanceSnapshot_New.class);

            // Kontrola objektu
            System.out.println("Kontrola ID instance_id " + instance_id);

            Model_Instance instance = Model_Instance.getById(instance_id);

            System.out.println("Kontrola help.version_id " + help.version_id);

            Model_BProgramVersion version = Model_BProgramVersion.getById(help.version_id);

            System.out.println("Kontrola version jsem našel " + version.id);

            System.out.println("JE v BProgram Verzi nějaký Grid??  Size: " + version.get_grid_project_snapshots().size());

            Model_InstanceSnapshot snapshot = new Model_InstanceSnapshot();
            snapshot.name = help.name;
            snapshot.description = help.description;
            snapshot.b_program_version = version;
            snapshot.instance = instance;
            snapshot.save();

            System.out.println("Kontrola version zeptám se na path ");
            System.out.println("Kontrola version: path: "+  snapshot.get_path());


            snapshot.program = Model_Blob.upload(help.json().toString(), "snapshot.json", snapshot.get_path());

            System.out.println("Program Uložen");

            snapshot.update();

            System.out.println("Snapshot Uložen");

            System.out.println("Uložil jseml snapshot " + snapshot.id);


            return created(snapshot);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit InstanceSnapshot",
            tags = {"Instance"},
            notes = "", //TODO
            produces = "application/json",
            consumes = "application/json",
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
            @ApiResponse(code = 200, message = "Successfully updated",      response = Model_InstanceSnapshot.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result instanceSnapshot_udpate(UUID snapshot_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.getById(snapshot_id);

            snapshot.name = help.name;
            snapshot.description = help.description;
            snapshot.update();

            return ok(snapshot);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get InstanceSnapshot",
            tags = {"Instance"},
            notes = "", //TODO
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_InstanceSnapshot.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instanceSnapshot_get(UUID snapshot_id) {
        try {

            Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.getById(snapshot_id);

            return ok(snapshot);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "deploy InstanceSnapshot",
            tags = {"Instance"},
            notes = "deploy instance of blocko to cloud.",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_InstanceSnapshot_Deploy",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully uploaded",     response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result instanceSnapshot_deploy() {
        try {

            // Get and Validate Object
            Swagger_InstanceSnapshot_Deploy help = baseFormFactory.formFromRequestWithValidation(Swagger_InstanceSnapshot_Deploy.class);

            // Kontrola objektu: Verze B programu kterou budu nahrávat do cloudu
            Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.getById(help.snapshot_id);

            // If upload time is "future"
            if (help.upload_time != null && help.upload_time != 0L) {

                logger.trace("instanceSnapshot_deploy:: Deploy Snapshot by Time");

                // Genereate Future Time
                Date future = new Date(help.upload_time);

                // Zkontroluji smysluplnost časové známky
                if (!future.after(new Date())) return badRequest("time must be set in the future");

                // Deployed is future
                snapshot.deployed = future;
                scheduler.scheduleInstanceDeployment(snapshot);

            } else {

                logger.trace("instanceSnapshot_deploy:: Deploy Snapshot Immediately");

                // Deploy immediately!
                snapshot.deployed = new Date();
                snapshot.update();

                snapshot.deploy();
            }

            return ok();

        } catch (Exception e) {
            e.printStackTrace();
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete InstanceSnapshot",
            tags = {"Instance"},
            notes = "delete Instance",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully deleted",      response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instanceSnapshot_delete(UUID snapshot_id) {
        try {

            Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.getById(snapshot_id);

            snapshot.delete();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
    
    @ApiOperation(value = "get Instance by Filter",
            tags = { "Instance"},
            notes = "Get List of Instances. According to permission - system return only Instance from project, where is user owner or" +
                    " all Instances if user have static Permission key",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Instance_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Instance_List.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_b_program_instance_by_filter(Integer page_number) {
        try {

            // Get and Validate Object
            Swagger_Instance_Filter help = baseFormFactory.formFromRequestWithValidation(Swagger_Instance_Filter.class);


            // Tvorba parametru dotazu
            Query<Model_Instance> query = Ebean.find(Model_Instance.class);
            query.where().eq("deleted", false);

            if (!help.instance_types.isEmpty() ) {
                query.where().in("instance_type", help.instance_types);
            }

            if (help.project_id != null ) {
                query.where().eq("project_id", help.project_id);
            }
            if (help.project_id == null) {
                query.where().isNull("project.id");
            }

            if (!help.server_unique_ids.isEmpty()) {
                query.where().in("server_main.id", help.server_unique_ids);
            }

            // Vytvářím seznam podle stránky
            Swagger_Instance_List result = new Swagger_Instance_List(query, page_number, help);

            // Vracím seznam
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "update Instance Grid Settings",
            tags = { "Instance"},
            notes = "",         //TODO
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_InstanceSnapShotConfiguration",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_InstanceSnapshot.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result instance_change_settings_grid_App(UUID snapshot_id) {
        try {

            // Get and Validate Object
            Swagger_InstanceSnapShotConfiguration help = baseFormFactory.formFromRequestWithValidation(Swagger_InstanceSnapShotConfiguration.class);


            // Hledám objekt
            Model_InstanceSnapshot snapshot_settings = Model_InstanceSnapshot.getById(snapshot_id);

            // Měním parameter
            snapshot_settings.json_additional_parameter = Json.toJson(help).toString();

            // Update
            snapshot_settings.update();

            // Vracím Objekt
            return ok(snapshot_settings);

        } catch (IllegalArgumentException e) {

            logger.internalServerError(new Exception("Incoming snapshot_settings is invalid."));
            return badRequest("snapshot_settings is not valid");

        } catch (Exception e) {

            return controllerServerError(e);
        }
    }

    // INSTANCE - API KEY  #############################################################################################

    @ApiOperation(value = "add Instance Api Key",
            tags = {"Instance"},
            notes = "add new Api key for selected instance",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Instance_Token",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 201, message = "Ok Result",                 response = Model_InstanceSnapshot.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instance_create_api_key(UUID instance_id) {
        try {

            // Get and Validate Object
            Swagger_Instance_Token help = baseFormFactory.formFromRequestWithValidation(Swagger_Instance_Token.class);

            Model_Instance instance = Model_Instance.getById(instance_id);

            Model_InstanceSnapshot current_snapshot = instance.current_snapshot();

            if(current_snapshot == null) {
                return notFound(Model_InstanceSnapshot.class);
            }

            Swagger_InstanceSnapShotConfiguration settings = current_snapshot.settings();

            Swagger_InstanceSnapShotConfigurationApiKeys key = new Swagger_InstanceSnapShotConfigurationApiKeys();

            key.token = UUID.randomUUID().toString();

            key.description = help.description;
            key.created = new Date().getTime();
            settings.api_keys.add(key);


            current_snapshot.json_additional_parameter = Json.toJson(settings).toString();
            current_snapshot.update();

            return created(current_snapshot);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "update Instance Api Key",
            tags = {"Instance"},
            notes = "update Api key for selected instance",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Instance_Token",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_InstanceSnapshot.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instance_edit_api_key(UUID instance_id, UUID token) {
        try {

            // Get and Validate Object
            Swagger_Instance_Token help = baseFormFactory.formFromRequestWithValidation(Swagger_Instance_Token.class);

            Model_Instance instance = Model_Instance.getById(instance_id);

            Model_InstanceSnapshot current_snapshot = instance.current_snapshot();

            if(current_snapshot == null) {
                return notFound(Model_InstanceSnapshot.class);
            }

            Swagger_InstanceSnapShotConfiguration settings = current_snapshot.settings();


            for(int i = 0; i <  settings.api_keys.size(); i++) {

                Swagger_InstanceSnapShotConfigurationApiKeys key = settings.api_keys.get(i);
                if(key.token.equals(token)){

                    settings.api_keys.get(i).description = help.description;
                    break;
                }
            }

            current_snapshot.json_additional_parameter = Json.toJson(settings).toString();
            current_snapshot.update();

            current_snapshot.update();

            return ok(current_snapshot);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "remove Instance Api Key",
            tags = {"Instance"},
            notes = "remove Api key for selected instance",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_InstanceSnapshot.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instance_remove_api_key(UUID instance_id, UUID token) {
        try {

            Model_Instance instance = Model_Instance.getById(instance_id);

            Model_InstanceSnapshot current_snapshot = instance.current_snapshot();

            if(current_snapshot == null) {
                return notFound(Model_InstanceSnapshot.class);
            }

            Swagger_InstanceSnapShotConfiguration settings = current_snapshot.settings();

            for(int i = 0; i <  settings.api_keys.size(); i++) {

                Swagger_InstanceSnapShotConfigurationApiKeys key = settings.api_keys.get(i);
                if(key.token.equals(token)){
                    settings.api_keys.remove(i);
                    break;
                }
            }

            current_snapshot.json_additional_parameter = Json.toJson(settings).toString();
            current_snapshot.update();

            return ok(current_snapshot);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    // INSTANCE - MESH NETWORK KEY  #############################################################################################

    @ApiOperation(value = "add Instance Mesh Network Key",
            tags = {"Instance"},
            notes = "add new Mesh Network key for selected instance",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Instance_Token",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 201, message = "Ok Result",                 response = Model_InstanceSnapshot.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instance_create_mesh_network_key(UUID instance_id) {
        try {

            // Get and Validate Object
            Swagger_Instance_Token help = baseFormFactory.formFromRequestWithValidation(Swagger_Instance_Token.class);

            Model_Instance instance = Model_Instance.getById(instance_id);

            Model_InstanceSnapshot current_snapshot = instance.current_snapshot();

            if(current_snapshot == null) {
                return notFound(Model_InstanceSnapshot.class);
            }

            Swagger_InstanceSnapShotConfiguration settings = current_snapshot.settings();

            Swagger_InstanceSnapShotConfigurationApiKeys key = new Swagger_InstanceSnapShotConfigurationApiKeys();


            StringBuilder sb = new StringBuilder(32);
            for(int i = 0; i < 32; i++) {
                sb.append("0123456789abcdef".charAt(new Random().nextInt("0123456789abcdef".length())));
            }

            key.token = sb.toString();


            key.description = help.description;
            key.created = new Date().getTime();
            settings.mesh_keys.add(key);


            current_snapshot.json_additional_parameter = Json.toJson(settings).toString();
            current_snapshot.update();

            return created(current_snapshot);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "update Instance Mesh Network Key",
            tags = {"Instance"},
            notes = "update Mesh Network key for selected instance",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Instance_Token",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_InstanceSnapshot.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instance_edit_mesh_network_key(UUID instance_id, UUID token) {
        try {

            // Get and Validate Object
            Swagger_Instance_Token help = baseFormFactory.formFromRequestWithValidation(Swagger_Instance_Token.class);

            Model_Instance instance = Model_Instance.getById(instance_id);

            Model_InstanceSnapshot current_snapshot = instance.current_snapshot();

            if(current_snapshot == null) {
                return notFound(Model_InstanceSnapshot.class);
            }

            Swagger_InstanceSnapShotConfiguration settings = current_snapshot.settings();


            for(int i = 0; i <  settings.mesh_keys.size(); i++) {

                Swagger_InstanceSnapShotConfigurationApiKeys key = settings.mesh_keys.get(i);
                if(key.token.equals(token)){

                    settings.mesh_keys.get(i).description = help.description;
                    break;
                }
            }

            current_snapshot.json_additional_parameter = Json.toJson(settings).toString();
            current_snapshot.update();

            current_snapshot.update();

            return ok(current_snapshot);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "remove Instance Mesh Network Key",
            tags = {"Instance"},
            notes = "remove Mesh Network key for selected instance",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_InstanceSnapshot.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instance_remove_mesh_network_key(UUID instance_id, UUID token) {
        try {

            Model_Instance instance = Model_Instance.getById(instance_id);

            Model_InstanceSnapshot current_snapshot = instance.current_snapshot();

            if(current_snapshot == null) {
                return notFound(Model_InstanceSnapshot.class);
            }

            Swagger_InstanceSnapShotConfiguration settings = current_snapshot.settings();

            for(int i = 0; i <  settings.mesh_keys.size(); i++) {

                Swagger_InstanceSnapShotConfigurationApiKeys key = settings.mesh_keys.get(i);
                if(key.token.equals(token)){
                    settings.mesh_keys.remove(i);
                    break;
                }
            }

            current_snapshot.json_additional_parameter = Json.toJson(settings).toString();
            current_snapshot.update();

            return ok(current_snapshot);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// BLOCK ###############################################################################################################

    @ApiOperation(value = "create Block",
            tags = {"Block"},
            notes = "creating new independent Block object for Blocko tools",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_NameAndDesc_ProjectIdOptional",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Block.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something went wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result block_create() {
        try {

            // Get and Validate Object
            Swagger_NameAndDesc_ProjectIdOptional help = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDesc_ProjectIdOptional.class);


            Model_Project project = null;

            if (help.project_id == null) {
                if (Model_Block.getPublicByName(help.name) != null) {
                    return badRequest("Block with this name already exists, type a new one.");
                }
            } else {
                project = Model_Project.getById(help.project_id);
            }

            // Vytvoření objektu
            Model_Block block = new Model_Block();
            block.name = help.name;
            block.description = help.description;
            block.author_id = person().id;
            block.setTags(help.tags);

            if (project != null) {
                block.project = project;
                block.publish_type = ProgramType.PRIVATE;
            } else {
                block.publish_type = ProgramType.PUBLIC;
            }
            
            // Uložení objektu
            block.save();

            // Získání šablony
            Model_BlockVersion scheme = Model_BlockVersion.get_scheme();

            // Kontrola objektu
            if (scheme == null) return created(block);

            // Vytvoření objektu první verze
            Model_BlockVersion blockoBlockVersion = new Model_BlockVersion();
            blockoBlockVersion.name = "0.0.0";
            blockoBlockVersion.description = "This is a first version of block.";
            blockoBlockVersion.approval_state = Approval.APPROVED;
            blockoBlockVersion.design_json = scheme.design_json;
            blockoBlockVersion.logic_json = scheme.logic_json;
            blockoBlockVersion.block = block;
            blockoBlockVersion.save();

            // Vrácení objektu
            return created(block);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "clone Block",
            tags = {"Block"},
            notes = "clone Block for private",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"

    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Block_Copy",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Block.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result block_clone() {
        try {

            // Get and Validate Object
            Swagger_Block_Copy help = baseFormFactory.formFromRequestWithValidation(Swagger_Block_Copy.class);

            // Vyhledám Objekt
            Model_Block blockOld = Model_Block.getById(help.block_id);
        
            // Vyhledám Objekt
            Model_Project project = Model_Project.getById(help.project_id);

            Model_Block blockNew = new Model_Block();
            blockNew.name = help.name;
            blockNew.description = help.description;
            blockNew.project = project;

            // Duplicate all versions
            for (Model_BlockVersion version : blockOld.getVersions()) {

                Model_BlockVersion copy_object = new Model_BlockVersion();
                copy_object.name        = version.name;
                copy_object.description = version.description;
                copy_object.design_json = version.design_json;
                copy_object.design_json = version.design_json;
                copy_object.logic_json  = version.logic_json;
                copy_object.block       = blockNew;

            }

            blockNew.save();

            // Vracím Objekt
            return ok(blockNew);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Block",
            tags = {"Block"},
            notes = "update basic information (name, and description) of the independent Block",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Block.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result block_update(UUID block_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_Block block = Model_Block.getById(block_id);
            
            // Úprava objektu
            block.description = help.description;
            block.name        = help.name;

            // Uložení objektu
            block.update();

            // Set Tags
            block.setTags(help.tags);

            // Vrácení objektu
            return ok(block);

        } catch (Exception e) {
            return controllerServerError(e);
        }

    }

    @ApiOperation(value = "tag Block",
            tags = {"Block"},
            notes = "", //TODO
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Block.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result block_addTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help = baseFormFactory.formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_Block block = Model_Block.getById(help.object_id);

            // Add Tags
            block.addTags(help.tags);

            // Vrácení objektu
            return ok(block);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "untag Block",
            tags = {"Block"},
            notes = "", //TODO
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Block.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result block_removeTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help = baseFormFactory.formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_Block block = Model_Block.getById(help.object_id);

            // Remove Tags
            block.removeTags(help.tags);

            // Vrácení objektu
            return ok(block);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Block",
            tags = {"Block"},
            notes = "get independent Block object",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Block.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result block_get( UUID block_id) {
        try {

            // Kontrola objektu
            Model_Block block = Model_Block.getById(block_id);
         
            // Vrácení objektu
            return ok(block);

        } catch (Exception e) {
            return controllerServerError(e);
        }

    }

    @ApiOperation(value = "get Block List by Filter",
            tags = {"Block"},
            notes = "get Block List",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Block_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",             response = Swagger_Block_List.class),
            @ApiResponse(code = 400, message = "Invalid body",          response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",  response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",     response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result block_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number) {
        try {

            // Get and Validate Object
            Swagger_Block_Filter help = baseFormFactory.formFromRequestWithValidation(Swagger_Block_Filter.class);

            // Musí být splněna alespoň jedna podmínka, aby mohl být Junction aktivní. V opačném případě by totiž způsobil bychu
            // která vypadá nějak takto:  where t0.deleted = false and and .... KDE máme 2x end!!!!!
            if (!(help.project_id != null || help.public_programs || help.pending_blocks)) {
                return ok(new Swagger_Block_List());
            }

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_Block> query = Ebean.find(Model_Block.class);

            // query.orderBy("UPPER(name) ASC");
            query.where().ne("deleted", true);

            ExpressionList<Model_Block> list = query.where();
            Junction<Model_Block> disjunction = list.disjunction();

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

            if (help.pending_blocks) {
                disjunction
                        .conjunction()
                        .eq("versions.approval_state", Approval.PENDING.name())
                        .ne("publish_type", ProgramType.DEFAULT_MAIN)
                        .endJunction();
            }

            disjunction.endJunction();

            // Vytvoření odchozího JSON
            Swagger_Block_List result = new Swagger_Block_List(query, page_number,help);

            // Vrácení výsledku
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Block",
            tags = {"Block"},
            notes = "delete Block",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
            
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result block_delete(UUID block_id) {
        try {

            // Kontrola objektu
            Model_Block blockoBlock = Model_Block.getById(block_id);
       
            // Smazání objektu
            blockoBlock.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "orderUp Block",
            tags = {"Block"},
            notes = "set up order",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result block_orderUp(UUID block_id) {
        try {

            // Kontrola objektu
            Model_Block block = Model_Block.getById(block_id);

            // Shift order up
            block.up();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "orderDown Block",
            tags = {"Block"},
            notes = "set up order",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result block_orderDown(UUID block_id) {
        try {

            // Kontrola objektu
            Model_Block block =  Model_Block.getById(block_id);

            // Shift order down
            block.down();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "deactivate Block",
            tags = {"Admin-Block"},
            notes = "deactivate Block",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result block_deactivate(UUID block_id) {
        try {

            Model_Block block = Model_Block.getById(block_id);
            if (!block.active) return badRequest("Block is already deactivated");
            
            block.active = false;

            block.update();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "activate Block",
            tags = {"Admin-Block"},
            notes = "activate Block",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Tariff.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result block_activate(UUID block_id) {
        try {

            Model_Block block = Model_Block.getById(block_id);

            if (block.active) return badRequest("Block is already activated");
            block.active = true;

            block.update();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit BlockVersion Response publication",
            tags = {"Admin-Block"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            consumes = "application/json",
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
    @BodyParser.Of(BodyParser.Json.class)
    public Result block_public_response() { // TODO asi nebude fungovat korektně
        try {

            // Get and Validate Object
            Swagger_Community_Version_Publish_Response help = baseFormFactory.formFromRequestWithValidation(Swagger_Community_Version_Publish_Response.class);

            // Kontrola názvu
            if (help.version_name.equals("version_scheme")) return badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockVersion private_block_version = Model_BlockVersion.getById(help.version_id);

            // Kontrola nadřazeného objektu
            Model_Block block_old = private_block_version.get_block();

            // Zkontroluji oprávnění
            if (!block_old.community_publishing_permission()) {
                return forbidden();
            }

            if (help.decision) {

                private_block_version.approval_state = Approval.APPROVED;
                private_block_version.update();

                UUID block_previous_id = Model_Block.find.query().where().eq("original_id", block_old.id).select("id").findSingleAttribute();

                Model_Block block = null;

                if (block_previous_id == null) {
                    // Vytvoření objektu
                    block = new Model_Block();
                    block.original_id = block_old.id;
                    block.name = help.program_name;
                    block.description = help.program_description;
                    block.author_id = private_block_version.get_block().get_author().id;
                    block.publish_type = ProgramType.PUBLIC;
                    block.active = true;
                    block.save();
                } else {
                    block = Model_Block.getById(block_previous_id);
                }

                // Vytvoření objektu
                Model_BlockVersion version = new Model_BlockVersion();
                version.name = help.version_name;
                version.description = help.version_description;
                version.design_json = private_block_version.design_json;
                version.logic_json = private_block_version.logic_json;
                version.approval_state = Approval.APPROVED;
                version.block = block;
                version.save();

                block.refresh();

                // TODO notifikace a emaily

                return ok();

            } else {

                // Změna stavu schválení
                private_block_version.approval_state = Approval.DISAPPROVED;

                // Odeslání emailu s důvodem
                try {

                    new Email()
                            .text("Version of Widget " + private_block_version.get_block().name + ": " + Email.bold(private_block_version.name) + " was not approved for this reason: ")
                            .text(help.reason)
                            .send(private_block_version.get_block().get_author().email, "Version of Widget disapproved");

                } catch (Exception e) {
                    logger.internalServerError (e);
                }

                // Uložení změn
                private_block_version.update();

                // Vrácení výsledku
                return ok();
            }

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// BLOCK VERSION #######################################################################################################

    @ApiOperation(value = "create BlockVersion",
            tags = {"Block"},
            notes = "new Block version",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_BlockVersion_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_BlockVersion.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something went wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockVersion_create(UUID block_id) {
        try {

            // Get and Validate Object
            Swagger_BlockVersion_New help = baseFormFactory.formFromRequestWithValidation(Swagger_BlockVersion_New.class);

            // Kontrola názvu
            if (help.name.equals("version_scheme")) return badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_Block block = Model_Block.getById(block_id);

            // Vytvoření objektu
            Model_BlockVersion version = new Model_BlockVersion();
            version.name = help.name;
            version.description = help.description;
            version.design_json = help.design_json;
            version.logic_json = help.logic_json;
            version.block = block;

            // Uložení objektu
            version.save();

            // Vrácení objektu
            return created(block);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get BlockVersion",
            tags = {"Block"},
            notes = "get version (content) from independent Block",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockVersion_get(UUID version_id) {
        try {
            
            // Kontrola objektu
            Model_BlockVersion version = Model_BlockVersion.getById(version_id);
          
            // Vrácení objektu
            return ok(version);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit BlockVersion",
            tags = {"Block"},
            notes = "You can edit only basic information of the version. If you want to update the code, " +
                    "you have to create a new version!",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_NameAndDescription",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockVersion.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something went wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockVersion_update(UUID version_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola názvu
            if (help.name.equals("version_scheme")) return badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockVersion version = Model_BlockVersion.getById(version_id);
  
            // Úprava objektu
            version.name = help.name;
            version.description = help.description;

            // Uložení objektu
            version.update();

            // Vrácení objektu
            return ok(version);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete BlockVersion",
            tags = {"Block"},
            notes = "delete Block version",
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
    public Result blockVersion_delete(UUID version_id) {
        try {

            // Kontrola objektu
            Model_BlockVersion version = Model_BlockVersion.getById(version_id);

            // Smazání objektu
            version.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "publish BlockVersion",
            tags = {"Block"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockVersion_make_public(UUID version_id) {
        try {

            // Kontrola objektu
            Model_BlockVersion version = Model_BlockVersion.getById(version_id);

            if (Model_WidgetVersion.find.query().where().eq("approval_state", Approval.PENDING.name())
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

            // Vrácení výsledku
            return ok(version);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "setMain BlockVersion",
            tags = {"Admin-Block"},
            notes = "",     //TODO
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 403, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockVersion_setMain(UUID version_id) {
        try {

            // Kontrola objektu
            Model_BlockVersion version = Model_BlockVersion.getById(version_id);

            if (!version.get_block_id().equals(UUID.fromString("00000000-0000-0000-0000-000000000001"))) {
                return notFound("BlockVersion not from default program");
            }

            Model_BlockVersion old_version = Model_BlockVersion.find.query().where().eq("publish_type", ProgramType.DEFAULT_VERSION.name()).select("id").findOne();
            if (old_version != null) {
                old_version = Model_BlockVersion.getById(old_version.id);
                old_version.publish_type = null;
                old_version.update();
            }

            version.publish_type = ProgramType.DEFAULT_VERSION;
            version.update();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// BLOCKO ADMIN ########################################################################################################*/

    @ApiOperation(value = "edit BlockVersion refuse publication",
            tags = {"Admin-Block"},
            notes = "sets disapproved from pending",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_BlockoObject_Approval",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoDisapprove() {
        try {

            // Get and Validate Object
            Swagger_BlockoObject_Approval help = baseFormFactory.formFromRequestWithValidation(Swagger_BlockoObject_Approval.class);

            // Kontrola objektu
            Model_BlockVersion version = Model_BlockVersion.getById(help.object_id);
          
            // Změna stavu schválení
            version.approval_state = Approval.DISAPPROVED;

            // Odeslání emailu s důvodem
            try {
                new Email()
                        .text("Version of Block " + version.get_block().name + ": " + Email.bold(version.name) + " was not approved for this reason: ")
                        .text(help.reason)
                        .send(version.get_block().get_author().email, "Version of Block disapproved" );

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            // Uložení změn
            version.update();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit BlockVersion accept publication",
            tags = {"Admin-Block"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_BlockoObject_Approve_withChanges",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoApproval() {
        try {

            // Get and Validate Object
            Swagger_BlockoObject_Approve_withChanges help = baseFormFactory.formFromRequestWithValidation(Swagger_BlockoObject_Approve_withChanges.class);

            // Kontrola názvu
            if (help.version_name.equals("version_scheme")) return badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockVersion privateVersion = Model_BlockVersion.getById(help.object_id);

            // Vytvoření objektu
            Model_Block block = new Model_Block();
            block.name = help.name;
            block.description = help.description;
            block.author_id = privateVersion.get_block().get_author().id;
            block.save();

            // Vytvoření objektu
            Model_BlockVersion version = new Model_BlockVersion();
            version.name = help.version_name;
            version.description = help.version_description;
            version.design_json = help.design_json;
            version.logic_json = help.logic_json;
            version.approval_state = Approval.APPROVED;
            version.block = block;
            version.save();

            // Pokud jde o schválení po ediatci
            if (help.state.equals("edit")) {
                privateVersion.approval_state = Approval.EDITED;

                // Odeslání emailu
                try {
                    new Email()
                            .text("Version of Block " + version.get_block().name + ": " + Email.bold(version.name) + " was edited before publishing for this reason: ")
                            .text(help.reason)
                            .send(version.get_block().get_author().email, "Version of Block edited" );

                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            } else privateVersion.approval_state = Approval.APPROVED;

            // Uložení úprav
            privateVersion.update();

            // Vrácení výsledku
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}