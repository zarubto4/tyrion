package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;
import utilities.emails.Email;
import utilities.enums.Enum_Approval_state;
import utilities.enums.Enum_Publishing_type;
import utilities.errors.Exceptions.Tyrion_Exp_ForbidenPermission;
import utilities.errors.Exceptions.Tyrion_Exp_ObjectNotValidAnymore;
import utilities.errors.Exceptions.Tyrion_Exp_Unauthorized;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.*;
import utilities.swagger.outboundClass.Filter_List.Swagger_GridWidget_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_Type_Of_Widget_List;

import java.util.*;


@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Grid extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Grid.class);

///###################################################################################################################*/

    @ApiOperation(value = "Create M_Project",
            tags = {"M_Program"},
            notes = "M_Project is package for M_Programs -> presupposition is that you need more control terminal for your IoT project. " +
                    "Different screens for family members, for employee etc.. But of course - you can used that for only one M_program",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "M_Project_create_permission", value = Model_MProject.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                        @ExtensionProperty(name = "Project.update_permission", value = "true"),
                        @ExtensionProperty(name = "Static Permission key",     value =  "M_Project_create" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_M_Project_New",
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
    @Security.Authenticated(Secured_API.class)
    public Result new_M_Project(String project_id) {
        try{

            final Form<Swagger_M_Project_New> form = Form.form(Swagger_M_Project_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_M_Project_New help = form.get();

            Model_Project project = Model_Project.get_byId( project_id );
            if(project == null) return GlobalResult.result_notFound("Project project_id not found");

            Model_MProject m_project = new Model_MProject();
            m_project.description = help.description;
            m_project.name = help.name;
            m_project.date_of_create = new Date();
            m_project.project = project;

            if (!m_project.create_permission())  return GlobalResult.result_forbidden();
            m_project.save();

            return GlobalResult.result_created( Json.toJson(m_project));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            @ExtensionProperty(name = "Static Permission key"    , value = "M_Project_read" ),
                            @ExtensionProperty(name = "Dynamic Permission key"   , value = "M_Project_read.{project_id}"),
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
    @Security.Authenticated(Secured_API.class)
    public Result get_M_Project(@ApiParam(value = "m_project_id String query", required = true) String m_project_id){
        try {

            Model_MProject m_project = Model_MProject.get_byId(m_project_id);
            if (m_project == null) return GlobalResult.result_notFound("M_Project m_project_id not found");

            if (!m_project.read_permission())  return GlobalResult.result_forbidden();
            return GlobalResult.result_ok(Json.toJson(m_project));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit M_Project",
            tags = {"M_Program"},
            notes = "edit basic information in M_Project by query = m_project_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Project.edit_permission", value = "true")
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_M_Project_New",
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
    @Security.Authenticated(Secured_API.class)
    public Result edit_M_Project(@ApiParam(value = "m_project_id String query", required = true) String m_project_id){
        try{

            final Form<Swagger_M_Project_New> form = Form.form(Swagger_M_Project_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_M_Project_New help = form.get();


            Model_MProject m_project = Model_MProject.get_byId(m_project_id);
            if(m_project == null) return GlobalResult.result_notFound("M_Project m_project_id not found");

            if (!m_project.edit_permission())  return GlobalResult.result_forbidden();

            m_project.description = help.description;
            m_project.name = help.name;

            m_project.update();
            return GlobalResult.result_ok( Json.toJson(m_project));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
    @Security.Authenticated(Secured_API.class)
    public Result remove_M_Project(@ApiParam(value = "m_project_id String query", required = true)  String m_project_id){
        try{

            Model_MProject m_project = Model_MProject.get_byId(m_project_id);
            if(m_project == null) return GlobalResult.result_notFound("M_project m_project_id not found");

            if (!m_project.delete_permission())  return GlobalResult.result_forbidden();
            m_project.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            @ExtensionProperty(name = "Static Permission key"      , value = "M_Project_read" ),
                            @ExtensionProperty(name = "Dynamic Permission key"     , value = "M_Project_read.{project_id}"),
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
    @Security.Authenticated(Secured_API.class)
    public Result get_M_Project_Interface_collection(@ApiParam(value = "m_project_id String query", required = true)  String m_project_id) {
        try{

            Model_MProject m_project = Model_MProject.get_byId(m_project_id);
            if(m_project == null) return GlobalResult.result_notFound("M_project m_project_id not found");

            if (!m_project.read_permission())  return GlobalResult.result_forbidden();


            Swagger_M_Project_Interface m_project_interface = new Swagger_M_Project_Interface();
            m_project_interface.name = m_project.name;
            m_project_interface.description = m_project.description;
            m_project_interface.id = m_project.id;

            for(Model_MProgram m_program : m_project.get_m_programs_not_deleted()) {

                Swagger_M_Program_Interface m_program_interface = new Swagger_M_Program_Interface();
                m_program_interface.description = m_program.description;
                m_program_interface.name        = m_program.name;
                m_program_interface.id          = m_program.id;

                m_program_interface.accessible_versions = m_program.program_versions_interface();
                m_project_interface.accessible_interface.add(m_program_interface);
            }

            return GlobalResult.result_ok(Json.toJson(m_project_interface));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }


//######################################################################################################################

    @ApiOperation(value = "Create M_Program",
            tags = {"M_Program"},
            notes = "creating new M_Program",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "M_Program.create_permission", value = Model_MProgram.create_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "M_Program_create" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_M_Program_New",
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
    @Security.Authenticated(Secured_API.class)
    public Result new_M_Program( @ApiParam(value = "m_project_id", required = true) String m_project_id) {
        try {

            final Form<Swagger_M_Program_New> form = Form.form(Swagger_M_Program_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_M_Program_New help = form.get();

            Model_MProject m_project = Model_MProject.get_byId( m_project_id );
            if(m_project == null) return GlobalResult.result_notFound("M_Project m_project_id not found");


            Model_MProgram m_program = new Model_MProgram();

            m_program.date_of_create      = new Date();
            m_program.description         = help.description;
            m_program.name                = help.name;

            m_program.m_project           = m_project;

            if (!m_program.create_permission()) return GlobalResult.result_forbidden();
            m_program.save();

            return GlobalResult.result_created(Json.toJson(m_program));
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            @ExtensionProperty(name = "Static Permission key", value =  "M_Program_create" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_M_Program_Version_New",
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
    @Security.Authenticated(Secured_API.class)
    public Result new_M_Program_version( @ApiParam(value = "m_program_id", required = true) String m_program_id) {
        try {

            final Form<Swagger_M_Program_Version_New> form = Form.form(Swagger_M_Program_Version_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_M_Program_Version_New help = form.get();

            Model_MProgram main_m_program = Model_MProgram.get_byId( m_program_id );
            if(main_m_program == null) return GlobalResult.result_notFound("M_Project m_project_id not found");

            if (!main_m_program.create_permission()) return GlobalResult.result_forbidden();

            Model_VersionObject version_object      = new Model_VersionObject();
            version_object.date_of_create      = new Date();
            version_object.version_description = help.version_description;
            version_object.version_name        = help.version_name;
            version_object.m_program           = main_m_program;
            version_object.author              = Controller_Security.get_person();
            version_object.public_version      = help.public_mode;
            version_object.m_program_virtual_input_output =  help.virtual_input_output;

            version_object.save();

            main_m_program.getVersion_objects_not_removed_by_person().add(version_object);

            ObjectNode content = Json.newObject();
            content.put("m_code", help.m_code);

            Model_FileRecord.uploadAzure_Version(content.toString(), "m_program.json" , main_m_program.get_path() ,  version_object);

            return GlobalResult.result_created( Json.toJson(  main_m_program.program_version(version_object) ) );

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            @ExtensionProperty(name = "Static Permission key"    , value = "M_Program_read" ),
                            @ExtensionProperty(name = "Dynamic Permission key"   , value = "M_Program_read.{project_id}"),
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
    @Security.Authenticated(Secured_API.class)
    public Result get_M_Program(@ApiParam(value = "m_program_id String query", required = true)  String m_program_id) {
        try {
            Model_MProgram m_program = Model_MProgram.get_byId(m_program_id);
            if (m_program == null) return GlobalResult.result_notFound("M_Project m_project_id not found");

            if (!m_program.read_permission())  return GlobalResult.result_forbidden();

            return GlobalResult.result_ok(Json.toJson(m_program));
        } catch (Exception e) {
            terminal_logger.internalServerError("get_M_Program:", e);
            return Server_Logger.result_internalServerError(e, request());
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
                            @ExtensionProperty(name = "Static Permission key"    , value = "M_Program_read" ),
                            @ExtensionProperty(name = "Dynamic Permission key"   , value = "M_Program_read.{project_id}"),
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
    @Security.Authenticated(Secured_API.class)
    public Result get_M_Program_version(@ApiParam(value = "m_program_version_id String query", required = true)  String m_program_version_id){

        try {
            // Kontrola objektu
            Model_VersionObject version_object = Model_VersionObject.get_byId(m_program_version_id);
            if (version_object == null) return GlobalResult.result_notFound("Version_Object version_id not found");

            // Kontrola oprávnění
            if (version_object.m_program == null)
                return GlobalResult.result_notFound("Version_Object is not version of B_Program");

            // Kontrola oprávnění
            if (!version_object.m_program.read_permission()) return GlobalResult.result_forbidden();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version_object.m_program.program_version(version_object)));

        }catch (Exception e) {
            e.printStackTrace();
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit M_Program",
            tags = {"M_Program"},
            notes = "update m_project - in this case we are not support versions of m_project",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Program.edit_permission", value = "true"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_M_Program_New",
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
    @Security.Authenticated(Secured_API.class)
    public Result edit_M_Program(@ApiParam(value = "m_program_id String query", required = true)  String m_program_id){
        try {

            final Form<Swagger_M_Program_New> form = Form.form(Swagger_M_Program_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_M_Program_New help = form.get();


            Model_MProgram m_program = Model_MProgram.get_byId(m_program_id);
            if (!m_program.edit_permission())  return GlobalResult.result_forbidden();

            if(m_program.m_project == null)  return GlobalResult.result_badRequest("You cannot change program on version");


            m_program.description = help.description;
            m_program.name        = help.name;

            m_program.update();

            return GlobalResult.result_ok(Json.toJson(m_program));
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit M_Program_Version",
            tags = {"M_Program"},
            notes = "edit M_Program_Version by quarry = m_program_version_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Program.edit_permision", value = "true"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_M_Program_Version_Edit",
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
    @Security.Authenticated(Secured_API.class)
    public Result edit_M_Program_version(@ApiParam(value = "m_program_version_id String query", required = true) String m_program_version_id){
        try {

            final Form<Swagger_M_Program_Version_Edit> form = Form.form(Swagger_M_Program_Version_Edit.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_M_Program_Version_Edit help = form.get();

            // Získání objektu
            Model_VersionObject version_object  = Model_VersionObject.get_byId(m_program_version_id);

            // Kontrola objektu
            if (version_object == null) return GlobalResult.result_notFound("Version_Object id not found");
            if (version_object.m_program == null) return GlobalResult.result_badRequest("M_Project m_project_id not found");

            // Kontrola oprávnění
            if (! version_object.m_program.edit_permission() ) return GlobalResult.result_forbidden();

            // Úprava objektu
            version_object.version_description = help.version_description;
            version_object.version_name        = help.version_name;

            version_object.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
    @Security.Authenticated(Secured_API.class)
    public Result remove_M_Program(@ApiParam(value = "m_program_id String query", required = true) String m_program_id){
        try {

            Model_MProgram m_program = Model_MProgram.get_byId(m_program_id);
            if (m_program == null) return GlobalResult.result_notFound("M_Project m_project_id not found");

            if (!m_program.delete_permission())  return GlobalResult.result_forbidden();
            m_program.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete M_Program_Version",
            tags = {"M_Program"},
            notes = "remove version of M_Program",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "M_Program.remove_permission", value = Model_MProgram.read_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "M_Program_remove" )
                    })
            }
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
            Model_VersionObject version_object  = Model_VersionObject.get_byId(m_program_version_id);

            // Kontrola objektu
            if (version_object == null) return GlobalResult.result_notFound("Version_Object id not found");
            if (version_object.m_program == null) return GlobalResult.result_badRequest("M_Project m_project_id not found");

            // Kontrola oprávnění
            if (! version_object.m_program.delete_permission() ) return GlobalResult.result_forbidden();

            // Smazání objektu
            version_object.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();


        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            @ExtensionProperty(name = "Static Permission key"    , value = "M_Program_read" ),
                            @ExtensionProperty(name = "Dynamic Permission key"   , value = "M_Program_read.{project_id}"),
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
    public Result get_M_Program_byQR_Token_forMobile(String qr_token){
        try{


            terminal_logger.debug("get_M_Program_byQR_Token_forMobile: Connection token: " + qr_token);

            Model_MProgramInstanceParameter parameter = Model_MProgramInstanceParameter.find
                    .where()
                    .eq("connection_token" , qr_token)
                    .isNotNull("m_project_program_snapshot.instance_versions.instance_record.actual_running_instance")
                    .findUnique();

            if(parameter == null) return GlobalResult.result_notFound("MProgramInstanceParameter by token not found in database");

            try{

                return GlobalResult.result_ok(Json.toJson(parameter.get_connection_summary( ctx())));

            }catch (Tyrion_Exp_ForbidenPermission e){

                return GlobalResult.result_forbidden();

            }catch (Tyrion_Exp_ObjectNotValidAnymore e){

                return GlobalResult.result_badRequest("QR token is not valid anymore");

            }catch (Tyrion_Exp_Unauthorized e){

                return GlobalResult.result_unauthorized();

            }


        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Grid_Terminal_Identf",
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
    public Result check_identifiactor(String terminal_id){
        try{

            final Form<Swagger_Grid_Terminal_Identf> form = Form.form(Swagger_Grid_Terminal_Identf.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Grid_Terminal_Identf help = form.get();


            Model_GridTerminal terminal = Model_GridTerminal.get_byId(terminal_id);
            if(terminal == null){

                terminal = new Model_GridTerminal();
                terminal.device_name = help.device_name;
                terminal.device_type = help.device_type;
                terminal.date_of_create = new Date();
                terminal.save();

                return GlobalResult.result_created(Json.toJson(terminal));

            }else {

                terminal.ws_permission = true;
                terminal.m_program_access = true;
                terminal.up_to_date = true;
                terminal.date_of_last_update = new Date();
                terminal.update();
                return GlobalResult.result_ok(Json.toJson(terminal));
            }

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Grid_Terminal_Identf",
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
    //@Security.Authenticated(Secured_API.class) - Není záměrně!!!! - Ověřuje se v read_permision program může být public!
    public Result get_identificator(){
        try{

            final Form<Swagger_Grid_Terminal_Identf> form = Form.form(Swagger_Grid_Terminal_Identf.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Grid_Terminal_Identf help = form.get();

            Model_GridTerminal terminal = new Model_GridTerminal();
            terminal.device_name = help.device_name;
            terminal.device_type = help.device_type;
            terminal.date_of_create = new Date();

            if( Http.Context.current().request().headers().get("User-Agent")[0] != null) terminal.user_agent =  Http.Context.current().request().headers().get("User-Agent")[0];
            else  terminal.user_agent = "Unknown browser";


            // Tato část je určená pro nalezení tokenu a přihlášení uživatele - bylo totiž nutné zpřístupnit tuto metodu i nepřihlášeným (bez loginu). Kvuli tomu že by to přes  @Security.Authenticated(Secured_API.class)  neprošlo
            String[] token_values =  Http.Context.current().request().headers().get("X-AUTH-TOKEN");


            if ((token_values != null) && (token_values.length == 1) && (token_values[0] != null)) {
                terminal_logger.debug("get_identificator :: HTTP request containts X-AUTH-TOKEN");
                Model_Person person = Model_Person.get_byAuthToken(token_values[0]);
                if (person != null) {
                    terminal_logger.debug("get_identificator :: Person with X-AUTH-TOKEN found");
                  terminal.person = person;

                }else {
                    terminal_logger.warn("get_identificator :: Person with X-AUTH-TOKEN not found!");
                }
            }

            terminal.save();
            return GlobalResult.result_created(Json.toJson(terminal));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

// TYPE_OF_WIDGET ######################################################################################################

    @ApiOperation(value = "create Type_Of_Widget",
            tags = {"Type-of-Widget"},
            notes = "creating group for GridWidgets -> Type_Of_Widget",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "TypeOfWidget_create_permission", value = Model_TypeOfWidget.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfWidget_create_permission" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfWidget_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_TypeOfWidget.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result typeOfWidget_create(){
        try{

            // Zpracování Json
            final Form<Swagger_TypeOfWidget_New> form = Form.form(Swagger_TypeOfWidget_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_TypeOfWidget_New help = form.get();


            if(   Model_TypeOfWidget.find.where().isNull("project").eq("name",help.name).findRowCount() > 0) return GlobalResult.result_badRequest("Type_Of_Widget with this name already exists, type a new one.");

            // Vytvoření objektu
            Model_TypeOfWidget typeOfWidget = new Model_TypeOfWidget();
            typeOfWidget.description = help.description;
            typeOfWidget.name        = help.name;


            // Nejedná se o privátní Typ Widgetu
            if(help.project_id != null){

                // Kontrola objektu
                Model_Project project = Model_Project.get_byId(help.project_id);
                if(project == null) return GlobalResult.result_notFound("Project project_id not found");
                if(! project.update_permission()) return GlobalResult.result_forbidden();
                typeOfWidget.publish_type = Enum_Publishing_type.private_program;

                // Úprava objektu
                typeOfWidget.project = project;
                project.cache_list_type_of_widgets_ids.clear();

            }else {
                typeOfWidget.publish_type = Enum_Publishing_type.public_program;

            }

            // Kontrola oprávnění těsně před uložením podle standardu
            if (!typeOfWidget.create_permission() ) return GlobalResult.result_forbidden();

            // Uložení objektu
            typeOfWidget.save();

            // Vrácení objektu
            return GlobalResult.result_created( Json.toJson(typeOfWidget));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Type_Of_Widget",
            tags = {"Type-of-Widget"},
            notes = "get TypeOfWidget",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "TypeOfWidget_read_permission", value = Model_TypeOfWidget.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project == null - Public TypeOfWidget", value = "Permission not Required!"),
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfWidget_create_permission" )
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_TypeOfWidget.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result typeOfWidget_get(@ApiParam(value = "type_of_widget_id String path",   required = true)  String type_of_widget_id){
        try {

            // Kontrola objektu
            Model_TypeOfWidget typeOfWidget = Model_TypeOfWidget.get_byId(type_of_widget_id);
            if(typeOfWidget == null) return GlobalResult.result_notFound("TypeOfWidget type_of_widget_id not found");

            // Kontrola oprávnění
            if (! typeOfWidget.read_permission() ) return GlobalResult.result_forbidden();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(typeOfWidget));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }


    }

    @ApiOperation(value = "edit Type_Of_Widget",
            tags = {"Type-of-Widget"},
            notes = "edit Type_Of_Widget object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfWidget.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfWidget_edit_permission" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfWidget_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_TypeOfWidget.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result typeOfWidget_update(@ApiParam(value = "type_of_widget_id String path",   required = true)  String type_of_widget_id){
        try{

            // Zpracování Json
            final Form<Swagger_TypeOfWidget_New> form = Form.form(Swagger_TypeOfWidget_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_TypeOfWidget_New help = form.get();

            // Kontrola objektu
            Model_TypeOfWidget typeOfWidget = Model_TypeOfWidget.get_byId(type_of_widget_id);
            if(typeOfWidget == null) return GlobalResult.result_notFound("TypeOfWidget type_of_widget_id not found");

            // Kontrola oprávnění
            if (! typeOfWidget.edit_permission() ) return GlobalResult.result_forbidden();

            // Úprava objektu
            typeOfWidget.description = help.description;
            typeOfWidget.name                = help.name;

            if(help.project_id != null){

                // Kontrola objektu
                Model_Project project = Model_Project.get_byId(help.project_id);
                if(project == null) return GlobalResult.result_notFound("Project project_id not found");

                // Úprava objektu
                typeOfWidget.project = project;

            }

            // Uložení objektu
            typeOfWidget.update();

            // Vrácení objektu
            return GlobalResult.result_ok( Json.toJson(typeOfWidget));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Type_Of_Widget",
            tags = {"Type-of-Widget"},
            notes = "delete group for GridWidgets -> Type_Of_Widget",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfWidget.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfWidget_delete_permission")
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
    @Security.Authenticated(Secured_API.class)
    public Result typeOfWidget_delete(@ApiParam(value = "type_of_widget_id String path",   required = true)  String type_of_widget_id){
        try{

            // Kontrola objektu
            Model_TypeOfWidget typeOfWidget = Model_TypeOfWidget.get_byId(type_of_widget_id);
            if(typeOfWidget == null) return GlobalResult.result_notFound("TypeOfWidget type_of_widget_id not found");

            // Kontrola oprávnění
            if (! typeOfWidget.delete_permission()) return GlobalResult.result_forbidden();

            // Smazání objektu
            typeOfWidget.delete();

            // Vrácení objektu
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Type_Of_Widget All",
            tags = {"Type-of-Widget"},
            notes = "get all groups for GridWidgets -> Type_Of_Widget",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_TypeOfWidget.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result typeOfWidget_getAll(){
        try {

            // Získání seznamu
            List<Model_TypeOfWidget> typeOfWidgets = Model_TypeOfWidget.get_all();

            // Vrácení seznamu
            return GlobalResult.result_ok(Json.toJson(typeOfWidgets));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Type_Of_Widget by Filter",
            tags = {"Type-of-Widget"},
            notes = "get TypeOfWidget List",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "TypeOfWidget_read_permission", value = "No need to check permission, because Tyrion returns only those results which user owns"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfWidget_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Type_Of_Widget_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result typeOfWidget_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number){
        try {

            // Získání JSON
            final Form<Swagger_TypeOfWidget_Filter> form = Form.form(Swagger_TypeOfWidget_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_TypeOfWidget_Filter help = form.get();

            // Získání všech objektů a následné odfiltrování soukormých TypeOfWidget
            Query<Model_TypeOfWidget> query = Ebean.find(Model_TypeOfWidget.class);

            // Order
            query.order().asc("order_position");

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if(help.project_id != null){

                Model_Project project = Model_Project.get_byId(help.project_id);
                if(project == null )return GlobalResult.result_notFound("Project not found");
                if(!project.read_permission())return GlobalResult.result_forbidden();

                query.where().eq("project.id", help.project_id).eq("removed_by_user", false);
            }

            if(help.public_programs){

                if(!Controller_Security.get_person().has_permission(Model_CProgram.permissions.C_Program_community_publishing_permission.name())) {
                    query.where().isNull("project").eq("removed_by_user", false).eq("publish_type", Enum_Publishing_type.public_program.name());
                }else {
                    query.where().isNull("project").eq("removed_by_user", false).eq("active", true).eq("publish_type", Enum_Publishing_type.public_program.name());
                }

            }


            // Order
            query.order().asc("order_position");


            // Vytvoření odchozího JSON
            Swagger_Type_Of_Widget_List result = new Swagger_Type_Of_Widget_List(query, page_number);

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            e.printStackTrace();
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "deactivate Type_Of_Widget",
            tags = {"Admin-Type-of-Widget"},
            notes = "deactivate Type of Widget",
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
    @Security.Authenticated(Secured_API.class)
    public Result typeOfWidget_deactivate(String type_of_widget_id){
        try {

            Model_TypeOfWidget typeOfWidget = Model_TypeOfWidget.get_byId(type_of_widget_id);
            if(typeOfWidget == null) return GlobalResult.result_notFound("TypeOfWidget type_of_widget_id not found");

            // Kontrola oprávnění
            if (! typeOfWidget.edit_permission() ) return GlobalResult.result_forbidden();


            if (!typeOfWidget.active) return GlobalResult.result_badRequest("Tariff is already deactivated");

            if(!typeOfWidget.update_permission()) return GlobalResult.result_forbidden();

            typeOfWidget.active = false;

            typeOfWidget.update();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "activate Type_Of_Widget",
            tags = {"Admin-Type-of-Widget"},
            notes = "activate Type of Widget",
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
    @Security.Authenticated(Secured_API.class)
    public Result typeOfWidget_activate(String type_of_widget_id){
        try {

            Model_TypeOfWidget typeOfWidget = Model_TypeOfWidget.get_byId(type_of_widget_id);
            if(typeOfWidget == null) return GlobalResult.result_notFound("TypeOfWidget type_of_widget_id not found");

            if (typeOfWidget.active) return GlobalResult.result_badRequest("Tariff is already activated");

            if(!typeOfWidget.update_permission()) return GlobalResult.result_forbidden();

            typeOfWidget.active = true;

            typeOfWidget.update();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "order Type_Of_Widget Up",
            tags = {"Type-of-Widget"},
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
    @Security.Authenticated(Secured_API.class)
    public Result typeOfWidget_order_up(@ApiParam(value = "type_of_widget_id String path",   required = true) String type_of_widget_id){
        try{

            Model_TypeOfWidget typeOfWidget = Model_TypeOfWidget.get_byId(type_of_widget_id);
            if(typeOfWidget == null) return GlobalResult.result_notFound("Tariff not found ");

            // Kontrola oprávnění
            if (! typeOfWidget.edit_permission()) return GlobalResult.result_forbidden();

            typeOfWidget.up();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "order Type_Of_Widget Down",
            tags = {"Type-of-Widget"},
            notes = "set down order in list",
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
    @Security.Authenticated(Secured_API.class)
    public Result typeOfWidget_order_down(@ApiParam(value = "type_of_widget_id String path",   required = true) String type_of_widget_id){
        try{

            Model_TypeOfWidget typeOfWidget = Model_TypeOfWidget.get_byId(type_of_widget_id);
            if(typeOfWidget == null) return GlobalResult.result_notFound("Tariff not found");

            // Kontrola oprávnění
            if (! typeOfWidget.edit_permission()) return GlobalResult.result_forbidden();

            typeOfWidget.down();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }
// GRID_WIDGET #########################################################################################################

    @ApiOperation(value = "create Grid_Widget",
            tags = {"Grid-Widget"},
            notes = "creating new independent Widget object for Grid tools",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "GridWidget_create_permission", value = Model_GridWidget.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfWidget.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "GridWidget_create_permission" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_GridWidget_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_GridWidget.class),
            @ApiResponse(code = 400, message = "Something went wrong",    response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result gridWidget_create(){
        try{

            // Zpracování Json
            final Form<Swagger_GridWidget_New> form = Form.form(Swagger_GridWidget_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_GridWidget_New help = form.get();

            // Kontrola objektu
            Model_TypeOfWidget typeOfWidget = Model_TypeOfWidget.get_byId( help.type_of_widget_id);
            if(typeOfWidget == null) return GlobalResult.result_notFound("TypeOfWidget type_of_widget_id not found");

            if (typeOfWidget.project == null && Model_GridWidget.get_publicByName(help.name) != null){
                return GlobalResult.result_badRequest("GridWidget with this name already exists, type a new one.");
            }

            // Vytvoření objektu
            Model_GridWidget gridWidget = new Model_GridWidget();

            gridWidget.description         = help.description;
            gridWidget.name                = help.name;
            gridWidget.author              = Controller_Security.get_person();
            gridWidget.type_of_widget      = typeOfWidget;
            gridWidget.publish_type        = Enum_Publishing_type.private_program;

            // Kontrola oprávnění těsně před uložením
            if (!gridWidget.create_permission() ) return GlobalResult.result_forbidden();

            // Uložení objektu
            gridWidget.save();

            gridWidget.refresh();
            typeOfWidget.grid_widgets_ids.add(0, gridWidget.id.toString());

            // Získání šablony
            Model_GridWidgetVersion scheme = Model_GridWidgetVersion.find.where().eq("publish_type", Enum_Publishing_type.default_version.name()).findUnique();

            // Kontrola objektu
            if(scheme == null) return GlobalResult.result_created( Json.toJson(gridWidget) );

            // Vytvoření objektu první verze
            Model_GridWidgetVersion gridWidgetVersion = new Model_GridWidgetVersion();
            gridWidgetVersion.version_name = "0.0.1";
            gridWidgetVersion.version_description = "This is a first version of widget.";
            gridWidgetVersion.approval_state = Enum_Approval_state.approved;
            gridWidgetVersion.design_json = scheme.design_json;
            gridWidgetVersion.logic_json = scheme.logic_json;
            gridWidgetVersion.date_of_create = new Date();
            gridWidgetVersion.grid_widget = gridWidget;
            gridWidgetVersion.save();

            // Vrácení objektu
            return GlobalResult.result_created( Json.toJson(gridWidget) );

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            dataType = "utilities.swagger.documentationClass.Swagger_GridWidget_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_GridWidget.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result gridWidget_update(@ApiParam(value = "grid_widget_id String path",   required = true)  String grid_widget_id){
        try {

            // Zpracování Json
            final Form<Swagger_GridWidget_New> form = Form.form(Swagger_GridWidget_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_GridWidget_New help = form.get();

            // Kontrola objektu
            Model_GridWidget gridWidget = Model_GridWidget.get_byId(grid_widget_id);
            if (gridWidget == null) return GlobalResult.result_notFound("GridWidget grid_widget_id not found");

            // Kontrola oprávnění
            if (! gridWidget.edit_permission() ) return GlobalResult.result_forbidden();

            // Úprava objektu
            gridWidget.description        = help.description;
            gridWidget.name                = help.name;

            // Kontrola objektu
            Model_TypeOfWidget typeOfWidget = Model_TypeOfWidget.get_byId(  help.type_of_widget_id);
            if(typeOfWidget == null) return GlobalResult.result_notFound("TypeOfWidget type_of_widget_id not found");

            // Úprava objektu
            gridWidget.type_of_widget = typeOfWidget;

            // Uložení objektu
            gridWidget.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(gridWidget));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            @ExtensionProperty(name = "GridWidgetVersion_read_permission", value = Model_GridWidgetVersion.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "GridWidget.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "GridWidgetVersion_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_GridWidgetVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result gridWidgetVersion_get(@ApiParam(value = "grid_widget_version_id String path",   required = true) String grid_widget_version_id){
        try {
            // Kontrola objektu
            Model_GridWidgetVersion version = Model_GridWidgetVersion.get_byId(grid_widget_version_id);
            if(version == null) return GlobalResult.result_notFound("GridWidget grid_widget_id not found");

            // Kontrola oprávnění
            if (!version.read_permission() ) return GlobalResult.result_forbidden("You have no permission to get that");

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            @ExtensionProperty(name = "GridWidget_read_permission", value = Model_GridWidget.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "GridWidget.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "GridWidget_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_GridWidget.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result gridWidget_get(@ApiParam(value = "grid_widget_id String path",   required = true) String grid_widget_id){
        try {
            // Kontrola objektu
            Model_GridWidget gridWidget = Model_GridWidget.get_byId(grid_widget_id);
            if(gridWidget == null) return GlobalResult.result_notFound("GridWidget grid_widget_id not found");

            // Kontrola oprávnění

            if (! gridWidget.read_permission() ) return GlobalResult.result_forbidden();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(gridWidget));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            dataType = "utilities.swagger.documentationClass.Swagger_GridWidget_Filter",
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
    @Security.Authenticated(Secured_API.class)
    public Result gridWidget_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number){
        try {

            // Získání JSON
            final Form<Swagger_GridWidget_Filter> form = Form.form(Swagger_GridWidget_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_GridWidget_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_GridWidget> query = Ebean.find(Model_GridWidget.class);

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if(help.project_id != null){

                Model_Project project = Model_Project.get_byId(help.project_id);
                if(project == null )return GlobalResult.result_notFound("Project not found");
                if(!project.read_permission())return GlobalResult.result_forbidden();

                query.where().eq("type_of_widget.project.id", help.project_id);
            }

            if(help.pending_widget){
                query.where().eq("grid_widget_versions.approval_state", Enum_Approval_state.pending.name()).eq("grid_widget_versions.removed_by_user", false);
            }

            // Vytvoření odchozího JSON
            Swagger_GridWidget_List result = new Swagger_GridWidget_List(query, page_number);

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            terminal_logger.internalServerError("gridWidget_getByFilter:", e);
            return Server_Logger.result_internalServerError(e, request());
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
    @Security.Authenticated(Secured_API.class)
    public Result gridWidget_delete(@ApiParam(value = "grid_widget_id String path",   required = true)  String grid_widget_id){
        try {

            // Kontrola objektu
            Model_GridWidget gridWidget = Model_GridWidget.get_byId(grid_widget_id);
            if(gridWidget == null) return GlobalResult.result_notFound("GridWidget grid_widget_id not found");

            // Kontrola oprávnění
            if (! gridWidget.delete_permission()) return GlobalResult.result_forbidden();

            // Smazání objektu
            gridWidget.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Grid_Widget_Copy",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_GridWidget.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Secured_API.class)
    public Result gridWidget_clone() {
        try {

            // Zpracování Json
            final Form<Swagger_Grid_Widget_Copy> form = Form.form(Swagger_Grid_Widget_Copy.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Grid_Widget_Copy help = form.get();

            // Vyhledám Objekt
            Model_GridWidget grid_widget_old = Model_GridWidget.get_byId(help.grid_widget_id);
            if(grid_widget_old == null) return GlobalResult.result_notFound("Model_GridWidget grid_widget_id not found");

            // Zkontroluji oprávnění
            if(!grid_widget_old.read_permission())  return GlobalResult.result_forbidden();

            // Vyhledám Objekt
            Model_Project project = Model_Project.get_byId(help.project_id);
            if (project == null) return GlobalResult.result_notFound("Project project_id not found");

            // Zkontroluji oprávnění
            if(!project.update_permission())  return GlobalResult.result_forbidden();


            // Kontrola objektu
            Model_TypeOfWidget typeOfWidget = Model_TypeOfWidget.get_byId(help.type_of_widget_id);
            if(typeOfWidget != null) {
                if (!Model_Project.get_byId(typeOfWidget.project_id()).update_permission()) {
                    return GlobalResult.result_forbidden();
                }
            }

            if(typeOfWidget == null) {
                typeOfWidget = new Model_TypeOfWidget();
                typeOfWidget.description = "Yea! My First Widget Group with Community Widget";
                typeOfWidget.name        = "Private Widget Group";
                typeOfWidget.project     = project;
                typeOfWidget.save();

                typeOfWidget.refresh();
            }


            Model_GridWidget grid_widget_new =  new Model_GridWidget();
            grid_widget_new.name = help.name;
            grid_widget_new.description = help.description;
            grid_widget_new.type_of_widget = typeOfWidget;
            grid_widget_new.save();

            grid_widget_new.refresh();


            for(Model_GridWidgetVersion version : grid_widget_old.get_grid_widget_versions()){

                Model_GridWidgetVersion copy_object = new Model_GridWidgetVersion();
                copy_object.version_name        = version.version_name;
                copy_object.date_of_create      = version.date_of_create;
                copy_object.version_description = version.version_description;
                copy_object.date_of_create      = new Date();
                copy_object.author              = version.author;
                copy_object.design_json         = version.design_json;
                copy_object.logic_json          = version.logic_json;
                copy_object.grid_widget         = grid_widget_new;

                // Zkontroluji oprávnění
                copy_object.save();

            }

            grid_widget_new.refresh();

            // Vracím Objekt
            return GlobalResult.result_ok(Json.toJson(grid_widget_new));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

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
    @Security.Authenticated(Secured_API.class)
    public Result gridWidgetVersion_delete(@ApiParam(value = "grid_widget_version_id String path",   required = true) String grid_widget_version_id){
        try {

            // Kontrola objektu
            Model_GridWidgetVersion version = Model_GridWidgetVersion.get_byId(grid_widget_version_id);
            if(version == null) return GlobalResult.result_notFound("GridWidgetVersion grid_widget_version_id not found");

            // Kontrola oprávnění
            if (! version.delete_permission()) return GlobalResult.result_forbidden();

            version.grid_widget.cache_value_grid_versions_id.clear();
            Model_GridWidget.cache.put(version.grid_widget.id.toString(), version.grid_widget);

            // Smazání objektu
            version.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
    @Security.Authenticated(Secured_API.class)
    public Result gridWidgetVersion_set_main(String grid_widget_version_id){
        try {

            // Kontrola objektu
            Model_GridWidgetVersion version = Model_GridWidgetVersion.get_byId(grid_widget_version_id);
            if(version == null) return GlobalResult.result_notFound("GridWidgetVersion grid_widget_version_id not found");

            // Kontrola oprávnění
            if (!version.edit_permission()) return GlobalResult.result_forbidden();

            if(!version.get_grid_widget_id().equals("00000000-0000-0000-0000-000000000001")){
                return GlobalResult.result_notFound("GridWidgetVersion grid_widget_version_id not from default program");
            }

            Model_GridWidgetVersion old_version = Model_GridWidgetVersion.find.where().eq("publish_type", Enum_Publishing_type.default_version.name()).select("id").findUnique();
            if(old_version != null) {
                old_version = Model_GridWidgetVersion.get_byId(old_version.id);
                old_version.publish_type = null;
                old_version.update();
                Model_GridWidgetVersion.cache.put(old_version.id, old_version);
            }

            version.publish_type = Enum_Publishing_type.default_version;
            version.update();

            Model_GridWidgetVersion.cache.put(version.id, version);

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            @ExtensionProperty(name = "Static Permission key", value = "C_Program_edit"),
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
    @Security.Authenticated(Secured_API.class)
    public Result gridWidgetVersion_version_make_public(String grid_widget_version_id){
        try {

            System.out.println("gridWidgetVersion_version_make_public .... ");
            // Kontrola objektu
            Model_GridWidgetVersion gridWidgetVersion = Model_GridWidgetVersion.get_byId(grid_widget_version_id);
            if(gridWidgetVersion == null) return GlobalResult.result_notFound("GridWidgetVersion grid_widget_version_id not found");

            // Kontrola orávnění
            if(!(gridWidgetVersion.read_permission())) return GlobalResult.result_forbidden();

            if(Model_GridWidgetVersion.find.where().eq("approval_state", Enum_Approval_state.pending.name())
                    .eq("author.id", Controller_Security.get_person_id())
                    .findList().size() > 3) {
                // TODO Notifikace uživatelovi
                return GlobalResult.result_badRequest("You can publish only 3 programs. Wait until the previous ones approved by the administrator. Thanks.");
            }

            if(gridWidgetVersion.approval_state != null)  return GlobalResult.result_badRequest("You cannot publish same program twice!");

            // Úprava objektu
            gridWidgetVersion.approval_state = Enum_Approval_state.pending;

            // Kontrola oprávnění
            if(!(gridWidgetVersion.edit_permission())) return GlobalResult.result_forbidden();

            // Uložení změn
            gridWidgetVersion.update();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
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
                            @ExtensionProperty(name = "GridWidgetVersion_create_permission", value = Model_GridWidgetVersion.create_permission_docs ),
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
                            dataType = "utilities.swagger.documentationClass.Swagger_GridWidgetVersion_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_GridWidgetVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result gridWidgetVersion_create(@ApiParam(value = "grid_widget_id String path",   required = true) String grid_widget_id){
        try {

            // Zpracování Json
            final Form<Swagger_GridWidgetVersion_New> form = Form.form(Swagger_GridWidgetVersion_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_GridWidgetVersion_New help = form.get();

            // Kontrola názvu
            if(help.version_name.equals("version_scheme")) return GlobalResult.result_badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_GridWidget gridWidget = Model_GridWidget.get_byId(grid_widget_id);
            if(gridWidget == null) return GlobalResult.result_notFound("GridWidget not found");

            // Vytvoření objektu
            Model_GridWidgetVersion version = new Model_GridWidgetVersion();
            version.date_of_create = new Date();

            version.version_name = help.version_name;
            version.version_description = help.version_description;
            version.design_json = help.design_json;
            version.logic_json = help.logic_json;
            version.grid_widget = gridWidget;
            version.author = Controller_Security.get_person();

            // Kontrola oprávnění
            if (! version.create_permission()) return GlobalResult.result_forbidden();

            // Uložení objektu
            version.save();

            version.refresh();

            gridWidget.cache_value_grid_versions_id.clear();
            Model_GridWidget.cache.put(gridWidget.id.toString(), gridWidget);

            // Vrácení objektu
            return GlobalResult.result_created(Json.toJson(gridWidget));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            dataType = "utilities.swagger.documentationClass.Swagger_GridWidgetVersion_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_GridWidgetVersion.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result gridWidgetVersion_edit(@ApiParam(value = "grid_widget_version_id String path",   required = true) String grid_widget_version_id){
        try {

            System.out.println("gridWidgetVersion_edit .... ");
            // Zpracování Json
            final Form<Swagger_GridWidgetVersion_Edit> form = Form.form(Swagger_GridWidgetVersion_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_GridWidgetVersion_Edit help = form.get();

            // Kontrola objektu
            Model_GridWidgetVersion version = Model_GridWidgetVersion.get_byId(grid_widget_version_id);
            if(version == null) {
                return GlobalResult.result_notFound("grid_widget_version_id not found");
            }

            System.out.println("Co je tohle za verzi??  " + version.version_name );

            // Úprava objektu
            version.version_name = help.version_name;
            version.version_description = help.version_description;


            // Uložení objektu
            version.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            @ExtensionProperty(name = "GridWidgetVersion_read_permission", value = Model_GridWidgetVersion.read_permission_docs),
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
                            dataType = "utilities.swagger.documentationClass.Swagger_GridWidgetVersion_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_GridWidgetVersion.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result gridWidgetVersion_getAll(@ApiParam(value = "grid_widget_id String path",   required = true) String grid_widget_id){
        try {

            // Kontrola objektu
            Model_GridWidget gridWidget = Model_GridWidget.get_byId(grid_widget_id);
            if (gridWidget == null) return GlobalResult.result_notFound("GridWidget grid_widget_id not found");

            // Kontrola oprávnění
            if (! gridWidget.read_permission()) return GlobalResult.result_forbidden();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(gridWidget.grid_widget_versions));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
    @Security.Authenticated(Secured_API.class)
    public Result gridWidget_deactivate(String grid_widget_id){
        try {

            Model_GridWidget gridWidget = Model_GridWidget.get_byId(grid_widget_id);
            if(gridWidget == null) return GlobalResult.result_notFound("GridWidget not found");

            // Kontrola oprávnění
            if (! gridWidget.update_permission() ) return GlobalResult.result_forbidden();

            if (!gridWidget.active) return GlobalResult.result_badRequest("Tariff is already deactivated");


            gridWidget.active = false;

            gridWidget.update();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
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
    @Security.Authenticated(Secured_API.class)
    public Result gridWidget_activate(String grid_widget_id){
        try {

            Model_GridWidget gridWidget = Model_GridWidget.get_byId(grid_widget_id);
            if(gridWidget == null) return GlobalResult.result_notFound("GridWidget not found");

            if(!gridWidget.update_permission()) return GlobalResult.result_forbidden();

            if (gridWidget.active) return GlobalResult.result_badRequest("Tariff is already activated");

            gridWidget.active = true;

            gridWidget.update();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
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
    @Security.Authenticated(Secured_API.class)
    public Result gridWidget_order_up(@ApiParam(value = "grid_widget_id String path",   required = true) String grid_widget_id){
        try{

            Model_GridWidget gridWidget = Model_GridWidget.get_byId(grid_widget_id);
            if(gridWidget == null) return GlobalResult.result_notFound("GridWidget not found");

            // Kontrola oprávnění
            if (! gridWidget.edit_permission()) return GlobalResult.result_forbidden();

            gridWidget.up();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
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
    @Security.Authenticated(Secured_API.class)
    public Result gridWidget_order_down(@ApiParam(value = "grid_widget_id String path",   required = true) String grid_widget_id){
        try{

            Model_GridWidget gridWidget =  Model_GridWidget.get_byId(grid_widget_id);
            if(gridWidget == null) return GlobalResult.result_notFound("GridWidget not found");

            // Kontrola oprávnění
            if (!gridWidget.edit_permission()) return GlobalResult.result_forbidden();

            gridWidget.down();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
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
                            dataType = "utilities.swagger.documentationClass.Swagger_GridWidget_Publish_Response",
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
    @Security.Authenticated(Secured_API.class)
    public Result grid_widget_public_response() {

        try {

            // Získání JSON
            final Form<Swagger_GridWidget_Publish_Response> form = Form.form(Swagger_GridWidget_Publish_Response.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_GridWidget_Publish_Response help = form.get();

            // Kontrola názvu
            if(help.version_name.equals("version_scheme")) return GlobalResult.result_badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_GridWidgetVersion privateGridWidgetVersion = Model_GridWidgetVersion.get_byId(help.version_id);
            if (privateGridWidgetVersion == null) return GlobalResult.result_notFound("grid_widget_version not found");

            // Kontrola nadřazeného objektu
            Model_GridWidget widget_old = Model_GridWidget.get_byId(privateGridWidgetVersion.get_grid_widget_id());

            // Zkontroluji oprávnění
            if(!widget_old.community_publishing_permission()){
                return GlobalResult.result_forbidden();
            }


            if(help.decision) {

                // Kontrola skupiny kam se widget zařadí
                Model_TypeOfWidget typeOfWidget_public = Model_TypeOfWidget.find.byId(help.grid_widget_type_of_widget_id);
                if (typeOfWidget_public == null) {
                    return GlobalResult.result_notFound("Model_TypeOfWidget not found");
                }

                if (typeOfWidget_public.publish_type != Enum_Publishing_type.public_program) {
                    return GlobalResult.result_badRequest("You cannot register Widget to non-public group");
                }

                System.out.println("help.decision je true!!!");

                privateGridWidgetVersion.approval_state = Enum_Approval_state.approved;
                privateGridWidgetVersion.update();

                Model_GridWidget gridWidget = Model_GridWidget.find.where().eq("id",widget_old.id.toString() + "_public_copy").findUnique();

                if(gridWidget == null) {
                    // Vytvoření objektu
                    gridWidget = new Model_GridWidget();
                    gridWidget.name = help.program_name;
                    gridWidget.description = help.program_description;
                    gridWidget.type_of_widget = typeOfWidget_public;
                    gridWidget.author = privateGridWidgetVersion.get_grid_widget().get_author();
                    gridWidget.publish_type = Enum_Publishing_type.public_program;
                    gridWidget.save();
                }

                // Vytvoření objektu
                Model_GridWidgetVersion gridWidgetVersion = new Model_GridWidgetVersion();
                gridWidgetVersion.version_name = help.version_name;
                gridWidgetVersion.version_description = help.version_description;
                gridWidgetVersion.design_json = privateGridWidgetVersion.design_json;
                gridWidgetVersion.logic_json = privateGridWidgetVersion.logic_json;
                gridWidgetVersion.approval_state = Enum_Approval_state.approved;
                gridWidgetVersion.grid_widget = gridWidget;
                gridWidgetVersion.date_of_create = new Date();
                gridWidgetVersion.save();

                gridWidget.refresh();

                // TODO notifikace a emaily

                return GlobalResult.result_ok();

            }else {
                // Změna stavu schválení
                privateGridWidgetVersion.approval_state = Enum_Approval_state.disapproved;

                // Odeslání emailu s důvodem
                try {

                    new Email()
                            .text("Version of Widget " + privateGridWidgetVersion.get_grid_widget().name + ": " + Email.bold(privateGridWidgetVersion.version_name) + " was not approved for this reason: ")
                            .text(help.reason)
                            .send(privateGridWidgetVersion.get_grid_widget().get_author().mail, "Version of Widget disapproved" );

                } catch (Exception e) {
                    terminal_logger.internalServerError (e);
                }

                // Uložení změn
                privateGridWidgetVersion.update();

                // Vrácení výsledku
                return GlobalResult.result_ok();
            }

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

}
