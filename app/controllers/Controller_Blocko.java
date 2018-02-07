package controllers;

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
import utilities.enums.GridAccess;
import utilities.enums.ProgramType;
import utilities.logger.Logger;
import utilities.scheduler.SchedulerController;
import utilities.swagger.input.*;
import utilities.swagger.output.filter_results.Swagger_B_Program_List;
import utilities.swagger.output.filter_results.Swagger_Block_List;
import utilities.swagger.output.filter_results.Swagger_Instance_List;
import utilities.swagger.output.Swagger_B_Program_Version;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Security.Authenticated(Authentication.class)
@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Blocko extends BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Blocko.class);

    private FormFactory formFactory;
    private SchedulerController scheduler;

    @Inject
    public Controller_Blocko(FormFactory formFactory, SchedulerController scheduler) {
        this.formFactory = formFactory;
        this.scheduler = scheduler;
    }
    
// B PROGRAM ###########################################################################################################

    @ApiOperation(value = "create B_Program",
            tags = {"B_Program"},
            notes = "create new B_Program",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
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
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_BProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bProgram_create(@ApiParam(value = "project_id String path", required = true) String project_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_NameAndDescription help = form.get();

            // Kontrola objektu
            Model_Project project = Model_Project.getById(project_id);
            if (project == null) return notFound("Project project_id not found");

            // Kontrola oprávnění
            if (!project.update_permission() ) return forbiddenEmpty();

            // Tvorba programu
            Model_BProgram b_program        = new Model_BProgram();
            b_program.description           = help.description;
            b_program.name                  = help.name;
            b_program.project               = project;

            // Kontrola oprávnění těsně před uložením
            if (!b_program.create_permission() ) return forbiddenEmpty();

            // Uložení objektu
            b_program.save();

            // Vrácení objektu
            return created(Json.toJson(b_program));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get B_Program",
            tags = {"B_Program"},
            notes = "get B_Program object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BProgram.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result bProgram_get(@ApiParam(value = "b_program_id String path", required = true) String b_program_id) {
        try {

            // Kontrola objektu
            Model_BProgram b_program = Model_BProgram.getById(b_program_id);
            if (b_program == null) return notFound("B_Program id not found");

            // Kontrola oprávnění
            if (!b_program.read_permission() ) return forbiddenEmpty();

            // Vrácení objektu
            return ok(Json.toJson(b_program));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get B_Program by Filter",
            tags = {"B_Program"},
            notes = "get B_Program List",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "B_Program_read_permission", value = "No need to check permission, because Tyrion returns only those results which user owns"),
                    }),
            }
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",             response = Swagger_B_Program_List.class),
            @ApiResponse(code = 400, message = "Invalid body",          response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",  response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",     response = Result_InternalServerError.class)
    })
    public Result bProgram_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number) {
        try {

            // Získání JSON
            final Form<Swagger_B_Program_Filter> form = formFactory.form(Swagger_B_Program_Filter.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_B_Program_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_BProgram> query = Ebean.find(Model_BProgram.class);
            query.where().eq("project.participants.person.id", BaseController.personId());

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if (help.project_id != null) {

                query.where().eq("project.id", help.project_id);
            }

            // Vytvoření odchozího JSON
            Swagger_B_Program_List result = new Swagger_B_Program_List(query, page_number);

            // Vrácení výsledku
            return ok(Json.toJson(result));

        } catch (Exception e) {
            return internalServerError(e);
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bProgram_update(@ApiParam(value = "b_program_id String path", required = true) String b_program_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_NameAndDescription help = form.get();

            // Kontrola objektu
            Model_BProgram b_program = Model_BProgram.getById(b_program_id);
            if (b_program == null) return notFound("B_Program not found");

            // Kontrola oprávěnní
            if (!b_program.edit_permission()) return forbiddenEmpty();

            // Úprava objektu
            b_program.description = help.description;
            b_program.name        = help.name;

            // Uložení objektu
            b_program.update();

            // Vrácení objektu
            return ok(Json.toJson(b_program));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete B_Program",
            tags = {"B_Program"},
            notes = "remove B_Program object",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.delete_permission", value = "true"),
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
    public Result bProgram_delete(@ApiParam(value = "b_program_id String path", required = true) String b_program_id) {
        try {

            // Kontrola objektu
            Model_BProgram program = Model_BProgram.getById(b_program_id);
            if (program == null) return notFound("B_Program id not found");

            // Kontrola oprávění
            if (! program.delete_permission() ) return forbiddenEmpty();

            // Smazání objektu
            program.delete();

            // Vrácení potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

// B PROGRAM VERSION ###################################################################################################

    @ApiOperation(value = "create B_Program_Version",
            tags = {"B_Program"},
            notes = "create new vesion in Blocko program",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.update_permission", value = "true"),
                    })
            }
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_B_Program_Version.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bProgramVersion_create(@ApiParam(value = "b_program_id String path", required = true) String b_program_id) {
        try {

            // Zpracování Json
            final Form<Swagger_B_Program_Version_New> form = formFactory.form(Swagger_B_Program_Version_New.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_B_Program_Version_New help = form.get();

            // Program který budu ukládat do data Storage v Azure
            String file_content =  help.program;

            // Ověření programu
            Model_BProgram b_program = Model_BProgram.getById(b_program_id);
            if (b_program == null) return notFound("B_Program id not found");

            // Kontrola oprávnění
            if (!b_program.update_permission()) return forbiddenEmpty();

            // První nová Verze
            Model_Version version = new Model_Version();
            version.name        = help.name;
            version.description = help.description;
            version.b_program   = b_program;
            version.author      = person();

            // Vytvořím Snapshoty Verze M_Programu
            if (help.m_project_snapshots != null) {

                for (Swagger_B_Program_Version_New.M_Project_SnapShot help_m_project_snap : help.m_project_snapshots) {

                    Model_MProject m_project = Model_MProject.getById(help_m_project_snap.m_project_id);
                    if (m_project == null) return notFound("M_Project not found");
                    if (!m_project.update_permission()) return forbiddenEmpty();

                    Model_MProjectProgramSnapShot snap = new Model_MProjectProgramSnapShot();
                    snap.m_project = m_project;

                    for (Swagger_B_Program_Version_New.M_Program_SnapShot help_m_program_snap : help_m_project_snap.m_program_snapshots) {
                        Model_Version m_program_version = Model_Version.find.query().where().eq("id", help_m_program_snap.version_object_id).eq("m_program.id", help_m_program_snap.m_program_id).eq("m_program.m_project.id", m_project.id).findOne();

                        if (m_program_version == null) return notFound("M_Program Version id not found");

                        Model_MProgramInstanceParameter snap_shot_parameter = new Model_MProgramInstanceParameter();

                        snap_shot_parameter.m_program_version = m_program_version;
                        snap_shot_parameter.m_project_program_snapshot = snap;

                        snap.m_program_snapshots.add(snap_shot_parameter);
                    }

                    version.b_program_version_snapshots.add(snap);
                }
            }

            // Uložení objektu
            version.save();

            // Nahrání na Azure
            Model_Blob.uploadAzure_Version(file_content, "blocko.json", b_program.get_path() , version);

            // Vrácení objektu
            return ok(Json.toJson(version.get_b_program().program_version(version)));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get B_Program_Version",
            tags = {"B_Program"},
            notes = "get B_Program version object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.read_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_B_Program_Version.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result bProgramVersion_get(@ApiParam(value = "version_id String path", required = true) String version_id) {
        try {

            // Kontrola objektu
            Model_Version version_object = Model_Version.getById(version_id);
            if (version_object == null) return notFound("Version_Object version_id not found");

            // Kontrola oprávnění
            if (version_object.get_b_program() == null) return notFound("Version_Object is not version of B_Program");

            // Kontrola oprávnění
            if (!version_object.get_b_program().read_permission()) return forbiddenEmpty();

            // Vrácení objektu
            return ok(Json.toJson(version_object.get_b_program().program_version(version_object)));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit B_Program_Version",
            tags = {"B_Program"},
            notes = "edit Version object",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.delete_permission", value = "true"),
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bProgramVersion_update(@ApiParam(value = "version_id String path", required = true) String version_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_NameAndDescription help = form.get();

            // Získání objektu
            Model_Version version_object = Model_Version.getById(version_id);
            if (version_object == null) return notFound("Version not found");

            version_object.name = help.name;
            version_object.description = help.description;

            // Kontrola oprávnění
            if (!version_object.get_b_program().edit_permission()) return forbiddenEmpty();

            // Smazání objektu
            version_object.update();

            // Vrácení potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete B_Program_Version",
            tags = {"B_Program"},
            notes = "remove B_Program version object",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.delete_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result bProgramVersion_delete(@ApiParam(value = "version_id String path", required = true) String version_id) {
        try {

            // Získání objektu
            Model_Version version_object  = Model_Version.getById(version_id);

            // Kontrola objektu
            if (version_object == null) return notFound("Version not found");
            if (version_object.get_b_program() == null) return badRequest("BProgram not found");

            // Kontrola oprávnění
            if (!version_object.get_b_program().delete_permission()) return forbiddenEmpty();

            // Smazání objektu
            version_object.delete();

            // Vrácení potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

// INSTANCE ############################################################################################################

    @ApiOperation(value = "create Instance",
            tags = {"Instance"},
            notes = "",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_NameAndDesc_ProjectIdRequired",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
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

            // Zpracování Json
            final Form<Swagger_NameAndDesc_ProjectIdRequired> form = formFactory.form(Swagger_NameAndDesc_ProjectIdRequired.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_NameAndDesc_ProjectIdRequired help = form.get();

            Model_Project project = Model_Project.getById(help.project_id);
            if (project == null) return notFound("Project not found");

            // Kontrola objektu
            Model_Instance instance = new Model_Instance();
            instance.name = help.name;
            instance.description = help.description;
            instance.project = project;

            if (!instance.create_permission()) return forbiddenEmpty();

            instance.save();

            return created(instance.json());

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Instance",
            tags = {"Instance"},
            notes = "",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Instance.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instance_get(@ApiParam(value = "instance_id String path", required = true) String instance_id) {
        try {

            Model_Instance instance = Model_Instance.getById(instance_id);
            if (instance == null) return notFound("Instance not found");

            if (!instance.read_permission()) return forbiddenEmpty();

            return ok(instance.json());

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Instance",
            tags = {"Instance"},
            notes = "",
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated",      response = Model_Instance.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result instance_update(@ApiParam(value = "instance_id String path", required = true) String instance_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_NameAndDescription help = form.get();

            // Kontrola objektu
            Model_Instance instance = Model_Instance.getById(instance_id);
            if (instance == null) return notFound("Instance not found");

            if (!instance.update_permission()) return forbiddenEmpty();

            instance.name = help.name;
            instance.description = help.description;

            instance.update();

            return ok(instance.json());

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete Instance",
            tags = {"Instance"},
            notes = "",
            protocols = "https"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instance_delete(@ApiParam(value = "instance_id String path", required = true) String instance_id) {
        try {

            // Kontrola objektu
            Model_Instance instance = Model_Instance.getById(instance_id);
            if (instance == null) return notFound("Instance not found");

            if (!instance.delete_permission()) return forbiddenEmpty();

            instance.delete();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "create InstanceSnapshot",
            tags = {"Instance"},
            notes = "",
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
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully updated",      response = Model_InstanceSnapshot.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result instanceSnapshot_create() {
        try {

            // Zpracování Json
            final Form<Swagger_InstanceSnapshot_New> form = formFactory.form(Swagger_InstanceSnapshot_New.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_InstanceSnapshot_New help = form.get();

            Model_Instance instance = Model_Instance.getById(help.instance_id);
            if (instance == null) return notFound("Instance not found");

            Model_Version version = Model_Version.getById(help.version_id);
            if (version == null) return notFound("Version not found");

            Model_InstanceSnapshot snapshot = new Model_InstanceSnapshot();
            snapshot.b_version = version;
            snapshot.instance = instance;
            snapshot.program = Model_Blob.upload(help.snapshot, "snapshot.json", "TODO" );

            return created(snapshot.json());

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get InstanceSnapshot",
            tags = {"Instance"},
            notes = "",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_InstanceSnapshot.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instanceSnapshot_get(@ApiParam(value = "snapshot_id String path", required = true) String snapshot_id) {
        try {

            Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.getById(snapshot_id);
            if (snapshot == null) return notFound("Snapshot not found");

            if (!snapshot.read_permission()) return forbiddenEmpty();

            return ok(snapshot.json());

        } catch (Exception e) {
            return internalServerError(e);
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
    @ApiResponses(value = {
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

            // Získání JSON
            final Form<Swagger_InstanceSnapshot_Deploy> form = formFactory.form(Swagger_InstanceSnapshot_Deploy.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_InstanceSnapshot_Deploy help = form.get();

            // Kontrola objektu: Verze B programu kterou budu nahrávat do cloudu
            Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.getById(help.snapshot_id);
            if (snapshot == null) return notFound("Snapshot not found");

            // Kontrola oprávnění
            if (!snapshot.update_permission()) return forbiddenEmpty();

            if (help.upload_time != null) {

                Date future = new Date(help.upload_time);

                // Zkontroluji smysluplnost časové známky
                if (!future.after(new Date())) return badRequest("time must be set in the future");
                snapshot.deployed = future;
                scheduler.scheduleInstanceDeployment(snapshot);
            } else {
                snapshot.deployed = new Date();
                snapshot.deploy();
            }

            snapshot.update();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "shutdown InstanceSnapshot",
            tags = {"Instance"},
            notes = "stop instance of blocko and remove from cloud.",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instanceSnapshot_shutdown(@ApiParam(value = "snapshot_id String path", required = true) String snapshot_id) {
        try {

            Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.getById(snapshot_id);
            if (snapshot == null) return notFound("Instance not found");

            if (!snapshot.update_permission()) return forbiddenEmpty();

            snapshot.stop();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete InstanceSnapshot",
            tags = {"Instance"},
            notes = "delete Instance",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully deleted",      response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instanceSnapshot_delete(@ApiParam(value = "snapshot_id String path", required = true) String snapshot_id) {
        try {

            Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.getById(snapshot_id);
            if (snapshot == null) return notFound("Instance not found");

            if (!snapshot.delete_permission()) return forbiddenEmpty();

            snapshot.delete();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "set Instance start or shutDown",
            tags = {"Instance"},
            notes = "If instance is not running this Command uploud instance to cloud and starter all procedures. " +
                    "If instance is online, stis Command shutdown instance immidietly with all procedures.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully removed",      response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instance_start_or_shut_down(String instance_name) {
        try {

            // Kontrola objektu
            Model_Instance homer_instance = Model_Instance.getById(instance_name);
            if (homer_instance == null) return notFound("Homer_Instance id not found");

            if (!homer_instance.update_permission()) return forbiddenEmpty();

            // Pokud má aktuální instance "Actual Instance record - znaemná to, že má běžet v cloudu"
            // Proto tento záznam odstraním
            /*if (homer_instance.get_current_snapshot() != null) {

                homer_instance.remove_from_cloud();
                r

            } else {

                if (homer_instance.instance_history.isEmpty()) {
                     return badRequest("We did not find any previous version running in the cloud. Please first select version in Blocko editor run.");
                }

                homer_instance.actual_instance = homer_instance.instance_history.get(0);
                homer_instance.update();

                homer_instance.get_current_snapshot().put_record_into_cloud();
            }    TODO
                */

                return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Instances List by Project",
            tags = {"Instance"},
            notes = "get list of instance_ids details under project id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.update_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully uploaded",     response = Model_Instance.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result get_b_program_instance_under_project(String project_id) {
        try {

            List<Model_Instance> instances = Model_Instance.find.query().where()
                    .isNotNull("actual_instance")
                    .eq("b_program.project.id", project_id)
                    .findList();

            return ok(Json.toJson(instances));

        } catch (Exception e) {
            return internalServerError(e);
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Instance_List.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_b_program_instance_by_filter(Integer page_number) {
        try {

            // Zpracování Json
            final Form<Swagger_Instance_Filter> form = formFactory.form(Swagger_Instance_Filter.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Instance_Filter help = form.get();

            // Tvorba parametru dotazu
            Query<Model_Instance> query = Ebean.find(Model_Instance.class);

            // If Json contains TypeOfBoards list of id's
            if (!help.instance_types.isEmpty() ) {
                query.where().in("instance_type", help.instance_types);
            }

            if (help.project_id != null ) {
                query.where().eq("project_id", help.project_id);
            }

            if (!help.server_unique_ids.isEmpty()) {
                query.where().in("cloud_homer_server.id", help.server_unique_ids);
            }

            // Vytvářím seznam podle stránky
            Swagger_Instance_List result = new Swagger_Instance_List(query, page_number);

            // Vracím seznam
            return ok(Json.toJson(result));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "update Instance Grid Settings",
            tags = { "Instance"},
            notes = "",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Instance_GridApp_Settings",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_MProgramInstanceParameter.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result instance_change_settings_grid_App() {
        try {

            // Zpracování Json
            final Form<Swagger_Instance_GridApp_Settings> form = formFactory.form(Swagger_Instance_GridApp_Settings.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Instance_GridApp_Settings help = form.get();

            // Hledám objekt
            Model_MProgramInstanceParameter program_parameter = Model_MProgramInstanceParameter.getById(help.m_program_parameter_id);
            if (program_parameter == null) return notFound("Object not found");

            //Ohlídám oprávnění
            if (!program_parameter.edit_permission()) return forbiddenEmpty();

            //PArsuju Enum kdyžtak chyba IllegalArgumentException
            GridAccess settings = GridAccess.valueOf(help.snapshot_settings);

            // Měním parameter
            program_parameter.snapshot_settings = settings;

            // Update
            program_parameter.update();

            // Vracím Objekt
            return ok(Json.toJson(program_parameter));

        } catch (IllegalArgumentException e) {

            logger.internalServerError(new Exception("Incoming snapshot_settings is invalid."));
            return badRequest("snapshot_settings is not valid");

        } catch (Exception e) {

            return internalServerError(e);
        }
    }

// BLOCK ###############################################################################################################

    @ApiOperation(value = "create BlockoBlock",
            tags = {"Blocko-Block"},
            notes = "creating new independent Block object for Blocko tools",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Block_create_permission", value = Model_Block.create_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBlock.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Block_create_permission" )
                    })
            }
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
    @ApiResponses(value = {
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

            // Zpracování Json
            final Form<Swagger_NameAndDesc_ProjectIdOptional> form = formFactory.form(Swagger_NameAndDesc_ProjectIdOptional.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_NameAndDesc_ProjectIdOptional help = form.get();

            Model_Project project = null;

            if (help.project_id == null ) {
                if (Model_Block.getPublicByName(help.name) != null) {
                    return badRequest("Block with this name already exists, type a new one.");
                }
            } else {
                project = Model_Project.getById(help.project_id);
                if (project == null) return notFound("Project not found");
            }

            // Vytvoření objektu
            Model_Block block = new Model_Block();
            block.name = help.name;
            block.description = help.description;
            block.author = person();

            if (project != null) {
                block.project = project;
                block.publish_type = ProgramType.PRIVATE;
            } else {
                block.publish_type = ProgramType.PUBLIC;
            }

            // Kontrola oprávnění těsně před uložením
            if (!block.create_permission()) return forbiddenEmpty();

            // Uložení objektu
            block.save();

            // Získání šablony
            Model_BlockVersion scheme = Model_BlockVersion.get_scheme();

            // Kontrola objektu
            if (scheme == null) return created(Json.toJson(block));

            // Vytvoření objektu první verze
            Model_BlockVersion blockoBlockVersion = new Model_BlockVersion();
            blockoBlockVersion.name = "0.0.0";
            blockoBlockVersion.description = "This is a first version of block.";
            blockoBlockVersion.approval_state = Approval.APPROVED;
            blockoBlockVersion.design_json = scheme.design_json;
            blockoBlockVersion.logic_json = scheme.logic_json;
            blockoBlockVersion.block = block;
            blockoBlockVersion.author = BaseController.person();
            blockoBlockVersion.save();

            // Vrácení objektu
            return created(Json.toJson(block));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "make_Clone BlockoBlock",
            tags = {"Blocko-Block"},
            notes = "clone Blocko Block for private",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Block.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result block_clone() {
        try {

            // Zpracování Json
            final Form<Swagger_Block_Copy> form = formFactory.form(Swagger_Block_Copy.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Block_Copy help = form.get();

            // Vyhledám Objekt
            Model_Block blockOld = Model_Block.getById(help.block_id);
            if (blockOld == null) return notFound("Block not found");

            // Zkontroluji oprávnění
            if (!blockOld.read_permission()) return forbiddenEmpty();

            // Vyhledám Objekt
            Model_Project project = Model_Project.getById(help.project_id);
            if (project == null) return notFound("Project not found");

            // Zkontroluji oprávnění
            if (!project.update_permission())  return forbiddenEmpty();

            Model_Block blockNew = new Model_Block();
            blockNew.name = help.name;
            blockNew.description = help.description;
            blockNew.project = project;
            blockNew.save();

            blockNew.refresh();

            for (Model_BlockVersion version : blockOld.getVersions()) {

                Model_BlockVersion copy_object = new Model_BlockVersion();
                copy_object.name        = version.name;
                copy_object.description = version.description;
                copy_object.author      = version.author;
                copy_object.design_json = version.design_json;
                copy_object.logic_json  = version.logic_json;
                copy_object.block       = blockNew;

                // Zkontroluji oprávnění
                copy_object.save();
            }

            blockNew.refresh();

            // Vracím Objekt
            return ok(Json.toJson(blockNew));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit BlockoBlock",
            tags = {"Blocko-Block"},
            notes = "update basic information (name, and description) of the independent BlockoBlock",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlock_edit_permission" )
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Block.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result block_update(@ApiParam(value = "block_id String path",   required = true)  String block_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_NameAndDescription help = form.get();

            // Kontrola objektu
            Model_Block block = Model_Block.getById(block_id);
            if (block == null) return notFound("Block not found");

            // Kontrola oprávnění
            if (!block.edit_permission()) return forbidden("You have no permission to edit");

            // Úprava objektu
            block.description = help.description;
            block.name        = help.name;

            // Uložení objektu
            block.update();

            // Vrácení objektu
            return ok(Json.toJson(block));

        } catch (Exception e) {
            return internalServerError(e);
        }

    }

    @ApiOperation(value = "get BlockoBlock",
            tags = {"Blocko-Block"},
            notes = "get independent BlockoBlock object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlock_read_permission", value = Model_Block.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlock_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Block.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result block_get(@ApiParam(value = "block_id String path",   required = true) String block_id) {
        try {
            // Kontrola objektu
            Model_Block block = Model_Block.getById(block_id);
            if (block == null) return notFound("Block not found");

            // Kontrola oprávnění
            if (!block.read_permission()) return forbiddenEmpty();

            // Vrácení objektu
            return ok(Json.toJson(block));

        } catch (Exception e) {
            return internalServerError(e);
        }

    }

    @ApiOperation(value = "get BlockoBlock by Filter",
            tags = {"Blocko-Block"},
            notes = "get BlockoBlock List",
            produces = "application/json",
            protocols = "https",
            code = 200
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",             response = Swagger_Block_List.class),
            @ApiResponse(code = 400, message = "Invalid body",          response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",  response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",     response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result block_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number) {
        try {

            // Získání JSON
            final Form<Swagger_Block_Filter> form = formFactory.form(Swagger_Block_Filter.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Block_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_Block> query = Ebean.find(Model_Block.class);
            query.where().eq("author.id", BaseController.personId());

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if (help.project_id != null) {

                query.where().eq("type_of_block.project.id", help.project_id);
            }

            // Vytvoření odchozího JSON
            Swagger_Block_List result = new Swagger_Block_List(query, page_number);

            // Vrácení výsledku
            return ok(Json.toJson(result));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete BlockoBlock",
            tags = {"Blocko-Block"},
            notes = "delete BlockoBlock",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlock_delete_permission")
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
    public Result block_delete(@ApiParam(value = "block_id String path",   required = true)  String block_id) {
        try {

            // Kontrola objektu
            Model_Block blockoBlock = Model_Block.getById(block_id);
            if (blockoBlock == null) return notFound("Block not found");

            // Kontrola oprávnění
            if (!blockoBlock.delete_permission()) return forbiddenEmpty();

            // Smazání objektu
            blockoBlock.delete();

            // Vrácení potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "order BlockoBlock Up",
            tags = {"Blocko-Block"},
            notes = "set up order",
            produces = "application/json",
            consumes = "text/html",
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
    public Result block_orderUp(@ApiParam(value = "block_version_id String path",   required = true) String block_id) {
        try {

            Model_Block block = Model_Block.getById(block_id);
            if (block == null) return notFound("Block not found");

            // Kontrola oprávnění
            if (!block.edit_permission()) return forbiddenEmpty();

            block.up();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "order BlockoBlock Down",
            tags = {"Blocko-Block"},
            notes = "set up order",
            produces = "application/json",
            consumes = "text/html",
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
    public Result block_orderDown(@ApiParam(value = "block_id String path",   required = true) String block_id) {
        try {

            Model_Block block =  Model_Block.getById(block_id);
            if (block == null) return notFound("Block not found");

            // Kontrola oprávnění
            if (!block.edit_permission()) return forbiddenEmpty();

            block.down();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "deactivate BlockoBlock",
            tags = {"Admin-Blocko-Block"},
            notes = "deactivate BlockoBlock",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result block_deactivate(String block_id) {
        try {

            Model_Block block = Model_Block.getById(block_id);
            if (block == null) return notFound("Block not found");

            // Kontrola oprávnění
            if (!block.edit_permission()) return forbiddenEmpty();


            if (!block.active) return badRequest("Block is already deactivated");

            if (!block.update_permission()) return forbiddenEmpty();

            block.active = false;

            block.update();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "activate BlockoBlock",
            tags = {"Admin-Blocko-Block"},
            notes = "activate Blocko Block",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Tariff.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result block_activate(String block_id) {
        try {

            Model_Block block = Model_Block.getById(block_id);
            if (block == null) return notFound("Block not found");

            if (block.active) return badRequest("Block is already activated");

            if (!block.update_permission()) return forbiddenEmpty();

            block.active = true;

            block.update();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit BlockoBlock_Version Response publication",
            tags = {"Admin-Blocko-Block"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_BlockoBlock_Publish_Response",
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
    public Result block_public_response() { // TODO asi nebude fungovat korektně
        try {

            // Získání JSON
            final Form<Swagger_BlockoBlock_Publish_Response> form = formFactory.form(Swagger_BlockoBlock_Publish_Response.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_BlockoBlock_Publish_Response help = form.get();

            // Kontrola názvu
            if (help.version_name.equals("version_scheme")) return badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockVersion private_block_version = Model_BlockVersion.getById(help.version_id);
            if (private_block_version == null) return notFound("grid_widget_version not found");

            // Kontrola nadřazeného objektu
            Model_Block block_old = private_block_version.get_block();

            // Zkontroluji oprávnění
            if (!block_old.community_publishing_permission()) {
                return forbiddenEmpty();
            }

            if (help.decision) {

                private_block_version.approval_state = Approval.APPROVED;
                private_block_version.update();

                Model_Block block = Model_Block.find.query().where().eq("id",block_old.id.toString() + "_public_copy").findOne(); // TODO won't work

                if (block == null) {
                    // Vytvoření objektu
                    block = new Model_Block();
                    block.name = help.program_name;
                    block.description = help.program_description;
                    block.author = private_block_version.get_block().get_author();
                    block.publish_type = ProgramType.PUBLIC;
                    block.save();
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

                return okEmpty();

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
                return okEmpty();
            }

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

// BLOCK VERSION #######################################################################################################

    @ApiOperation(value = "create BlockoBlock_Version",
            tags = {"Blocko-Block"},
            notes = "new BlockoBlock version",
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
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_BlockVersion.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something went wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockVersion_create(@ApiParam(value = "block_id String path",   required = true) String block_id) {
        try {

            // Zpracování Json
            final Form<Swagger_BlockVersion_New> form = formFactory.form(Swagger_BlockVersion_New.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_BlockVersion_New help = form.get();

            // Kontrola názvu
            if (help.name.equals("version_scheme")) return badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_Block block = Model_Block.getById(block_id);
            if (block == null) return notFound("Block not found");

            // Vytvoření objektu
            Model_BlockVersion version = new Model_BlockVersion();
            version.name = help.name;
            version.description = help.description;
            version.design_json = help.design_json;
            version.logic_json = help.logic_json;
            version.block = block;
            version.author = person();

            // Kontrola oprávnění
            if (!version.create_permission()) return forbiddenEmpty();

            // Uložení objektu
            version.save();

            // Vrácení objektu
            return created(Json.toJson(block));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get BlockoBlock_Version",
            tags = {"Blocko-Block"},
            notes = "get version (content) from independent BlockoBlock",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion_read_permission", value = Model_BlockVersion.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockVersion_get(@ApiParam(value = "version_id String path",   required = true) String version_id) {
        try {
            // Kontrola objektu
            Model_BlockVersion version = Model_BlockVersion.getById(version_id);
            if (version == null) return notFound("BlockVersion not found");

            // Kontrola oprávnění
            if (!version.read_permission()) return forbidden("You have no permission to get that");

            // Vrácení objektu
            return ok(Json.toJson(version));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit BlockoBlock_Version",
            tags = {"Blocko-Block"},
            notes = "You can edit only basic information of the version. If you want to update the code, " +
                    "you have to create a new version!",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_edit_permission" )
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockVersion.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something went wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockVersion_update(@ApiParam(value = "version_id String path",   required = true) String version_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_NameAndDescription help = form.get();

            // Kontrola názvu
            if (help.name.equals("version_scheme")) return badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockVersion version = Model_BlockVersion.getById(version_id);
            if (version == null) return notFound("Version not found");

            // Úprava objektu
            version.name = help.name;
            version.description = help.description;

            // Uložení objektu
            version.update();

            // Vrácení objektu
            return ok(Json.toJson(version));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete BlockoBlock_Version",
            tags = {"Blocko-Block"},
            notes = "delete BlockoBlock version",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_delete_permission")
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
    public Result blockVersion_delete(@ApiParam(value = "version_id String path",   required = true) String version_id) {
        try {

            // Kontrola objektu
            Model_BlockVersion version = Model_BlockVersion.getById(version_id);
            if (version == null) return notFound("BlockVersion not found");

            // Kontrola oprávnění
            if (!version.delete_permission()) return forbiddenEmpty();

            // Smazání objektu
            version.delete();

            // Vrácení potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "make BlockoBlock_Version public",
            tags = {"Blocko-Block"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockVersion_makePublic(@ApiParam(value = "version_id String path",   required = true) String version_id) {
        try {

            // Kontrola objektu
            Model_BlockVersion blockoBlockVersion = Model_BlockVersion.getById(version_id);
            if (blockoBlockVersion == null) return notFound("BlockVersion not found");

            // Kontrola orávnění
            if (!(blockoBlockVersion.edit_permission())) return forbiddenEmpty();

            // Úprava objektu
            blockoBlockVersion.approval_state = Approval.PENDING;

            // Uložení změn
            blockoBlockVersion.update();

            // Vrácení výsledku
            return ok(Json.toJson(blockoBlockVersion));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "set_As_Main BlockoBlock_Version",
            tags = {"Admin-Blocko-Block"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "GridWidgetVersion.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "GridWidgetVersion_delete_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result blockVersion_setMain(String version_id) {
        try {

            // Kontrola objektu
            Model_BlockVersion version = Model_BlockVersion.getById(version_id);
            if (version == null) return notFound("BlockVersion version_id not found");

            // Kontrola oprávnění
            if (!version.edit_permission()) return forbiddenEmpty();

            if (!version.get_block_id().equals(UUID.fromString("00000000-0000-0000-0000-000000000001"))) {
                return notFound("BlockVersion version_id not from default program");
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
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

// BLOCKO ADMIN ########################################################################################################*/

    @ApiOperation(value = "edit BlockoBlock_Version refuse publication",
            tags = {"Admin-Blocko-Block"},
            notes = "sets disapproved from pending",
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
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoDisapprove() {
        try {

            // Získání JSON
            final Form<Swagger_BlockoObject_Approval> form = formFactory.form(Swagger_BlockoObject_Approval.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_BlockoObject_Approval help = form.get();

            // Kontrola objektu
            Model_BlockVersion version = Model_BlockVersion.getById(help.object_id);
            if (version == null) return notFound("blocko_block_version not found");

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
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit BlockoBlock_Version accept publication",
            tags = {"Admin-Blocko-Block"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoApproval() {
        try {

            // Získání JSON
            final Form<Swagger_BlockoObject_Approve_withChanges> form = formFactory.form(Swagger_BlockoObject_Approve_withChanges.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_BlockoObject_Approve_withChanges help = form.get();

            // Kontrola názvu
            if (help.version_name.equals("version_scheme")) return badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockVersion privateBlockoBlockVersion = Model_BlockVersion.getById(help.object_id);
            if (privateBlockoBlockVersion == null) return notFound("blocko_block_version not found");

            // Vytvoření objektu
            Model_Block block = new Model_Block();
            block.name = help.name;
            block.description = help.description;
            block.author = privateBlockoBlockVersion.get_block().get_author();
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
                privateBlockoBlockVersion.approval_state = Approval.EDITED;

                // Odeslání emailu
                try {
                    new Email()
                            .text("Version of Block " + version.get_block().name + ": " + Email.bold(version.name) + " was edited before publishing for this reason: ")
                            .text(help.reason)
                            .send(version.get_block().get_author().email, "Version of Block edited" );

                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }
            else privateBlockoBlockVersion.approval_state = Approval.APPROVED;

            // Uložení úprav
            privateBlockoBlockVersion.update();

            // Vrácení výsledku
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }
}