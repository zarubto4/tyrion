package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.*;
import models.compiler.Version_Object;
import models.grid.Screen_Size_Type;
import models.person.Person;
import models.project.global.Project;
import models.project.m_program.Grid_Terminal;
import models.project.m_program.M_Program;
import models.project.m_program.M_Project;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;
import utilities.UtilTools;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Swagger_Screen_Size_Type_Combination;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


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
            m_project.auto_incrementing = help.auto_incrementing;
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
            if(m_project == null) return GlobalResult.notFoundObject("SecurityRole role_id not found");


            if (!m_project.delete_permission())  return GlobalResult.forbidden_Permission();
            m_project.delete();

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all M_Project by Logged Person",
            tags = {"M_Program"},
            notes = "get List<M_Project> by logged person ->that's required valid token in html head",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "M_Project.delete_permission", value = M_Project.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Project.remove_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key"      , value = "M_Project_delete" ),
                            @ExtensionProperty(name = "Dynamic Permission key"     , value = "M_Project_delete.{project_id}"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = M_Project.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result get_M_Projects_ByLoggedPerson(){
        try{

            Person person = SecurityController.getPerson();
            List<M_Project> m_projects = M_Project.find.where().eq("project.ownersOfProject.id", person.id).findList();

            return GlobalResult.result_ok(Json.toJson(m_projects));

        }catch (Exception e){
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

            Screen_Size_Type screen_size_type = Screen_Size_Type.find.byId( help.screen_type_id);
            if(screen_size_type == null) return GlobalResult.notFoundObject("Screen_Size_Type screen_type_id not found");

            M_Program m_program = new M_Program();

            m_program.date_of_create      = new Date();
            m_program.description = help.description;
            m_program.name        = help.name;

            m_program.m_project           = m_project;

            m_program.screen_size_type    = screen_size_type;
            m_program.height_lock         = help.height_lock;
            m_program.width_lock          = help.width_lock;

            m_program.set_QR_Token();

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

            Version_Object version_object = new Version_Object();
            version_object.date_of_create = new Date();
            version_object.version_description = help.version_description;
            version_object.version_name        = help.version_name;
            version_object.m_program           = main_m_program;
            version_object.save();

            main_m_program.version_objects.add(version_object);

            ObjectNode content = Json.newObject();
            content.put("m_code", help.m_code);
            content.put("virtual_input_output", help.virtual_input_output);

            UtilTools.uploadAzure_Version(content.toString(), "m_program.json" , main_m_program.get_path() ,  version_object);

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
    public Result remove_M_Program_version( @ApiParam(value = "m_program_id", required = true) String m_program_id) {
        try {

            M_Program main_m_program = M_Program.find.byId( m_program_id );
            if(main_m_program == null) return GlobalResult.notFoundObject("M_Project m_project_id not found");

            if (!main_m_program.delete_permission()) return GlobalResult.forbidden_Permission();

            return GlobalResult.result_ok( );

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get M_Program by generated token",
            tags = {"APP-Api"},
            notes = "get M_Program by token",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "M_Program.read_qr_token_permission", value = M_Program.read_qr_token_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "M_Program.read_qr_token_permission", value = "true"),
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
    public Result get_M_Program_byQR_Token_forMobile(@ApiParam(value = "qr_token String query", required = true) String qr_token){
       try{

           M_Program m_program = M_Program.find.where().eq("qr_token", qr_token).findUnique();
           if(m_program == null) return GlobalResult.notFoundObject("M_Project m_project_id not found");

           if (!m_program.read_qr_token_permission())  return GlobalResult.forbidden_Permission();
           return GlobalResult.result_ok(Json.toJson(m_program));

       }catch (Exception e){
           return Loggy.result_internalServerError(e, request());
       }
    }

    @ApiOperation(value = "get all M_Program b yLogged Person",
            tags = {"APP-Api"},
            notes = "get list of M_Programs by logged Person",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = M_Program.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result get_M_Program_all_forMobile(){
        try{

            List<M_Program> m_programs = M_Program.find.where().eq("m_project.project.ownersOfProject.id", SecurityController.getPerson().id).findList().stream().filter(M_Program::read_permission).collect(Collectors.toList());

            return GlobalResult.result_ok(Json.toJson(m_programs));

        }catch (Exception e){
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

            Screen_Size_Type screen_size_type = Screen_Size_Type.find.byId(help.screen_type_id);
            if(screen_size_type == null) return GlobalResult.notFoundObject("Screen_Size_Type screen_type_id not found");

            M_Program m_program = M_Program.find.byId(m_program_id);
            if (!m_program.edit_permission())  return GlobalResult.forbidden_Permission();

            if(m_program.m_project == null)  return GlobalResult.result_BadRequest("You cannot change program on version");


            m_program.description = help.description;
            m_program.name        = help.name;
            m_program.screen_size_type    = screen_size_type;
            m_program.height_lock         = help.height_lock;
            m_program.width_lock          = help.width_lock;

            m_program.update();

            return GlobalResult.created(Json.toJson(m_program));
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

    @ApiOperation(value = "create ScreenType",
            tags = {"Screen_Size_Type"},
            notes = "Create type of screen - its used for describe Grid dimensions for regular users - (Iphone 5, Samsung Galaxy S3 etc..). " +
                    "Its also possible create private Screen for Personal/Enterprises projects if you add to json parameter { \"project_id\" : \"{1576}\"} " +
                    "If json not contain project_id - you need Permission For that!!",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Screen_Size_Type.create_permission", value = Screen_Size_Type.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Screen_Size_Type_create" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_ScreeSizeType_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )

            }

    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Screen_Size_Type.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result new_Screen_Size_Type(){
        try {

            final Form<Swagger_ScreeSizeType_New> form = Form.form(Swagger_ScreeSizeType_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_ScreeSizeType_New help = form.get();

            Screen_Size_Type screen_size_type = new Screen_Size_Type();
            screen_size_type.name = help.name;

            screen_size_type.landscape_height = help.landscape_height;
            screen_size_type.landscape_width = help.landscape_width;
            screen_size_type.landscape_square_height = help.landscape_square_height;
            screen_size_type.landscape_square_width = help.landscape_square_width;
            screen_size_type.landscape_max_screens = help.landscape_max_screens;
            screen_size_type.landscape_min_screens = help.landscape_min_screens;

            screen_size_type.portrait_height = help.portrait_height;
            screen_size_type.portrait_width = help.portrait_width;
            screen_size_type.portrait_square_height = help.portrait_square_height;
            screen_size_type.portrait_square_width = help.portrait_square_width;
            screen_size_type.portrait_max_screens = help.portrait_max_screens;
            screen_size_type.portrait_min_screens = help.portrait_min_screens;

            screen_size_type.height_lock = help.height_lock;
            screen_size_type.width_lock = help.width_lock;
            screen_size_type.touch_screen = help.touch_screen;

            if( help.project_id != null) {
                Project project = Project.find.byId(help.project_id);
                if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

                screen_size_type.project = project;
            }

            if (!screen_size_type.create_permission())  return GlobalResult.forbidden_Permission();

            screen_size_type.save();

            return GlobalResult.created(Json.toJson(screen_size_type));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get ScreenType",
            tags = {"Screen_Size_Type"},
            notes = "get ScreenType. If you want get private ScreenType you have to owned that. Public are without person_permissions",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Screen_Size_Type.read_permission", value = Screen_Size_Type.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Screen_Size_Type.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Screen_Size_Type_create" ),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Screen_Size_Type.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result get_Screen_Size_Type(@ApiParam(value = "screen_size_type_id String query", required = true) String screen_size_type_id){
        try {
            Screen_Size_Type screen_size_type = Screen_Size_Type.find.byId(screen_size_type_id);
            if (screen_size_type == null) return GlobalResult.notFoundObject("Screen_Size_Type screen_type_id not found");

            if (!screen_size_type.read_permission())  return GlobalResult.forbidden_Permission();

            return GlobalResult.result_ok(Json.toJson(screen_size_type));
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all ScreenType",
            tags = {"Screen_Size_Type"},
            notes = "get all ScreenType. Private_types areon every Persons projects",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Screen_Size_Type.read_permission", value = Screen_Size_Type.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Screen_Size_Type.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Screen_Size_Type_read" ),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Screen_Size_Type_Combination.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result get_Screen_Size_Type_Combination(){
        try {

            List<Screen_Size_Type> public_list = new ArrayList<>();
            List<Screen_Size_Type> private_list =  new ArrayList<>();

            for(Screen_Size_Type type : Screen_Size_Type.find.where().eq("project", null).findList() )                                                  if (type.read_permission())  public_list.add(type);
            for(Screen_Size_Type type : Screen_Size_Type.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).findList() )  if (type.read_permission())  private_list.add(type);

            Swagger_Screen_Size_Type_Combination help = new Swagger_Screen_Size_Type_Combination();
            help.private_types = private_list;
            help.public_types = public_list;

            return GlobalResult.result_ok(Json.toJson(help));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit ScreenType",
            tags = {"Screen_Size_Type"},
            notes = "Edit all ScreenType information",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Screen_Size_Type.edit_permission", value = "true"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_ScreeSizeType_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )

            }

    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Screen_Size_Type.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured_API.class)
    public Result edit_Screen_Size_Type(@ApiParam(value = "screen_size_type_id String query", required = true) String screen_size_type_id){
        try {

            final Form<Swagger_ScreeSizeType_New> form = Form.form(Swagger_ScreeSizeType_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_ScreeSizeType_New help = form.get();

            Screen_Size_Type screen_size_type = Screen_Size_Type.find.byId(screen_size_type_id);
            if(screen_size_type == null) return GlobalResult.notFoundObject("Screen_Size_Type screen_type_id not found");

            if (!screen_size_type.edit_permission())  return GlobalResult.forbidden_Permission();

            screen_size_type.name = help.name;

            screen_size_type.landscape_height = help.landscape_height;
            screen_size_type.landscape_width = help.landscape_width;
            screen_size_type.landscape_square_height = help.landscape_square_height;
            screen_size_type.landscape_square_width = help.landscape_square_width;
            screen_size_type.landscape_max_screens = help.landscape_max_screens;
            screen_size_type.landscape_min_screens = help.landscape_min_screens;

            screen_size_type.portrait_height = help.portrait_height;
            screen_size_type.portrait_width = help.portrait_width;
            screen_size_type.portrait_square_height = help.portrait_square_height;
            screen_size_type.portrait_square_width = help.portrait_square_width;
            screen_size_type.portrait_max_screens = help.portrait_max_screens;
            screen_size_type.portrait_min_screens = help.portrait_min_screens;


            screen_size_type.height_lock = help.height_lock;
            screen_size_type.width_lock = help.width_lock;
            screen_size_type.touch_screen = help.touch_screen;


            if( help.project_id != null) {
                Project project = Project.find.byId(help.project_id);
                if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

                screen_size_type.project = project;
            }

            screen_size_type.update();

            return GlobalResult.result_ok(Json.toJson(screen_size_type));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove ScreenType",
            tags = {"Screen_Size_Type"},
            notes = "remove ScreenType",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Screen_Size_Type_delete_permission", value = "true"),
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
    public Result remove_Screen_Size_Type(@ApiParam(value = "screen_size_type_id String query", required = true)  String screen_size_type_id){
        try {
            Screen_Size_Type screen_size_type = Screen_Size_Type.find.byId(screen_size_type_id);
            if(screen_size_type == null) return GlobalResult.notFoundObject("Screen_Size_Type screen_type_id not found");

            if (!screen_size_type.delete_permission())  return GlobalResult.forbidden_Permission();

            screen_size_type.delete();

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

//######################################################################################################################

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


}
