package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.*;
import models.blocko.BlockoBlock;
import models.blocko.BlockoBlockVersion;
import models.blocko.TypeOfBlock;
import models.compiler.Board;
import models.compiler.FileRecord;
import models.compiler.TypeOfBoard;
import models.compiler.Version_Object;
import models.person.Invitation;
import models.person.Person;
import models.project.b_program.B_Pair;
import models.project.b_program.B_Program;
import models.project.b_program.B_Program_Hw_Group;
import models.project.b_program.Homer_Instance;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.b_program.servers.Private_Homer_Server;
import models.project.c_program.C_Program;
import models.project.global.Product;
import models.project.global.Project;
import play.api.libs.mailer.MailerClient;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.UtilTools;
import utilities.emails.EmailTool;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Filter_List.Swagger_B_Program_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_Blocko_Block_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_Homer_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_Type_Of_Block_List;
import utilities.swagger.outboundClass.Swagger_B_Program_Version;
import utilities.webSocket.WS_BlockoServer;
import utilities.webSocket.WebSCType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class ProgramingPackageController extends Controller {

    @Inject MailerClient mailerClient;

// Loger  ##############################################################################################################
    static play.Logger.ALogger logger = play.Logger.of("Loggy");


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

            Product product = Product.find.byId(help.product_id);
            if(product == null)return GlobalResult.notFoundObject("Product not found");
            if(!product.create_new_project()) return GlobalResult.result_BadRequest(product.create_new_project_if_not());


            // Vytvoření objektu
            Project project  = new Project();
            project.project_name = help.project_name;
            project.project_description = help.project_description;
            project.product = product;

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
    public  Result getProject(@ApiParam(value = "project_id String path", required = true)  String project_id){
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
            consumes = "text/html",
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
    public  Result deleteProject(@ApiParam(value = "project_id String path", required = true) String project_id){
        try {

            // Kontrola objektu
            Project project = Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (!project.delete_permission())   return GlobalResult.forbidden_Permission();

           // Kvuli bezpečnosti abych nesmazal něco co nechceme
           for(C_Program c : project.c_programs){ c.delete();}


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
    public  Result edit_Project(@ApiParam(value = "project_id String path", required = true)  String project_id){
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
            tags = {"Project"},
            notes = "sends Invitation to all users in list: List<persons_mail>",
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
    public Result shareProjectWithUsers(@ApiParam(value = "project_id String path", required = true)  String project_id){
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

            // Získání seznamu uživatelů, kteří jsou registrovaní(listIn) a kteří ne(listOut)
            List<Person> listIn = new ArrayList<>();
            List<String> toRemove = new ArrayList<>();

            // Roztřídění seznamů
            for (String mail : help.persons_mail){
                Person person =  Person.find.where().eq("mail",mail).findUnique();
                if(!(person == null)){
                    listIn.add(person);
                    toRemove.add(person.mail);
                }
            }

            for (String mail : toRemove){
                help.persons_mail.remove(mail);
            }

            // Vytvoření pozvánky pro nezaregistrované uživatele
            for (String mail :  help.persons_mail){

                Invitation invitation = Invitation.find.where().eq("mail", mail).findUnique();
                if(invitation == null){
                    invitation = new Invitation();
                    invitation.mail = mail;
                    invitation.time_of_creation = new Date();
                    invitation.owner = SecurityController.getPerson();
                    invitation.project = project;
                    invitation.save();
                    project.invitations.add(invitation);
                }

                String link = Server.becki_invitationToCollaborate + "/" + mail;

                // Odeslání emailu s linkem pro registraci
                try {
                             new EmailTool()
                            .addEmptyLineSpace()
                            .startParagraph("13")
                            .addText("User ")
                            .addBoldText(SecurityController.getPerson().full_name)
                            .addText(" invites you to collaborate on the project ")
                            .addBoldText(project.project_name + ". ")
                            .addText("If you would like to participate in it, please click on the link below and register yourself. ")
                            .endParagraph()
                            .addEmptyLineSpace()
                            .addLine()
                            .addEmptyLineSpace()
                            .addLink(link,"Click here to collaborate","18")
                            .addEmptyLineSpace()
                            .sendEmail(mail, "Invitation to Collaborate" );


                } catch (Exception e) {
                    logger.error ("Sending mail -> critical error", e);
                    e.printStackTrace();
                }
            }

            for (Person person : listIn) {

                Invitation invitation = Invitation.find.where().eq("mail", person.mail).findUnique();
                if(invitation == null){
                    invitation = new Invitation();
                    invitation.mail = person.mail;
                    invitation.time_of_creation = new Date();
                    invitation.owner = SecurityController.getPerson();
                    invitation.project = project;
                    invitation.save();
                    project.invitations.add(invitation);
                }

                NotificationController.project_invitation(SecurityController.getPerson(), person, project, invitation);
            }

            // Uložení do DB
            project.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(project));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


    @ApiOperation(value = "add participant to a Project",
            tags = {"Project"},
            notes = "adds Person to a Project, every piece of information is held in Invitation",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",            response = Project.class),
            @ApiResponse(code = 400, message = "Objects not found",    response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result addParticipantToProject(String id, boolean decision ){
        try{

            // Kontroly objektů
            Invitation invitation = Invitation.find.byId(id);
            if(invitation == null) return GlobalResult.notFoundObject("Invitation no longer exists");

            Person person = Person.find.where().eq("mail", invitation.mail).findUnique();
            if(person == null) return GlobalResult.notFoundObject("Person does not exist");

            Project project = invitation.project;
            if(project == null) return GlobalResult.notFoundObject("Project no longer exists");

            if ((!person.owningProjects.contains(project))&&(decision)) {

                // Úprava objektů
                project.ownersOfProject.add(person);
                person.owningProjects.add(project);
            }

            // Odeslání notifikace podle rozhodnutí uživatele
            if(!decision){
                NotificationController.project_rejected_by_invited_person(invitation.owner, person, project);
            }else{
                NotificationController.project_accepted_by_invited_person(invitation.owner, person, project);
            }

            // Smazání pozvánky
            invitation.delete();

            // Uložení do DB
            person.update();
            project.update();

            return GlobalResult.result_ok();
        }catch (Exception e){
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
            @ApiResponse(code = 200, message = "Ok Result",                                 response = Project.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",                      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",                  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result unshareProjectWithUsers(@ApiParam(value = "project_id String path", required = true) String project_id){
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
            List<Person> list = Person.find.where().eq("mail",help.persons_mail).findList();

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
                            @ExtensionProperty(name = "Private_Homer_Server.create_permission", value = Private_Homer_Server.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Private_Homer_Server_create" )
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
            @ApiResponse(code = 201, message = "Successfully created", response =  Private_Homer_Server.class),
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
            if ( Private_Homer_Server.find.where().eq("mac_address", help.mac_address).findUnique() != null ) return GlobalResult.result_BadRequest("Homer with this id exist");

            // Vytvoření objektu
            Private_Homer_Server privateHomerServer = new Private_Homer_Server();
            privateHomerServer.mac_address = help.mac_address;
            privateHomerServer.type_of_device = help.type_of_device;

            if(help.project_id != null){
                Project project = Project.find.byId(help.project_id);
                if(project == null) return GlobalResult.notFoundObject("Project project_id not found");
                privateHomerServer.project = project;
            }

            // Kontrola oprávnění těsně před uložením
            if (!privateHomerServer.create_permission() )   return GlobalResult.forbidden_Permission();

            // Uložení objektu
            privateHomerServer.save();

            // Vrácení objektu
            return GlobalResult.created(Json.toJson(privateHomerServer));

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
                            @ExtensionProperty(name = "Private_Homer_Server.delete_permission", value = "true"),
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
    public  Result removeHomer(@ApiParam(value = "b_program_id String path",   required = true) String homer_id){
        try{

           // Kontrola objektu
           Private_Homer_Server privateHomerServer = Private_Homer_Server.find.byId(homer_id);
           if(privateHomerServer == null) return GlobalResult.notFoundObject("Homer id not found");

           // Kontrola oprávnění
           if (!privateHomerServer.delete_permission() )   return GlobalResult.forbidden_Permission();

           // Smazání objektu
           privateHomerServer.delete();

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
                            @ExtensionProperty(name = "Private_Homer_Server.read_permission", value = Private_Homer_Server.read_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Private_Homer_Server.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Private_Homer_Server_read" )
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Private_Homer_Server.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result getHomer(@ApiParam(value = "b_program_id String path",   required = true) String homer_id){
        try {

            // Kontrola objektu
            Private_Homer_Server privateHomerServer = Private_Homer_Server.find.byId(homer_id);
            if (privateHomerServer == null) return GlobalResult.notFoundObject("Homer id not found");

            // Kontrola oprávnění
            if (!privateHomerServer.read_permission() )   return GlobalResult.forbidden_Permission();

            // Vrácení objektu
            return GlobalResult.result_ok( Json.toJson(privateHomerServer) );

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


    @ApiOperation(value = "get Homers with Filters parameters",
            tags = {"Homer"},
            notes = "If you want get all or only some Homers you can use filter parameters in Json. But EveryTime server return maximal 25 objects \n\n" +
                    "so, you have to used that limit for frontend pagination -> first round (0,25), second round (26, 50) etc... in Json we help you with pages list \n ",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Permission: ", value = "Permission is not required!" ),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Homer_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",               response = Swagger_Homer_List.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result get_Homers_by_Filter( @ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) Integer page_number){
        try {

            // Zpracování Json
            final Form<Swagger_Homer_Filter> form = Form.form(Swagger_Homer_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Homer_Filter help = form.get();

            // Získání objektu
            Query<Private_Homer_Server> query = Ebean.find(Private_Homer_Server.class);

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
            consumes = "text/html",
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
    public  Result connectHomerWithProject(@ApiParam(value = "project_id String path",   required = true) String project_id, @ApiParam(value = "id String path",   required = true) String homer_id){
        try{

            // Získání objektů
            Project project = Project.find.byId(project_id);
            Private_Homer_Server privateHomerServer = Private_Homer_Server.find.byId(homer_id);

            // Kontrola objektů
            if(project == null)  return GlobalResult.notFoundObject("Project project_id not found");
            if(privateHomerServer == null)  return GlobalResult.notFoundObject("Homer id not found");

            // Kontrola oprávnění
            if (!project.update_permission() ) return GlobalResult.forbidden_Permission();
            if (!privateHomerServer.update_permission() )   return GlobalResult.forbidden_Permission();

            // Úprava objektu
            privateHomerServer.project = project;

            // Uložení objektu
            privateHomerServer.update();

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
            consumes = "text/html",
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
    public  Result disconnectHomerWithProject(@ApiParam(value = "project_id String path",   required = true) String project_id, @ApiParam(value = "id String path",   required = true) String homer_id){
        try{

            // Získání objektů
            Project project = Project.find.byId(project_id);
            Private_Homer_Server privateHomerServer = Private_Homer_Server.find.byId(homer_id);

            // Kontrola objektů
            if(project == null)  return GlobalResult.notFoundObject("Project project_id not found");
            if(privateHomerServer == null)  return GlobalResult.notFoundObject("Homer id not found");

            // Kontrola oprávnění
            if (!project.update_permission() ) return GlobalResult.forbidden_Permission();
            if (!privateHomerServer.update_permission() )   return GlobalResult.forbidden_Permission();

            // Úprava objektu
            if( project.privateHomerServerList.contains(privateHomerServer)) privateHomerServer.project = null;

            // Uložení objektu
            privateHomerServer.update();

            // Úprava objektu
            project.privateHomerServerList.remove(privateHomerServer);

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
            b_program.dateOfCreate          = new Date();
            b_program.program_description   = help.program_description;
            b_program.name                  = help.name;
            b_program.project               = project;

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
    public  Result get_b_Program(@ApiParam(value = "b_program_id String path", required = true)  String b_program_id){
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
    public  Result get_b_Program_version(@ApiParam(value = "version_id String path", required = true)  String version_id){
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
    public  Result edit_b_Program(@ApiParam(value = "b_program_id String path", required = true)  String b_program_id){
        try{

            // Zpracování Json
            final Form<Swagger_B_Program_New> form = Form.form(Swagger_B_Program_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_B_Program_New help = form.get();


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
            notes = "edit basic information in B_Program object",
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
            @ApiResponse(code = 200, message = "Ok Result", response =  Swagger_B_Program_Version.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result update_b_program(@ApiParam(value = "b_program_id String path", required = true)  String b_program_id){
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
            Version_Object version_object          = new Version_Object();
            version_object.version_name            = help.version_name;
            version_object.version_description     = help.version_description;
            version_object.date_of_create          = new Date();
            version_object.b_program               = b_program;



            // Definování main Board
            for( Swagger_B_Program_Version_New.Hardware_group group : help.hardware_group) {

                B_Program_Hw_Group b_program_hw_group = new B_Program_Hw_Group();

                // Definuji Main Board - Tedy yodu pokud v Json přišel (není podmínkou)
                if(group.main_board != null) {

                    B_Pair b_pair = new B_Pair();

                    b_pair.board = Board.find.byId(group.main_board.board_id);
                    if ( b_pair.board == null) return GlobalResult.notFoundObject("Board board_id not found");
                    if (!b_pair.board.type_of_board.connectible_to_internet)  return GlobalResult.result_BadRequest("Main Board must be internet connectible!");
                    if(!b_pair.board.update_permission()) return GlobalResult.forbidden_Permission();

                    b_pair.c_program_version = Version_Object.find.byId(group.main_board.c_program_version_id);
                    if ( b_pair.c_program_version == null) return GlobalResult.notFoundObject("C_Program Version_Object c_program_version_id not found");
                    if ( b_pair.c_program_version.c_program == null)  return GlobalResult.result_BadRequest("Version is not from C_Program");


                    if( TypeOfBoard.find.where().eq("c_programs.id",  b_pair.c_program_version.c_program.id ).where().eq("boards.id",  b_pair.board.id).findRowCount() < 1){
                        return GlobalResult.result_BadRequest("You want upload C++ program version id: " +  b_pair.c_program_version.id + " thats not compatible with hardware " + b_pair.board.id);
                    }

                    b_program_hw_group.main_board_pair = b_pair;

                }

                // Definuji Devices - Tedy yodu pokud v Json přišly (není podmínkou)

                if(group.boards != null) {

                    for(Swagger_B_Program_Version_New.Connected_Board connected_board : group.boards ){

                        B_Pair b_pair = new B_Pair();

                        b_pair.board = Board.find.byId(connected_board.board_id);
                        if ( b_pair.board == null) return GlobalResult.notFoundObject("Board board_id not found");
                        if(!b_pair.board.update_permission()) return GlobalResult.forbidden_Permission();


                        b_pair.c_program_version = Version_Object.find.byId(connected_board.c_program_version_id);
                        if ( b_pair.c_program_version == null) return GlobalResult.notFoundObject("C_Program Version_Object c_program_version_id not found");
                        if ( b_pair.c_program_version.c_program == null)  return GlobalResult.result_BadRequest("Version is not from C_Program");

                        if( TypeOfBoard.find.where().eq("c_programs.id",  b_pair.c_program_version.c_program.id ).where().eq("boards.id",  b_pair.board.id).findRowCount() < 1){
                            return GlobalResult.result_BadRequest("You want upload C++ program version id: " +  b_pair.c_program_version.id + " thats not compatible with hardware " + b_pair.board.id);
                        }

                        b_program_hw_group.device_board_pairs.add(b_pair);
                    }
                }
                version_object.b_program_hw_groups.add(b_program_hw_group);
            }



            // Uložení objektu
            version_object.save();

            // Úprava objektu
            b_program.version_objects.add(version_object);

            // Uložení objektu
            b_program.update();

            // Update verze
            version_object.refresh();

            // Nahrání na Azure
             UtilTools.uploadAzure_Version(file_content, "program.js", b_program.get_path() , version_object);

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson( version_object.b_program.program_version(version_object) ));

        } catch (Exception e) {
            e.printStackTrace();
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove B Program",
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
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result remove_b_Program(@ApiParam(value = "b_program_id String path", required = true)  String b_program_id){
        try{

            // Kontrola objektu
            B_Program program  = B_Program.find.byId(b_program_id);
            if (program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Kontrola oprávění
            if (! program.delete_permission() ) return GlobalResult.forbidden_Permission();


            // Před smazáním blocko programu je nutné smazat jeho běžící cloud instance
            List<Homer_Instance> blockoInstnaces = Homer_Instance.find.where().eq("version_object.b_program.id", program.id).findList();


            for(Homer_Instance blockoInstnace : blockoInstnaces){
               if(  WebSocketController_Incoming.blocko_servers.containsKey(blockoInstnace.cloud_homer_server.server_name)){

                   WS_BlockoServer server = (WS_BlockoServer)  WebSocketController_Incoming.blocko_servers.get(blockoInstnace.cloud_homer_server.server_name);
                   WebSocketController_Incoming.blocko_server_remove_instance( server, blockoInstnace.blocko_instance_name);
                   if(WebSocketController_Incoming.incomingConnections_homers.containsKey( blockoInstnace.blocko_instance_name ))   WebSocketController_Incoming.incomingConnections_homers.get(blockoInstnace.blocko_instance_name).onClose();
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
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result remove_b_Program_version(@ApiParam(value = "version_id String path", required = true) String version_id){
        try{

            // Získání objektu
            Version_Object version_object  = Version_Object.find.byId(version_id);

            // Kontrola objektu
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object id not found");
            if (version_object.b_program == null) return GlobalResult.result_BadRequest("B_Program not found");

            // Kontrola oprávnění
            if (! version_object.b_program.delete_permission() ) return GlobalResult.forbidden_Permission();


            // Před smazáním verze je nutné smazat jeho běžící cloud instanco
            //* Jestli tedy nějaké
            if(version_object.homer_instance != null) {
                Homer_Instance blockoInstnace = version_object.homer_instance;

                Cloud_Homer_Server server_cloud = Cloud_Homer_Server.find.where().eq("cloud_programs.id", version_object.homer_instance.id).findUnique();

                if (WebSocketController_Incoming.blocko_servers.containsKey(server_cloud.server_name)) {

                    WS_BlockoServer server = (WS_BlockoServer) WebSocketController_Incoming.blocko_servers.get(blockoInstnace.cloud_homer_server.server_name);
                    WebSocketController_Incoming.blocko_server_remove_instance(server, blockoInstnace.blocko_instance_name);

                    if (WebSocketController_Incoming.incomingConnections_homers.containsKey(blockoInstnace.blocko_instance_name))
                        WebSocketController_Incoming.incomingConnections_homers.get(blockoInstnace.blocko_instance_name).onClose();
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
                            dataType = "utilities.swagger.documentationClass.Swagger_B_Program_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_B_Program_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_b_Program_by_Filter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) Integer page_number){
        try {

            // Získání JSON
            final Form<Swagger_B_Program_Filter> form = Form.form(Swagger_B_Program_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_B_Program_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<B_Program> query = Ebean.find(B_Program.class);
            query.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id);

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if(help.project_id != null){

                query.where().eq("project.id", help.project_id);
            }

            // Vytvoření odchozího JSON
            Swagger_B_Program_List result = new Swagger_B_Program_List(query, page_number);

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            return GlobalResult.internalServerError();
        }
    }



    @ApiOperation(value = "upload B_Program (version) to Homer",
            tags = {"B_Program", "Homer"},
            notes = "If you want upload program (!Immediately!) to Homer -> Homer must be online and connect to Cloud Server, " +
                    "you are uploading B_program version. And if connected M_Project is set to \"Auto_update\", it will automatically update all Grid Terminals.",
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
            @ApiResponse(code = 200, message = "Successful Uploaded",                       response = Result_ok.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",                      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",                  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result uploadProgramToHomer_Immediately(@ApiParam(value = "b_program_id", required = true) String b_program_id,
                                                    @ApiParam(value = "version_id", required = true)    String version_id,
                                                    @ApiParam(value = "homer_id", required = true)     String homer_id){
        try {


            // Kontrola objektu
            // B program, který chci nahrát do Cloudu na Blocko cloud_blocko_server
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
            Private_Homer_Server private_homer_server = Private_Homer_Server.find.byId(homer_id);
            if (private_homer_server == null)  return GlobalResult.notFoundObject("Homer id not found");


            if(! WebSocketController_Incoming.homer_online_state(homer_id)) return GlobalResult.result_BadRequest("Device is not online");


            Thread thread = new Thread(){ @Override public void run() {
                try {

                    //Získání objektu
                    // Na homerovi musím zabít a smazat předchozí program - jedná se pouze o nahrávání na cloud !!!
                    Homer_Instance old_one = Homer_Instance.find.where().eq("private_server.id", private_homer_server.id).findUnique();

                    // Smazání objektu
                    if (old_one != null) { old_one.delete(); }

                    // Vytvoření objektu
                    Homer_Instance program_homer = new Homer_Instance();
                    program_homer.private_server = private_homer_server;
                    program_homer.running_from = new Date();
                    program_homer.version_object = version_object;
                    program_homer.save();


                    version_object.homer_instance = program_homer;
                    // Uložení objektu
                    version_object.update();



                    if(!  WebSocketController_Incoming.homer_online_state(private_homer_server.b_program_homer.blocko_instance_name) ) {
                       NotificationController.unload_Instance_was_unsuccessfull( SecurityController.getPerson() , program_homer , "One of the components of the server is not available");
                       this.interrupt();
                   }

                    JsonNode result = WebSocketController_Incoming.homer_upload_program(WebSocketController_Incoming.incomingConnections_homers.get(private_homer_server.id), version_object.id, version_object.files.get(0).get_fileRecord_from_Azure_inString());

                    if(result.get("status").asText().equals("success")){
                       NotificationController.upload_Instance_was_successful( SecurityController.getPerson() , program_homer);
                    } else {
                       NotificationController.unload_Instance_was_unsuccessfull( SecurityController.getPerson() , program_homer , result.get("error").asText() );
                    }

                } catch (Exception e) {
                    NotificationController.unload_of_Instance_was_unsuccessfull_with_error( SecurityController.getPerson() , version_object);
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
            @ApiResponse(code = 200, message = "Successful Uploaded",                       response = Homer_Instance.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",                      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",                  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result upload_b_Program_ToCloud(@ApiParam(value = "version_id String path", required = true) String version_id){
        try {

            System.out.println("\n \n \n \n \n \n");

            // Kontrola objektu: Verze B programu kterou budu nahrávat do cloudu
            Version_Object version_object = Version_Object.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object version_id not found");


            // Kontrola objektu: B program, který chci nahrát do Cloudu na Blocko cloud_blocko_server
            if (version_object.b_program == null) return GlobalResult.result_BadRequest("Version_Object is not version of B_Program");
            B_Program b_program = version_object.b_program;

            // Kontrola oprávnění
            if (! b_program.update_permission() ) return GlobalResult.forbidden_Permission();

            // Kontroluji přítomnost Yody - Dovolím nahrát instnaci bez HW
            // if(version_object.b_program_hw_groups == null || version_object.yoda_board_pair.board == null) return GlobalResult.result_BadRequest("Version needs Main Board connectible to internet!");

            // Pokud už nějaká instance běžela, tak na ní budu nahrávat nový program a odstraním vazbu na běžící instanci b programu
            if(version_object.b_program_hw_groups!= null) {
                logger.debug("Uploud version to cloud contains Hardware!");

                for(B_Program_Hw_Group group : version_object.b_program_hw_groups){

                    // Kontrola Yody
                   if(group.main_board_pair != null ) {

                       Board yoda = group.main_board_pair.board;

                       //1. Pokud už běží v jiné instanci mimo vlastní dočasnou instnaci
                       if (yoda.private_instance != null) {
                           logger.debug("Yoda měl už vlastní historickou instanci! Proto jí musím smazat a to i z Homer serveru");
                           yoda.private_instance.delete();
                       }

                       if(Homer_Instance.find.where().eq("version_object.b_program_hw_groups.main_board_pair.board.id", yoda.id ).findUnique() != null  ){
                           return GlobalResult.result_BadRequest("Master Device is used in another working instance, you cannoct create two instance with same Master Device");
                       }



                        if(group.device_board_pairs != null) {
                            //


                            //2. Pokud nikdy nebyl spárován


                            // Kontrola Deviců
                            //1. Jestli nejsou už v jiné instanci

                            //2.
                        }
                   }else {
                       logger.debug("Instance neobsahovala žádný HW - respektive neobsahovala Yodu!!");
                   }
                }

            }


            // TODO Chytré dělení na servery - kam se blocko program nahraje??
            Cloud_Homer_Server destination_server = Cloud_Homer_Server.find.where().eq("server_name", "Alfa").findUnique();

            if(! WebSocketController_Incoming.blocko_servers.containsKey( destination_server.server_name) ) {

                // Instance se dá nahrát se spožděním - Upozornění v Result
                // TODO - upozornění uživatelovi přes bublinu
            }


            // Vytvářím nový záznam v databázi pro běžící instanci b programu na blocko serveru
            Homer_Instance program_cloud        = new Homer_Instance();
            program_cloud.project               = b_program.project;
            program_cloud.running_from          = new Date();
            program_cloud.version_object        = version_object;
            program_cloud.cloud_homer_server    = destination_server;
            program_cloud.setUnique_blocko_instance_name();


            // Pak smazat!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
           // program_cloud.id = "i1";

            // Uložení objektu
            program_cloud.save();

            try {

                // Vytvářím instanci na serveru
                WS_BlockoServer server = (WS_BlockoServer) WebSocketController_Incoming.blocko_servers.get(destination_server.server_name);
                WebSCType homer = WebSocketController_Incoming.blocko_server_add_instance(server, program_cloud, true);
                ActualizationController.add_new_actualization_request_Checking_HW_Firmware(b_program.project, program_cloud);
                return GlobalResult.result_ok();

            }catch (Exception e){
                // Neproběhlo to úspěšně smažu zástupný objekt!!!
                return GlobalResult.result_BadRequest("Došlo k chybě");
            }

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


    // TODO
    public Result update_blocko_code_in_instance_verison(String instance_name, String version_id){
        try{

            // Kontrola objektu
            Homer_Instance homer_instance = Homer_Instance.find.where().eq("blocko_instance_name",instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            Version_Object version_object = Version_Object.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object id not found");
            if(version_object.b_program == null)  return GlobalResult.result_BadRequest("Version_Object is not version of B_Program");

            // Kontrola oprávěnní
            if (! version_object.b_program.edit_permission() ) return GlobalResult.forbidden_Permission();

            // Kontrola oprávěnní
            if (! homer_instance.project.update_permission() ) return GlobalResult.forbidden_Permission();

            if(! homer_instance.is_online()) return GlobalResult.result_BadRequest("Instance is offline");

            FileRecord fileRecord = FileRecord.find.where().eq("version_object.id", version_id ).eq("file_name","program.js").findUnique();
            if(fileRecord == null) return GlobalResult.notFoundObject("File with Blocko Code not found under version " + version_id);

            homer_instance.version_object = version_object;
            homer_instance.update();

            // Updajtuju sice kod ? Ale nikoliv HW?
            JsonNode result = WebSocketController_Incoming.homer_upload_program(homer_instance.get_instance(), version_object.id, fileRecord.get_fileRecord_from_Azure_inString() );

            // TODO - Update HW???


            if(result.has("state") && result.get("state").asText().equals("success")){
                // Vrácení potvrzení
                return GlobalResult.result_ok();
            }else {
                return GlobalResult.result_BadRequest(result);
            }
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "upload B Program (code) to instnace ",
            hidden = true
    )
    public Result update_blocko_code_in_instance_with_code(String instance_name){
        try{

            // Zpracování Json
            final Form<Swagger_Instance_UpdateCode> form = Form.form(Swagger_Instance_UpdateCode.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Instance_UpdateCode help = form.get();

            // Kontrola objektu
            Homer_Instance homer_instance = Homer_Instance.find.where().eq("blocko_instance_name",instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            if(!homer_instance.is_online()) return GlobalResult.notFoundObject("Homer_Instance is not online");

            JsonNode result = WebSocketController_Incoming.homer_upload_program(homer_instance.get_instance(), "fake_program", help.code );

            if(result.has("state") && result.get("state").asText().equals("success")){
                // Vrácení potvrzení
                return GlobalResult.result_ok();
            }else {
                return GlobalResult.result_BadRequest(result);
            }
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "add yoda to instnace ",
            hidden = true
    )
    public Result add_Yoda_To_Instance(String instance_name, String yoda_device_id){
        try{

            logger.debug("Add Yoda To Instnace " + instance_name + " yodaId " + yoda_device_id);

            // Kontrola objektu
            Homer_Instance homer_instance = Homer_Instance.find.where().eq("blocko_instance_name",instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            Board board = Board.find.byId(yoda_device_id);
            if (board == null) return GlobalResult.notFoundObject("Board id not found");
            if (!board.main_board()) return GlobalResult.notFoundObject("Board id not main board!");

            Version_Object version_object = homer_instance.version_object;

            // Kontrola oprávěnní
            if (! version_object.b_program.edit_permission() ) return GlobalResult.forbidden_Permission();

            // Kontrola oprávěnní
            if (! homer_instance.project.update_permission() ) return GlobalResult.forbidden_Permission();

            if(! homer_instance.is_online()) return GlobalResult.result_BadRequest("Instance is offline");

            B_Pair yoda_pair = new B_Pair();
            yoda_pair.board = board;
            yoda_pair.c_program_version = board.actual_c_program_version;
            yoda_pair.save();

            B_Program_Hw_Group group = new B_Program_Hw_Group();
            group.main_board_pair = yoda_pair;
            group.save();

            yoda_pair.main_board_pair = group;
            yoda_pair.update();

            version_object.b_program_hw_groups.add(group);
            version_object.update();

            // Updajtuju sice kod ? Ale nikoliv HW?
            JsonNode result = WebSocketController_Incoming.homer_add_Yoda_to_instance(homer_instance.get_instance(), yoda_device_id);

            if(result.has("state") && result.get("state").asText().equals("success")){
                // Vrácení potvrzení
                return GlobalResult.result_ok();
            }else {
                return GlobalResult.result_BadRequest(result);
            }
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "add yoda to instnace ",
            hidden = true
    )
    public Result remove_Yoda_from_Instance(String instance_name, String yoda_device_id){
        try{

            // Kontrola objektu
            Homer_Instance homer_instance =  Homer_Instance.find.where().eq("blocko_instance_name",instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            Board board = Board.find.byId(yoda_device_id);
            if (board == null) return GlobalResult.notFoundObject("Board id not found");
            if (!board.main_board()) return GlobalResult.notFoundObject("Board id not main board!");

            Version_Object version_object = homer_instance.version_object;

            // Kontrola oprávěnní
            if (! version_object.b_program.edit_permission() ) return GlobalResult.forbidden_Permission();

            // Kontrola oprávěnní
            if (! homer_instance.project.update_permission() ) return GlobalResult.forbidden_Permission();

            if(! homer_instance.is_online()) return GlobalResult.result_BadRequest("Instance is offline");

            B_Pair yoda_pair = B_Pair.find.where().eq("main_board_pair.b_program_version_groups.id",version_object.id).findUnique();
            if(yoda_pair == null) return GlobalResult.notFoundObject("yoda_pair for removing not found");
            yoda_pair.main_board_pair.delete();

            // Updajtuju sice kod ? Ale nikoliv HW?
            JsonNode result = WebSocketController_Incoming.homer_remove_Yoda_from_instance(homer_instance.get_instance(), yoda_device_id);

            if(result.has("state") && result.get("state").asText().equals("success")){
                // Vrácení potvrzení
                return GlobalResult.result_ok();
            }else {
                return GlobalResult.result_BadRequest(result);
            }
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "add yoda to instnace ",
            hidden = true
    )
    public Result add_Device_To_Instance(String yoda_device_id, String device_id){
        try{

            Board yoda_board = Board.find.byId(yoda_device_id);
            if (yoda_board == null) return GlobalResult.notFoundObject("Board yoda_device_id not found");
            if (!yoda_board.main_board()) return GlobalResult.notFoundObject("Board yoda_device_id is not main board!");


            Board device_board = Board.find.byId(device_id);
            if (device_board == null) return GlobalResult.notFoundObject("Board device_id not found");
            if (device_board.main_board()) return GlobalResult.notFoundObject("Board device_id main board - you cannot add Main board under main board!");

            // Kontrola objektu
            Homer_Instance homer_instance = Homer_Instance.find.where().eq("version_object.b_program_hw_groups.main_board_pair.board.id", yoda_board.id).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            Version_Object version_object = homer_instance.version_object;

            // Kontrola oprávěnní
            if (! version_object.b_program.edit_permission() ) return GlobalResult.forbidden_Permission();

            // Kontrola oprávěnní
            if (! homer_instance.project.update_permission() ) return GlobalResult.forbidden_Permission();

            if(! homer_instance.is_online()) return GlobalResult.result_BadRequest("Instance is offline");


            B_Program_Hw_Group group = B_Program_Hw_Group.find.where().eq("main_board_pair.board.id", yoda_device_id).eq("b_program_version_groups.homer_instance.id", homer_instance.id).findUnique();

            B_Pair device_pair = new B_Pair();
            device_pair.board = device_board;
            device_pair.c_program_version = device_board.actual_c_program_version;
            device_pair.save();

            group.device_board_pairs.add(device_pair);
            group.update();

            // Updajtuju sice kod ? Ale nikoliv HW?
            JsonNode result = WebSocketController_Incoming.homer_add_Device_to_instance(homer_instance.get_instance(), yoda_device_id, device_id);

            if(result.has("state") && result.get("state").asText().equals("success")){
                // Vrácení potvrzení
                return GlobalResult.result_ok();
            }else {
                return GlobalResult.result_BadRequest(result);
            }
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "add yoda to instnace ",
            hidden = true
    )
    public Result remove_Device_From_Instance(String yoda_device_id, String device_id){

        try{

            Board yoda_board = Board.find.byId(yoda_device_id);
            if (yoda_board == null) return GlobalResult.notFoundObject("Board yoda_device_id not found");
            if (!yoda_board.main_board()) return GlobalResult.notFoundObject("Board yoda_device_id is not main board!");


            Board device_board = Board.find.byId(device_id);
            if (device_board == null) return GlobalResult.notFoundObject("Board device_id not found");
            if (device_board.main_board()) return GlobalResult.notFoundObject("Board device_id main board - you cannot add Main board under main board!");

            // Kontrola objektu
            Homer_Instance homer_instance = Homer_Instance.find.where().eq("version_object.b_program_hw_groups.main_board_pair.board.id", yoda_board.id).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            Version_Object version_object = homer_instance.version_object;

            // Kontrola oprávěnní
            if (! version_object.b_program.edit_permission() ) return GlobalResult.forbidden_Permission();

            // Kontrola oprávěnní
            if (! homer_instance.project.update_permission() ) return GlobalResult.forbidden_Permission();

            if(! homer_instance.is_online()) return GlobalResult.result_BadRequest("Instance is offline");

            B_Pair device_pair = B_Pair.find.where().eq("device_board_pair.b_program_version_groups.homer_instance.id", homer_instance.id).eq("device_board_pair.b_program_version_groups.main_board_pair.board.id",yoda_device_id).findUnique();
            if(device_pair == null) return GlobalResult.notFoundObject("device_pair for removing not found");
            device_pair.delete();

            // Updajtuju sice kod ? Ale nikoliv HW?
            JsonNode result = WebSocketController_Incoming.homer_remove_Device_from_instance(homer_instance.get_instance(), yoda_device_id, device_id);

            if(result.has("state") && result.get("state").asText().equals("success")){
                // Vrácení potvrzení
                return GlobalResult.result_ok();
            }else {
                return GlobalResult.result_BadRequest(result);
            }
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "add yoda to instnace ",
            hidden = true
    )
    public Result send_command_to_instnace(String instance_name, String target_id, String string_command){
        try{

            Homer_Instance homer_instance = Homer_Instance.find.where().eq("blocko_instance_name",instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            // Kontrola oprávnění
            if (!homer_instance.project.update_permission() ) return GlobalResult.forbidden_Permission();

            // Kontrola oprávnění
            Board board = Board.find.byId(target_id);
            if (board == null) return GlobalResult.notFoundObject("Board targetId not found");


            Homer_Instance.TypeOfCommand command = Homer_Instance.TypeOfCommand.getTypeCommand(string_command);
            if(command == null) return GlobalResult.notFoundObject("Command not found!");

            if(!homer_instance.is_online()) return GlobalResult.result_BadRequest("Instance is offline");

            // Updajtuju sice kod ? Ale nikoliv HW?
            JsonNode result = WebSocketController_Incoming.homer_devices_commands( homer_instance.get_instance(), target_id, command);

            if(result.has("state") && result.get("state").asText().equals("success")){
                // Vrácení potvrzení
                return GlobalResult.result_ok();
            }else {
                return GlobalResult.result_ok(result);
            }

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

   }

    //TODO
    public Result create_list_of_instances(){
        try{

            //1. Verze Blocko programu

            //2. Yodu

            //3.
            return ok();

        }catch (Exception e){
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
                            @ExtensionProperty(name = "Cloud_Homer_Server.create_permission", value = Cloud_Homer_Server.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Cloud_Blocko_Server.create_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Homer_Server_creat" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Cloud_Homer_Server_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",    response = Cloud_Homer_Server.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result create_Blocko_Server(){
        try{

            // Zpracování Json
            final Form<Swagger_Cloud_Homer_Server_New> form = Form.form(Swagger_Cloud_Homer_Server_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Cloud_Homer_Server_New help = form.get();

            // Vytvoření objektu
            Cloud_Homer_Server server = new Cloud_Homer_Server();
            server.server_name = help.server_name;
            server.destination_address = Server.tyrion_webSocketAddress + "/websocket/blocko_server/" + server.server_name;

            server.set_hash_certificate();

            // Kontrola oprávnění
            if(!server.create_permission()) return GlobalResult.forbidden_Permission();

            // Uložení objektu
            server.save();

            // Vrácení objektu
            return GlobalResult.created(Json.toJson(server));

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
                            @ExtensionProperty(name = "Cloud_Homer_Server.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Homer_Server_edit" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Cloud_Homer_Server_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Updated successfully",    response = Cloud_Homer_Server.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Blocko_Server( @ApiParam(value = "server_id ", required = true) String server_id ){
        try{

            // Zpracování Json
            final Form<Swagger_Cloud_Homer_Server_New> form = Form.form(Swagger_Cloud_Homer_Server_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Cloud_Homer_Server_New help = form.get();

            // Kontrola objektu
            Cloud_Homer_Server server = Cloud_Homer_Server.find.byId(server_id);
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
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Cloud_Homer_Server.read_permission", value = Cloud_Homer_Server.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Cloud_Homer_Server.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Homer_Server_read")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Cloud_Homer_Server.class, responseContainer = "List "),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_All_Blocko_Server(){
        try{

            // Získání seznamu
            List<Cloud_Homer_Server> servers = Cloud_Homer_Server.find.all();

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
                            @ExtensionProperty(name = "Cloud_Homer_Server.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Homer_Server_delete")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_Blocko_Server( @ApiParam(value = "server_id ", required = true)  String server_id ){
        try{

            // Kontrola objektu
            Cloud_Homer_Server server = Cloud_Homer_Server.find.byId(server_id);
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
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "TypeOfBlock_create_permission", value = TypeOfBlock.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
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
            @ApiResponse(code = 201, message = "Successfully created",    response = TypeOfBlock.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
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
                if(! project.update_permission()) return GlobalResult.forbidden_Permission();

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
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "TypeOfBlock_read_permission", value = TypeOfBlock.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project == null - Public TypeOfBlock", value = "Permission not Required!"),
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfBlock_create_permission" )
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = TypeOfBlock.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
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
            @ApiResponse(code = 200, message = "Ok Result",               response =  TypeOfBlock.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result editTypeOfBlock(@ApiParam(value = "type_of_block_id String path",   required = true)  String type_of_block_id){
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
            consumes = "text/html",
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result deleteTypeOfBlock(@ApiParam(value = "type_of_block_id String path",   required = true)  String type_of_block_id){
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
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response =  TypeOfBlock.class, responseContainer = "List"),
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

    @ApiOperation(value = "get TypeOfBlock by Filter",
            tags = {"Type of Block"},
            notes = "get TypeOfBlock List",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "TypeOfBlock_read_permission", value = "No need to check permission, because Tyrion returns only those results which user owns"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Type_Of_Block_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Type_Of_Block_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_TypeOfBlock_by_Filter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) Integer page_number){
        try {

            // Získání JSON
            final Form<Swagger_Type_Of_Block_Filter> form = Form.form(Swagger_Type_Of_Block_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Type_Of_Block_Filter help = form.get();

            // Získání všech objektů a následné odfiltrování soukormých TypeOfBlock
            Query<TypeOfBlock> query = Ebean.find(TypeOfBlock.class);

            if(help.private_type){
                query.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id);
            }else{
                query.where().eq("project", null);
            }

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if(help.project_id != null){

                query.where().eq("project.id", help.project_id);
            }

            // Vytvoření odchozího JSON
            Swagger_Type_Of_Block_List result = new Swagger_Type_Of_Block_List(query, page_number);

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            e.printStackTrace();
            return GlobalResult.internalServerError();
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
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlock_create_permission", value = BlockoBlock.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBlocko.update_permission", value = "true"),
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
            @ApiResponse(code = 201, message = "Successfully created",    response = BlockoBlock.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
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
            @ApiResponse(code = 200, message = "Ok Result",               response = BlockoBlock.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Block(@ApiParam(value = "blocko_block_id String path",   required = true)  String blocko_block_id){
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
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion_read_permission", value = BlockoBlockVersion.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = BlockoBlockVersion.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
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
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlock_read_permission", value = BlockoBlock.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlock_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = BlockoBlock.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result getBlockoBlock(@ApiParam(value = "blocko_block_id String path",   required = true) String blocko_block_id){
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

    @ApiOperation(value = "get BlockoBlock by Filter",
            tags = {"Blocko-Block"},
            notes = "get BlockoBlock List",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlock_read_permission", value = "No need to check permission, because Tyrion returns only those results which user owns"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Blocko_Block_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Blocko_Block_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_BlockoBlock_by_Filter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) Integer page_number){
        try {

            // Získání JSON
            final Form<Swagger_Blocko_Block_Filter> form = Form.form(Swagger_Blocko_Block_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Blocko_Block_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<BlockoBlock> query = Ebean.find(BlockoBlock.class);
            query.where().eq("author.id", SecurityController.getPerson().id);

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if(help.project_id != null){

                query.where().eq("type_of_block.project.id", help.project_id);
            }

            // Vytvoření odchozího JSON
            Swagger_Blocko_Block_List result = new Swagger_Blocko_Block_List(query, page_number);

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            e.printStackTrace();
            return GlobalResult.internalServerError();
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result deleteBlock(@ApiParam(value = "blocko_block_id String path",   required = true)  String blocko_block_id){
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
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
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion_creat_permission", value = BlockoBlockVersion.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.update_permission", value = "true"),
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
            @ApiResponse(code = 201, message = "Successfully created",    response = BlockoBlockVersion.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_BlockoBlock_Version(@ApiParam(value = "blocko_block_id String path",   required = true) String blocko_block_id){
        try {

            // Zpracování Json
            final Form<Swagger_BlockoBlock_BlockoVersion_New> form = Form.form(Swagger_BlockoBlock_BlockoVersion_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_New help = form.get();

            // Kontrola objektu
            BlockoBlock blockoBlock = BlockoBlock.find.byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.notFoundObject("blockoBlock not found");

            // Vytvoření objektu
            BlockoBlockVersion version = new BlockoBlockVersion();
            version.date_of_create = new Date();

            version.version_name = help.version_name;
            version.version_description = help.version_description;
            version.design_json = help.design_json;
            version.logic_json = help.logic_json;
            version.blocko_block = blockoBlock;

            // Kontrola oprávnění
            if (! version.create_permission()) return GlobalResult.forbidden_Permission();

            // Uložení objektu
            version.save();

            // Vrácení objektu
            return GlobalResult.created(Json.toJson(blockoBlock));

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
            @ApiResponse(code = 200, message = "Ok Result",               response = BlockoBlockVersion.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
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

            // Kontrola objektu
            BlockoBlockVersion version = BlockoBlockVersion.find.byId(blocko_block_version_id);
            if(version == null) return GlobalResult.notFoundObject("blocko_block_version_id not found");

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
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion_readd_permission", value = BlockoBlockVersion.read_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.read_permission", value = "true"),
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
            @ApiResponse(code = 200, message = "Ok Result",               response = BlockoBlockVersion.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
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
            return GlobalResult.result_ok(Json.toJson(blockoBlock.blocko_versions));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


// BOARD ###################################################################################################################*/


// ACTUALIZATION PROCEDUES ###################################################################################################################*/


}
