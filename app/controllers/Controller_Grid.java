package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import io.ebean.Ebean;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.*;
import responses.*;
import utilities.authentication.Authentication;
import utilities.emails.Email;
import utilities.enums.Approval;
import utilities.enums.ProgramType;
import utilities.errors.Exceptions.Tyrion_Exp_ForbidenPermission;
import utilities.errors.Exceptions.Tyrion_Exp_ObjectNotValidAnymore;
import utilities.errors.Exceptions.Tyrion_Exp_Unauthorized;
import utilities.logger.Logger;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_M_Program_Interface;
import utilities.swagger.output.Swagger_M_Project_Interface;
import utilities.swagger.output.Swagger_Mobile_Connection_Summary;
import utilities.swagger.output.filter_results.Swagger_GridWidget_List;

import java.util.*;


@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Grid extends BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Grid.class);

    private FormFactory formFactory;

    @Inject
    public Controller_Grid(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

///###################################################################################################################*/

    @ApiOperation(value = "Create M_Project",
            tags = {"M_Program"},
            notes = "M_Project is package for M_Programs -> presupposition is that you need more control terminal for your IoT project. " +
                    "Different screens for family members, for employee etc.. But of course - you can used that for only one M_program",
            produces = "application/json",
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
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",     response = Model_MProject.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result new_M_Project(String project_id) {
        try {

            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_NameAndDescription help = form.get();

            Model_Project project = Model_Project.getById( project_id );
            if (project == null) return notFound("Project project_id not found");

            Model_MProject m_project = new Model_MProject();
            m_project.description = help.description;
            m_project.name = help.name;
            m_project.project = project;

            if (!m_project.create_permission())  return forbiddenEmpty();
            m_project.save();

            return created( Json.toJson(m_project));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get M_Project",
            tags = {"M_Program"},
            notes = "get M_Project by query = m_project_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "M_Project.read_permission", value = Model_MProject.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key"    , value = "MProject_read" ),
                            @ExtensionProperty(name = "Dynamic Permission key"   , value = "MProject_read.{project_id}"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_MProject.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Authentication.class)
    public Result get_M_Project(@ApiParam(value = "m_project_id String query", required = true) String m_project_id) {
        try {

            Model_MProject m_project = Model_MProject.getById(m_project_id);
            if (m_project == null) return notFound("M_Project m_project_id not found");

            if (!m_project.read_permission())  return forbiddenEmpty();
            return ok(Json.toJson(m_project));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit M_Project",
            tags = {"M_Program"},
            notes = "edit basic information in M_Project by query = m_project_id",
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_MProject.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result edit_M_Project(@ApiParam(value = "m_project_id String query", required = true) String m_project_id) {
        try {

            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_NameAndDescription help = form.get();


            Model_MProject m_project = Model_MProject.getById(m_project_id);
            if (m_project == null) return notFound("M_Project m_project_id not found");

            if (!m_project.edit_permission())  return forbiddenEmpty();

            m_project.description = help.description;
            m_project.name = help.name;

            m_project.update();
            return ok( Json.toJson(m_project));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete M_Project",
            tags = {"M_Program"},
            notes = "remove M_Project by query = m_project_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Project.delete_permission", value = "true")
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
    @Security.Authenticated(Authentication.class)
    public Result remove_M_Project(@ApiParam(value = "m_project_id String query", required = true)  String m_project_id) {
        try {

            Model_MProject m_project = Model_MProject.getById(m_project_id);
            if (m_project == null) return notFound("M_project m_project_id not found");

            if (!m_project.delete_permission())  return forbiddenEmpty();
            m_project.delete();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get M_Project Accessible interface",
            tags = {"M_Program"},
            notes = "get accessible interface from M_Project",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "M_Project.read_permission", value = Model_MProject.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Project.remove_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key"      , value = "MProject_read" ),
                            @ExtensionProperty(name = "Dynamic Permission key"     , value = "MProject_read.{project_id}"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_M_Project_Interface.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Empty.class)
    @Security.Authenticated(Authentication.class)
    public Result get_M_Project_Interface_collection(@ApiParam(value = "m_project_id String query", required = true)  String m_project_id) {
        try {

            Model_MProject m_project = Model_MProject.getById(m_project_id);
            if (m_project == null) return notFound("M_project m_project_id not found");

            if (!m_project.read_permission())  return forbiddenEmpty();


            Swagger_M_Project_Interface m_project_interface = new Swagger_M_Project_Interface();
            m_project_interface.name = m_project.name;
            m_project_interface.description = m_project.description;
            m_project_interface.id = m_project.id;

            for (Model_MProgram m_program : m_project.get_m_programs_not_deleted()) {

                Swagger_M_Program_Interface m_program_interface = new Swagger_M_Program_Interface();
                m_program_interface.description = m_program.description;
                m_program_interface.name        = m_program.name;
                m_program_interface.id          = m_program.id;

                m_program_interface.accessible_versions = m_program.program_versions_interface();
                m_project_interface.accessible_interface.add(m_program_interface);
            }

            return ok(Json.toJson(m_project_interface));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }


//######################################################################################################################

    @ApiOperation(value = "Create M_Program",
            tags = {"M_Program"},
            notes = "creating new M_Program",
            produces = "application/json",
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
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_MProgram.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result new_M_Program( @ApiParam(value = "m_project_id", required = true) String m_project_id) {
        try {

            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_NameAndDescription help = form.get();

            Model_MProject m_project = Model_MProject.getById( m_project_id );
            if (m_project == null) return notFound("M_Project m_project_id not found");

            Model_MProgram m_program = new Model_MProgram();
            m_program.description         = help.description;
            m_program.name                = help.name;
            m_program.m_project           = m_project;

            if (!m_program.create_permission()) return forbiddenEmpty();
            m_program.save();

            return created(Json.toJson(m_program));
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "Create M_Program_Version",
            tags = {"M_Program"},
            notes = "creating new Version M_Program",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "M_Program.create_permission", value = Model_MProgram.create_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "MProgram_create" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_M_Program_Version_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Swagger_M_Program_Version.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result new_M_Program_version( @ApiParam(value = "m_program_id", required = true) String m_program_id) {
        try {

            final Form<Swagger_M_Program_Version_New> form = formFactory.form(Swagger_M_Program_Version_New.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_M_Program_Version_New help = form.get();

            Model_MProgram main_m_program = Model_MProgram.getById( m_program_id );
            if (main_m_program == null) return notFound("M_Project m_project_id not found");

            if (!main_m_program.create_permission()) return forbiddenEmpty();

            Model_Version version       = new Model_Version();
            version.name                = help.name;
            version.description         = help.description;
            version.m_program           = main_m_program;
            version.author              = BaseController.person();
            version.public_version      = help.public_mode;
            version.m_program_virtual_input_output =  help.virtual_input_output;

            version.save();

            main_m_program.getVersions_not_removed_by_person().add(version);

            ObjectNode content = Json.newObject();
            content.put("m_code", help.m_code);

            Model_Blob.uploadAzure_Version(content.toString(), "m_program.json" , main_m_program.get_path() ,  version);

            return created(Json.toJson(Model_MProgram.program_version(version)));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get M_Program",
            tags = {"M_Program"},
            notes = "get M_Program by quarry m_program_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "M_Program.read_permission", value = Model_MProgram.read_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Program.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key"    , value = "MProgram_read" ),
                            @ExtensionProperty(name = "Dynamic Permission key"   , value = "MProgram_read.{project_id}"),
                    })
            }

    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_MProgram.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Authentication.class)
    public Result get_M_Program(@ApiParam(value = "m_program_id String query", required = true)  String m_program_id) {
        try {
            Model_MProgram m_program = Model_MProgram.getById(m_program_id);
            if (m_program == null) return notFound("M_Project m_project_id not found");

            if (!m_program.read_permission())  return forbiddenEmpty();

            return ok(Json.toJson(m_program));
        } catch (Exception e) {
            logger.internalServerError(e);
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get M_Program_Version",
            tags = {"M_Program"},
            notes = "get M_Program_Version by quarry m_program_version_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "M_Program.read_permission", value = Model_MProgram.read_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Program.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key"    , value = "MProgram_read" ),
                            @ExtensionProperty(name = "Dynamic Permission key"   , value = "MProgram_read.{project_id}"),
                    })
            }

    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_M_Program_Version.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Authentication.class)
    public Result get_M_Program_version(@ApiParam(value = "m_program_version_id String query", required = true)  String m_program_version_id) {

        try {
            // Kontrola objektu
            Model_Version version_object = Model_Version.getById(m_program_version_id);
            if (version_object == null) return notFound("Version_Object version_id not found");

            // Kontrola oprávnění
            if (version_object.m_program == null)
                return notFound("Version_Object is not version of B_Program");

            // Kontrola oprávnění
            if (!version_object.m_program.read_permission()) return forbiddenEmpty();

            // Vrácení objektu
            return ok(Json.toJson(Model_MProgram.program_version(version_object)));

        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit M_Program",
            tags = {"M_Program"},
            notes = "update m_project - in this case we are not support versions of m_project",
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_MProject.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result edit_M_Program(@ApiParam(value = "m_program_id String query", required = true)  String m_program_id) {
        try {

            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_NameAndDescription help = form.get();


            Model_MProgram m_program = Model_MProgram.getById(m_program_id);
            if (!m_program.edit_permission())  return forbiddenEmpty();

            if (m_program.m_project == null)  return badRequest("You cannot change program on version");


            m_program.description = help.description;
            m_program.name        = help.name;

            m_program.update();

            return ok(Json.toJson(m_program));
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit M_Program_Version",
            tags = {"M_Program"},
            notes = "edit M_Program_Version by quarry = m_program_version_id",
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Authentication.class)
    public Result edit_M_Program_version(@ApiParam(value = "m_program_version_id String query", required = true) String m_program_version_id) {
        try {

            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_NameAndDescription help = form.get();

            // Získání objektu
            Model_Version version  = Model_Version.getById(m_program_version_id);

            // Kontrola objektu
            if (version == null) return notFound("Version_Object id not found");
            if (version.m_program == null) return badRequest("M_Project m_project_id not found");

            // Kontrola oprávnění
            if (!version.m_program.edit_permission()) return forbiddenEmpty();

            // Úprava objektu
            version.description = help.description;
            version.name        = help.name;

            version.update();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete M_Program",
            tags = {"M_Program"},
            notes = "remove M_Program by quarry = m_program_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Program.delete_permission", value = "true"),
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
    @Security.Authenticated(Authentication.class)
    public Result remove_M_Program(@ApiParam(value = "m_program_id String query", required = true) String m_program_id) {
        try {

            Model_MProgram m_program = Model_MProgram.getById(m_program_id);
            if (m_program == null) return notFound("M_Project m_project_id not found");

            if (!m_program.delete_permission())  return forbiddenEmpty();
            m_program.delete();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete M_Program_Version",
            tags = {"M_Program"},
            notes = "remove version of M_Program",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created",    response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Empty.class)
    public Result remove_M_Program_version( @ApiParam(value = "m_program_version_id", required = true) String m_program_version_id) {
        try {

            // Získání objektu
            Model_Version version_object  = Model_Version.getById(m_program_version_id);

            // Kontrola objektu
            if (version_object == null) return notFound("Version_Object id not found");
            if (version_object.m_program == null) return badRequest("M_Project m_project_id not found");

            // Kontrola oprávnění
            if (! version_object.m_program.delete_permission() ) return forbiddenEmpty();

            // Smazání objektu
            version_object.delete();

            // Vrácení potvrzení
            return okEmpty();


        } catch (Exception e) {
            return internalServerError(e);
        }
    }
//######################################################################################################################

    // Příkazy pro Terminál

    @ApiOperation(value = "get M_Program by generated token",
            tags = {"APP-Api"},
            notes = "get M_Program by token",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Program.read_qr_token_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key"    , value = "MProgram_read" ),
                            @ExtensionProperty(name = "Dynamic Permission key"   , value = "MProgram_read.{project_id}"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",  response = Swagger_Mobile_Connection_Summary.class),
            @ApiResponse(code = 400, message = "Bad Request - Probably token is not valid anymore", response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 477, message = "External Server is offline", response = Result_ServerOffline.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_M_Program_byQR_Token_forMobile(String qr_token) {
        try {


            logger.debug("get_M_Program_byQR_Token_forMobile: Connection token: " + qr_token);

            Model_MProgramInstanceParameter parameter = Model_MProgramInstanceParameter.find.query()
                    .where()
                    .eq("connection_token" , qr_token)
                    .isNotNull("m_project_program_snapshot.instance_versions.instance_record.actual_running_instance")
                    .findOne();

            if (parameter == null) return notFound("MProgramInstanceParameter by token not found in database");

            try {

                return ok(Json.toJson(parameter.get_connection_summary( ctx())));

            } catch (Tyrion_Exp_ForbidenPermission e) {

                return forbiddenEmpty();

            } catch (Tyrion_Exp_ObjectNotValidAnymore e) {

                return badRequest("QR token is not valid anymore");

            } catch (Tyrion_Exp_Unauthorized e) {

                return unauthorizedEmpty();
            }

        } catch (Exception e) {
            return internalServerError(e);
        }
    }


    @ApiOperation(value = "check Terminal",
            tags = {"APP-Api"},
            notes = "For every app (terminal) opening you have to valid your terminal_id.",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Grid_Terminal_Identf",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Valid Identification",      response = Model_GridTerminal.class),
            @ApiResponse(code = 400, message = "Invalid Identification",    response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result check_identifiactor(String terminal_id) {
        try {

            final Form<Swagger_Grid_Terminal_Identf> form = formFactory.form(Swagger_Grid_Terminal_Identf.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Grid_Terminal_Identf help = form.get();


            Model_GridTerminal terminal = Model_GridTerminal.getById(terminal_id);
            if (terminal == null) {

                terminal = new Model_GridTerminal();
                terminal.device_name = help.device_name;
                terminal.device_type = help.device_type;
                terminal.save();

                return created(Json.toJson(terminal));

            } else {

                terminal.ws_permission = true;
                terminal.m_program_access = true;
                terminal.update();
                return ok(Json.toJson(terminal));
            }

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Terminal",
            tags = {"APP-Api"},
            notes = "Only for Grid Terminals! Before when you want connect terminal (grid) application with Tyrion throw WebSocker. " +
                    "You need unique identification key. If Person loggs to you application Tyrion connects this device with Person. Try to " +
                    "save this key to cookies or on mobile device, or you have to ask every time again",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Grid_Terminal_Identf",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_GridTerminal.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_InvalidBody.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    //@Security.Authenticated(Authentication.class) - Není záměrně!!!! - Ověřuje se v read_permision program může být public!
    public Result get_identificator() {
        try {

            final Form<Swagger_Grid_Terminal_Identf> form = formFactory.form(Swagger_Grid_Terminal_Identf.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Grid_Terminal_Identf help = form.get();

            Model_GridTerminal terminal = new Model_GridTerminal();
            terminal.device_name = help.device_name;
            terminal.device_type = help.device_type;

            if ( Http.Context.current().request().headers().get("User-Agent")[0] != null) terminal.user_agent =  Http.Context.current().request().headers().get("User-Agent")[0];
            else  terminal.user_agent = "Unknown browser";


            // Tato část je určená pro nalezení tokenu a přihlášení uživatele - bylo totiž nutné zpřístupnit tuto metodu i nepřihlášeným (bez loginu). Kvuli tomu že by to přes  @Security.Authenticated(Authentication.class)  neprošlo
            String[] token_values =  Http.Context.current().request().headers().get("X-AUTH-TOKEN");


            if ((token_values != null) && (token_values.length == 1) && (token_values[0] != null)) {
                logger.debug("get_identificator :: HTTP request containts X-AUTH-TOKEN");
                Model_Person person = Model_Person.getByAuthToken(UUID.fromString(token_values[0]));
                if (person != null) {
                    logger.debug("get_identificator :: Person with X-AUTH-TOKEN found");
                  terminal.person = person;

                } else {
                    logger.warn("get_identificator :: Person with X-AUTH-TOKEN not found!");
                }
            }

            terminal.save();
            return created(Json.toJson(terminal));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

// WIDGET ##############################################################################################################

    @ApiOperation(value = "create Grid_Widget",
            tags = {"Grid-Widget"},
            notes = "creating new independent Widget object for Grid tools",
            produces = "application/json",
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
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_Widget.class),
            @ApiResponse(code = 400, message = "Something went wrong",    response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result widget_create() {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDesc_ProjectIdOptional> form = formFactory.form(Swagger_NameAndDesc_ProjectIdOptional.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_NameAndDesc_ProjectIdOptional help = form.get();

            Model_Project project = null;

            if (help.project_id == null ) {
                if (Model_Widget.getPublicByName(help.name) != null) {
                    return badRequest("Widget with this name already exists, type a new one.");
                }
            } else {
                project = Model_Project.getById(help.project_id);
                if (project == null) return notFound("Project not found");
            }

            // Vytvoření objektu
            Model_Widget widget = new Model_Widget();
            widget.name = help.name;
            widget.description = help.description;
            widget.author = person();

            if (project != null) {
                widget.project = project;
                widget.publish_type = ProgramType.PRIVATE;
            } else {
                widget.publish_type = ProgramType.PUBLIC;
            }

            // Kontrola oprávnění těsně před uložením
            if (!widget.create_permission() ) return forbiddenEmpty();

            // Uložení objektu
            widget.save();

            // Získání šablony
            Model_WidgetVersion scheme = Model_WidgetVersion.find.query().where().eq("publish_type", ProgramType.DEFAULT_VERSION.name()).findOne();

            // Kontrola objektu
            if (scheme == null) return created( Json.toJson(widget) );

            // Vytvoření objektu první verze
            Model_WidgetVersion gridWidgetVersion = new Model_WidgetVersion();
            gridWidgetVersion.name = "0.0.1";
            gridWidgetVersion.description = "This is a first version of widget.";
            gridWidgetVersion.approval_state = Approval.APPROVED;
            gridWidgetVersion.design_json = scheme.design_json;
            gridWidgetVersion.logic_json = scheme.logic_json;
            gridWidgetVersion.widget = widget;
            gridWidgetVersion.save();

            // Vrácení objektu
            return created( Json.toJson(widget) );

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Grid_Widget",
            tags = {"Grid-Widget"},
            notes = "update basic information (name, and description) of the independent GridWidget",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "GridWidget.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "GridWidget_edit_permission" )
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Widget.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result widget_update(@ApiParam(value = "widget_id String path",   required = true)  String grid_widget_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDesc_ProjectIdOptional> form = formFactory.form(Swagger_NameAndDesc_ProjectIdOptional.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_NameAndDesc_ProjectIdOptional help = form.get();

            // Kontrola objektu
            Model_Widget widget = Model_Widget.getById(grid_widget_id);
            if (widget == null) return notFound("GridWidget widget_id not found");

            // Kontrola oprávnění
            if (!widget.edit_permission()) return forbiddenEmpty();

            // Úprava objektu
            widget.description = help.description;
            widget.name        = help.name;

            // Uložení objektu
            widget.update();

            // Vrácení objektu
            return ok(Json.toJson(widget));

        } catch (Exception e) {
            return internalServerError(e);
        }

    }

    @ApiOperation(value = "get Grid_Widget",
            tags = {"Grid-Widget"},
            notes = "get independent Grid_Widget object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "GridWidget_read_permission", value = Model_Widget.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "GridWidget.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "GridWidget_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Widget.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Authentication.class)
    public Result widget_get(@ApiParam(value = "widget_id String path",   required = true) String grid_widget_id) {
        try {
            // Kontrola objektu
            Model_Widget gridWidget = Model_Widget.getById(grid_widget_id);
            if (gridWidget == null) return notFound("GridWidget widget_id not found");

            // Kontrola oprávnění

            if (! gridWidget.read_permission() ) return forbiddenEmpty();

            // Vrácení objektu
            return ok(Json.toJson(gridWidget));

        } catch (Exception e) {
            return internalServerError(e);
        }

    }

    @ApiOperation(value = "get Grid_Widget by Filter",
            tags = {"Grid-Widget"},
            notes = "get GridWidget List",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "GridWidget_read_permission", value = "No need to check permission, because Tyrion returns only those results which user owns"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_GridWidget_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_GridWidget_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Authentication.class)
    public Result widget_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number) {
        try {

            // Získání JSON
            final Form<Swagger_GridWidget_Filter> form = formFactory.form(Swagger_GridWidget_Filter.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_GridWidget_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_Widget> query = Ebean.find(Model_Widget.class);

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if (help.project_id != null) {

                Model_Project project = Model_Project.getById(help.project_id);
                if (project == null )return notFound("Project not found");
                if (!project.read_permission())return forbiddenEmpty();

                query.where().eq("type_of_widget.project.id", help.project_id);
            }

            if (help.pending_widget) {
                query.where().eq("versions.approval_state", Approval.PENDING.name()).eq("versions.deleted", false);
            }

            // Vytvoření odchozího JSON
            Swagger_GridWidget_List result = new Swagger_GridWidget_List(query, page_number);

            // Vrácení výsledku
            return ok(Json.toJson(result));

        } catch (Exception e) {
            logger.internalServerError(e);
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete Grid_Widget",
            tags = {"Grid-Widget"},
            notes = "delete GridWidget",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "GridWidget.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "GridWidget_delete_permission")
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
    @Security.Authenticated(Authentication.class)
    public Result widget_delete(@ApiParam(value = "widget_id String path",   required = true)  String grid_widget_id) {
        try {

            // Kontrola objektu
            Model_Widget gridWidget = Model_Widget.getById(grid_widget_id);
            if (gridWidget == null) return notFound("GridWidget widget_id not found");

            // Kontrola oprávnění
            if (! gridWidget.delete_permission()) return forbiddenEmpty();

            // Smazání objektu
            gridWidget.delete();

            // Vrácení potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "make_Clone Grid_Widget",
            tags = {"Grid_Widget"},
            notes = "clone Grid_Widget for private",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Grid_Widget_Copy",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Widget.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result widget_clone() {
        try {

            // Zpracování Json
            final Form<Swagger_Grid_Widget_Copy> form = formFactory.form(Swagger_Grid_Widget_Copy.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Grid_Widget_Copy help = form.get();

            // Vyhledám Objekt
            Model_Widget grid_widget_old = Model_Widget.getById(help.widget_id);
            if (grid_widget_old == null) return notFound("Model_GridWidget widget_id not found");

            // Zkontroluji oprávnění
            if (!grid_widget_old.read_permission())  return forbiddenEmpty();

            // Vyhledám Objekt
            Model_Project project = Model_Project.getById(help.project_id);
            if (project == null) return notFound("Project project_id not found");

            // Zkontroluji oprávnění
            if (!project.update_permission())  return forbiddenEmpty();

            Model_Widget grid_widget_new =  new Model_Widget();
            grid_widget_new.name = help.name;
            grid_widget_new.description = help.description;
            grid_widget_new.project = project;
            grid_widget_new.save();

            grid_widget_new.refresh();

            for (Model_WidgetVersion version : grid_widget_old.getVersions()) {

                Model_WidgetVersion copy_object = new Model_WidgetVersion();
                copy_object.name        = version.name;
                copy_object.description = version.description;
                copy_object.author      = version.author;
                copy_object.design_json = version.design_json;
                copy_object.logic_json  = version.logic_json;
                copy_object.widget      = grid_widget_new;

                // Zkontroluji oprávnění
                copy_object.save();
            }

            grid_widget_new.refresh();

            // Vracím Objekt
            return ok(Json.toJson(grid_widget_new));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "deactivate Grid_Widget",
            tags = {"Admin-Grid-Widget"},
            notes = "deactivate Widget",
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
    @Security.Authenticated(Authentication.class)
    public Result widget_deactivate(String grid_widget_id) {
        try {

            Model_Widget gridWidget = Model_Widget.getById(grid_widget_id);
            if (gridWidget == null) return notFound("GridWidget not found");

            // Kontrola oprávnění
            if (! gridWidget.update_permission() ) return forbiddenEmpty();

            if (!gridWidget.active) return badRequest("Tariff is already deactivated");


            gridWidget.active = false;

            gridWidget.update();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "activate Grid_Widget",
            tags = {"Admin-Grid-Widget"},
            notes = "activate Widget",
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
    @Security.Authenticated(Authentication.class)
    public Result widget_activate(String grid_widget_id) {
        try {

            Model_Widget gridWidget = Model_Widget.getById(grid_widget_id);
            if (gridWidget == null) return notFound("GridWidget not found");

            if (!gridWidget.update_permission()) return forbiddenEmpty();

            if (gridWidget.active) return badRequest("Tariff is already activated");

            gridWidget.active = true;

            gridWidget.update();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "order Grid_Widget Up",
            tags = {"Grid-Widget"},
            notes = "set up order",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Authentication.class)
    public Result widget_order_up(@ApiParam(value = "widget_id String path",   required = true) String grid_widget_id) {
        try {

            Model_Widget gridWidget = Model_Widget.getById(grid_widget_id);
            if (gridWidget == null) return notFound("GridWidget not found");

            // Kontrola oprávnění
            if (! gridWidget.edit_permission()) return forbiddenEmpty();

            gridWidget.up();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "order Grid_Widget Down",
            tags = {"Grid-Widget"},
            notes = "set down order",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Authentication.class)
    public Result widget_order_down(@ApiParam(value = "widget_id String path",   required = true) String grid_widget_id) {
        try {

            Model_Widget gridWidget =  Model_Widget.getById(grid_widget_id);
            if (gridWidget == null) return notFound("GridWidget not found");

            // Kontrola oprávnění
            if (!gridWidget.edit_permission()) return forbiddenEmpty();

            gridWidget.down();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

// WIDGET VERSION ######################################################################################################    

    @ApiOperation(value = "delete Grid_Widget_Version",
            tags = {"Grid-Widget"},
            notes = "delete GridWidget version",
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
    @Security.Authenticated(Authentication.class)
    public Result widgetVersion_delete(@ApiParam(value = "grid_widget_version_id String path",   required = true) String grid_widget_version_id) {
        try {

            // Kontrola objektu
            Model_WidgetVersion version = Model_WidgetVersion.getById(grid_widget_version_id);
            if (version == null) return notFound("GridWidgetVersion grid_widget_version_id not found");

            // Kontrola oprávnění
            if (! version.delete_permission()) return forbiddenEmpty();

            // Smazání objektu
            version.delete();

            // Vrácení potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "set_As_Main Grid_Widget_Version",
            tags = {"Admin-Grid-Widget"},
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
    @Security.Authenticated(Authentication.class)
    public Result widgetVersion_set_main(String grid_widget_version_id) {
        try {

            // Kontrola objektu
            Model_WidgetVersion version = Model_WidgetVersion.getById(grid_widget_version_id);
            if (version == null) return notFound("GridWidgetVersion grid_widget_version_id not found");

            // Kontrola oprávnění
            if (!version.edit_permission()) return forbiddenEmpty();

            if (!version.get_grid_widget_id().equals("00000000-0000-0000-0000-000000000001")) {
                return notFound("GridWidgetVersion grid_widget_version_id not from default program");
            }

            Model_WidgetVersion old_version = Model_WidgetVersion.find.query().where().eq("publish_type", ProgramType.DEFAULT_VERSION.name()).select("id").findOne();
            if (old_version != null) {
                old_version = Model_WidgetVersion.getById(old_version.id);
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

    @ApiOperation(value = "make Grid_Widget_Version public",
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
    @Security.Authenticated(Authentication.class)
    public Result widgetVersion_version_make_public(String grid_widget_version_id) {
        try {

            System.out.println("widgetVersion_version_make_public .... ");
            // Kontrola objektu
            Model_WidgetVersion gridWidgetVersion = Model_WidgetVersion.getById(grid_widget_version_id);
            if (gridWidgetVersion == null) return notFound("GridWidgetVersion grid_widget_version_id not found");

            // Kontrola orávnění
            if (!(gridWidgetVersion.read_permission())) return forbiddenEmpty();

            if (Model_WidgetVersion.find.query().where().eq("approval_state", Approval.PENDING.name())
                    .eq("author.id", BaseController.personId())
                    .findList().size() > 3) {
                // TODO Notifikace uživatelovi
                return badRequest("You can publish only 3 programs. Wait until the previous ones approved by the administrator. Thanks.");
            }

            if (gridWidgetVersion.approval_state != null)  return badRequest("You cannot publish same program twice!");

            // Úprava objektu
            gridWidgetVersion.approval_state = Approval.PENDING;

            // Kontrola oprávnění
            if (!(gridWidgetVersion.edit_permission())) return forbiddenEmpty();

            // Uložení změn
            gridWidgetVersion.update();

            // Vrácení potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "create Grid_Widget_Version",
            tags = {"Grid-Widget"},
            notes = "new GridWidget version",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "GridWidgetVersion_create_permission", value = Model_WidgetVersion.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "GridWidget.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "GridWidgetVersion_create_permission" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_GridWidgetVersion_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_WidgetVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result widgetVersion_create(@ApiParam(value = "widget_id String path",   required = true) String grid_widget_id) {
        try {

            // Zpracování Json
            final Form<Swagger_GridWidgetVersion_New> form = formFactory.form(Swagger_GridWidgetVersion_New.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_GridWidgetVersion_New help = form.get();

            // Kontrola názvu
            if (help.name.equals("version_scheme")) return badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_Widget gridWidget = Model_Widget.getById(grid_widget_id);
            if (gridWidget == null) return notFound("GridWidget not found");

            // Vytvoření objektu
            Model_WidgetVersion version = new Model_WidgetVersion();
            version.name = help.name;
            version.description = help.description;
            version.design_json = help.design_json;
            version.logic_json = help.logic_json;
            version.widget = gridWidget;
            version.author = BaseController.person();

            // Kontrola oprávnění
            if (! version.create_permission()) return forbiddenEmpty();

            // Uložení objektu
            version.save();

            // Vrácení objektu
            return created(Json.toJson(gridWidget));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Grid_Widget_Version",
            tags = {"Grid-Widget"},
            notes = "get version (content) from independent GridWidget",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "GridWidgetVersion_read_permission", value = Model_WidgetVersion.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "GridWidget.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "GridWidgetVersion_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_WidgetVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Authentication.class)
    public Result widgetVersion_get(@ApiParam(value = "grid_widget_version_id String path",   required = true) String grid_widget_version_id) {
        try {
            // Kontrola objektu
            Model_WidgetVersion version = Model_WidgetVersion.getById(grid_widget_version_id);
            if (version == null) return notFound("GridWidget widget_id not found");

            // Kontrola oprávnění
            if (!version.read_permission() ) return forbidden("You have no permission to get that");

            // Vrácení objektu
            return ok(Json.toJson(version));

        } catch (Exception e) {
            return internalServerError(e);
        }

    }

    @ApiOperation(value = "edit Grid_Widget_Version",
            tags = {"Grid-Widget"},
            notes = "You can edit only basic information of the version. If you want to update the code, " +
                    "you have to create a new version!",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "GridWidgetVersion.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "GridWidgetVersion_edit_permission" )
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_WidgetVersion.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Authentication.class)
    public Result widgetVersion_edit(@ApiParam(value = "version_id String path",   required = true) String version_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_NameAndDescription help = form.get();

            // Kontrola objektu
            Model_WidgetVersion version = Model_WidgetVersion.getById(version_id);
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

    @ApiOperation(value = "get Grid_Widget_Versions",
            tags = {"Grid-Widget"},
            notes = "get all versions (content) from independent GridWidget",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "GridWidgetVersion_read_permission", value = Model_WidgetVersion.read_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "GridWidget.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "GridWidgetVersion_read_permission")
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_GridWidgetVersion_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_WidgetVersion.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Authentication.class)
    public Result widgetVersion_getAll(@ApiParam(value = "widget_id String path",   required = true) String grid_widget_id) {
        try {

            // Kontrola objektu
            Model_Widget gridWidget = Model_Widget.getById(grid_widget_id);
            if (gridWidget == null) return notFound("GridWidget widget_id not found");

            // Kontrola oprávnění
            if (! gridWidget.read_permission()) return forbiddenEmpty();

            // Vrácení objektu
            return ok(Json.toJson(gridWidget.versions));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }


// GRID ADMIN ##########################################################################################################

    @ApiOperation(value = "edit Grid_Widget_Version Response publication",
            tags = {"Admin-Grid-Widget"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Widget_Publish_Response",
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
    @Security.Authenticated(Authentication.class)
    public Result widget_public_response() {
        try {

            // Získání JSON
            final Form<Swagger_Widget_Publish_Response> form = formFactory.form(Swagger_Widget_Publish_Response.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Widget_Publish_Response help = form.get();

            // Kontrola názvu
            if (help.version_name.equals("version_scheme")) return badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_WidgetVersion privateGridWidgetVersion = Model_WidgetVersion.getById(help.version_id);
            if (privateGridWidgetVersion == null) return notFound("grid_widget_version not found");

            // Kontrola nadřazeného objektu
            Model_Widget widget_old = Model_Widget.getById(privateGridWidgetVersion.get_grid_widget_id());

            // Zkontroluji oprávnění
            if (!widget_old.community_publishing_permission()) {
                return forbiddenEmpty();
            }

            if (help.decision) {

                privateGridWidgetVersion.approval_state = Approval.APPROVED;
                privateGridWidgetVersion.update();

                Model_Widget widget = Model_Widget.find.query().where().eq("id",widget_old.id.toString() + "_public_copy").findOne(); // TODO won't work

                if (widget == null) {
                    // Vytvoření objektu
                    widget = new Model_Widget();
                    widget.name = help.program_name;
                    widget.description = help.program_description;
                    widget.author = privateGridWidgetVersion.get_grid_widget().get_author();
                    widget.publish_type = ProgramType.PUBLIC;
                    widget.save();
                }

                // Vytvoření objektu
                Model_WidgetVersion version = new Model_WidgetVersion();
                version.name = help.version_name;
                version.description = help.version_description;
                version.design_json = privateGridWidgetVersion.design_json;
                version.logic_json = privateGridWidgetVersion.logic_json;
                version.approval_state = Approval.APPROVED;
                version.widget = widget;
                version.save();

                widget.refresh();

                // TODO notifikace a emaily

                return okEmpty();

            } else {
                // Změna stavu schválení
                privateGridWidgetVersion.approval_state = Approval.DISAPPROVED;

                // Odeslání emailu s důvodem
                try {

                    new Email()
                            .text("Version of Widget " + privateGridWidgetVersion.get_grid_widget().name + ": " + Email.bold(privateGridWidgetVersion.name) + " was not approved for this reason: ")
                            .text(help.reason)
                            .send(privateGridWidgetVersion.get_grid_widget().get_author().email, "Version of Widget disapproved" );

                } catch (Exception e) {
                    logger.internalServerError (e);
                }

                // Uložení změn
                privateGridWidgetVersion.update();

                // Vrácení výsledku
                return okEmpty();
            }

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

}
