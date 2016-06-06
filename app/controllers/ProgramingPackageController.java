package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.*;
import models.blocko.BlockoBlock;
import models.blocko.BlockoBlockVersion;
import models.blocko.Cloud_Blocko_Server;
import models.blocko.TypeOfBlock;
import models.compiler.Version_Object;
import models.person.Person;
import models.project.b_program.B_Program;
import models.project.b_program.B_Program_Cloud;
import models.project.b_program.B_Program_Homer;
import models.project.b_program.Homer;
import models.project.global.Project;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.UtilTools;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured;
import utilities.notification.Notification_level;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Filter_List.Swagger_Homer_List;
import utilities.webSocket.WS_BlockoServer;

import javax.websocket.server.PathParam;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured.class)
public class ProgramingPackageController extends Controller {

// GENERAL PROJECT #####################################################################################################

    @ApiOperation(value = "create new Project",
            tags = {"Project"},
            notes = "create new Project",
            produces = "application/json",
            protocols = "https",
            code = 201
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

            if (!project.create_permission())  return GlobalResult.forbidden_Permission();

            project.save();

            return GlobalResult.created( Json.toJson(project) );


        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Project by logged Person",
            tags = {"Project"},
            notes = "get all Projects by logged Person",
            produces = "application/json",
            protocols = "https",
            code = 200
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

            return GlobalResult.result_ok(Json.toJson( projects ));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "get Project",
            tags = {"Project"},
            notes = "get Projects by project_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = Project.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Project_read" ),
                            @ExtensionProperty(name = "Dynamic Permission key", value = "Project_read.{project_id}"),
                    })
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
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            if (!project.read_permission())   return GlobalResult.forbidden_Permission();

            return GlobalResult.result_ok(Json.toJson(project));

         } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Project",
            tags = {"Project"},
            notes = "delete Projects by project_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.delete_permission", value = "true")
                    })
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
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            if (!project.delete_permission())   return GlobalResult.forbidden_Permission();

            project.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Project",
            tags = {"Project"},
            notes = "edit ne Project",
            produces = "application/json",
            protocols = "https",
            response =  Project.class,
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.edit_permission", value = "true")
                    })
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
    public  Result edit_Project(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {

            final Form<Swagger_Project_New> form = Form.form(Swagger_Project_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Project_New help = form.get();

            Project project = Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            if (!project.edit_permission() )   return GlobalResult.forbidden_Permission();

            project.project_name = help.project_name;
            project.project_description = help.project_description;
            project.update();

            return GlobalResult.result_ok(Json.toJson(project));


        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "share Project with Users",
            tags = {"Project", "Board"},
            notes = "share Project with all users in list: List<person_id>",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.share_permission", value = "true")
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_ShareProject_Person",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
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
            final Form<Swagger_ShareProject_Person> form = Form.form(Swagger_ShareProject_Person.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_ShareProject_Person help = form.get();

            Project project = Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

            if (!project.share_permission() )   return GlobalResult.forbidden_Permission();

            System.out.println("Velikost pole : " + help.persons_id.size() );

            List<Person> list = Person.find.where().idIn(help.persons_id).findList();

            for (Person person : list) {
                if (!person.owningProjects.contains(project)) {
                    project.ownersOfProject.add(person);
                    person.owningProjects.add(project);
                    person.update();
                }
            }

            project.update();

            return GlobalResult.result_ok(Json.toJson(project));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "unshare Project with Persons",
            tags = {"Project", "Board"},
            notes = "unshare Project with all users in list: List<person_id>",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.unshare_permission", value = "true")
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_ShareProject_Person",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
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
            final Form<Swagger_ShareProject_Person> form = Form.form(Swagger_ShareProject_Person.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_ShareProject_Person help = form.get();

            Project project = Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

            if (!project.unshare_permission() )   return GlobalResult.forbidden_Permission();

            List<Person> list = Person.find.where().idIn(help.persons_id).findList();

            for (Person person : list) {
                if (person.owningProjects.contains(project)) {
                    project.ownersOfProject.remove(person);
                    person.owningProjects.remove(project);
                    person.update();
                }
            }

            project.update();

            return GlobalResult.result_ok(Json.toJson(project));
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


// HOMER   #############################################################################################################

    @ApiOperation(value = "create new Homer",
            tags = {"Homer"},
            notes = "create new Homer",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Homer.create_permission", value = Homer.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Homer.create_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Homer_create_permission" )
                    })
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


            if ( Homer.find.where().eq("id", help.homer_id).findUnique() != null ) return GlobalResult.result_BadRequest("Homer with this id exist");

            Homer homer = new Homer();
            homer.id = help.homer_id;
            homer.type_of_device = help.type_of_device;

            if (!homer.create_permission() )   return GlobalResult.forbidden_Permission();

            homer.save();

            return GlobalResult.result_ok(Json.toJson(homer));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove Homer",
            tags = {"Homer"},
            notes = "remove Homer",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Homer.delete_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result removeHomer(@ApiParam(value = "id String path",   required = true) @PathParam("id") String homer_id){
        try{

           Homer homer = Homer.find.byId(homer_id);
           if(homer == null) return GlobalResult.notFoundObject("Homer id not found");

            if (!homer.delete_permission() )   return GlobalResult.forbidden_Permission();

           homer.delete();

           return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove Homer",
            tags = {"Homer"},
            notes = "remove Homer",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Homer.read_permission", value = Homer.read_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Homer.remove_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Homer.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result getHomer(@ApiParam(value = "id String path",   required = true) @PathParam("id")String homer_id){
        try {
            Homer homer = Homer.find.byId(homer_id);
            if (homer == null) return GlobalResult.notFoundObject("Homer id not found");

            if (!homer.read_permission() )   return GlobalResult.forbidden_Permission();

            return GlobalResult.result_ok( Json.toJson(homer) );

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


    public  Result get_Homers_by_Filter( @ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1", required = true) @PathParam("page_number") Integer page_number){
        try {

            final Form<Swagger_Homer_Filter> form = Form.form(Swagger_Homer_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Homer_Filter help = form.get();

            Query<Homer> query = Ebean.find(Homer.class);

            // If Json contains project_ids list of id's
            if(help.project_ids != null ){
                query.where().in("project.id", help.project_ids);
            }

            Swagger_Homer_List result = new Swagger_Homer_List(query, page_number);

            return GlobalResult.result_ok(Json.toJson(result));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

// HOMMER CONNECTIONS ##################################################################################################

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
    public  Result connectHomerWithProject(@ApiParam(value = "project_id String path",   required = true) @PathParam("project_id") String project_id, @ApiParam(value = "id String path",   required = true) @PathParam("id") String homer_id){
        try{

            Project project = Project.find.byId(project_id);
            Homer homer = Homer.find.byId(homer_id);

            if(project == null)  return GlobalResult.notFoundObject("Project project_id not found");
            if(homer == null)  return GlobalResult.notFoundObject("Homer id not found");

            if (!project.update_permission() ) return GlobalResult.forbidden_Permission();
            if (!homer.update_permission() )   return GlobalResult.forbidden_Permission();


            homer.project = project;
            homer.update();

            return GlobalResult.result_ok(Json.toJson(project));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "connect Homer with Project",
            tags = {"Homer", "Project"},
            notes = "remove Homer",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Project.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result disconnectHomerWithProject(@ApiParam(value = "project_id String path",   required = true) @PathParam("project_id") String project_id, @ApiParam(value = "id String path",   required = true) @PathParam("id") String homer_id){
        try{

            Project project = Project.find.byId(project_id);
            Homer homer = Homer.find.byId(homer_id);

            if(project == null)  return GlobalResult.notFoundObject("Project project_id not found");
            if(homer == null)  return GlobalResult.notFoundObject("Homer id not found");


            if (!project.update_permission() ) return GlobalResult.forbidden_Permission();
            if (!homer.update_permission() )   return GlobalResult.forbidden_Permission();

            if( project.homerList.contains(homer)) homer.project = null;
            homer.update();
            project.homerList.remove(homer);

            return GlobalResult.result_ok(Json.toJson(project));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

// B PROGRAM ############################################################################################################

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
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            if (!project.update_permission() ) return GlobalResult.forbidden_Permission();

            // Tvorba programu
            B_Program b_program             = new B_Program();
            b_program.azurePackageLink      = "personal-program";
            b_program.dateOfCreate          = new Date();
            b_program.program_description   = help.program_description;
            b_program.name                  = help.name;
            b_program.project               = project;
            b_program.setUniqueAzureStorageLink();

            if (!b_program.create_permission() ) return GlobalResult.forbidden_Permission();

            b_program.save();

            return GlobalResult.created(Json.toJson(b_program));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
    public  Result get_b_Program(@ApiParam(value = "id String path", required = true) @PathParam("id") String b_program_id){
        try{

            B_Program b_program = B_Program.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject("B_Program id not found");

            if (!b_program.read_permission() ) return GlobalResult.forbidden_Permission();

            return GlobalResult.result_ok(Json.toJson(b_program));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
            if (program == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            if (! program.b_program.read_permission() ) return GlobalResult.forbidden_Permission();

            return GlobalResult.result_ok(Json.toJson(program));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
    public  Result edit_b_Program(@ApiParam(value = "id String path", required = true) @PathParam("id") String b_program_id){
        try{
            Swagger_B_Program_New help = Json.fromJson(request().body().asJson(), Swagger_B_Program_New.class);


            B_Program b_program  = B_Program.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject("B_Program id not found");

            if (! b_program.edit_permission() ) return GlobalResult.forbidden_Permission();

            b_program.program_description = help.program_description;
            b_program.name                  = help.name;

            b_program.update();
            return GlobalResult.result_ok(Json.toJson(b_program));


        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
    public  Result update_b_program(@ApiParam(value = "id String path", required = true) @PathParam("id") String b_program_id){
        try{

            final Form<Swagger_B_Program_Version_New> form = Form.form(Swagger_B_Program_Version_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_B_Program_Version_New help = form.get();

            // Program který budu ukládat do data Storage v Azure
            String file_content =  help.program;

            // Ověřím program
            B_Program b_program = B_Program.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject("B_Program id not found");

            if (! b_program.update_permission() ) return GlobalResult.forbidden_Permission();

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
            UtilTools.uploadAzure_Version("b-program", file_content, "program.js", b_program.azureStorageLink, b_program.azurePackageLink, versionObjectObject);

            return GlobalResult.result_ok(Json.toJson(versionObjectObject));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
    public  Result remove_b_Program(@ApiParam(value = "id String path", required = true) @PathParam("id") String b_program_id){
        try{

            B_Program program  = B_Program.find.byId(b_program_id);
            if (program == null) return GlobalResult.notFoundObject("B_Program id not found");

            if (! program.delete_permission() ) return GlobalResult.forbidden_Permission();


            // Před smazáním blocko programu je nutné smazat jeho běžící cloud instance
            System.out.println("Snažím se odstanit instance ze serverů");
            List<B_Program_Cloud> b_program_clouds = B_Program_Cloud.find.where().eq("version_object.b_program.id", program.id).findList();
            System.out.println("Počet instancí " + b_program_clouds.size()  );

            for(B_Program_Cloud b_program_cloud : b_program_clouds){
               if(  WebSocketController_Incoming.blocko_servers.containsKey(b_program_cloud.server.server_name)){

                   WS_BlockoServer server = (WS_BlockoServer)  WebSocketController_Incoming.blocko_servers.get(b_program_cloud.server.server_name);
                   WebSocketController_Incoming.blocko_server_remove_instance( server, b_program_cloud.blocko_instance_name);
                   if(WebSocketController_Incoming.incomingConnections_homers.containsKey( b_program_cloud.blocko_instance_name ))   WebSocketController_Incoming.incomingConnections_homers.get(b_program_cloud.blocko_instance_name).onClose();
               }
            }

           program.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


    @ApiOperation(value = "upload B_Program (version) to Homer",
            tags = {"B_Program", "Homer"},
            notes = "If you want upload program (!Immediately!) to Homer -> Homer must be online and connect to Cloud Server, " +
                    "you are uploading B_program version. And if connected M_Project is set to \"Auto_update\", it will automatically update all Grid Terminals.",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful Uploaded",                       response = Result_ok.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",                      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",                  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result uploadProgramToHomer_Immediately(@ApiParam(value = "id", required = true) @PathParam("id") String b_program_id,
                                                    @ApiParam(value = "version_id", required = true) @PathParam("version_id") String version_id,
                                                    @ApiParam(value = "id", required = true) @PathParam("id") String homer_id){
        try {

            Person person = SecurityController.getPerson();

            // B program, který chci nahrát do Cloudu na Blocko server
            B_Program b_program = B_Program.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Verze B programu kterou budu nahrávat do cloudu
            Version_Object version_object = Version_Object.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Homer na který budu nahrávat b_program
            Homer homer = Homer.find.byId(homer_id);
            if (homer == null)  return GlobalResult.notFoundObject("Homer id not found");


            if(! WebSocketController_Incoming.homer_is_online(homer_id)) return GlobalResult.result_BadRequest("Device is not online");


            Thread thread = new Thread(){ @Override public void run() {
                try {

                    // Na homerovi musím zabít a smazat předchozí program - jedná se pouze o nahrávání na cloud !!!
                    B_Program_Homer old_one = B_Program_Homer.find.where().eq("homer.id", homer.id).findUnique();
                    if (old_one != null) { old_one.delete(); }


                    B_Program_Homer program_homer = new B_Program_Homer();
                    program_homer.homer = homer;
                    program_homer.running_from = new Date();
                    program_homer.version_object = version_object;

                   if(!  WebSocketController_Incoming.homer_is_online(homer.id) ) {
                       System.out.println("Homer není online při pokusu na něj nahrát instanci a není dodělané zpžděné nahrátí");

                   };


                    JsonNode result = WebSocketController_Incoming.homer_UploadProgram(WebSocketController_Incoming.incomingConnections_homers.get(homer.id), version_object.id, version_object.files.get(0).get_fileRecord_from_Azure_inString());

                    if(result.get("status").asText().equals("success")){

                        program_homer.save();

                        version_object.b_program_homer = program_homer;
                        version_object.update();

                            NotificationController.send_notification(homer.project.ownersOfProject, Notification_level.success, "Homer was updated successfully");

                    } else  NotificationController.send_notification(homer.project.ownersOfProject, Notification_level.error,   "Attempt updating Homer device to new version. Update Error - " + result.get("error").asText());

                } catch (TimeoutException e) {
                    NotificationController.send_notification(homer.project.ownersOfProject, Notification_level.error, "Attempt updating Homer device to new version. Timeout for connection.");
                } catch (InterruptedException e){
                    NotificationController.send_notification(homer.project.ownersOfProject, Notification_level.error, "Attempt updating Homer device to new version. Server side problem.");
                } catch (Exception e ){
                    NotificationController.send_notification(homer.project.ownersOfProject, Notification_level.error, "Attempt updating Homer device to new version. Critical bug with loading. The error was automatically reported to technical support.");
                }

            }};

            thread.start();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "upload B Program (version) to cloud",
            tags = {"B_Program"},
            notes = "upload version of B Program to cloud. Its possible have only one version from B program in cloud. If you uploud new one - old one will be replaced",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful Uploaded",                       response = B_Program_Homer.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",                      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",                  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result upload_b_Program_ToCloud(@ApiParam(value = "id String path", required = true) @PathParam("id") String b_program_id, @ApiParam(value = "version_id String path", required = true) @PathParam("version_id") String version_id){
        try {

            // B program, který chci nahrát do Cloudu na Blocko server
            B_Program b_program = B_Program.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Verze B programu kterou budu nahrávat do cloudu
            Version_Object version_object = Version_Object.find.where().eq("id", version_id).eq("b_program.id", b_program.id).findUnique();
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Pokud už nějaká instance běžela, tak na ní budu nabrávat nový program a odstraním vazbu na běžící instanci b programu
            if( version_object.b_program_cloud != null ) {

               if(WebSocketController_Incoming.incomingConnections_homers.containsKey(version_object.b_program_cloud.blocko_instance_name )) WebSocketController_Incoming.homer_destroyInstance(version_object.b_program_cloud.blocko_instance_name);

               B_Program_Cloud b_program_cloud = version_object.b_program_cloud;
               b_program_cloud.delete();
            }

            // TODO Chytré dělení na servery - kam se blocko program nahraje

            Cloud_Blocko_Server destination_server = Cloud_Blocko_Server.find.where().eq("server_name", "Alfa").findUnique();


            // Vytvářím nový záznam v databázi pro běžící instanci b programu na blocko serveru
            B_Program_Cloud program_cloud       = new B_Program_Cloud();
            program_cloud.running_from          = new Date();
            program_cloud.version_object        = version_object;
            program_cloud.server                = destination_server;
            program_cloud.setUnique_blocko_instance_name();
            program_cloud.save();

            System.out.println("blocko server size " + WebSocketController_Incoming.blocko_servers.size() );

            if(! WebSocketController_Incoming.blocko_servers.containsKey( destination_server.server_name) ) return GlobalResult.result_BadRequest("Server is offline");

            // Vytvářím instanci na serveru
            WS_BlockoServer server = (WS_BlockoServer) WebSocketController_Incoming.blocko_servers.get(destination_server.server_name);

            JsonNode result =  WebSocketController_Incoming.blocko_server_add_instance(server, program_cloud);

            System.out.println("Příchozí zpráva: " + result.asText() );

            if( result.get("status").asText().equals("success") ) {
                // Ukládám po úspěšné nastartvoání programu v cloudu jeho databázový ekvivalent
                return GlobalResult.result_ok();
            }

            // Neproběhlo to úspěšně smažu zástupný objekt!!!
            program_cloud.delete();
            return GlobalResult.result_BadRequest("Došlo k chybě");

         } catch (TimeoutException a) {
            return GlobalResult.result_BadRequest("Nepodařilo se včas nahrát na server");
         } catch (InterruptedException a) {
            return GlobalResult.result_BadRequest("Vlákno nahrávání bylo přerušeno ");
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    //TODO
    public Result listOfUploadedHomers(String id) {
        //Na id B_Program vezmu všechny Houmry na kterých je program nahrán
        return TODO;
    }

    //TODO
    public Result listOfHomersWaitingForUpload(String id){
        //Na id B_Program vezmu všechny Houmry na které jsem program ještě nenahrál
        return TODO;
    }


// B PROGRAM / HOMER / BLOCKO CLOUD SERVER #############################################################################

    @ApiOperation(value = "Create new Blocko Server",
            tags = {"External Server"},
            notes = "Create new Gate for Blocko Server",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Cloud_Blocko_Server.create_permission", value = Cloud_Blocko_Server.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Cloud_Blocko_Server.create_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Blocko_Server_create_permission" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Cloud_Compilation_Server_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = Cloud_Blocko_Server.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result create_Blocko_Server(){
        try{

            final Form<Swagger_Cloud_Blocko_Server_New> form = Form.form(Swagger_Cloud_Blocko_Server_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Cloud_Blocko_Server_New help = form.get();

            Cloud_Blocko_Server server = new Cloud_Blocko_Server();
            server.server_name = help.server_name;
            server.destination_address = Server.tyrion_webSocketAddress + "/websocket/blocko_server/" + server.server_name;

            server.set_hash_certificate();

            if(!server.create_permission()) return GlobalResult.forbidden_Permission();

            server.save();
            return GlobalResult.result_ok(Json.toJson(server));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Compilation Server",
            tags = {"External Server"},
            notes = "Edit basic information Compilation Server",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Cloud_Blocko_Server.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Blocko_Server_edit_permission" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Cloud_Compilation_Server_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Update successfuly",      response = Cloud_Blocko_Server.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Blocko_Server( @ApiParam(value = "server_id ", required = true) @PathParam("server_id") String server_id ){
        try{

            final Form<Swagger_Cloud_Blocko_Server_New> form = Form.form(Swagger_Cloud_Blocko_Server_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Cloud_Blocko_Server_New help = form.get();

            Cloud_Blocko_Server server = Cloud_Blocko_Server.find.byId(server_id);
            if (server == null) return GlobalResult.notFoundObject("Cloud_Blocko_Server server_id not found");

            if(!server.edit_permission()) return GlobalResult.forbidden_Permission();

            server.server_name = help.server_name;

            server.save();
            return GlobalResult.result_ok(Json.toJson(server));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Blocko Servers",
            tags = {"External Server"},
            notes = "get all Blocko Servers",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Cloud_Blocko_Server.read_permission", value = Cloud_Blocko_Server.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Cloud_Blocko_Server.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Blocko_Server_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Cloud_Blocko_Server.class, responseContainer = "List "),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_All_Blocko_Server(){
        try{

            List<Cloud_Blocko_Server> servers = Cloud_Blocko_Server.find.all();

            return GlobalResult.result_ok(Json.toJson(servers));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove Compilation Servers",
            tags = {"External Server"},
            notes = "remove Compilation Servers",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Cloud_Blocko_Server.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Blocko_Server_delete_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Result_ok.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_Blocko_Server( @ApiParam(value = "server_id ", required = true) @PathParam("server_id") String server_id ){
        try{

            Cloud_Blocko_Server server = Cloud_Blocko_Server.find.byId(server_id);

            if (server == null) return GlobalResult.notFoundObject("Cloud_Compilation_Server server_id not found");
            if(!server.delete_permission()) return GlobalResult.forbidden_Permission();

            server.delete();
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

// TYPE OF BLOCK #######################################################################################################

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
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfBlock_New",
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
            typeOfBlock.general_description = help.general_description;
            typeOfBlock.name                = help.name;


            if(help.project_id != null){

                Project project = Project.find.byId(help.project_id);
                if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

                typeOfBlock.project = project;

            }

            if (! typeOfBlock.create_permission() ) return GlobalResult.forbidden_Permission();

            typeOfBlock.save();

            return GlobalResult.created( Json.toJson(typeOfBlock));
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get TypeOfBlock ",
            tags = {"Blocko-Block"},
            notes = "get BlockoBlock ",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response =  TypeOfBlock.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_TypeOfBlock(String type_of_block_id){
        try {

            TypeOfBlock typeOfBlock = TypeOfBlock.find.byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock type_of_block_id not found");

            if (! typeOfBlock.read_permission() ) return GlobalResult.forbidden_Permission();

            return GlobalResult.result_ok(Json.toJson(typeOfBlock));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
            if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock type_of_block_id not found");

            if (! typeOfBlock.edit_permission() ) return GlobalResult.forbidden_Permission();

            typeOfBlock.general_description = help.general_description;
            typeOfBlock.name                = help.name;

            if(help.project_id != null){

                Project project = Project.find.byId(help.project_id);
                if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

                typeOfBlock.project = project;

            }

            typeOfBlock.update();
            return GlobalResult.result_ok( Json.toJson(typeOfBlock));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
            if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock type_of_block_id not found");

            if (! typeOfBlock.delete_permission()) return GlobalResult.forbidden_Permission();

            typeOfBlock.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
    public Result getAllTypeOfBlocks(){
        try {

            List<TypeOfBlock> typeOfBlocks = TypeOfBlock.find.where().isNull("project").findList();
            typeOfBlocks.addAll( TypeOfBlock.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).findList() );

            for(TypeOfBlock typeOfBlock :typeOfBlocks ) if(! typeOfBlock.read_permission())  return GlobalResult.forbidden_Permission();

            return GlobalResult.result_ok(Json.toJson(typeOfBlocks));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

// BLOCK ###############################################################################################################

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
           if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock type_of_block_id not found");

           blockoBlock.type_of_block = typeOfBlock;

           if (! blockoBlock.create_permission() ) return GlobalResult.forbidden_Permission();

           blockoBlock.save();

            return GlobalResult.result_ok( Json.toJson(blockoBlock) );
       } catch (Exception e) {
           return Loggy.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result", response =  BlockoBlock.class),
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

                if (blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");

                if (! blockoBlock.edit_permission() ) return GlobalResult.forbidden_Permission("You have no permission to edit");


                blockoBlock.general_description = help.general_description;
                blockoBlock.name                = help.name;

                TypeOfBlock typeOfBlock = TypeOfBlock.find.byId(  help.type_of_block_id);
                if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock type_of_block_id not found");

                blockoBlock.type_of_block = typeOfBlock;

                blockoBlock.update();

                return GlobalResult.result_ok(Json.toJson(blockoBlock));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
                if(blocko_version == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");


                if (! blocko_version.read_permission() ) return GlobalResult.forbidden_Permission("You have no permission to get that");

                return GlobalResult.result_ok(Json.toJson(blocko_version));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
    public Result getBlockoBlock(@ApiParam(value = "blocko_block_id String path",   required = true) @PathParam("blocko_block_id") String blocko_block_id){
        try {
            BlockoBlock blockoBlock = BlockoBlock.find.byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");

            if (! blockoBlock.read_permission() ) return GlobalResult.forbidden_Permission();

            return GlobalResult.result_ok(Json.toJson(blockoBlock));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
            if(blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");

            if (! blockoBlock.delete_permission()) return GlobalResult.forbidden_Permission();

            blockoBlock.delete();

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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

            BlockoBlockVersion version = BlockoBlockVersion.find.byId(blocko_block_version_id);
            if(version == null) return GlobalResult.notFoundObject("BlockoBlockVersion blocko_block_version_id not found");

            if (! version.delete_permission()) return GlobalResult.forbidden_Permission();

            version.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "create BlockoBlock version",
            tags = {"Blocko-Block"},
            notes = "new BlockoBlock version",
            produces = "application/json",
            protocols = "https",
            code = 201,
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

            final Form<Swagger_BlockoBlock_BlockoVersion_New> form = Form.form(Swagger_BlockoBlock_BlockoVersion_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_New help = form.get();

            BlockoBlock blockoBlock = BlockoBlock.find.byId(blocko_block_id);

            BlockoBlockVersion version = new BlockoBlockVersion();
            version.date_of_create = new Date();

            version.version_name = help.version_name;
            version.version_description = help.version_description;
            version.design_json = help.design_json;
            version.logic_json = help.logic_json;
            version.blocko_block = blockoBlock;

            if (! blockoBlock.create_permission()) return GlobalResult.forbidden_Permission();

            version.save();

            return GlobalResult.result_ok(Json.toJson(blockoBlock));


        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
            final Form<Swagger_BlockoBlock_BlockoVersion_Edit> form = Form.form(Swagger_BlockoBlock_BlockoVersion_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_Edit help = form.get();


            BlockoBlockVersion version = BlockoBlockVersion.find.byId(blocko_block_version_id);

            version.version_name = help.version_name;
            version.version_description = help.version_description;

            version.update();
            return GlobalResult.result_ok(Json.toJson(version));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
            if (blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");
            if (! blockoBlock.read_permission()) return GlobalResult.forbidden_Permission();


            return GlobalResult.ok(Json.toJson(blockoBlock.blocko_versions));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

///###################################################################################################################*/

}
