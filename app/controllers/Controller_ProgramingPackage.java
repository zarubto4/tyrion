package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.*;
import models.blocko.Model_BlockoBlock;
import models.blocko.Model_BlockoBlockVersion;
import models.blocko.Model_TypeOfBlock;
import models.compiler.Model_Board;
import models.compiler.Model_FileRecord;
import models.compiler.Model_TypeOfBoard;
import models.compiler.Model_VersionObject;
import models.notification.Model_Notification;
import models.person.Model_Invitation;
import models.person.Model_Person;
import models.project.b_program.Model_BPair;
import models.project.b_program.Model_BProgram;
import models.project.b_program.Model_BProgramHwGroup;
import models.project.b_program.instnace.Model_HomerInstance;
import models.project.b_program.instnace.Model_HomerInstanceRecord;
import models.project.b_program.servers.Model_HomerServer;
import models.project.c_program.Model_CProgram;
import models.project.global.Model_Product;
import models.project.global.Model_Project;
import models.project.global.Model_ProjectParticipant;
import models.project.m_program.Model_MProject;
import models.project.m_program.Model_MProjectProgramSnapShot;
import play.api.libs.mailer.MailerClient;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.emails.EmailTool;
import utilities.enums.Approval_state;
import utilities.enums.CLoud_Homer_Server_Type;
import utilities.enums.Participant_status;
import utilities.enums.Type_of_command;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_API;
import utilities.loginEntities.Secured_Admin;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Filter_List.Swagger_B_Program_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_Blocko_Block_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_Type_Of_Block_List;
import utilities.swagger.outboundClass.Swagger_B_Program_Version;
import utilities.swagger.outboundClass.Swagger_BlockoBlock_Version_scheme;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_ProgramingPackage extends Controller {

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
            @ApiResponse(code = 201, message = "Successfully created", response =  Model_Project.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_create() {
        try{

            // Zpracování Json
            final Form<Swagger_Project_New> form = Form.form(Swagger_Project_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Project_New help = form.get();

            System.out.println("product_id: " + help.product_id);

            Model_Product product = Model_Product.find.byId(help.product_id);
            if(product == null){return GlobalResult.notFoundObject("Product not found");}
            if(!product.create_new_project()) return GlobalResult.result_BadRequest(product.create_new_project_if_not());

            // Vytvoření objektu
            Model_Project project  = new Model_Project();
            project.name = help.project_name;
            project.description = help.project_description;
            project.product = product;

            // Kontrola oprávnění těsně před uložením
            if (!project.create_permission())  return GlobalResult.forbidden_Permission();


            // Uložení objektu
            project.save();

            project.refresh();

            Model_ProjectParticipant participant = new Model_ProjectParticipant();
            participant.person = product.payment_details.person;
            participant.project = project;
            participant.state = Participant_status.owner;

            participant.save();

            project.refresh();

            // Vrácení objektu
            return GlobalResult.created( Json.toJson(project) );


        } catch (Exception e) {
            e.printStackTrace();
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
            @ApiResponse(code = 200, message = "Ok Result", response =  Model_Project.class, responseContainer = "list"),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result project_getByUser(){
        try {

            // Získání seznamu
            List<Model_Project> projects = Model_Project.find.where().eq("participants.person.id", Controller_Security.getPerson().id).eq("product.active", true).order().asc("name").findList();

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
                            @ExtensionProperty(name = "Project.read_permission", value = Model_Project.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Project_read" ),
                            @ExtensionProperty(name = "Dynamic Permission key", value = "Project_read.{project_id}"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Model_Project.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result project_get(@ApiParam(value = "project_id String path", required = true)  String project_id){
        try {

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (!project.read_permission())   return GlobalResult.forbidden_Permission();

            /*
            Swagger_Project_Individual_DashBoard object = new Swagger_Project_Individual_DashBoard();
            object.project = project;

            // TODO doplňovat Widgety dle libosti!!
            object.widget.add( Becki_Widget_Generator.create_A_Type_Widget("Total Participants", null, project.participants().size(), Becki_color.byzance_blue, "fa-users" ));
            object.widget.add( Becki_Widget_Generator.create_A_Type_Widget("Instances in cloud", null, Homer_Instance.find.where().eq("b_program.project.id", project.id).isNotNull("actual_instance").findRowCount(), Becki_color.byzance_pink, "fa-cloud-upload"));

            */

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
    public Result project_delete(@ApiParam(value = "project_id String path", required = true) String project_id){
        try {

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (!project.delete_permission())   return GlobalResult.forbidden_Permission();

           // Kvuli bezpečnosti abych nesmazal něco co nechceme
           for(Model_CProgram c : project.c_programs){ c.delete();}


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
                            dataType = "utilities.swagger.documentationClass.Swagger_Project_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Model_Project.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_update(@ApiParam(value = "project_id String path", required = true)  String project_id){
        try {

            // Zpracování Json
            final Form<Swagger_Project_Edit> form = Form.form(Swagger_Project_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Project_Edit help = form.get();

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (!project.edit_permission() )   return GlobalResult.forbidden_Permission();

            // Úprava objektu
            project.name = help.project_name;
            project.description = help.project_description;

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
            @ApiResponse(code = 200, message = "Ok Result", response = Model_Project.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_invite(@ApiParam(value = "project_id String path", required = true)  String project_id){
        try {

            // Zpracování Json
            final Form<Swagger_ShareProject_Person> form = Form.form(Swagger_ShareProject_Person.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_ShareProject_Person help = form.get();

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (!project.share_permission() )   return GlobalResult.forbidden_Permission();

            // Získání seznamu uživatelů, kteří jsou registrovaní(listIn) a kteří ne(listOut)
            List<Model_Person> listIn = new ArrayList<>();
            List<String> toRemove = new ArrayList<>();

            // Roztřídění seznamů
            for (String mail : help.persons_mail){
                Model_Person person =  Model_Person.find.where().eq("mail",mail).findUnique();
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

                Model_Invitation invitation = Model_Invitation.find.where().eq("mail", mail).eq("project.id", project.id).findUnique();
                if(invitation == null){
                    invitation = new Model_Invitation();
                    invitation.mail = mail;
                    invitation.date_of_creation = new Date();
                    invitation.owner = Controller_Security.getPerson();
                    invitation.project = project;
                    invitation.save();
                }

                String link = Server.becki_invitationToCollaborate + "/" + mail;

                // Odeslání emailu s linkem pro registraci
                try {
                             new EmailTool()
                            .addEmptyLineSpace()
                            .startParagraph("13")
                            .addText("User ")
                            .addBoldText(Controller_Security.getPerson().full_name)
                            .addText(" invites you to collaborate on the project ")
                            .addBoldText(project.name + ". ")
                            .addText("If you would like to participate in it, please click on the link below and register yourself. ")
                            .endParagraph()
                            .addEmptyLineSpace()
                            .addSeparatorLine()
                            .addEmptyLineSpace()
                            .addLink(link,"Click here to collaborate","18")
                            .addEmptyLineSpace()
                            .sendEmail(mail, "Invitation to Collaborate" );


                } catch (Exception e) {
                    logger.error ("Sending mail -> critical error", e);
                    e.printStackTrace();
                }
            }

            for (Model_Person person : listIn) {

                Model_Invitation invitation = Model_Invitation.find.where().eq("mail", person.mail).eq("project.id", project.id).findUnique();
                if(invitation == null){
                    invitation = new Model_Invitation();
                    invitation.mail = person.mail;
                    invitation.date_of_creation = new Date();
                    invitation.owner = Controller_Security.getPerson();
                    invitation.project = project;
                    invitation.save();
                }

                project.notification_project_invitation(person, invitation);
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
            @ApiResponse(code = 200, message = "Ok Result",            response = Model_Project.class),
            @ApiResponse(code = 400, message = "Objects not found",    response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result project_addParticipant(String id, boolean decision ){
        try{

            // Kontroly objektů
            Model_Invitation invitation = Model_Invitation.find.byId(id);
            if(invitation == null) return GlobalResult.notFoundObject("Invitation no longer exists");

            Model_Person person = Model_Person.find.where().eq("mail", invitation.mail).findUnique();
            if(person == null) return GlobalResult.notFoundObject("Person does not exist");

            Model_Project project = invitation.project;
            if(project == null) return GlobalResult.notFoundObject("Project no longer exists");

            if ((Model_ProjectParticipant.find.where().eq("person.id", person.id).eq("project.id", project.id).findUnique() == null)&&(decision)) {

                Model_ProjectParticipant participant = new Model_ProjectParticipant();
                participant.person = person;
                participant.project = project;
                participant.state = Participant_status.member;

                participant.save();
            }

            // Odeslání notifikace podle rozhodnutí uživatele
            if(!decision){
                project.notification_project_invitation_rejected(invitation.owner);
            }else{
                project.notification_project_invitation_accepted(invitation.owner);
            }

            Model_Notification notification = null;
            if(invitation.notification_id != null)
                notification = Model_Notification.find.byId(invitation.notification_id);
            if(notification != null) notification.confirm();

            // Smazání pozvánky
            invitation.delete();

            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "change participant status",
            tags = {"Project"},
            notes = "Changes participant status ",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Project_Participant_status",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_ProjectParticipant.class),
            @ApiResponse(code = 400, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_changeParticipantStatus(@ApiParam(value = "project_id String path", required = true)  String project_id){
        try{

            // Zpracování Json
            final Form<Swagger_Project_Participant_status> form = Form.form(Swagger_Project_Participant_status.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Project_Participant_status help = form.get();

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject("Project no longer exists");

            // Kontrola oprávnění
            if (!project.admin_permission()) return GlobalResult.forbidden_Permission();

            // Kontrola objektu
            Model_ProjectParticipant participant = Model_ProjectParticipant.find.where().eq("person.id", help.person_id).eq("project.id", project_id).findUnique();
            if (participant == null) return GlobalResult.notFoundObject("Participant not found");

            // Uložení změn
            participant.state = help.state;
            participant.update();

            // Odeslání notifikace uživateli
            project.notification_project_participant_change_status(participant);

            return GlobalResult.result_ok(Json.toJson(participant));
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "unshare Project with Persons",
            tags = {"Project"},
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
            @ApiResponse(code = 200, message = "Ok Result",                                 response = Model_Project.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",                      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",                  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_removeParticipant(@ApiParam(value = "project_id String path", required = true) String project_id){
        try {

            // Zpracování Json
            final Form<Swagger_ShareProject_Person> form = Form.form(Swagger_ShareProject_Person.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_ShareProject_Person help = form.get();

            //Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (!project.unshare_permission() )   return GlobalResult.forbidden_Permission();

            List<Model_Person> list = new ArrayList<>();

            // Získání seznamu
            for (String mail : help.persons_mail){

                Model_Person person = Model_Person.find.where().eq("mail",mail).findUnique();
                if(person != null)
                    list.add(person);
            }

            for (Model_Person person : list) {
                Model_ProjectParticipant participant = Model_ProjectParticipant.find.where().eq("person.id", person.id).eq("project.id", project.id).findUnique();
                if (participant != null) {

                    // Úprava objektu
                    participant.delete();
                }
            }

            // Obnovení v DB
            project.refresh();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(project));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Invitation into Project",
            tags = {"Project"},
            notes = "Deletes invitation into the Project, also deletes notification about this invitation.",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                                 response = Result_ok.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",                      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result project_deleteInvitation(@ApiParam(value = "invitation_id String path", required = true)  String invitation_id){
        try {

            Model_Invitation invitation = Model_Invitation.find.where().eq("owner.id", Controller_Security.getPerson().id).eq("id", invitation_id).findUnique();
            if(invitation == null) return GlobalResult.notFoundObject("Invitation does not exist");

            Model_Notification notification = null;
            if(invitation.notification_id != null)
                notification = Model_Notification.find.byId(invitation.notification_id);
            if(notification != null) notification.delete();

            invitation.delete();

            return GlobalResult.result_ok("Invitation successfully deleted.");

        }catch (Exception e){
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
            @ApiResponse(code = 201, message = "Successfully created", response =  Model_BProgram.class),
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
            Model_Project project = Model_Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (!project.update_permission() ) return GlobalResult.forbidden_Permission();

            // Tvorba programu
            Model_BProgram b_program             = new Model_BProgram();
            b_program.date_of_create        = new Date();
            b_program.description           = help.description;
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
            @ApiResponse(code = 200, message = "Ok Result", response =  Model_BProgram.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result get_b_Program(@ApiParam(value = "b_program_id String path", required = true)  String b_program_id){
        try{

            // Kontrola objektu
            Model_BProgram b_program = Model_BProgram.find.byId(b_program_id);
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
            @ApiResponse(code = 200, message = "Ok Result", response =  Swagger_B_Program_Version.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result get_b_Program_version(@ApiParam(value = "version_id String path", required = true)  String version_id){
        try{

            // Kontrola objektu
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Kontrola oprávnění
            if (version_object.b_program == null) return GlobalResult.notFoundObject("Version_Object is not version of B_Program");

            // Kontrola oprávnění
            if (! version_object.b_program.read_permission() ) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version_object.b_program.program_version(version_object)));

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
            @ApiResponse(code = 200, message = "Ok Result", response =  Model_BProgram.class),
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
            Model_BProgram b_program  = Model_BProgram.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Kontrola oprávěnní
            if (! b_program.edit_permission() ) return GlobalResult.forbidden_Permission();

            // Úprava objektu
            b_program.description           = help.description;
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
            notes = "edit Blocko program / new Version in B_Program object",
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
    public  Result update_b_program_new_version(@ApiParam(value = "b_program_id String path", required = true)  String b_program_id){
        try{

            // Zpracování Json
            final Form<Swagger_B_Program_Version_New> form = Form.form(Swagger_B_Program_Version_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_B_Program_Version_New help = form.get();

            // Program který budu ukládat do data Storage v Azure
            String file_content =  help.program;

            // Ověření programu
            Model_BProgram b_program = Model_BProgram.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Kontrola oprávnění
            if (! b_program.update_permission() ) return GlobalResult.forbidden_Permission();

            // První nová Verze
            Model_VersionObject version_object     = new Model_VersionObject();
            version_object.version_name            = help.version_name;
            version_object.version_description     = help.version_description;
            version_object.date_of_create          = new Date();
            version_object.b_program               = b_program;
            version_object.author                  = Controller_Security.getPerson();



            if(help.m_project_snapshots != null)
            for(Swagger_B_Program_Version_New.M_Project_SnapShot help_m_project_snap : help.m_project_snapshots){

                Model_MProject m_project = Model_MProject.find.byId(help_m_project_snap.m_project_id);
                if(m_project == null) return GlobalResult.notFoundObject("M_Project id not found");
                if(!m_project.update_permission()) return GlobalResult.forbidden_Permission();


                Model_MProjectProgramSnapShot snap = new Model_MProjectProgramSnapShot();
                snap.m_project = m_project;


                for(Swagger_B_Program_Version_New.M_Program_SnapShot help_m_program_snap : help_m_project_snap.m_program_snapshots){
                    Model_VersionObject m_program_version = Model_VersionObject.find.where().eq("id", help_m_program_snap.version_object_id ).eq("m_program.id", help_m_program_snap.m_program_id).eq("m_program.m_project.id", m_project.id).findUnique();
                    if(m_program_version == null) return GlobalResult.notFoundObject("M_Program Version id not found");
                    snap.version_objects_program.add(m_program_version);
                }


                version_object.b_program_version_snapshots.add(snap);
            }


            logger.debug("Controller_ProgramingPackage:: update_b_program_new_version:: Saving Snap");
            for(Model_MProjectProgramSnapShot snap : version_object.b_program_version_snapshots){
                snap.save();
            }


            // Definování main Board
            for( Swagger_B_Program_Version_New.Hardware_group group : help.hardware_group) {

                Model_BProgramHwGroup b_program_hw_group = new Model_BProgramHwGroup();

                // Definuji Main Board - Tedy yodu pokud v Json přišel (není podmínkou)
                if(group.main_board_pair != null) {

                    Model_BPair b_pair = new Model_BPair();

                    b_pair.board = Model_Board.find.byId(group.main_board_pair.board_id);
                    if ( b_pair.board == null) return GlobalResult.notFoundObject("Board board_id not found");
                    if (!b_pair.board.type_of_board.connectible_to_internet)  return GlobalResult.result_BadRequest("Main Board must be internet connectible!");
                    if(!b_pair.board.update_permission()) return GlobalResult.forbidden_Permission();

                    b_pair.c_program_version = Model_VersionObject.find.byId(group.main_board_pair.c_program_version_id);
                    if ( b_pair.c_program_version == null) return GlobalResult.notFoundObject("C_Program Version_Object c_program_version_id not found");
                    if ( b_pair.c_program_version.c_program == null)  return GlobalResult.result_BadRequest("Version is not from C_Program");


                    if( Model_TypeOfBoard.find.where().eq("c_programs.id",  b_pair.c_program_version.c_program.id ).where().eq("boards.id",  b_pair.board.id).findRowCount() < 1){
                        return GlobalResult.result_BadRequest("You want upload C++ program version id: " +  b_pair.c_program_version.id + " thats not compatible with hardware " + b_pair.board.id);
                    }

                    b_program_hw_group.main_board_pair = b_pair;

                }

                // Definuji Devices - Tedy yodu pokud v Json přišly (není podmínkou)

                if(group.device_board_pairs != null) {

                    for(Swagger_B_Program_Version_New.Connected_Board connected_board : group.device_board_pairs ){

                        Model_BPair b_pair = new Model_BPair();

                        b_pair.board = Model_Board.find.byId(connected_board.board_id);
                        if ( b_pair.board == null) return GlobalResult.notFoundObject("Board board_id not found");
                        if(!b_pair.board.update_permission()) return GlobalResult.forbidden_Permission();


                        b_pair.c_program_version = Model_VersionObject.find.byId(connected_board.c_program_version_id);
                        if ( b_pair.c_program_version == null) return GlobalResult.notFoundObject("C_Program Version_Object c_program_version_id not found");
                        if ( b_pair.c_program_version.c_program == null)  return GlobalResult.result_BadRequest("Version is not from C_Program");

                        if( Model_TypeOfBoard.find.where().eq("c_programs.id",  b_pair.c_program_version.c_program.id ).where().eq("boards.id",  b_pair.board.id).findRowCount() < 1){
                            return GlobalResult.result_BadRequest("You want upload C++ program version id: " +  b_pair.c_program_version.id + " thats not compatible with hardware " + b_pair.board.id);
                        }

                        b_program_hw_group.device_board_pairs.add(b_pair);
                    }
                }
                version_object.b_program_hw_groups.add(b_program_hw_group);
            }

            logger.debug("Controller_ProgramingPackage:: update_b_program_new_version:: Saving b_program_hw_group");
            for(Model_BProgramHwGroup b_program_hw_group : version_object.b_program_hw_groups){

                b_program_hw_group.main_board_pair.save();

                for(Model_BPair b_pair : b_program_hw_group.device_board_pairs){
                    b_pair.save();
                }

                b_program_hw_group.save();
            }

            logger.debug("Controller_ProgramingPackage:: update_b_program_new_version:: Saving version");
            // Uložení objektu
            version_object.save();


            // Úprava objektu
            b_program.getVersion_objects().add(version_object);

            logger.debug("Controller_ProgramingPackage:: update_b_program_new_version:: Updating b_program");
            // Uložení objektu
            b_program.update();

            // Update verze
            version_object.refresh();

            // Nahrání na Azure
             Model_FileRecord.uploadAzure_Version(file_content, "program.js", b_program.get_path() , version_object);

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson( version_object.b_program.program_version(version_object) ));

        } catch (Exception e) {
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
            Model_BProgram program  = Model_BProgram.find.byId(b_program_id);
            if (program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Kontrola oprávění
            if (! program.delete_permission() ) return GlobalResult.forbidden_Permission();

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
            Model_VersionObject version_object  = Model_VersionObject.find.byId(version_id);

            // Kontrola objektu
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object id not found");
            if (version_object.b_program == null) return GlobalResult.result_BadRequest("B_Program not found");

            // Kontrola oprávnění
            if (! version_object.b_program.delete_permission() ) return GlobalResult.forbidden_Permission();

            // Smazání objektu
            version_object.removed_by_user = true;
            version_object.update();

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
    public Result get_b_Program_by_Filter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number){
        try {

            // Získání JSON
            final Form<Swagger_B_Program_Filter> form = Form.form(Swagger_B_Program_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_B_Program_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_BProgram> query = Ebean.find(Model_BProgram.class);
            query.where().eq("project.participants.person.id", Controller_Security.getPerson().id);

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if(help.project_id != null){

                query.where().eq("project.id", help.project_id);
            }

            // Vytvoření odchozího JSON
            Swagger_B_Program_List result = new Swagger_B_Program_List(query, page_number);

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
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
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_B_Program_Upload_Instance",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
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
    public  Result upload_b_Program_ToCloud(@ApiParam(value = "version_id String path", required = true) String version_id){
        try {

            // Získání JSON
            final Form<Swagger_B_Program_Upload_Instance> form = Form.form(Swagger_B_Program_Upload_Instance.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_B_Program_Upload_Instance help = form.get();

            // Kontrola objektu: Verze B programu kterou budu nahrávat do cloudu
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Kontrola objektu: B program, který chci nahrát do Cloudu na Blocko cloud_blocko_server
            if (version_object.b_program == null) return GlobalResult.result_BadRequest("Version_Object is not version of B_Program");
            Model_BProgram b_program = version_object.b_program;

            // Kontrola oprávnění
            if (! b_program.update_permission() ) return GlobalResult.forbidden_Permission();



            if(b_program.instance.actual_instance != null && b_program.instance.actual_instance.version_object.id.equals(version_object.id)) return GlobalResult.result_BadRequest("This Version is already in Cloud!");


            Model_HomerInstanceRecord record = new Model_HomerInstanceRecord();
            record.main_instance_history = b_program.instance;
            record.version_object = version_object;
            record.date_of_created = new Date();

            if(help.upload_time != null) {
                
                // Zkontroluji smysluplnost časvé známky
                if (!help.upload_time.after(new Date()))  return GlobalResult.result_BadRequest("time must be set in the future");
                record.planed_when = help.upload_time;

                b_program.instance.actual_instance =null;
                b_program.instance.update();

                record.actual_running_instance = b_program.instance;

            } else record.running_from = new Date();

            record.save();

            // Kontrola HW
            if(version_object.b_program_hw_groups != null) {

                logger.trace("Upload version to cloud contains Hardware!");
                for(Model_BProgramHwGroup group : version_object.b_program_hw_groups){

                    // Kontrola Yody
                    if(group.main_board_pair != null ) {

                        Model_Board yoda = group.main_board_pair.board;

                        //1. Pokud už běží v jiné instanci mimo vlastní dočasnou instnaci
                        if (yoda.virtual_instance_under_project != null) {
                            yoda.virtual_instance_under_project.remove_board_from_virtual_instance(yoda);
                        }

                        if(group.device_board_pairs != null) {
                            //1.

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


            //Určím podle časové konstanty zda nahraju hned nebo až za chvíli
            if(record.planed_when != null) return GlobalResult.result_ok();

            if( b_program.instance.actual_instance != null) {
                b_program.instance.actual_instance.actual_running_instance = null;
                b_program.instance.actual_instance.update();
            }

            b_program.instance.actual_instance = record;
            record.actual_running_instance = b_program.instance;
            record.update();
            b_program.instance.update();

            Thread upload_instance = new Thread() {
                @Override
                public void run() {

                    try {

                        // Ověřím připojený server
                        if (!Controller_WebSocket.homer_servers.containsKey(b_program.instance.cloud_homer_server.unique_identificator)) {
                            b_program.instance.notification_instance_unsuccessful_upload("Server is offline now. It will be uploaded as soon as possible");
                            logger.warn("Server je offline!! Nenahraju instanci!!");
                            return;
                        }

                        // Server je připojený
                        try {


                            if (!record.main_instance_history.instance_online()) {
                                 record.main_instance_history.add_instance_to_server();
                            } else {
                                 record.main_instance_history.update_instance_to_actual_instance_record();
                            }

                        } catch (Exception e) {
                            logger.error("Error while cloud_compilation_server tried compile version of C_program", e);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };

            upload_instance.start();
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "shutDown Instance by Instnace Id",
            tags = {"Instance"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful removed",                        response = Result_ok.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",                      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",                  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result instance_shut_down(String instance_name){
        try{

            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name", instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            if (!homer_instance.getB_program().update_permission() ) return GlobalResult.forbidden_Permission();

            JsonNode result = homer_instance.remove_instance_from_server();

            if(result.has("status") && result.get("status").asText().equals("success")) return GlobalResult.result_ok();
            return GlobalResult.result_BadRequest(result);

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Instance by Project ID",
            tags = {"Instance"},
            notes = "get unique instance under Blocko program (now its 1:1) we are not supporting multi-instnace schema yet",
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
            @ApiResponse(code = 200, message = "Successful Uploaded",                       response = Model_HomerInstance.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",                      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",                  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_b_program_instance_under_project(String project_id){
        try{


            List<Model_HomerInstance> instances = Model_HomerInstance.find.where()
                                     .isNotNull("actual_instance")
                                     .eq("b_program.project.id", project_id)
                                     .findList();


            return GlobalResult.result_ok(Json.toJson(instances));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Instance by instance_id",
            tags = {"Instance"},
            notes = "get unique instance under Blocko program (now its 1:1) we are not supporting multi-instnace schema yet",
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
            @ApiResponse(code = 200, message = "Successful Uploaded",                       response = Model_HomerInstance.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",                      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",                  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_b_program_instance(String instance_id){
        try{


            Model_HomerInstance instance = Model_HomerInstance.find.byId(instance_id);
            if (instance == null) return GlobalResult.notFoundObject("Homer_Instance instance_id not found");
            if(instance.getB_program() == null ) return GlobalResult.notFoundObject("Homer_Instance is virtual!!");

            if(!instance.getB_program().read_permission()) return GlobalResult.forbidden_Permission();

            return GlobalResult.result_ok(Json.toJson(instance));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }



    @ApiOperation(value = "Only for Tyrion Front End",  hidden = true)
    public Result instance_add_temporary_instance(){
        try{

            // Zpracování Json
            final Form<Swagger_Instance_Temporary> form = Form.form(Swagger_Instance_Temporary.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Instance_Temporary help = form.get();

            Model_HomerServer server = Model_HomerServer.find.where().eq("unique_identificator", help.unique_identificator).findUnique();
            if(server == null) return GlobalResult.notFoundObject("Server not found");

            JsonNode result = server.add_temporary_instance(help.instance_name);

            if(result.get("status").asText().equals("success")) return GlobalResult.result_ok(result);
            else return GlobalResult.result_BadRequest(result);

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

            JsonNode json = Json.parse(help.code);
            System.out.println(json.toString());

            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name",instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            if(!homer_instance.instance_online()) return GlobalResult.notFoundObject("Homer_Instance is not online");

            JsonNode result = homer_instance.upload_blocko_program("fake_program", help.code );

            if(result.has("status") && result.get("status").asText().equals("success")) return GlobalResult.result_ok();
            return GlobalResult.result_BadRequest(result);

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


    @ApiOperation(value = "Only for Tyrion Front End",  hidden = true)
    public Result ping_instance(String instance_name){
        try{
            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name",instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            if(!homer_instance.instance_online()) return GlobalResult.notFoundObject("Homer_Instance on Tyrion is not online");


            JsonNode result = homer_instance.ping();

            if(result.has("status") && result.get("status").asText().equals("success")) return GlobalResult.result_ok();
            return GlobalResult.result_BadRequest(result);
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion Front End",  hidden = true)
    public Result instance_add_yoda(String instance_name, String yoda_id){
        try{

            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name", instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            if (!homer_instance.instance_online()) return GlobalResult.notFoundObject("Homer_Instance on Tyrion is not online");

            JsonNode result = homer_instance.add_Yoda_to_instance( yoda_id);

            if(result.has("status") && result.get("status").asText().equals("success")) return GlobalResult.result_ok();
            return GlobalResult.result_BadRequest(result);

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion Front End",  hidden = true)
    public Result instance_remove_yoda(String instance_name, String yoda_id){
        try{

            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name", instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            if (!homer_instance.instance_online()) return GlobalResult.notFoundObject("Homer_Instance on Tyrion is not online");

            JsonNode result = homer_instance.remove_Yoda_from_instance(yoda_id);


            if(result.has("status") && result.get("status").asText().equals("success")) return GlobalResult.result_ok();
            return GlobalResult.result_BadRequest(result);

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion Front End",  hidden = true)
    public Result instance_add_device(String instance_name, String yoda_id, String device_id){
        try{

            // Transformace na seznam
            List<String> list_of_devices = new ArrayList<>();
            list_of_devices.add(device_id);

            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name", instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            if (!homer_instance.instance_online()) return GlobalResult.notFoundObject("Homer_Instance on Tyrion is not online");


            JsonNode result = homer_instance.add_Device_to_instance(yoda_id, list_of_devices);

            if(result.has("status") && result.get("status").asText().equals("success")) return GlobalResult.result_ok();
            return GlobalResult.result_BadRequest(result);

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion Front End",  hidden = true)
    public Result instance_remove_device(String instance_name, String yoda_id, String device_id){
        try{
            // Transformace na seznam
            List<String> list_of_devices = new ArrayList<>();
            list_of_devices.add(device_id);

            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name", instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            if (!homer_instance.instance_online()) return GlobalResult.notFoundObject("Homer_Instance on Tyrion is not online");


            JsonNode result = homer_instance.remove_Device_from_instance(yoda_id, list_of_devices);

            if(result.has("status") && result.get("status").asText().equals("success")) return GlobalResult.result_ok();
            return GlobalResult.result_BadRequest(result);

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }








    @ApiOperation(value = "send command to instance", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result send_command_to_instance(String instance_name, String target_id, String string_command){
        try{

            // Kontrola oprávnění
            Model_Board board = Model_Board.find.byId(target_id);
            if (board == null) return GlobalResult.notFoundObject("Board targetId not found");

            // Kontrola objektu
            Type_of_command command = Type_of_command.getTypeCommand(string_command);
            if(command == null) return GlobalResult.notFoundObject("Command not found!");

            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name", instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            JsonNode result = homer_instance.devices_commands(target_id, command);

            if(result.has("status") && result.get("status").asText().equals("success")) return GlobalResult.result_ok();
            return GlobalResult.result_BadRequest(result);

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

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
                            @ExtensionProperty(name = "Cloud_Homer_Server.create_permission", value = Model_HomerServer.create_permission_docs ),
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
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_HomerServer.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result create_Homer_Server(){
        try{

            // Zpracování Json
            final Form<Swagger_Cloud_Homer_Server_New> form = Form.form(Swagger_Cloud_Homer_Server_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Cloud_Homer_Server_New help = form.get();

            // Vytvoření objektu
            Model_HomerServer server = new Model_HomerServer();
            server.personal_server_name = help.personal_server_name;
            server.server_type = CLoud_Homer_Server_Type.public_server;

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
            @ApiResponse(code = 200, message = "Updated successfully",    response = Model_HomerServer.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Homer_Server(@ApiParam(value = "server_id ", required = true) String server_id ){
        try{

            // Zpracování Json
            final Form<Swagger_Cloud_Homer_Server_New> form = Form.form(Swagger_Cloud_Homer_Server_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Cloud_Homer_Server_New help = form.get();

            // Kontrola objektu
            Model_HomerServer server = Model_HomerServer.find.byId(server_id);
            if (server == null) return GlobalResult.notFoundObject("Cloud_Blocko_Server server_id not found");

            // Kontrola oprávnění
            if(!server.edit_permission()) return GlobalResult.forbidden_Permission();

            // Úprava objektu
            server.personal_server_name = help.personal_server_name;

            server.mqtt_port = help.mqtt_port;
            server.mqtt_password = help.mqtt_password;
            server.mqtt_username = help.mqtt_username;

            server.grid_port = help.grid_port;

            server.webView_port = help.webView_port;

            server.server_url = help.server_url;

            // Uložení objektu
            server.update();


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
                            @ExtensionProperty(name = "Cloud_Homer_Server.read_permission", value = Model_HomerServer.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Cloud_Homer_Server.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Cloud_Homer_Server_read")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Model_HomerServer.class, responseContainer = "List "),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_All_Homer_Server(){
        try{

            // Získání seznamu
            List<Model_HomerServer> servers = Model_HomerServer.find.all();

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
    public Result delete_Homer_Server(@ApiParam(value = "server_id ", required = true)  String server_id ){
        try{

            // Kontrola objektu
            Model_HomerServer server = Model_HomerServer.find.byId(server_id);
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
            tags = {"Type-of-Block"},
            notes = "creating group for BlockoBlocks -> Type of block",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "TypeOfBlock_create_permission", value = Model_TypeOfBlock.create_permission_docs ),
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
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_TypeOfBlock.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBlock_create(){
        try{

            // Zpracování Json
            final Form<Swagger_TypeOfBlock_New> form = Form.form(Swagger_TypeOfBlock_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBlock_New help = form.get();

            if(Model_TypeOfBlock.find.where().isNull("project").eq("name",help.name).findUnique() != null)
                return GlobalResult.result_BadRequest("Type of Block with this name already exists, type a new one.");

            // Vytvoření objektu
            Model_TypeOfBlock typeOfBlock = new Model_TypeOfBlock();
            typeOfBlock.description = help.description;
            typeOfBlock.name                = help.name;

            // Nejedná se o privátní Typ Bločku
            if(help.project_id != null){

                // Kontrola objektu
                Model_Project project = Model_Project.find.byId(help.project_id);
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
            tags = {"Type-of-Block"},
            notes = "get BlockoBlock ",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "TypeOfBlock_read_permission", value = Model_TypeOfBlock.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project == null - Public TypeOfBlock", value = "Permission not Required!"),
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfBlock_create_permission" )
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_TypeOfBlock.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result typeOfBlock_get(@ApiParam(value = "type_of_block_id String path",   required = true)  String type_of_block_id){
        try {

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.find.byId(type_of_block_id);
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
            tags = {"Type-of-Block"},
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
            @ApiResponse(code = 200, message = "Ok Result",               response =  Model_TypeOfBlock.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBlock_update(@ApiParam(value = "type_of_block_id String path",   required = true)  String type_of_block_id){
        try{

            // Zpracování Json
            final Form<Swagger_TypeOfBlock_New> form = Form.form(Swagger_TypeOfBlock_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBlock_New help = form.get();

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.find.byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock type_of_block_id not found");

            // Kontrola oprávnění
            if (! typeOfBlock.edit_permission() ) return GlobalResult.forbidden_Permission();

            // Úprava objektu
            typeOfBlock.description = help.description;
            typeOfBlock.name        = help.name;

            if(help.project_id != null){

                // Kontrola objektu
                Model_Project project = Model_Project.find.byId(help.project_id);
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
            tags = {"Type-of-Block"},
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
    public Result typeOfBlock_delete(@ApiParam(value = "type_of_block_id String path",   required = true)  String type_of_block_id){
        try{

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.find.byId(type_of_block_id);
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
            tags = {"Type-of-Block"},
            notes = "get all groups for BlockoBlocks -> Type of block",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response =  Model_TypeOfBlock.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result typeOfBlock_getAll(){
        try {

            // Získání seznamu
            List<Model_TypeOfBlock> typeOfBlocks = Model_TypeOfBlock.find.where().isNull("project").findList();
            typeOfBlocks.addAll( Model_TypeOfBlock.find.where().eq("project.participants.person.id", Controller_Security.getPerson().id ).findList() );

            // Kontrola oprávnění
            for(Model_TypeOfBlock typeOfBlock :typeOfBlocks ) if(! typeOfBlock.read_permission())  return GlobalResult.forbidden_Permission();

            // Vrácení seznamu
            return GlobalResult.result_ok(Json.toJson(typeOfBlocks));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get TypeOfBlock by Filter",
            tags = {"Type-of-Block"},
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
    public Result typeOfBlock_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number){
        try {

            // Získání JSON
            final Form<Swagger_Type_Of_Block_Filter> form = Form.form(Swagger_Type_Of_Block_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Type_Of_Block_Filter help = form.get();

            // Získání všech objektů a následné odfiltrování soukormých TypeOfBlock
            Query<Model_TypeOfBlock> query = Ebean.find(Model_TypeOfBlock.class);

            if(help.private_type){
                query.where().eq("project.participants.person.id", Controller_Security.getPerson().id);
            }else{
                query.where().isNull("project");
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
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlock_create_permission", value = Model_BlockoBlock.create_permission_docs ),
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
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_BlockoBlock.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 400, message = "Something went wrong",    response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoBlock_create(){
       try{

           // Zpracování Json
           final Form<Swagger_BlockoBlock_New> form = Form.form(Swagger_BlockoBlock_New.class).bindFromRequest();
           if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
           Swagger_BlockoBlock_New help = form.get();

           if(Model_BlockoBlock.find.where().isNull("type_of_block.project").eq("name", help.name).findUnique()!= null)
               return GlobalResult.result_BadRequest("BlockoBlock with this name already exists, type a new one.");

           // Kontrola objektu
           Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.find.byId( help.type_of_block_id);
           if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock type_of_block_id not found");

           // Vytvoření objektu
           Model_BlockoBlock blockoBlock = new Model_BlockoBlock();

           blockoBlock.description = help.general_description;
           blockoBlock.name                = help.name;
           blockoBlock.author              = Controller_Security.getPerson();
           blockoBlock.type_of_block       = typeOfBlock;

           // Kontrola oprávnění těsně před uložením
           if (! blockoBlock.create_permission() ) return GlobalResult.forbidden_Permission();

           // Uložení objektu
           blockoBlock.save();

           // Získání šablony
           Model_BlockoBlockVersion scheme = Model_BlockoBlockVersion.find.where().eq("version_name", "version_scheme").findUnique();

           // Kontrola objektu
           if(scheme == null) return GlobalResult.created( Json.toJson(blockoBlock) );

           // Vytvoření objektu první verze
           Model_BlockoBlockVersion blockoBlockVersion = new Model_BlockoBlockVersion();
           blockoBlockVersion.version_name = "0.0.1";
           blockoBlockVersion.version_description = "This is a first version of block.";
           blockoBlockVersion.approval_state = Approval_state.approved;
           blockoBlockVersion.design_json = scheme.design_json;
           blockoBlockVersion.logic_json = scheme.logic_json;
           blockoBlockVersion.date_of_create = new Date();
           blockoBlockVersion.blocko_block = blockoBlock;
           blockoBlockVersion.save();

           // Vrácení objektu
           return GlobalResult.created( Json.toJson(blockoBlock) );

       } catch (Exception e) {
           return Loggy.result_internalServerError(e, request());
       }
    }

    @ApiOperation(value = "edit basic information of the BlockoBlock",
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
                            dataType = "utilities.swagger.documentationClass.Swagger_BlockoBlock_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_BlockoBlock.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoBlock_update(@ApiParam(value = "blocko_block_id String path",   required = true)  String blocko_block_id){
        try {

                // Zpracování Json
                final Form<Swagger_BlockoBlock_New> form = Form.form(Swagger_BlockoBlock_New.class).bindFromRequest();
                if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
                Swagger_BlockoBlock_New help = form.get();

                // Kontrola objektu
                Model_BlockoBlock blockoBlock = Model_BlockoBlock.find.byId(blocko_block_id);
                if (blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");

                // Kontrola oprávnění
                if (! blockoBlock.edit_permission() ) return GlobalResult.forbidden_Permission("You have no permission to edit");

                // Úprava objektu
                blockoBlock.description = help.general_description;
                blockoBlock.name        = help.name;

                // Kontrola objektu
                Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.find.byId(  help.type_of_block_id);
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
                            @ExtensionProperty(name = "BlockoBlockVersion_read_permission", value = Model_BlockoBlockVersion.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_BlockoBlockVersion.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result blockoBlockVersion_get(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_version_id){
        try {
                // Kontrola objektu
                Model_BlockoBlockVersion blocko_version = Model_BlockoBlockVersion.find.byId(blocko_block_version_id);
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
                            @ExtensionProperty(name = "BlockoBlock_read_permission", value = Model_BlockoBlock.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlock_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_BlockoBlock.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result blockoBlock_get(@ApiParam(value = "blocko_block_id String path",   required = true) String blocko_block_id){
        try {
            // Kontrola objektu
            Model_BlockoBlock blockoBlock = Model_BlockoBlock.find.byId(blocko_block_id);
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
    public Result blockoBlock_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number){
        try {

            // Získání JSON
            final Form<Swagger_Blocko_Block_Filter> form = Form.form(Swagger_Blocko_Block_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Blocko_Block_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_BlockoBlock> query = Ebean.find(Model_BlockoBlock.class);
            query.where().eq("author.id", Controller_Security.getPerson().id);

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
            return Loggy.result_internalServerError(e, request());
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
    public Result blockoBlock_delete(@ApiParam(value = "blocko_block_id String path",   required = true)  String blocko_block_id){
        try {

            // Kontrola objektu
            Model_BlockoBlock blockoBlock = Model_BlockoBlock.find.byId(blocko_block_id);
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
    public Result blockoBlockVersion_delete(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_version_id){
        try {

            // Kontrola objektu
            Model_BlockoBlockVersion version = Model_BlockoBlockVersion.find.byId(blocko_block_version_id);
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
                            @ExtensionProperty(name = "BlockoBlockVersion_create_permission", value = Model_BlockoBlockVersion.create_permission_docs ),
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
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_BlockoBlockVersion.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoBlockVersion_create(@ApiParam(value = "blocko_block_id String path",   required = true) String blocko_block_id){
        try {

            // Zpracování Json
            final Form<Swagger_BlockoBlock_BlockoVersion_New> form = Form.form(Swagger_BlockoBlock_BlockoVersion_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_New help = form.get();

            // Kontrola názvu
            if(help.version_name.equals("version_scheme")) return GlobalResult.result_BadRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockoBlock blockoBlock = Model_BlockoBlock.find.byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.notFoundObject("blockoBlock not found");

            // Vytvoření objektu
            Model_BlockoBlockVersion version = new Model_BlockoBlockVersion();
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_BlockoBlockVersion.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoBlockVersion_update(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_version_id){
        try {

            // Zpracování Json
            final Form<Swagger_BlockoBlock_BlockoVersion_Edit> form = Form.form(Swagger_BlockoBlock_BlockoVersion_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_Edit help = form.get();

            // Kontrola názvu
            if(help.version_name.equals("version_scheme")) return GlobalResult.result_BadRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockoBlockVersion version = Model_BlockoBlockVersion.find.byId(blocko_block_version_id);
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
                            @ExtensionProperty(name = "BlockoBlockVersion_read_permission", value = Model_BlockoBlockVersion.read_permission_docs),
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_BlockoBlockVersion.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result blockoBlockVersion_getAll(@ApiParam(value = "blocko_block_id String path",   required = true) String blocko_block_id){
        try {

            // Kontrola objektu
            Model_BlockoBlock blockoBlock = Model_BlockoBlock.find.byId(blocko_block_id);
            if (blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");

            // Kontrola oprávnění
            if (! blockoBlock.read_permission()) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(blockoBlock.blocko_versions));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "make BlockoBlock version public",
            tags = {"Blocko-Block"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion_edit_permission", value = "If user has BlockoBlock.update_permission"),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_edit_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_BlockoBlockVersion.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result blockoBlockVersion_makePublic(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_version_id){
        try{

            // Kontrola objektu
            Model_BlockoBlockVersion blockoBlockVersion = Model_BlockoBlockVersion.find.byId(blocko_block_version_id);
            if(blockoBlockVersion == null) return GlobalResult.notFoundObject("BlockoBlockVersion blocko_block_version_id not found");

            // Kontrola orávnění
            if(!(blockoBlockVersion.edit_permission())) return GlobalResult.forbidden_Permission();

            // Úprava objektu
            blockoBlockVersion.approval_state = Approval_state.pending;

            // Uložení změn
            blockoBlockVersion.update();

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(blockoBlockVersion));

        }catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

// BLOCKO ADMIN ########################################################################################################*/

    @Security.Authenticated(Secured_Admin.class)
    public Result blockoDisapprove(){
        try {

            // Získání JSON
            final Form<Swagger_BlockoObject_Approval> form = Form.form(Swagger_BlockoObject_Approval.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoObject_Approval help = form.get();

            // Kontrola objektu
            Model_BlockoBlockVersion blockoBlockVersion = Model_BlockoBlockVersion.find.byId(help.object_id);
            if (blockoBlockVersion == null) return GlobalResult.notFoundObject("blocko_block_version not found");

            // Změna stavu schválení
            blockoBlockVersion.approval_state = Approval_state.disapproved;

            // Odeslání emailu s důvodem
            try {
                new EmailTool()
                        .addEmptyLineSpace()
                        .startParagraph("13")
                        .addText("Version of Block " + blockoBlockVersion.blocko_block.name + ": ")
                        .addBoldText(blockoBlockVersion.version_name)
                        .addText(" was not approved for this reason: ")
                        .endParagraph()
                        .startParagraph("13")
                        .addText( help.reason)
                        .endParagraph()
                        .addEmptyLineSpace()
                        .sendEmail(blockoBlockVersion.blocko_block.author.mail, "Version of Block disapproved" );

            } catch (Exception e) {
                logger.error ("Sending mail -> critical error", e);
                e.printStackTrace();
            }

            // Uložení změn
            blockoBlockVersion.update();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());

        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result blockoApproval() {

        try {

            // Získání JSON
            final Form<Swagger_BlockoObject_Approve_withChanges> form = Form.form(Swagger_BlockoObject_Approve_withChanges.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoObject_Approve_withChanges help = form.get();

            // Kontrola názvu
            if(help.blocko_block_version_name.equals("version_scheme")) return GlobalResult.result_BadRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockoBlockVersion privateBlockoBlockVersion = Model_BlockoBlockVersion.find.byId(help.object_id);
            if (privateBlockoBlockVersion == null) return GlobalResult.notFoundObject("blocko_block_version not found");

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.find.byId(help.blocko_block_type_of_block_id);
            if (typeOfBlock == null) return GlobalResult.notFoundObject("type_of_block not found");

            // Vytvoření objektu
            Model_BlockoBlock blockoBlock = new Model_BlockoBlock();
            blockoBlock.name = help.blocko_block_name;
            blockoBlock.description = help.blocko_block_general_description;
            blockoBlock.type_of_block = typeOfBlock;
            blockoBlock.author = privateBlockoBlockVersion.blocko_block.author;
            blockoBlock.save();

            // Vytvoření objektu
            Model_BlockoBlockVersion blockoBlockVersion = new Model_BlockoBlockVersion();
            blockoBlockVersion.version_name = help.blocko_block_version_name;
            blockoBlockVersion.version_description = help.blocko_block_version_description;
            blockoBlockVersion.design_json = help.blocko_block_design_json;
            blockoBlockVersion.logic_json = help.blocko_block_logic_json;
            blockoBlockVersion.approval_state = Approval_state.approved;
            blockoBlockVersion.blocko_block = blockoBlock;
            blockoBlockVersion.date_of_create = new Date();
            blockoBlockVersion.save();

            // Pokud jde o schválení po ediatci
            if(help.state.equals("edit")) {
                privateBlockoBlockVersion.approval_state = Approval_state.edited;

                // Odeslání emailu
                try {
                    new EmailTool()
                            .addEmptyLineSpace()
                            .startParagraph("13")
                            .addText("Version of Block " + blockoBlockVersion.blocko_block.name + ": ")
                            .addBoldText(blockoBlockVersion.version_name)
                            .addText(" was edited before publishing for this reason: ")
                            .endParagraph()
                            .startParagraph("13")
                            .addText( help.reason)
                            .endParagraph()
                            .addEmptyLineSpace()
                            .sendEmail(blockoBlockVersion.blocko_block.author.mail, "Version of Block edited" );

                } catch (Exception e) {
                    logger.error ("Sending mail -> critical error", e);
                    e.printStackTrace();
                }
            }
            else privateBlockoBlockVersion.approval_state = Approval_state.approved;

            // Uložení úprav
            privateBlockoBlockVersion.update();

            // Vrácení výsledku
            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result blockoBlockVersion_editScheme(){

        try {

            // Získání JSON
            final Form<Swagger_BlockoBlock_BlockoVersion_Scheme_Edit> form = Form.form(Swagger_BlockoBlock_BlockoVersion_Scheme_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_Scheme_Edit help = form.get();

            // Kontrola objektu
            Model_BlockoBlockVersion blockoBlockVersion = Model_BlockoBlockVersion.find.where().eq("version_name", "version_scheme").findUnique();
            if (blockoBlockVersion == null) return GlobalResult.notFoundObject("Scheme not found");

            // Úprava objektu
            blockoBlockVersion.design_json = help.design_json;
            blockoBlockVersion.logic_json = help.logic_json;

            // Uložení změn
            blockoBlockVersion.update();

            // Vrácení výsledku
            return GlobalResult.result_ok();
        }catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result blockoBlockVersion_getScheme(){

        try {

            // Kontrola objektu
            Model_BlockoBlockVersion blockoBlockVersion = Model_BlockoBlockVersion.find.where().eq("version_name", "version_scheme").findUnique();
            if (blockoBlockVersion == null) return GlobalResult.notFoundObject("Scheme not found");

            // Vytvoření výsledku
            Swagger_BlockoBlock_Version_scheme result = new Swagger_BlockoBlock_Version_scheme();
            result.design_json = blockoBlockVersion.design_json;
            result.logic_json = blockoBlockVersion.logic_json;

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));
        }catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result blockoBlockVersion_createScheme(){

        try {

            Model_BlockoBlockVersion scheme = Model_BlockoBlockVersion.find.where().eq("version_name", "version_scheme").findUnique();
            if (scheme != null) return GlobalResult.result_BadRequest("Scheme already exists.");

            // Získání JSON
            final Form<Swagger_BlockoBlock_BlockoVersion_Scheme_Edit> form = Form.form(Swagger_BlockoBlock_BlockoVersion_Scheme_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_Scheme_Edit help = form.get();

            // Úprava objektu
            scheme = new Model_BlockoBlockVersion();
            scheme.version_name = "version_scheme";
            scheme.design_json = help.design_json;
            scheme.logic_json = help.logic_json;

            // Uložení změn
            scheme.save();

            // Vrácení výsledku
            return GlobalResult.result_ok();
        }catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

// BOARD ###################################################################################################################*/


// ACTUALIZATION PROCEDUES ###################################################################################################################*/


}
