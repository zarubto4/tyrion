package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.*;
import models.grid.Screen_Size_Type;
import models.persons.Person;
import models.project.global.Project;
import models.project.m_program.M_Program;
import models.project.m_program.M_Project;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;
import utilities.response.response_objects.JsonValueMissing;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_ok;
import utilities.swagger.documentationClass.Swagger_M_Program_ByToken;
import utilities.swagger.documentationClass.Swagger_TypeOfBoard_Combination;

import javax.websocket.server.PathParam;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Api(value = "GridController - nezdokumentované",
        description = "Compilation operation (Role, Permission and permissions operations",
        authorizations = { @Authorization(value="logged_in", scopes = {} )}
)

public class GridController extends play.mvc.Controller {

    @ApiOperation(value = "Create new M_Project",
            tags = {"M_Program"},
            notes = "M_Project is box for M_Programs -> presupposition is that you need more control terminal for your IoT project. " +
                    "Different screens for family members, for employes etc.. But of course - you can used that for only one M_program",
            produces = "application/json",
            response =  M_Project.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Person need this value of permission")}
                    )
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
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
   // @Dynamic("project.owner")
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public Result new_M_Project(String project_id) {
        try{
            JsonNode json = request().body().asJson();

            Project project = Project.find.byId( project_id );
            if(project == null) return GlobalResult.notFoundObject();

            M_Project m_project = new M_Project();
            m_project.program_description = json.get("program_description").asText();
            m_project.program_name = json.get("program_name").asText();
            m_project.date_of_create = new Date();
            m_project.project = project;

            m_project.save();

            return GlobalResult.created( Json.toJson(m_project));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "program_name - String", "program_description - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get M_Project",
            tags = {"M_Program"},
            notes = "get M_Project by query = m_project_id",
            produces = "application/json",
            response =  M_Project.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Person need this value of permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = M_Project.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result get_M_Project(@ApiParam(value = "m_project_id String query", required = true) @PathParam("m_project_id") String m_project_id){
        try {
            M_Project m_project = M_Project.find.byId(m_project_id);
            if (m_project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(m_project));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "edit M_Project",
            tags = {"M_Program"},
            notes = "edit basic information in M_Project by query = m_project_id",
            produces = "application/json",
            response =  M_Project.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Person need this value of permission")}
                    )
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
            @ApiResponse(code = 200, message = "Ok Result", response = M_Project.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public Result edit_M_Project(@ApiParam(value = "m_project_id String query", required = true) @PathParam("m_project_id") String m_project_id){
        try{
            JsonNode json = request().body().asJson();

            M_Project m_project = M_Project.find.byId(m_project_id);
            if(m_project == null) return GlobalResult.notFoundObject();

            m_project.program_description = json.get("program_description").asText();
            m_project.program_name = json.get("program_name").asText();

            m_project.update();
            return GlobalResult.update( Json.toJson(m_project));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "project_id - String", "program_name - String", "program_description - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "remove M_Project",
            tags = {"M_Program"},
            notes = "remove M_Project by query = m_project_id",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Person need this value of permission")}
                    )
            }
    )
     @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result remove_M_Project(@ApiParam(value = "m_project_id String query", required = true) @PathParam("m_project_id") String m_project_id){
        try{
            M_Project m_project = M_Project.find.byId(m_project_id);
            if(m_project == null) return GlobalResult.notFoundObject();

            m_project.delete();

            return GlobalResult.okResult();

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "projectName - String", "projectDescription - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all M_Project by General Project",
            tags = {"M_Program", "Project"},
            notes = "get List<M_Project> by query = project_id",
            produces = "application/json",
            response =  M_Project.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Person need this value of permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = M_Project.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result get_M_Projects_from_GlobalProject(String project_id){
        try {

            List<M_Project> m_projects = M_Project.find.where().eq("project.projectId", project_id).findList();
            return GlobalResult.okResult(Json.toJson(m_projects));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all M_Project by Logged Person",
            tags = {"M_Program", "APP-Api"},
            notes = "get List<M_Project> by logged person ->that's required valid token in html head",
            produces = "application/json",
            response =  M_Project.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Person need this value of permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = M_Project.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result get_M_Projects_ByLoggedPerson(){
        try{

            Person person = SecurityController.getPerson();
            List<M_Project> m_projects = M_Project.find.where().eq("project.ownersOfProject.id", person.id).findList();

            return GlobalResult.okResult(Json.toJson(m_projects));

        }catch (Exception e){
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }

    }

//######################################################################################################################

    @ApiOperation(value = "Create new M_Program",
            tags = {"M_Program"},
            notes = "creating new M_Program",
            produces = "application/json",
            response =  M_Program.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Person need this value of permission")}
                    )
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
            @ApiResponse(code = 201, message = "Successful created",      response = M_Program.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public Result new_M_Program() {
        try {
            JsonNode json = request().body().asJson();

            M_Project m_project = M_Project.find.byId( json.get("m_program_id").asText() );
            if(m_project == null) return GlobalResult.notFoundObject();

            Screen_Size_Type screen_size_type = Screen_Size_Type.find.byId(json.get("screen_type_id").asText());
            if(screen_size_type == null) return GlobalResult.notFoundObject();

            M_Program m_program_ = new M_Program();
            m_program_.date_of_create      = new Date();
            m_program_.program_description = json.get("program_description").asText();
            m_program_.program_name        = json.get("program_name").asText();
            m_program_.m_project           = m_project;
            m_program_.programInString     = json.get("m_code").toString();
            m_program_.screen_size_type    = screen_size_type;
            m_program_.height_lock         = json.get("height_lock").asBoolean();
            m_program_.width_lock          = json.get("width_lock").asBoolean();

            while(true){ // I need Unique Value
                m_program_.qr_token  = UUID.randomUUID().toString();
                if (M_Program.find.where().eq("qr_token", m_program_.qr_token).findUnique() == null) break;
            }

            m_program_.save();

            return GlobalResult.created(Json.toJson(m_program_));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "m_program_id - String", "screen_type_id - String",
                     "program_description - String", "program_name - String", "m_code - String",
                    "height_lock - boolean", "width_lock - boolean");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }

    }

    @ApiOperation(value = "get M_Program by generated token",
            tags = {"M_Program", "APP-Api"},
            notes = "get M_Program by token - it will return only native M_Program code",
            produces = "application/json",
            response =  Swagger_M_Program_ByToken.class,
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Swagger_M_Program_ByToken.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_M_Program_byQR_Token_forMobile(@ApiParam(value = "qr_token String query", required = true) @PathParam("qr_token") String qr_token){
       try{

           M_Program m_program = M_Program.find.where().eq("qr_token", qr_token).findUnique();
           if(m_program == null) return GlobalResult.notFoundObject();

           Swagger_M_Program_ByToken program = new Swagger_M_Program_ByToken();
           program.program = m_program.programInString;

           return GlobalResult.okResult(Json.toJson(program));

       }catch (Exception e){
           Logger.error("Error", e);
           Logger.error("ProgramingPackageController - postNewProject ERROR");
           return GlobalResult.internalServerError();
       }
    }

    @ApiOperation(value = "get all M_Program b yLogged Person",
            tags = {"M_Program", "APP-Api"},
            notes = "get list of M_Programs by logged Person",
            produces = "application/json",
            response =  M_Program.class,
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = M_Program.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result get_M_Program_all_forMobile(){
        try{

            List<M_Program> m_programs = M_Program.find.where().eq("m_project.project.ownersOfProject.id", SecurityController.getPerson().id ).findList();

            return GlobalResult.okResult(Json.toJson(m_programs));

        }catch (Exception e){
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get M_Program",
            tags = {"M_Program"},
            notes = "get M_Program by quarry m_program_id",
            produces = "application/json",
            response =  M_Project.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Person need this value of permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = M_Project.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result get_M_Program(@ApiParam(value = "m_program_id String query", required = true) @PathParam("m_program_id") String m_program_id) {
        try {
            M_Program m_program = M_Program.find.byId(m_program_id);
            if (m_program == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(m_program));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "update M_Program",
            tags = {"M_Program"},
            notes = "update m_program - in this case we are not support versions of m_program",
            produces = "application/json",
            response =  M_Project.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Person need this value of permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = M_Project.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public Result edit_M_Program(@ApiParam(value = "m_program_id String query", required = true) @PathParam("m_program_id") String m_program_id){
        try {

            JsonNode json = request().body().asJson();

            Screen_Size_Type screen_size_type = Screen_Size_Type.find.byId(json.get("screen_type_id").asText());
            if(screen_size_type == null) return GlobalResult.notFoundObject();

            M_Program m_program_ = M_Program.find.byId(m_program_id);
            m_program_.program_description = json.get("program_description").asText();
            m_program_.program_name        = json.get("program_name").asText();
            m_program_.programInString     = json.get("m_code").asText();
            m_program_.screen_size_type    = screen_size_type;
            m_program_.height_lock         = json.get("height_lock").asBoolean();
            m_program_.width_lock          = json.get("width_lock").asBoolean();
            m_program_.last_update         = new Date();

            m_program_.update();

            return GlobalResult.created(Json.toJson(m_program_));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "screen_type_id - String",
                    "program_description - String", "program_name - String", "m_code - String",
                    "height_lock - boolean", "width_lock - boolean");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "remove M_Program",
            tags = {"M_Program"},
            notes = "remove M_Program by quarry = m_program_id",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Person need this value of permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result remove_M_Program(@ApiParam(value = "m_program_id String query", required = true) @PathParam("m_program_id") String m_program_id){
        try {
            M_Program m_program_ = M_Program.find.byId(m_program_id);
            if (m_program_ == null) return GlobalResult.notFoundObject();

            m_program_.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all M_Program by M_Project",
            tags = {"M_Program"},
            notes = "remove M_Program by quarry = m_project_id",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Person need this value of permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result getAll_M_Program_from_M_Project(@ApiParam(name = "m_project_id", value = "project_id String query", required = true) @PathParam("m_project_id") String m_project_id){
        try {
            M_Project project = M_Project.find.byId(m_project_id);
            if (project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(project.m_program_s));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all M_Program by General Project",
            tags = {"M_Program", "Project"},
            notes = "remove M_Program by quarry = m_project_id",
            produces = "application/json",
            protocols = "https",
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Person need this value of permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = M_Program.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result get_all_M_Program_from_Project(@ApiParam(value = "project_id String query", required = true) @PathParam("project_id") String project_id){
        try {

            List<M_Program> list = M_Program.find.where().eq("m_project.project.projectId", project_id).findList();
            return GlobalResult.okResult(Json.toJson(list));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
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
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Only if you want create personal ScreenType"),
                                       @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")
                            }
                    )
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
            @ApiResponse(code = 201, message = "Successful created",       response = Screen_Size_Type.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public Result new_Screen_Size_Type(){
        try {

            // TODO Chybí ochrana před ukládáním nesmyslů jako mínusové velikosti + nezapomenout to zdokumentovat v objektu Swagger_ScreeSizeType_New

            JsonNode json = request().body().asJson();
            Screen_Size_Type screen_size_type = new Screen_Size_Type();
            screen_size_type.name = json.get("name").asText();
            screen_size_type.height = json.get("height").asInt();
            screen_size_type.width = json.get("width").asInt();
            screen_size_type.height_lock = json.get("height_lock").asBoolean();
            screen_size_type.width_lock = json.get("width_lock").asBoolean();
            screen_size_type.touch_screen = json.get("touch_screen").asBoolean();

            if( json.has("project_id")) {
                Project project = Project.find.byId(json.get("project_id").asText());
                if (project == null) return GlobalResult.notFoundObject();

                screen_size_type.project = project;
            }

            screen_size_type.save();

            return GlobalResult.created(Json.toJson(screen_size_type));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "name - String", "height - Integer", "width - Integer", "height_lock - boolean", "width_lock - boolean", "touch_screen boolean");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get ScreenType",
            tags = {"Screen_Size_Type"},
            notes = "get ScreenType. If you want get private ScreenType you have to owned that. Public are without permissions",
            produces = "application/json",
            protocols = "https",
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Only if you want get personal ScreenType you have to be project owner")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Ok Result",               response = Screen_Size_Type.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result get_Screen_Size_Type(@ApiParam(value = "screen_size_type_id String query", required = true) @PathParam("screen_size_type_id") String screen_size_type_id){
        try {
            Screen_Size_Type screen_size_type = Screen_Size_Type.find.byId(screen_size_type_id);
            if (screen_size_type == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(screen_size_type));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all ScreenType",
            tags = {"Screen_Size_Type"},
            notes = "get all ScreenType. Private_types areon every Persons projects",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Ok Result", response = Swagger_TypeOfBoard_Combination.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result get_Screen_Size_Type_Combination(){
        try {

            List<Screen_Size_Type> public_list = Screen_Size_Type.find.where().eq("project", null).findList();
            List<Screen_Size_Type> private_list = Screen_Size_Type.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).findList();

            ObjectNode result = Json.newObject();
            result.set("private_types", Json.toJson(private_list));
            result.set("public_types", Json.toJson(public_list));

            return GlobalResult.okResult(result);
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "edit ScreenType",
            tags = {"Screen_Size_Type"},
            notes = "Edit all ScreenType information",
            produces = "application/json",
            protocols = "https",
            authorizations = {
                    @Authorization(
                        value="permission",
                        scopes = { @AuthorizationScope(scope = "project.owner", description = "Only if you want get personal ScreenType, you have to be project owner")}
            )
    }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Ok Result",               response = Screen_Size_Type.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public Result edit_Screen_Size_Type(@ApiParam(value = "screen_size_type_id String query", required = true) @PathParam("screen_size_type_id") String screen_size_type_id){
        try {
            JsonNode json = request().body().asJson();

            Screen_Size_Type screen_size_type = Screen_Size_Type.find.byId(screen_size_type_id);
            if(screen_size_type == null) return GlobalResult.notFoundObject();

            screen_size_type.name           = json.get("name").asText();
            screen_size_type.height         = json.get("height").asInt();
            screen_size_type.width          = json.get("width").asInt();
            screen_size_type.height_lock    = json.get("height_lock").asBoolean();
            screen_size_type.width_lock     = json.get("width_lock").asBoolean();
            screen_size_type.touch_screen   = json.get("touch_screen").asBoolean();


            if( json.has("project_id")) {
                Project project = Project.find.byId(json.get("project_id").asText());
                if (project == null) return GlobalResult.notFoundObject();

                screen_size_type.project = project;
            }

            screen_size_type.save();

            screen_size_type.update();
            return GlobalResult.update(Json.toJson(screen_size_type));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "name - String", "height - Integer", "width - Integer", "height_lock - boolean", "width_lock - boolean", "touch_screen boolean");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }


    @ApiOperation(value = "remove ScreenType",
            tags = {"Screen_Size_Type"},
            notes = "remove ScreenType",
            produces = "application/json",
            protocols = "https",
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Only if you want delete personal ScreenType"),
                                       @AuthorizationScope(scope = "SuperAdmin"   , description = "Or person must be SuperAdmin role")
                            }
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result remove_Screen_Size_Type(@ApiParam(value = "screen_size_type_id String query", required = true) @PathParam("screen_size_type_id") String screen_size_type_id){
        try {
            Screen_Size_Type screen_size_type = Screen_Size_Type.find.byId(screen_size_type_id);
            if(screen_size_type == null) return GlobalResult.notFoundObject();

            screen_size_type.delete();

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }


}
