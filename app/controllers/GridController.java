package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.*;
import models.compiler.FileRecord;
import models.compiler.Version_Object;
import models.project.b_program.instnace.Homer_Instance_Record;
import models.project.global.Project;
import models.project.m_program.Grid_Terminal;
import models.project.m_program.M_Program;
import models.project.m_program.M_Project;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Swagger_M_Program_Interface;
import utilities.swagger.outboundClass.Swagger_M_Project_Interface;
import utilities.swagger.outboundClass.Swagger_Mobile_Connection_Summary;
import utilities.swagger.outboundClass.Swagger_Mobile_M_Project_Snapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Api(value = "Not Documented API - InProgress or Stuck")
public class GridController extends Controller {

    @ApiOperation(value = "Create new M_Project",
            tags = {"M_Program"},
            notes = "M_Project is package for M_Programs -> presupposition is that you need more control terminal for your IoT project. " +
                    "Different screens for family members, for employee etc.. But of course - you can used that for only one M_program",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "M_Project_create_permission", value = M_Project.create_permission_docs ),
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
            @ApiResponse(code = 201, message = "Successful created",      response = M_Project.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result new_M_Project(String project_id) {
        try{

            final Form<Swagger_M_Project_New> form = Form.form(Swagger_M_Project_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_M_Project_New help = form.get();

            Project project = Project.find.byId( project_id );
            if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

            M_Project m_project = new M_Project();
            m_project.description = help.description;
            m_project.name = help.name;
            m_project.date_of_create = new Date();
            m_project.project = project;

            if (!m_project.create_permission())  return GlobalResult.forbidden_Permission();
            m_project.save();

            return GlobalResult.created( Json.toJson(m_project));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
                            @ExtensionProperty(name = "M_Project.read_permission", value = M_Project.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key"    , value = "M_Project_read" ),
                            @ExtensionProperty(name = "Dynamic Permission key"   , value = "M_Project_read.{project_id}"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = M_Project.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result get_M_Project(@ApiParam(value = "m_project_id String query", required = true) String m_project_id){
        try {

            M_Project m_project = M_Project.find.byId(m_project_id);
            if (m_project == null) return GlobalResult.notFoundObject("M_Project m_project_id not found");

            if (!m_project.read_permission())  return GlobalResult.forbidden_Permission();
            return GlobalResult.result_ok(Json.toJson(m_project));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result",               response = M_Project.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result edit_M_Project(@ApiParam(value = "m_project_id String query", required = true) String m_project_id){
        try{

            final Form<Swagger_M_Project_New> form = Form.form(Swagger_M_Project_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_M_Project_New help = form.get();


            M_Project m_project = M_Project.find.byId(m_project_id);
            if(m_project == null) return GlobalResult.notFoundObject("M_Project m_project_id not found");

            if (!m_project.edit_permission())  return GlobalResult.forbidden_Permission();

            m_project.description = help.description;
            m_project.name = help.name;

            m_project.update();
            return GlobalResult.result_ok( Json.toJson(m_project));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove M_Project",
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result remove_M_Project(@ApiParam(value = "m_project_id String query", required = true)  String m_project_id){
        try{

            M_Project m_project = M_Project.find.byId(m_project_id);
            if(m_project == null) return GlobalResult.notFoundObject("M_project m_project_id not found");

            if (!m_project.delete_permission())  return GlobalResult.forbidden_Permission();
            m_project.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get accessible interface from M_Project",
            tags = {"M_Program"},
            notes = "get accessible interface from M_Project",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "M_Project.read_permission", value = M_Project.read_permission_docs ),
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
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Empty.class)
    @Security.Authenticated(Secured_API.class)
    public Result get_M_Project_Interface_collection(@ApiParam(value = "m_project_id String query", required = true)  String m_project_id) {
        try{

            M_Project m_project = M_Project.find.byId(m_project_id);
            if(m_project == null) return GlobalResult.notFoundObject("M_project m_project_id not found");

            if (!m_project.read_permission())  return GlobalResult.forbidden_Permission();


            Swagger_M_Project_Interface m_project_interface = new Swagger_M_Project_Interface();
            m_project_interface.name = m_project.name;
            m_project_interface.description = m_project.description;
            m_project_interface.id = m_project.id;

            for(M_Program m_program : m_project.m_programs) {

                Swagger_M_Program_Interface m_program_interface = new Swagger_M_Program_Interface();
                m_program_interface.description = m_program.description;
                m_program_interface.name        = m_program.name;
                m_program_interface.id          = m_program.id;

                m_program_interface.accessible_versions = m_program.program_versions_interface();
                m_project_interface.accessible_interface.add(m_program_interface);
            }

            return GlobalResult.result_ok(Json.toJson(m_project_interface));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


//######################################################################################################################

    @ApiOperation(value = "Create new M_Program",
            tags = {"M_Program"},
            notes = "creating new M_Program",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "M_Program.create_permission", value = M_Program.create_permission_docs),
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
            @ApiResponse(code = 201, message = "Successfully created",    response = M_Program.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result new_M_Program( @ApiParam(value = "m_project_id", required = true) String m_project_id) {
        try {

            final Form<Swagger_M_Program_New> form = Form.form(Swagger_M_Program_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_M_Program_New help = form.get();

            M_Project m_project = M_Project.find.byId( m_project_id );
            if(m_project == null) return GlobalResult.notFoundObject("M_Project m_project_id not found");


            M_Program m_program = new M_Program();

            m_program.date_of_create      = new Date();
            m_program.description         = help.description;
            m_program.name                = help.name;

            m_program.m_project           = m_project;

            if (!m_program.create_permission()) return GlobalResult.forbidden_Permission();
            m_program.save();

            return GlobalResult.created(Json.toJson(m_program));
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "Create new Version of M_Program",
            tags = {"M_Program"},
            notes = "creating new Version M_Program",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "M_Program.create_permission", value = M_Program.create_permission_docs),
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
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result new_M_Program_version( @ApiParam(value = "m_program_id", required = true) String m_program_id) {
        try {

            final Form<Swagger_M_Program_Version_New> form = Form.form(Swagger_M_Program_Version_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_M_Program_Version_New help = form.get();

            M_Program main_m_program = M_Program.find.byId( m_program_id );
            if(main_m_program == null) return GlobalResult.notFoundObject("M_Project m_project_id not found");

            if (!main_m_program.create_permission()) return GlobalResult.forbidden_Permission();

            Version_Object version_object      = new Version_Object();
            version_object.date_of_create      = new Date();
            version_object.version_description = help.version_description;
            version_object.version_name        = help.version_name;
            version_object.m_program           = main_m_program;
            version_object.author              = SecurityController.getPerson();
            version_object.public_version      = help.public_mode;
            version_object.qr_token            = UUID.randomUUID().toString() + UUID.randomUUID().toString();

            version_object.save();

            main_m_program.getVersion_objects().add(version_object);

            ObjectNode content = Json.newObject();
            content.put("m_code", help.m_code);
            content.put("virtual_input_output", help.virtual_input_output);

            FileRecord.uploadAzure_Version(content.toString(), "m_program.json" , main_m_program.get_path() ,  version_object);

            return GlobalResult.created( Json.toJson(  main_m_program.program_version(version_object) ) );

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Remove  Version of M_Program",
            tags = {"M_Program"},
            notes = "remove bersion of M_Program",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "M_Program.remove_permission", value = M_Program.read_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "M_Program_remove" )
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created",    response = Result_ok.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Empty.class)
    public Result remove_M_Program_version( @ApiParam(value = "m_program_version_id", required = true) String m_program_version_id) {
        try {

            // Získání objektu
            Version_Object version_object  = Version_Object.find.byId(m_program_version_id);

            // Kontrola objektu
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object id not found");
            if (version_object.m_program == null) return GlobalResult.result_BadRequest("M_Project m_project_id not found");

            // Kontrola oprávnění
            if (! version_object.m_program.delete_permission() ) return GlobalResult.forbidden_Permission();

            // Smazání objektu
            version_object.removed_by_user = true;
            version_object.update();

            // Vrácení potvrzení
            return GlobalResult.result_ok();


        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
                            @ExtensionProperty(name = "M_Program.read_permission", value = M_Program.read_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Program.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key"    , value = "M_Program_read" ),
                            @ExtensionProperty(name = "Dynamic Permission key"   , value = "M_Program_read.{project_id}"),
                    })
            }

    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = M_Program.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result get_M_Program(@ApiParam(value = "m_program_id String query", required = true)  String m_program_id) {
        try {
            M_Program m_program = M_Program.find.byId(m_program_id);
            if (m_program == null) return GlobalResult.notFoundObject("M_Project m_project_id not found");

            if (!m_program.read_permission())  return GlobalResult.forbidden_Permission();

            return GlobalResult.result_ok(Json.toJson(m_program));
        } catch (Exception e) {
            e.printStackTrace();
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update M_Program",
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
            @ApiResponse(code = 200, message = "Ok Result",               response = M_Project.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result edit_M_Program(@ApiParam(value = "m_program_id String query", required = true)  String m_program_id){
        try {

            final Form<Swagger_M_Program_New> form = Form.form(Swagger_M_Program_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_M_Program_New help = form.get();


            M_Program m_program = M_Program.find.byId(m_program_id);
            if (!m_program.edit_permission())  return GlobalResult.forbidden_Permission();

            if(m_program.m_project == null)  return GlobalResult.result_BadRequest("You cannot change program on version");


            m_program.description = help.description;
            m_program.name        = help.name;

            m_program.update();

            return GlobalResult.result_ok(Json.toJson(m_program));
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove M_Program",
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result remove_M_Program(@ApiParam(value = "m_program_id String query", required = true) String m_program_id){
        try {

            M_Program m_program = M_Program.find.byId(m_program_id);
            if (m_program == null) return GlobalResult.notFoundObject("M_Project m_project_id not found");

            if (!m_program.delete_permission())  return GlobalResult.forbidden_Permission();
            m_program.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_M_Program_Version.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_M_Program_byQR_Token_forMobile(@ApiParam(value = "qr_token String query", required = true) String qr_token){
        try{

            Homer_Instance_Record record = Homer_Instance_Record.find.where().eq("websocket_grid_token", qr_token).findUnique();
            if(record == null) return GlobalResult.notFoundObject("Instance not found");
            if(!record.version_object.public_version) return GlobalResult.forbidden_Permission("Instance is not public!");
            if(record.actual_running_instance == null)  return GlobalResult.notFoundObject("Instance not found or not running in cloud!");

            Swagger_Mobile_Connection_Summary summary = new Swagger_Mobile_Connection_Summary();
            summary.url = "ws://" + record.actual_running_instance.cloud_homer_server.server_url  + record.actual_running_instance.cloud_homer_server.grid_port + "/" + record.websocket_grid_token + "/#grid_connection_token";
            summary.m_program = M_Program.get_m_code(record.version_object);

            return GlobalResult.result_ok(Json.toJson(summary));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all M_Project( Programs) by Logged Person",
            tags = {"APP-Api"},
            notes = "get list of M_Programs by logged Person accasable and connectable to Homer server",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Mobile_M_Project_Snapshot.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result get_M_Project_all_forTerminal(){
        try{

            List<Homer_Instance_Record> list = Homer_Instance_Record.find.where()
                    .isNotNull("actual_running_instance")
                    .eq("main_instance_history.b_program.project.ownersOfProject.id",  SecurityController.getPerson().id)
                    .select("version_object")
                    .select("main_instance_history")
                    .select("id")
                    .findList();


            List<Swagger_Mobile_M_Project_Snapshot> result = new ArrayList<>();
            for(Homer_Instance_Record instnace : list){

                Swagger_Mobile_M_Project_Snapshot o = new Swagger_Mobile_M_Project_Snapshot();
                o.b_program_name = instnace.main_instance_history.b_program_name();
                o.b_program_name = instnace.main_instance_history.b_program_description();

                o.instance_id = instnace.id;
                o.snapshots = instnace.m_project_snapshop();
                result.add(o);
            }

            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get url + m_program code for Terminal",
            tags = {"APP-Api"},
            notes = "",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Mobile_Connection_Request",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful created",      response = Swagger_M_Program_Version.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 477, message = "Instance is offline",     response = Result_BadRequest.class),
            @ApiResponse(code = 478, message = "External Server Error",   response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_conection_url(){
        try{

            final Form<Swagger_Mobile_Connection_Request> form = Form.form(Swagger_Mobile_Connection_Request.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Mobile_Connection_Request help = form.get();

            Homer_Instance_Record record = Homer_Instance_Record.find.byId(help.instance_id);
            if(record == null) return GlobalResult.notFoundObject("Instance not found");

            if(!record.actual_running_instance.instance_online()){
                return GlobalResult.result_external_server_is_offline("Instance is offline");
            }

            Version_Object version_object = Version_Object.find.where().eq("id",help.version_object_id).eq("m_program.id",help.m_program_id).findUnique();
            if(version_object == null) return GlobalResult.notFoundObject("M_program not found");

            Swagger_Mobile_Connection_Summary summary = new Swagger_Mobile_Connection_Summary();
            summary.url = "ws://" + record.actual_running_instance.cloud_homer_server.server_url + record.actual_running_instance.cloud_homer_server.grid_port + "/" + record.websocket_grid_token + "/";
            summary.m_program = M_Program.get_m_code(version_object);

            return GlobalResult.created(Json.toJson(summary));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "check Terminal terminal_id",
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
            @ApiResponse(code = 200, message = "Valid Identification",      response = Grid_Terminal.class),
            @ApiResponse(code = 400, message = "Invalid Identification",    response = Result_BadRequest.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result check_identifiactor(String terminal_id){
        try{

            final Form<Swagger_Grid_Terminal_Identf> form = Form.form(Swagger_Grid_Terminal_Identf.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Grid_Terminal_Identf help = form.get();


            Grid_Terminal terminal = Grid_Terminal.find.byId(terminal_id);
            if(terminal == null){

                terminal = new Grid_Terminal();
                terminal.set_terminal_id();
                terminal.device_name = help.device_name;
                terminal.device_type = help.device_type;
                terminal.date_of_create = new Date();
                terminal.save();

                return GlobalResult.created(Json.toJson(terminal));

            }else {

                terminal.ws_permission = true;
                terminal.m_program_access = true;
                terminal.up_to_date = true;
                terminal.date_of_last_update = new Date();
                terminal.update();
                return GlobalResult.result_ok(Json.toJson(terminal));
            }

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Terminal terminal_id",
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
            @ApiResponse(code = 201, message = "Successful created",      response = Grid_Terminal.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    //@Security.Authenticated(Secured_API.class) - Není záměrně!!!! - Ověřuje se v read permision program může být public!
    public Result get_identificator(){
        try{

            final Form<Swagger_Grid_Terminal_Identf> form = Form.form(Swagger_Grid_Terminal_Identf.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Grid_Terminal_Identf help = form.get();

            Grid_Terminal terminal = new Grid_Terminal();
            terminal.device_name = help.device_name;
            terminal.device_type = help.device_type;
            terminal.date_of_create = new Date();

            if( Http.Context.current().request().headers().get("User-Agent")[0] != null) terminal.user_agent =  Http.Context.current().request().headers().get("User-Agent")[0];
            else  terminal.user_agent = "Unknown browser";

            terminal.set_terminal_id();


            if(SecurityController.getPerson() !=  null) {
                terminal.person = SecurityController.getPerson();
            }

            terminal.save();
            return GlobalResult.created(Json.toJson(terminal));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }
}
