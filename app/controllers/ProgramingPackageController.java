package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.*;
import models.blocko.BlockoBlock;
import models.blocko.BlockoBlockVersion;
import models.blocko.Cloud_Blocko_Server;
import models.blocko.TypeOfBlock;
import models.compiler.Board;
import models.compiler.TypeOfBoard;
import models.compiler.Version_Object;
import models.person.Person;
import models.project.b_program.*;
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
import utilities.swagger.outboundClass.Swagger_Boards_For_Blocko;
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
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result postNewProject() {
        try{

            // Zpracování Json
            final Form<Swagger_Project_New> form = Form.form(Swagger_Project_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Project_New help = form.get();

            // Vytvoření objektu
            Project project  = new Project();
            project.project_name = help.project_name;
            project.project_description = help.project_description;

            project.ownersOfProject.add( SecurityController.getPerson() );

            // Kontrola oprávnění těsně před uložením
            if (!project.create_permission())  return GlobalResult.forbidden_Permission();

            // Uložení objektu
            project.save();

            // Vrácení objektu
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
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result getProjectsByUserAccount(){
        try {

            // Získání seznamu
            List<Project> projects = SecurityController.getPerson().owningProjects;

            // Vrácení seznamu
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
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result getProject(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {

            // Kontrola objektu
            Project project = Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (!project.read_permission())   return GlobalResult.forbidden_Permission();

            // Vraácení objektu
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
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result deleteProject(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {

            // Kontrola objektu
            Project project = Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (!project.delete_permission())   return GlobalResult.forbidden_Permission();

            // Smazání objektu
            project.delete();

            // Vrácení potvrzení
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
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result edit_Project(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {

            // Zpracování Json
            final Form<Swagger_Project_New> form = Form.form(Swagger_Project_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Project_New help = form.get();

            // Kontrola objektu
            Project project = Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (!project.edit_permission() )   return GlobalResult.forbidden_Permission();

            // Úprava objektu
            project.project_name = help.project_name;
            project.project_description = help.project_description;

            // Uložení do DB
            project.update();

            // Vrácení změny
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
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result shareProjectWithUsers(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {

            // Zpracování Json
            final Form<Swagger_ShareProject_Person> form = Form.form(Swagger_ShareProject_Person.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_ShareProject_Person help = form.get();

            // Kontrola objektu
            Project project = Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (!project.share_permission() )   return GlobalResult.forbidden_Permission();

            // Získání seznamu
            List<Person> list = Person.find.where().idIn(help.persons_id).findList();

            for (Person person : list) {
                if (!person.owningProjects.contains(project)) {

                    // Úprava objektů
                    project.ownersOfProject.add(person);
                    person.owningProjects.add(project);

                    // Uložení do DB
                    person.update();
                }
            }

            // Uložení do DB
            project.update();

            // Vrácení objektu
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
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result unshareProjectWithUsers(@ApiParam(value = "project_id String path", required = true) @PathParam("project_id") String project_id){
        try {

            // Zpracování Json
            final Form<Swagger_ShareProject_Person> form = Form.form(Swagger_ShareProject_Person.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_ShareProject_Person help = form.get();

            //Kontrola objektu
            Project project = Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (!project.unshare_permission() )   return GlobalResult.forbidden_Permission();

            // Získání seznamu
            List<Person> list = Person.find.where().idIn(help.persons_id).findList();

            for (Person person : list) {
                if (person.owningProjects.contains(project)) {

                    // Úprava objektů
                    project.ownersOfProject.remove(person);
                    person.owningProjects.remove(project);

                    // Uložení do DB
                    person.update();
                }
            }

            // Uložení do DB
            project.update();

            // Vrácení objektu
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
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result newHomer(){
        try{

            // Zpracování Json
            final Form<Swagger_Homer_New> form = Form.form(Swagger_Homer_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Homer_New help = form.get();

            // Kontrola před vytvořením objektu
            if ( Homer.find.where().eq("id", help.homer_id).findUnique() != null ) return GlobalResult.result_BadRequest("Homer with this id exist");

            // Vytvoření objektu
            Homer homer = new Homer();
            homer.id = help.homer_id;
            homer.type_of_device = help.type_of_device;

            // Kontrola oprávnění těsně před uložením
            if (!homer.create_permission() )   return GlobalResult.forbidden_Permission();

            // Uložení objektu
            homer.save();

            // Vrácení objektu
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
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result removeHomer(@ApiParam(value = "b_program_id String path",   required = true) @PathParam("id") String homer_id){
        try{

           // Kontrola objektu
           Homer homer = Homer.find.byId(homer_id);
           if(homer == null) return GlobalResult.notFoundObject("Homer id not found");

           // Kontrola oprávnění
           if (!homer.delete_permission() )   return GlobalResult.forbidden_Permission();

           // Smazání objektu
           homer.delete();

           // Vrácení potvrzení
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
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result getHomer(@ApiParam(value = "b_program_id String path",   required = true) @PathParam("id")String homer_id){
        try {

            // Kontrola objektu
            Homer homer = Homer.find.byId(homer_id);
            if (homer == null) return GlobalResult.notFoundObject("Homer id not found");

            // Kontrola oprávnění
            if (!homer.read_permission() )   return GlobalResult.forbidden_Permission();

            // Vrácení objektu
            return GlobalResult.result_ok( Json.toJson(homer) );

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


    public  Result get_Homers_by_Filter( @ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1", required = true) @PathParam("page_number") Integer page_number){
        try {

            // Zpracování Json
            final Form<Swagger_Homer_Filter> form = Form.form(Swagger_Homer_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Homer_Filter help = form.get();

            // Získání objektu
            Query<Homer> query = Ebean.find(Homer.class);

            // If Json contains project_ids list of id's
            if(help.project_ids != null ){
                query.where().in("project.id", help.project_ids);
            }

            // Omezení počtu vrácených objektů
            Swagger_Homer_List result = new Swagger_Homer_List(query, page_number);

            // Vrácení objektu
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
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission", value = "It requires both permission"),

                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Homer.update_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Project.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result connectHomerWithProject(@ApiParam(value = "project_id String path",   required = true) @PathParam("project_id") String project_id, @ApiParam(value = "id String path",   required = true) @PathParam("id") String homer_id){
        try{

            // Získání objektů
            Project project = Project.find.byId(project_id);
            Homer homer = Homer.find.byId(homer_id);

            // Kontrola objektů
            if(project == null)  return GlobalResult.notFoundObject("Project project_id not found");
            if(homer == null)  return GlobalResult.notFoundObject("Homer id not found");

            // Kontrola oprávnění
            if (!project.update_permission() ) return GlobalResult.forbidden_Permission();
            if (!homer.update_permission() )   return GlobalResult.forbidden_Permission();

            // Úprava objektu
            homer.project = project;

            // Uložení objektu
            homer.update();

            // Vrácení objektu
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
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission", value = "It requires both permission"),

                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Homer.update_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Project.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result disconnectHomerWithProject(@ApiParam(value = "project_id String path",   required = true) @PathParam("project_id") String project_id, @ApiParam(value = "id String path",   required = true) @PathParam("id") String homer_id){
        try{

            // Získání objektů
            Project project = Project.find.byId(project_id);
            Homer homer = Homer.find.byId(homer_id);

            // Kontrola objektů
            if(project == null)  return GlobalResult.notFoundObject("Project project_id not found");
            if(homer == null)  return GlobalResult.notFoundObject("Homer id not found");

            // Kontrola oprávnění
            if (!project.update_permission() ) return GlobalResult.forbidden_Permission();
            if (!homer.update_permission() )   return GlobalResult.forbidden_Permission();

            // Úprava objektu
            if( project.homerList.contains(homer)) homer.project = null;

            // Uložení objektu
            homer.update();

            // Úprava objektu
            project.homerList.remove(homer);

            // Vrácení objektu
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
                            dataType = "utilities.swagger.documentationClass.Swagger_B_Program_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created", response =  B_Program.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result new_b_Program(String project_id){
        try{

            // Zpracování Json
            final Form<Swagger_B_Program_New> form = Form.form(Swagger_B_Program_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_B_Program_New help = form.get();

            // Kontrola objektu
            Project project = Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (!project.update_permission() ) return GlobalResult.forbidden_Permission();

            // Tvorba programu
            B_Program b_program             = new B_Program();
            b_program.azurePackageLink      = "personal-program";
            b_program.dateOfCreate          = new Date();
            b_program.program_description   = help.program_description;
            b_program.name                  = help.name;
            b_program.project               = project;
            b_program.setUniqueAzureStorageLink();

            // Kontrola oprávnění těsně před uložením
            if (!b_program.create_permission() ) return GlobalResult.forbidden_Permission();

            // Uložení objektu
            b_program.save();

            // Vrácení objektu
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
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  B_Program.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result get_b_Program(@ApiParam(value = "b_program_id String path", required = true) @PathParam("b_program_id") String b_program_id){
        try{

            // Kontrola objektu
            B_Program b_program = B_Program.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Kontrola oprávnění
            if (!b_program.read_permission() ) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
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
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.read_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  B_Program.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result get_b_Program_version(@ApiParam(value = "version_id String path", required = true) @PathParam("version_id") String version_id){
        try{

            // Kontrola objektu
            Version_Object program = Version_Object.find.byId(version_id);
            if (program == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Kontrola oprávnění
            if (! program.b_program.read_permission() ) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
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
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.edit_permission", value = "true"),
                    })
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
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result edit_b_Program(@ApiParam(value = "b_program_id String path", required = true) @PathParam("b_program_id") String b_program_id){
        try{

            // Zpracování Json
            Swagger_B_Program_New help = Json.fromJson(request().body().asJson(), Swagger_B_Program_New.class);

            // Kontrola objektu
            B_Program b_program  = B_Program.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Kontrola oprávěnní
            if (! b_program.edit_permission() ) return GlobalResult.forbidden_Permission();

            // Úprava objektu
            b_program.program_description = help.program_description;
            b_program.name                  = help.name;

            // Uložení objektu
            b_program.update();

            // Vrácení objektu
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
                            dataType = "utilities.swagger.documentationClass.Swagger_B_Program_Version_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Version_Object.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result update_b_program(@ApiParam(value = "b_program_id String path", required = true) @PathParam("b_program_id") String b_program_id){
        try{

            // Zpracování Json
            final Form<Swagger_B_Program_Version_New> form = Form.form(Swagger_B_Program_Version_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_B_Program_Version_New help = form.get();

            // Program který budu ukládat do data Storage v Azure
            String file_content =  help.program;

            // Ověření programu
            B_Program b_program = B_Program.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Kontrola oprávnění
            if (! b_program.update_permission() ) return GlobalResult.forbidden_Permission();

            // První nová Verze
            Version_Object versionObjectObject          = new Version_Object();
            versionObjectObject.version_name            = help.version_name;
            versionObjectObject.version_description     = help.version_description;
            versionObjectObject.azureLinkVersion        = new Date().toString();
            versionObjectObject.date_of_create          = new Date();
            versionObjectObject.b_program               = b_program;

            // Uložení objektu
            versionObjectObject.save();

            for(Swagger_B_Program_Version_New.Connected_Board h_board : help.boards){

                // Kontrola objektu
                Board board = Board.find.byId(h_board.board_id);
                if (board == null) return GlobalResult.notFoundObject("Board board_id not found");

                // Kontrola objektu
                Version_Object c_program_version = Version_Object.find.byId(h_board.c_program_version_id);
                if (c_program_version == null) return GlobalResult.notFoundObject("B_Program b_program_id not found");

                // Vytvoření objektu
                B_Pair b_pair = new B_Pair();
                b_pair.board = board;
                b_pair.c_program_version = c_program_version;
                b_pair.b_program_version = versionObjectObject;

                // Uložení objektu
                b_pair.save();

            }

            // Úprava objektu
            b_program.version_objects.add(versionObjectObject);

            // Uložení objektu
            b_program.update();

            // Nahrání na Azure
            UtilTools.uploadAzure_Version("b-program", file_content, "program.js", b_program.azureStorageLink, b_program.azurePackageLink, versionObjectObject);

            // Vrácení objektu
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
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.delete_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result remove_b_Program(@ApiParam(value = "b_program_id String path", required = true) @PathParam("b_program_id") String b_program_id){
        try{

            // Kontrola objektu
            B_Program program  = B_Program.find.byId(b_program_id);
            if (program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Kontrola oprávění
            if (! program.delete_permission() ) return GlobalResult.forbidden_Permission();


            // Před smazáním blocko programu je nutné smazat jeho běžící cloud instance
            List<B_Program_Cloud> b_program_clouds = B_Program_Cloud.find.where().eq("version_object.b_program.id", program.id).findList();


            for(B_Program_Cloud b_program_cloud : b_program_clouds){
               if(  WebSocketController_Incoming.blocko_servers.containsKey(b_program_cloud.server.server_name)){

                   WS_BlockoServer server = (WS_BlockoServer)  WebSocketController_Incoming.blocko_servers.get(b_program_cloud.server.server_name);
                   WebSocketController_Incoming.blocko_server_remove_instance( server, b_program_cloud.blocko_instance_name);
                   if(WebSocketController_Incoming.incomingConnections_homers.containsKey( b_program_cloud.blocko_instance_name ))   WebSocketController_Incoming.incomingConnections_homers.get(b_program_cloud.blocko_instance_name).onClose();
               }
            }

            // Smazání objektu
            program.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove B Program version",
            tags = {"B_Program"},
            notes = "remove B_Program version object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.delete_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result remove_b_Program_version(@ApiParam(value = "version_id String path", required = true) @PathParam("version_id") String version_id){
        try{

            // Získání objektu
            Version_Object version_object  = Version_Object.find.byId(version_id);

            // Kontrola objektu
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object id not found");
            if (version_object.b_program == null) return GlobalResult.badRequest("B_Program not found");

            // Kontrola oprávnění
            if (! version_object.b_program.delete_permission() ) return GlobalResult.forbidden_Permission();


            // Před smazáním blocko programu je nutné smazat jeho běžící cloud instance
            List<B_Program_Cloud> b_program_clouds = B_Program_Cloud.find.where().eq("version_object.id", version_object.id).findList();

            for(B_Program_Cloud b_program_cloud : b_program_clouds){
                if(  WebSocketController_Incoming.blocko_servers.containsKey(b_program_cloud.server.server_name)){

                    WS_BlockoServer server = (WS_BlockoServer)  WebSocketController_Incoming.blocko_servers.get(b_program_cloud.server.server_name);
                    WebSocketController_Incoming.blocko_server_remove_instance( server, b_program_cloud.blocko_instance_name);
                    if(WebSocketController_Incoming.incomingConnections_homers.containsKey( b_program_cloud.blocko_instance_name ))   WebSocketController_Incoming.incomingConnections_homers.get(b_program_cloud.blocko_instance_name).onClose();
                }
            }

            // Smazání objektu
            version_object.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


/*

    @ApiOperation(value = "connect hardware to B Program (version) ",
            tags = {"B_Program"},
            notes = "sconnect to B_Program version users board",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.update_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  B_Pair.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result connect_b_Program_hardware(@ApiParam(value = "version_id String path", required = true) @PathParam("version_id") String version_id, @ApiParam(value = "board_id String path", required = true) @PathParam("board_id") String board_id ){
        try{

            Version_Object program  = Version_Object.find.byId(version_id);
            if (program == null) return GlobalResult.notFoundObject("B_Program b_program_id not found");

            if(program.b_program == null ) return GlobalResult.badRequest("Version_Object is not from B_Program");

            Board board = Board.find.byId(board_id);
            if (board == null) return GlobalResult.notFoundObject("Board board_id not found");

            if (! program.b_program.update_permission() ) return GlobalResult.forbidden_Permission();


            B_Pair b_pair = new B_Pair();
            b_pair.board = board;
            b_pair.c_program_version = program;
            b_pair.save();


            return GlobalResult.result_ok(Json.toJson(b_pair));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "disconnect hardware to B Program (version) ",
            tags = {"B_Program"},
            notes = "disconnect to B_Program version users board",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.update_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  B_Pair.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result disconnect_b_Program_hardware(@ApiParam(value = "version_id String path", required = true) @PathParam("version_id") String version_id,  @ApiParam(value = "board_id String path", required = true) @PathParam("board_id") String board_id ){
        try{

            if( Version_Object.find.byId(version_id) == null) return GlobalResult.notFoundObject("B_Program b_program_id not found");
            if( Board.find.byId(board_id) == null)            return GlobalResult.notFoundObject("Board board_id not found");

            B_Pair b_pair = B_Pair.find.where().eq("c_program_version.id", version_id).eq("board.id", board_id).findUnique();
            if(b_pair == null) return GlobalResult.badRequest("Connection not exist!");

            if (! b_pair.c_program_version.b_program.update_permission() ) return GlobalResult.forbidden_Permission();

            b_pair.delete();

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

*/


    @ApiOperation(value = "upload B_Program (version) to Homer",
            tags = {"B_Program", "Homer"},
            notes = "If you want upload program (!Immediately!) to Homer -> Homer must be online and connect to Cloud Server, " +
                    "you are uploading B_program version. And if connected M_Project is set to \"Auto_update\", it will automatically update all Grid Terminals.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.update_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful Uploaded",                       response = Result_ok.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",                      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",                  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result uploadProgramToHomer_Immediately(@ApiParam(value = "b_program_id", required = true) @PathParam("b_program_id") String b_program_id,
                                                    @ApiParam(value = "version_id", required = true)   @PathParam("version_id") String version_id,
                                                    @ApiParam(value = "homer_id", required = true)     @PathParam("homer_id") String homer_id){
        try {

            // Získání objektu
            Person person = SecurityController.getPerson();

            // Kontrola objektu
            // B program, který chci nahrát do Cloudu na Blocko server
            B_Program b_program = B_Program.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Kontrola oprávnění
            if (! b_program.update_permission() ) return GlobalResult.forbidden_Permission();

            // Kontrola objektu
            // Verze B programu kterou budu nahrávat do cloudu
            Version_Object version_object = Version_Object.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Kontrola objektu
            // Homer na který budu nahrávat b_program
            Homer homer = Homer.find.byId(homer_id);
            if (homer == null)  return GlobalResult.notFoundObject("Homer id not found");


            if(! WebSocketController_Incoming.homer_is_online(homer_id)) return GlobalResult.result_BadRequest("Device is not online");


            Thread thread = new Thread(){ @Override public void run() {
                try {

                    //Získání objektu
                    // Na homerovi musím zabít a smazat předchozí program - jedná se pouze o nahrávání na cloud !!!
                    B_Program_Homer old_one = B_Program_Homer.find.where().eq("homer.id", homer.id).findUnique();

                    // Smazání objektu
                    if (old_one != null) { old_one.delete(); }

                    // Vytvoření objektu
                    B_Program_Homer program_homer = new B_Program_Homer();
                    program_homer.homer = homer;
                    program_homer.running_from = new Date();
                    program_homer.version_object = version_object;

                   if(!  WebSocketController_Incoming.homer_is_online(homer.id) ) {
                       System.out.println("Homer není online při pokusu na něj nahrát instanci a není dodělané zpožděné nahrátí");
                       this.interrupt();
                   }


                    JsonNode result = WebSocketController_Incoming.homer_UploadProgram(WebSocketController_Incoming.incomingConnections_homers.get(homer.id), version_object.id, version_object.files.get(0).get_fileRecord_from_Azure_inString());

                    if(result.get("status").asText().equals("success")){

                        // Uložení objektu
                        program_homer.save();

                        // Úprava objektu
                        version_object.b_program_homer = program_homer;

                        // Uložení objektu
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

            // Vrácení potvrzení
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
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.update_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful Uploaded",                       response = B_Program_Homer.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",                      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",                  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result upload_b_Program_ToCloud(@ApiParam(value = "b_program_id String path", required = true) @PathParam("b_program_id") String b_program_id, @ApiParam(value = "version_id String path", required = true) @PathParam("version_id") String version_id){
        try {

            // Kontrola objektu
            // B program, který chci nahrát do Cloudu na Blocko server
            B_Program b_program = B_Program.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Kontrola objektu
            // Verze B programu kterou budu nahrávat do cloudu
            Version_Object version_object = Version_Object.find.where().eq("id", version_id).eq("b_program.id", b_program.id).findUnique();
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Kontrola oprávnění
            if (! b_program.update_permission() ) return GlobalResult.forbidden_Permission();

            // Pokud už nějaká instance běžela, tak na ní budu nahrávat nový program a odstraním vazbu na běžící instanci b programu
            if( version_object.b_program_cloud != null ) {

                if(WebSocketController_Incoming.incomingConnections_homers.containsKey(version_object.b_program_cloud.blocko_instance_name )) WebSocketController_Incoming.homer_destroyInstance(version_object.b_program_cloud.blocko_instance_name);

                // Úprava objektu
                B_Program_Cloud b_program_cloud = version_object.b_program_cloud;

                //Smazání objektu
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

            // Uložení objektu
            program_cloud.save();

            if(! WebSocketController_Incoming.blocko_servers.containsKey( destination_server.server_name) ) return GlobalResult.result_BadRequest("Server is offline");

            // Vytvářím instanci na serveru
            WS_BlockoServer server = (WS_BlockoServer) WebSocketController_Incoming.blocko_servers.get(destination_server.server_name);

            JsonNode result =  WebSocketController_Incoming.blocko_server_add_instance(server, program_cloud);

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

            // Zpracování Json
            final Form<Swagger_Cloud_Blocko_Server_New> form = Form.form(Swagger_Cloud_Blocko_Server_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Cloud_Blocko_Server_New help = form.get();

            // Vytvoření objektu
            Cloud_Blocko_Server server = new Cloud_Blocko_Server();
            server.server_name = help.server_name;
            server.destination_address = Server.tyrion_webSocketAddress + "/websocket/blocko_server/" + server.server_name;

            server.set_hash_certificate();

            // Kontrola oprávnění
            if(!server.create_permission()) return GlobalResult.forbidden_Permission();

            // Uložení objektu
            server.save();

            // Vrácení objektu
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
            @ApiResponse(code = 200, message = "Update successfully",      response = Cloud_Blocko_Server.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Blocko_Server( @ApiParam(value = "server_id ", required = true) @PathParam("server_id") String server_id ){
        try{

            // Zpracování Json
            final Form<Swagger_Cloud_Blocko_Server_New> form = Form.form(Swagger_Cloud_Blocko_Server_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Cloud_Blocko_Server_New help = form.get();

            // Kontrola objektu
            Cloud_Blocko_Server server = Cloud_Blocko_Server.find.byId(server_id);
            if (server == null) return GlobalResult.notFoundObject("Cloud_Blocko_Server server_id not found");

            // Kontrola oprávnění
            if(!server.edit_permission()) return GlobalResult.forbidden_Permission();

            // Úprava objektu
            server.server_name = help.server_name;

            // Uložení objektu
            server.save();

            // Vrácení objektu
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

            // Získání seznamu
            List<Cloud_Blocko_Server> servers = Cloud_Blocko_Server.find.all();

            // Vrácení seznamu
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

            // Kontrola objektu
            Cloud_Blocko_Server server = Cloud_Blocko_Server.find.byId(server_id);
            if (server == null) return GlobalResult.notFoundObject("Cloud_Compilation_Server server_id not found");

            // Kontrola oprávnění
            if(!server.delete_permission()) return GlobalResult.forbidden_Permission();

            // Smzání objektu
            server.delete();

            // Vrácení potvrzení
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
            code = 201,
            extensions = {
                    // TODO Description
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBlock.create_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfBlock_create_permission" )
                    })
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

            // Zpracování Json
            final Form<Swagger_TypeOfBlock_New> form = Form.form(Swagger_TypeOfBlock_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBlock_New help = form.get();

            // Vytvoření objektu
            TypeOfBlock typeOfBlock = new TypeOfBlock();
            typeOfBlock.general_description = help.general_description;
            typeOfBlock.name                = help.name;

            // Nejedná se o privátní Typ Bločku
            if(help.project_id != null){

                // Kontrola objektu
                Project project = Project.find.byId(help.project_id);
                if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

                // Úprava objektu
                typeOfBlock.project = project;

            }

            // Kontrola oprávnění těsně před uložením podle standardu
            if (! typeOfBlock.create_permission() ) return GlobalResult.forbidden_Permission();

            // Uložení objektu
            typeOfBlock.save();

            // Vrácení objektu
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

            // Kontrola objektu
            TypeOfBlock typeOfBlock = TypeOfBlock.find.byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock type_of_block_id not found");

            // Kontrola oprávnění
            if (! typeOfBlock.read_permission() ) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
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
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBlock.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfBlock_edit_permission" )
                    })
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

            // Zpracování Json
            final Form<Swagger_TypeOfBlock_New> form = Form.form(Swagger_TypeOfBlock_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBlock_New help = form.get();

            // Kontrola objektu
            TypeOfBlock typeOfBlock = TypeOfBlock.find.byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock type_of_block_id not found");

            // Kontrola oprávnění
            if (! typeOfBlock.edit_permission() ) return GlobalResult.forbidden_Permission();

            // Úprava objektu
            typeOfBlock.general_description = help.general_description;
            typeOfBlock.name                = help.name;

            if(help.project_id != null){

                // Kontrola objektu
                Project project = Project.find.byId(help.project_id);
                if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

                // Úprava objektu
                typeOfBlock.project = project;

            }

            // Uložení objektu
            typeOfBlock.update();

            // Vrácení objektu
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
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBlock.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfBlock_delete_permission")
                    })
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

            // Kontrola objektu
            TypeOfBlock typeOfBlock = TypeOfBlock.find.byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock type_of_block_id not found");

            // Kontrola oprávnění
            if (! typeOfBlock.delete_permission()) return GlobalResult.forbidden_Permission();

            // Smazání objektu
            typeOfBlock.delete();

            // Vrácení objektu
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
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  TypeOfBlock.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result getAllTypeOfBlocks(){
        try {

            // Získání seznamu
            List<TypeOfBlock> typeOfBlocks = TypeOfBlock.find.where().isNull("project").findList();
            typeOfBlocks.addAll( TypeOfBlock.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).findList() );

            // Kontrola oprávnění
            for(TypeOfBlock typeOfBlock :typeOfBlocks ) if(! typeOfBlock.read_permission())  return GlobalResult.forbidden_Permission();

            // Vrácení seznamu
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
            code = 201,
            extensions = {
                    // TODO Description
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.create_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlock_create_permission" )
                    })
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

           // Zpracování Json
           final Form<Swagger_BlockoBlock_New> form = Form.form(Swagger_BlockoBlock_New.class).bindFromRequest();
           if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
           Swagger_BlockoBlock_New help = form.get();

           // Vytvoření objektu
           BlockoBlock blockoBlock = new BlockoBlock();

           blockoBlock.general_description = help.general_description;
           blockoBlock.name                = help.name;
           blockoBlock.author              = SecurityController.getPerson();

           // Kontrola objektu
           TypeOfBlock typeOfBlock = TypeOfBlock.find.byId( help.type_of_block_id);
           if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock type_of_block_id not found");

           // Úprava objektu
           blockoBlock.type_of_block = typeOfBlock;

           // Kontrola oprávnění těsně před uložením
           if (! blockoBlock.create_permission() ) return GlobalResult.forbidden_Permission();

           // Uložení objektu
           blockoBlock.save();

           // Vrácení objektu
           return GlobalResult.created( Json.toJson(blockoBlock) );

       } catch (Exception e) {
           return Loggy.result_internalServerError(e, request());
       }
    }

    @ApiOperation(value = "edit basic information of the BlockoBlock",
            tags = {"Blocko-Block"},
            notes = "update basic information (name, and desription) of the independent BlockoBlock",
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

                // Zpracování Json
                final Form<Swagger_BlockoBlock_New> form = Form.form(Swagger_BlockoBlock_New.class).bindFromRequest();
                if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
                Swagger_BlockoBlock_New help = form.get();

                // Kontrola objektu
                BlockoBlock blockoBlock = BlockoBlock.find.byId(blocko_block_id);
                if (blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");

                // Kontrola oprávnění
                if (! blockoBlock.edit_permission() ) return GlobalResult.forbidden_Permission("You have no permission to edit");

                // Úprava objektu
                blockoBlock.general_description = help.general_description;
                blockoBlock.name                = help.name;

                // Kontrola objektu
                TypeOfBlock typeOfBlock = TypeOfBlock.find.byId(  help.type_of_block_id);
                if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock type_of_block_id not found");

                // Úprava objektu
                blockoBlock.type_of_block = typeOfBlock;

                // Uložení objektu
                blockoBlock.update();

                // Vrácení objektu
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
            code = 200,
            extensions = {
                    // TODO Description
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_read_permission")
                    })
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
                // Kontrola objektu
                BlockoBlockVersion blocko_version = BlockoBlockVersion.find.byId(blocko_version_id);
                if(blocko_version == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");

                // Kontrola oprávnění
                if (! blocko_version.read_permission() ) return GlobalResult.forbidden_Permission("You have no permission to get that");

                // Vrácení objektu
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
            code = 200,
            extensions = {
                    // TODO Description
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlock_read_permission")
                    })
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
            // Kontrola objektu
            BlockoBlock blockoBlock = BlockoBlock.find.byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");

            // Kontrola oprávnění
            if (! blockoBlock.read_permission() ) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
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
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlock_delete_permission")
                    })
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

            // Kontrola objektu
            BlockoBlock blockoBlock = BlockoBlock.find.byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");

            // Kontrola oprávnění
            if (! blockoBlock.delete_permission()) return GlobalResult.forbidden_Permission();

            // Smazání objektu
            blockoBlock.delete();

            // Vrácení potvrzení
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
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_delete_permission")
                    })
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

            // Kontrola objektu
            BlockoBlockVersion version = BlockoBlockVersion.find.byId(blocko_block_version_id);
            if(version == null) return GlobalResult.notFoundObject("BlockoBlockVersion blocko_block_version_id not found");

            // Kontrola oprávnění
            if (! version.delete_permission()) return GlobalResult.forbidden_Permission();

            // Smazání objektu
            version.delete();

            // Vrácení potvrzení
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
            extensions = {
                    // TODO Description
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion.create_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_create_permission" )
                    })
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

            // Zpracování Json
            final Form<Swagger_BlockoBlock_BlockoVersion_New> form = Form.form(Swagger_BlockoBlock_BlockoVersion_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_New help = form.get();

            // Získání objektu
            BlockoBlock blockoBlock = BlockoBlock.find.byId(blocko_block_id);

            // Vytvoření objektu
            BlockoBlockVersion version = new BlockoBlockVersion();
            version.date_of_create = new Date();

            version.version_name = help.version_name;
            version.version_description = help.version_description;
            version.design_json = help.design_json;
            version.logic_json = help.logic_json;
            version.blocko_block = blockoBlock;

            // Kontrola oprávnění
            if (! blockoBlock.create_permission()) return GlobalResult.forbidden_Permission();

            // Uložení objektu
            version.save();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(blockoBlock));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit BlockoBlock version",
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

            // Zpracování Json
            final Form<Swagger_BlockoBlock_BlockoVersion_Edit> form = Form.form(Swagger_BlockoBlock_BlockoVersion_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_Edit help = form.get();

            // Získání objektu
            BlockoBlockVersion version = BlockoBlockVersion.find.byId(blocko_block_version_id);

            // Úprava objektu
            version.version_name = help.version_name;
            version.version_description = help.version_description;

            // Uložení objektu
            version.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all BlockoBlock version",
            tags = {"Blocko-Block"},
            notes = "get all versions (content) from independent BlockoBlock",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    // TODO Description
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_read_permission")
                    })
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

            // Kontrola objektu
            BlockoBlock blockoBlock = BlockoBlock.find.byId(blocko_block_id);
            if (blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");

            // Kontrola oprávnění
            if (! blockoBlock.read_permission()) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
            return GlobalResult.ok(Json.toJson(blockoBlock.blocko_versions));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


// BOARD ###################################################################################################################*/


    public Result board_all_details_for_blocko(String project_id){
        try {

            // Kontrola objektu
            Project project = Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (! project.read_permission()) return GlobalResult.forbidden_Permission();

            // Získání objektu
            Swagger_Boards_For_Blocko boards_for_blocko = new Swagger_Boards_For_Blocko();
            boards_for_blocko.boards = project.boards;
            boards_for_blocko.typeOfBoards = TypeOfBoard.find.where().eq("boards.project.id", project.id ).findList();
            boards_for_blocko.c_programs = project.c_programs;

            // Vrácení objektu
            return GlobalResult.ok(Json.toJson(boards_for_blocko));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

}
