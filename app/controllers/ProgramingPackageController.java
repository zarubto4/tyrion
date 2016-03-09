package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.*;
import models.blocko.BlockoBlock;
import models.blocko.BlockoBlockVersion;
import models.blocko.TypeOfBlock;
import models.compiler.Board;
import models.compiler.Version_Object;
import models.persons.Person;
import models.project.b_program.B_Program;
import models.project.b_program.B_Program_Cloud;
import models.project.b_program.B_Program_Homer;
import models.project.c_program.C_Program;
import models.project.global.Homer;
import models.project.global.Project;
import models.project.m_program.M_Project;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.UtilTools;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_ok;
import utilities.swagger.documentationClass.*;

import javax.websocket.server.PathParam;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured.class)
public class ProgramingPackageController extends Controller {

    @ApiOperation(value = "create new Project",
            tags = {"Project"},
            notes = "create new Project",
            produces = "application/json",
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Project_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created", response =  Project.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result postNewProject() {
        try{

            final Form<Swagger_Project_New> form = Form.form(Swagger_Project_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Project_New help = form.get();

            Project project  = new Project();

            project.project_name = help.project_name;
            project.project_description = help.project_description;

            project.ownersOfProject.add( SecurityController.getPerson() );
            project.save();

            return GlobalResult.okResult( Json.toJson(project) );


        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Project by logged Person",
            tags = {"Project"},
            notes = "get all Projects by logged Person",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Project.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result getProjectsByUserAccount(){
        try {

            List<Project> projects = SecurityController.getPerson().owningProjects;

            return GlobalResult.okResult(Json.toJson( projects ));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getProjectsByUserAccount ERROR");
            return GlobalResult.internalServerError();
        }

    }

    @ApiOperation(value = "get Project",
            tags = {"Project"},
            notes = "get Projects by project_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Project.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result getProject(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {

            Project project = Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject();

            if (    Project.find.where().eq("ownersOfProject.id", SecurityController.getPerson().id).eq("id", project_id).findUnique() == null ) return GlobalResult.forbidden_Global();

            return GlobalResult.okResult(Json.toJson(project));

         } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getProjectsByUserAccount ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "delete Project",
            tags = {"Project"},
            notes = "delete Projects by project_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result deleteProject(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {

            Project project = Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject();

            if (!project.ownersOfProject.contains( SecurityController.getPerson() ) ) return GlobalResult.forbidden_Global();

            project.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - deleteProject ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Homers from Project",
            tags = {"Project", "Homer"},
            notes = "get List of Homers from Projects by project_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Homer.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result get_Project_homers(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try{

            Project project  = Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(project.homerList));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getProgramhomerList ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get B_Programs from Project",
            tags = {"Project","B_Program"},
            notes = "get List of B_Programs from Projects by project_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  B_Program.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result get_Project_b_Programs(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {

            Project project = Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson( project.b_programs));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get C_Programs from Project",
            tags = {"Project","C_Program"},
            notes = "get List of C_Programs from Projects by project_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  C_Program.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result get_Project_c_Programs(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {

            Project project = Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson( project.c_programs));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get M_Projects from Project",
            tags = {"Project","M_Program"},
            notes = "get List of M_Projects from Projects by project_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  M_Project.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result get_Project_m_Projects(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {

            Project project = Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject();


            return GlobalResult.okResult(Json.toJson( project.m_projects));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "edit Project",
            tags = {"Project"},
            notes = "edit ne Project",
            produces = "application/json",
            protocols = "https",
            response =  Project.class,
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Project_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Project.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result updateProject(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {
            Swagger_Project_New help = Json.fromJson(request().body().asJson() , Swagger_Project_New.class);

            Project project = Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject();

            if (!project.ownersOfProject.contains( SecurityController.getPerson() ) ) return GlobalResult.forbidden_Global();

            project.project_name = help.project_name;
            project.project_description = help.project_description;
            project.update();

            return GlobalResult.okResult(Json.toJson(project));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "project_name - String", "project_description - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - updateProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all Boards from Project",
            tags = {"Project", "Board"},
            notes = "get all Boards (IoT) from Projects by project_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Board.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result getProjectsBoard(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {

            Project project = Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(project.boards));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Board ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "share Project with Users",
            tags = {"Project", "Board"},
            notes = "share Project with all users in list: List<person_id>",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Project.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result shareProjectWithUsers(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {

            Swagger_ShareProject_Person help = Json.fromJson(request().body().asJson(), Swagger_ShareProject_Person.class);

            Project project = Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject();


            for (Person person : help.get_person()) {
                if (!person.owningProjects.contains(project)) {
                    project.ownersOfProject.add(person);
                    person.owningProjects.add(project);
                    person.update();
                }
            }

            project.update();

            return GlobalResult.okResult(Json.toJson(project));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "persons[id]");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - shareProjectWithUsers ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "unshare Project with Persons",
            tags = {"Project", "Board"},
            notes = "unshare Project with all users in list: List<person_id>",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Project.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result unshareProjectWithUsers(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {

            Swagger_ShareProject_Person help = Json.fromJson(request().body().asJson(), Swagger_ShareProject_Person.class);    JsonNode json = request().body().asJson();

            Project project = Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject();

            for (Person person : help.get_person()) {
                if (person.owningProjects.contains(project)) {
                    project.ownersOfProject.remove(person);
                    person.owningProjects.remove(project);
                    person.update();
                }
            }

            project.update();

            return GlobalResult.okResult(Json.toJson(project));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "persons[is]");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - unshareProjectWithUsers ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all Persons from Project",
            tags = {"Project", "Board"},
            notes = "unshare Project with all users in list: List<person_id>",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Person.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Project_Owners(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {

            Project project = Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(project.ownersOfProject));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - shareProjectWithUsers ERROR");
            return GlobalResult.internalServerError();
        }
    }


///###################################################################################################################*/

    @ApiOperation(value = "create new Homer",
            tags = {"Homer"},
            notes = "create new Homer",
            produces = "application/json",
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Homer_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created", response =  Homer.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result newHomer(){
        try{
            final Form<Swagger_Homer_New> form = Form.form(Swagger_Homer_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Homer_New help = form.get();


            if ( Homer.find.where().eq("homer_id", help.homer_id).findUnique() != null ) return GlobalResult.badRequest("Homer with this id exist");

            Homer homer = new Homer();
            homer.homer_id = help.homer_id;
            homer.type_of_device = help.type_of_device;

            homer.save();

            return GlobalResult.okResult(Json.toJson(homer));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - newHomer ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "remove Homer",
            tags = {"Homer"},
            notes = "remove Homer",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result removeHomer(@ApiParam(value = "homer_id String path",   required = true) @PathParam("homer_id") String homer_id){
        try{

           Homer homer = Homer.find.byId(homer_id);
           if(homer == null) return GlobalResult.notFoundObject();

           homer.delete();

           return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeHomer ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "remove Homer",
            tags = {"Homer"},
            notes = "remove Homer",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result getHomer(@ApiParam(value = "homer_id String path",   required = true) @PathParam("homer_id")String homer_id){
        try {
            Homer homer = Homer.find.byId(homer_id);
            if (homer == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult( Json.toJson(homer) );

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeHomer ERROR");
            return GlobalResult.internalServerError();
        }
    }

    //TODO http://youtrack.byzance.cz/youtrack/issue/TYRION-142
    public  Result get_Homers_by_Filter(){
        try {
            List<Homer> homers = Homer.find.all();

            System.out.println("Filter není dodělaný...........");

            // TODO dodělat filter

            return GlobalResult.okResult(Json.toJson(homers));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getAllHomers ERROR");
            return GlobalResult.internalServerError();
        }
    }

// ###################################################################################################################*/

    @ApiOperation(value = "connect Homer with Project",
            tags = {"Homer", "Project"},
            notes = "remove Homer",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Project.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result connectHomerWithProject(@ApiParam(value = "project_id String path",   required = true) @PathParam("project_id") String project_id, @ApiParam(value = "homer_id String path",   required = true) @PathParam("homer_id") String homer_id){
        try{

            Project project = Project.find.byId(project_id);
            Homer homer = Homer.find.byId(homer_id);

            if(project == null)  return GlobalResult.notFoundObject();
            if(homer == null)  return GlobalResult.notFoundObject();

            homer.project = project;
            homer.update();

            return GlobalResult.okResult(Json.toJson(project));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - connectHomerWithProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "connect Homer with Project",
            tags = {"Homer", "Project"},
            notes = "remove Homer",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Project.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result disconnectHomerWithProject(@ApiParam(value = "project_id String path",   required = true) @PathParam("project_id") String project_id, @ApiParam(value = "homer_id String path",   required = true) @PathParam("homer_id") String homer_id){
        try{

            Project project = Project.find.byId(project_id);
            Homer homer = Homer.find.byId(homer_id);

            if(project == null)  return GlobalResult.notFoundObject();
            if(homer == null)  return GlobalResult.notFoundObject();


            if( project.homerList.contains(homer)) homer.project = null;
            homer.update();
            project.homerList.remove(homer);

            return GlobalResult.okResult(Json.toJson(project));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - unConnectHomerWithProject ERROR");
            return GlobalResult.internalServerError();
        }
    }

/// ###################################################################################################################*/

    @ApiOperation(value = "create new B_Program",
            tags = {"B_Program"},
            notes = "create new B_Program",
            produces = "application/json",
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_B_Program_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created", response =  B_Program.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result new_b_Program(String project_id){
        try{

            final Form<Swagger_B_Program_New> form = Form.form(Swagger_B_Program_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_B_Program_New help = form.get();

            Project project = Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject();

            // Tvorba programu
            B_Program b_program             = new B_Program();
            b_program.azurePackageLink      = "personal-program";
            b_program.dateOfCreate          = new Date();
            b_program.program_description   = help.program_description;
            b_program.name                  = help.name;
            b_program.project               = project;
            b_program.setUniqueAzureStorageLink();

            b_program.save();

            return GlobalResult.okResult(Json.toJson(b_program));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewBProgram ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get B Program",
            tags = {"B_Program"},
            notes = "get B_Program object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  B_Program.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result get_b_Program(@ApiParam(value = "b_program_id String path", required = true) @PathParam("b_program_id") String b_program_id){
        try{

            B_Program program = B_Program.find.byId(b_program_id);
            if (program == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(program));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }



    @ApiOperation(value = "get B Program version",
            tags = {"B_Program"},
            notes = "get B_Program version object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  B_Program.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result get_b_Program_verison(@ApiParam(value = "version_id String path", required = true) @PathParam("version_id") String version_id){
        try{

            Version_Object program = Version_Object.find.byId(version_id);
            if (program == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(program));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }


    @ApiOperation(value = "edit B_Program",
            tags = {"B_Program"},
            notes = "edit basic information in B_Program object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_B_Program_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  B_Program.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result edit_b_Program(@ApiParam(value = "b_program_id String path", required = true) @PathParam("b_program_id") String b_program_id){
        try{
            Swagger_B_Program_New help = Json.fromJson(request().body().asJson(), Swagger_B_Program_New.class);


            B_Program b_program  = B_Program.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject();

            b_program.program_description = help.program_description;
            b_program.name                  = help.name;

            b_program.update();
            return GlobalResult.okResult(Json.toJson(b_program));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult("Some Json Value missing");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - editProgram ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "create new Version of B Program",
            tags = {"B_Program"},
            notes = "edit basic infromation in B_Program object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_B_Program_Version_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Version_Object.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result update_b_program(@ApiParam(value = "b_program_id String path", required = true) @PathParam("b_program_id") String b_program_id){
        try{
            Swagger_B_Program_Version_New help = Json.fromJson( request().body().asJson(), Swagger_B_Program_Version_New.class);

            // Program který budu ukládat do data Storage v Azure
            String file_content =  help.program;

            // Ověřím program
            B_Program b_program = B_Program.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject();

            // První nová Verze
            Version_Object versionObjectObject          = new Version_Object();
            versionObjectObject.version_name            = help.version_name;
            versionObjectObject.version_description     = help.version_description;

            if(b_program.versionObjects.isEmpty() ) versionObjectObject.azureLinkVersion = 1;
            else versionObjectObject.azureLinkVersion    = ++b_program.versionObjects.get(0).azureLinkVersion; // Zvednu verzi o jednu

            versionObjectObject.date_of_create      = new Date();
            versionObjectObject.b_program           = b_program;
            versionObjectObject.save();

            b_program.versionObjects.add(versionObjectObject);
            b_program.update();

            // Nahraje do Azure a připojí do verze soubor (lze dělat i cyklem - ale název souboru musí být vždy jiný)
            UtilTools.uploadAzure_Version("b-program", file_content, "b-program-file", b_program.azureStorageLink, b_program.azurePackageLink, versionObjectObject);

            return GlobalResult.okResult(Json.toJson(b_program));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "program_name - String", "program_description - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - editProgram ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "remove B Program",
            tags = {"B_Program"},
            notes = "remove B_Program object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result remove_b_Program(@ApiParam(value = "b_program_id String path", required = true) @PathParam("b_program_id") String b_program_id){
        try{

            B_Program program  = B_Program.find.byId(b_program_id);
            if (program == null) return GlobalResult.notFoundObject();


            program.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    //TODO SWAGGER  a taky celá logika nahrávání do homera
    public  Result uploadProgramToHomer_Immediately(@ApiParam(value = "b_program_id String path", required = true) @PathParam("b_program_id") String b_program_id,
                                                    @ApiParam(value = "version_id String path", required = true) @PathParam("version_id") String version_id,
                                                    @ApiParam(value = "homer_id String path", required = true) @PathParam("homer_id") String homer_id){
        try {

            // B program, který chci nahrát do Cloudu na Blocko server
            B_Program b_program = B_Program.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject();

            // Verze B programu kterou budu nahrávat do cloudu
            Version_Object version_object = Version_Object.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject();

            // Homer na který budu nahrávatb_program_cloud
            Homer homer = Homer.find.byId(homer_id);
            if (homer == null)  return GlobalResult.notFoundObject();

            //*********************//

            System.out.println("Homer je online?");
            if(! WebSocketController_Incoming.homer_is_online(homer_id)) return GlobalResult.badResult("Homer není online");


            B_Program_Homer old_one =  B_Program_Homer.find.where().eq("homer.homer_id", homer.homer_id).findUnique();
            // Na homerovi musím zabít a smazat předchozí program!!!
            if( old_one  != null )  {
                System.out.println("Homer měl předchozí program a proto ho mažu");
                WebSocketController_Incoming.homer_KillInstance( homer.homer_id);
                old_one.delete();
             }

            System.out.println("Vytvářím nový program");
            B_Program_Homer program_homer = new B_Program_Homer();
            program_homer.homer = homer;

            program_homer.running_from = new Date();
            program_homer.version_object = version_object;
            program_homer.save();


            System.out.println("teď nahraju program");
            WebSocketController_Incoming.homer_UploadInstance(homer.homer_id,  version_object.files.get(0).get_fileRecord_from_Azure_inString()  );


            version_object.b_program_homer = program_homer;
            version_object.update();


            return GlobalResult.okResult( Json.toJson("Program was upload To Homer successfully and started"));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - uploadProgramToHomer_Immediately ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "upload B Program to cloud",
            tags = {"B_Program"},
            notes = "upload version of B Program to cloud. Its possible have only one version from B program in cloud. If you uploud new one - old one will be replaced",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result upload_b_Program_ToCloud(@ApiParam(value = "b_program_id String path", required = true) @PathParam("b_program_id") String b_program_id, @ApiParam(value = "version_id String path", required = true) @PathParam("version_id") String version_id){
        try {


            // B program, který chci nahrát do Cloudu na Blocko server
            B_Program b_program = B_Program.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject();

            // Verze B programu kterou budu nahrávat do cloudu
            Version_Object version_object = Version_Object.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject();



            // Pokud už nějaká instance běžela, tak jí zabiju a z databáze odstraním vazbu na běžící instanci b programu
            if( version_object.b_program_cloud != null ) {

               WebSocketController_OutComing.blockoServerKillInstance(version_object.b_program_cloud.blocko_server_name,  version_object.b_program_cloud.blocko_instance_name);

               B_Program_Cloud b_program_cloud = version_object.b_program_cloud;
               b_program_cloud.delete();
            }

            // Vytvářím nový záznam v databázi pro běžící instanci b programu na blocko serveru
            B_Program_Cloud program_cloud       = new B_Program_Cloud();
            program_cloud.running_from          = new Date();
            program_cloud.version_object        = version_object;
            program_cloud.blocko_server_name    = Configuration.root().getString("Servers.blocko.server1.name");
            program_cloud.setUnique_blocko_instance_name();

            // if(WebSocketController_OutComing.servers.containsKey( Configuration.root().getString("Servers.blocko.server1.name")) && WebSocketController_OutComing.servers.get(Configuration.root().getString("Servers.blocko.server1.name") ).session.isOpen()) {

            // Vytvářím instanci na serveru
            WebSocketController_OutComing.blockoServerCreateInstance( program_cloud.blocko_server_name,  program_cloud.blocko_instance_name);

            // Nahrávám do instance program
            WebSocketController_OutComing.blockoServerUploadProgram(  program_cloud.blocko_server_name,  program_cloud.blocko_instance_name, program_cloud.version_object.files.get(0).get_fileRecord_from_Azure_inString());

            // Ukládám po úspěšné nastartvoání programu v cloudu jeho databázový ekvivalent
            program_cloud.save();

            return GlobalResult.okResult();
        } catch (NullPointerException a) {
            // TODO dopřeložit a nějak definovat????
            return GlobalResult.nullPointerResult("Server není nastartován");
         } catch (TimeoutException a) {
            return GlobalResult.nullPointerResult("Nepodařilo se včas nahrát na server");
         } catch (InterruptedException a) {
            return GlobalResult.nullPointerResult("Vlákno nahrávání bylo přerušeno ");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    //TODO
    public Result listOfUploadedHomers(String id) {
        //Na id B_Program vezmu všechny Houmry na kterých je program nahrán
        return GlobalResult.ok("Nutné dodělat - listOfUploadedHomers");
    }

    //TODO
    public Result listOfHomersWaitingForUpload(String id){
        //Na id B_Program vezmu všechny Houmry na které jsem program ještě nenahrál
        return GlobalResult.ok("Nutné dodělat - listOfHomersWaitingForUpload");
    }

///###################################################################################################################*/

    @ApiOperation(value = "create new Type of Block",
            tags = {"Type of Block"},
            notes = "creating group for BlockoBlocks -> Type of block",
            produces = "application/json",
            protocols = "https",
            response =  TypeOfBlock.class,
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_BlockoBlock_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created", response =  TypeOfBlock.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result newTypeOfBlock(){
        try{
            final Form<Swagger_TypeOfBlock_New> form = Form.form(Swagger_TypeOfBlock_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBlock_New help = form.get();


            TypeOfBlock typeOfBlock = new TypeOfBlock();
            typeOfBlock.generalDescription  = help.general_description;
            typeOfBlock.name                = help.name;


            if(help.project_id != null){

                Project project = Project.find.byId(help.project_id);
                if(project == null) return GlobalResult.notFoundObject();

                typeOfBlock.project = project;

            }

            typeOfBlock.save();

            return GlobalResult.created( Json.toJson(typeOfBlock));
        } catch (Exception e) {
            Logger.error("Error", e);
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "edit Type of Block",
            tags = {"Type of Block"},
            notes = "edit Type of block object",
            produces = "application/json",
            protocols = "https",
            response =  TypeOfBlock.class,
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfBlock_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  TypeOfBlock.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result editTypeOfBlock(@ApiParam(value = "type_of_block_id String path",   required = true) @PathParam("type_of_block_id") String type_of_block_id){
        try{

            final Form<Swagger_TypeOfBlock_New> form = Form.form(Swagger_TypeOfBlock_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBlock_New help = form.get();

            TypeOfBlock typeOfBlock = TypeOfBlock.find.byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.notFoundObject();

            typeOfBlock.generalDescription  = help.general_description;
            typeOfBlock.name                = help.name;

            if(help.project_id != null){

                Project project = Project.find.byId(help.project_id);
                if(project == null) return GlobalResult.notFoundObject();

                typeOfBlock.project = project;

            }

            typeOfBlock.update();
            return GlobalResult.update( Json.toJson(typeOfBlock));
        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "name - String", "general_description - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "delete Type of Block",
            tags = {"Type of Block"},
            notes = "delete group for BlockoBlocks -> Type of block",
            produces = "application/json",
            protocols = "https",
            response =  Result_ok.class,
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteTypeOfBlock(@ApiParam(value = "type_of_block_id String path",   required = true) @PathParam("type_of_block_id") String type_of_block_id){
        try{

            TypeOfBlock typeOfBlock = TypeOfBlock.find.byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.notFoundObject();

            typeOfBlock.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all Type of Block from Project",
            tags = {"Type of Block", "Project"},
            notes = "get all Type of Block from project",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                      @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  TypeOfBlock.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_TypeOfBlock_by_Project(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try{

            Project project = Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(project.type_of_blocks));

        } catch (Exception e) {
            Logger.error("Error", e);
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all Type of Block list",
            tags = {"Type of Block"},
            notes = "delete group for BlockoBlocks -> Type of block",
            produces = "application/json",
            protocols = "https",
            response =  Result_ok.class,
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  TypeOfBlock.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result getAllTypeOfBlocks(){
        try {
            return GlobalResult.okResult(Json.toJson(TypeOfBlock.find.all()));
        } catch (Exception e) {
            Logger.error("Error", e);
            return GlobalResult.internalServerError();
        }
    }

///###################################################################################################################*/


    @ApiOperation(value = "create new Block",
            tags = {"Blocko-Block"},
            notes = "creating new independent Block object for Blocko tools",
            produces = "application/json",
            protocols = "https",
            response =  BlockoBlock.class,
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_BlockoBlock_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created", response =  BlockoBlock.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_Block(){
       try{
           final Form<Swagger_BlockoBlock_New> form = Form.form(Swagger_BlockoBlock_New.class).bindFromRequest();
           if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
           Swagger_BlockoBlock_New help = form.get();


            BlockoBlock blockoBlock = new BlockoBlock();
            blockoBlock.general_description = help.general_description;
            blockoBlock.name                = help.name;
            blockoBlock.author              = SecurityController.getPerson();


           TypeOfBlock typeOfBlock = TypeOfBlock.find.byId( help.type_of_block_id);
           if(typeOfBlock == null) return GlobalResult.notFoundObject();

           blockoBlock.type_of_block = typeOfBlock;
           blockoBlock.save();


            return GlobalResult.okResult( Json.toJson(blockoBlock) );
       } catch (Exception e) {
           Logger.error("Error", e);
           Logger.error("ProgramingPackageController - newBlock ERROR");
           return GlobalResult.internalServerError();
       }
    }

    @ApiOperation(value = "edit basic information of the BlockoBlock",
            tags = {"Blocko-Block"},
            notes = "update basic information (name, and desription) of the independent BlockoBlock",
            produces = "application/json",
            protocols = "https",
            response =  BlockoBlock.class,
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_BlockoBlock_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Ok Result", response =  BlockoBlock.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Block(@ApiParam(value = "blocko_block_id String path",   required = true) @PathParam("blocko_block_id") String blocko_block_id){
        try {

                final Form<Swagger_BlockoBlock_New> form = Form.form(Swagger_BlockoBlock_New.class).bindFromRequest();
                if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
                Swagger_BlockoBlock_New help = form.get();


                BlockoBlock blockoBlock = BlockoBlock.find.byId(blocko_block_id);
                if (blockoBlock == null) return GlobalResult.notFoundObject();

                blockoBlock.general_description = help.general_description;
                blockoBlock.name                = help.name;

                TypeOfBlock typeOfBlock = TypeOfBlock.find.byId(  help.type_of_block_id);
                if(typeOfBlock == null) return GlobalResult.notFoundObject();

                blockoBlock.type_of_block = typeOfBlock;

                blockoBlock.update();

                return GlobalResult.okResult(Json.toJson(blockoBlock));
        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "name", "version_description", "type_of_block_id");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getBlockLast ERROR");
            return GlobalResult.internalServerError();
        }

    }

    @ApiOperation(value = "get version of the BlockoBlock",
            tags = {"Blocko-Block"},
            notes = "get version (content) from independent BlockoBlock",
            produces = "application/json",
            protocols = "https",
            response =  BlockoBlockVersion.class,
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  BlockoBlockVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_BlockoBlock_Version(String blocko_version_id){
        try {
                BlockoBlockVersion blocko_version = BlockoBlockVersion.find.byId(blocko_version_id);
                if(blocko_version == null) return GlobalResult.notFoundObject();

                return GlobalResult.okResult(Json.toJson(blocko_version));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getBlockVersion ERROR");
            return GlobalResult.internalServerError();
        }

    }

    @ApiOperation(value = "get BlockoBlock",
            tags = {"Blocko-Block"},
            notes = "get independent BlockoBlock object",
            produces = "application/json",
            protocols = "https",
            response =  BlockoBlock.class,
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  BlockoBlock.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result getBlockBlock(@ApiParam(value = "blocko_block_id String path",   required = true) @PathParam("blocko_block_id") String blocko_block_id){
        try {
            BlockoBlock blockoBlock = BlockoBlock.find.byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(blockoBlock));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getBlockVersion ERROR");
            return GlobalResult.internalServerError();
        }

    }

    @ApiOperation(value = "get Block from Category",
            tags = {"Blocko-Block"},
            notes = "get list of BlockoBlocks objects",
            produces = "application/json",
            protocols = "https",
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor",   description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  BlockoBlock.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_BlockoBlocks_from_Category(String type_of_block_id){
        try {

            TypeOfBlock typeOfBlock = TypeOfBlock.find.byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(typeOfBlock.blockoBlocks));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getBlockLast ERROR");
            return GlobalResult.internalServerError();
        }


    }

    @ApiOperation(value = "delete BlockoBlock",
            tags = {"Blocko-Block"},
            notes = "delete BlockoBlock",
            produces = "application/json",
            protocols = "https",
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor",   description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result deleteBlock(@ApiParam(value = "blocko_block_id String path",   required = true) @PathParam("blocko_block_id") String blocko_block_id){
        try {

            BlockoBlock blockoBlock = BlockoBlock.find.byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.notFoundObject();
            blockoBlock.delete();

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - shareProjectWithUsers ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "delete BlockoBlock version",
            tags = {"Blocko-Block"},
            notes = "delete BlockoBlock version",
            produces = "application/json",
            protocols = "https",
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor",   description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_BlockoBlock_Version(String blocko_block_version_id){
        try {

            BlockoBlockVersion blockoContentBlock = BlockoBlockVersion.find.byId(blocko_block_version_id);
            if(blockoContentBlock == null) return GlobalResult.notFoundObject();

            blockoContentBlock.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - shareProjectWithUsers ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "create BlockoBlock version",
            tags = {"Blocko-Block"},
            notes = "new BlockoBlock version",
            produces = "application/json",
            protocols = "https",
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor",   description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_BlockoBlock_BlockoVersion_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created", response =  BlockoBlockVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_BlockoBlock_Version(@ApiParam(value = "blocko_block_id String path",   required = true) @PathParam("blocko_block_id") String blocko_block_id){
        try {

            Swagger_BlockoBlock_BlockoVersion_New help = Json.fromJson( request().body().asJson() , Swagger_BlockoBlock_BlockoVersion_New.class);


            BlockoBlock blockoBlock = BlockoBlock.find.byId(blocko_block_id);

            BlockoBlockVersion version = new BlockoBlockVersion();
            version.dateOfCreate = new Date();

            version.version_name = help.version_name;
            version.version_description = help.version_description;
            version.design_json = help.design_json;
            version.logic_json = help.logic_json;
            version.blocko_block = blockoBlock;
            version.save();

            //blocko_block.blocko_versions.add(version);
            return GlobalResult.okResult(Json.toJson(blockoBlock));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "version_name - String", "version_description - TEXT", "design_json - TEXT", "logic_json - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - shareProjectWithUsers ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "edit BlockoBlock version",
            tags = {"Blocko-Block"},
            notes = "You can adit only basic information of version. If you wnat update code, " +
                    "you have to create new version!",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor",   description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_BlockoBlock_BlockoVersion_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  BlockoBlockVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_BlockBlock_version(String blocko_block_version_id){
        try {
            Swagger_BlockoBlock_BlockoVersion_Edit help = Json.fromJson( request().body().asJson() , Swagger_BlockoBlock_BlockoVersion_Edit.class);

            BlockoBlockVersion version = BlockoBlockVersion.find.byId(blocko_block_version_id);

            version.version_name = help.version_name;
            version.version_description = help.version_description;

            version.update();
            return GlobalResult.okResult(Json.toJson(version));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "version_name", "version_description");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - shareProjectWithUsers ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "edit BlockoBlock version",
            tags = {"Blocko-Block"},
            notes = "You can adit only basic infromation of version. If you wnat update code, " +
                    "you have to create new version!",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor",   description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_BlockoBlock_BlockoVersion_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  BlockoBlockVersion.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_BlockoBlock_all_versions(String blocko_block_id){
        try {
            BlockoBlock blockoBlock = BlockoBlock.find.byId(blocko_block_id);
            if (blockoBlock == null) return GlobalResult.notFoundObject();
            return GlobalResult.ok(Json.toJson(blockoBlock.blocko_versions));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - allPrevVersions ERROR");
            return GlobalResult.internalServerError();
        }
    }

///###################################################################################################################*/

}
