package controllers;

import io.swagger.annotations.*;
import models.Model_Notification;
import models.Model_Invitation;
import models.Model_Person;
import models.Model_CProgram;
import models.Model_Product;
import models.Model_Project;
import models.Model_ProjectParticipant;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.emails.Email;
import utilities.enums.*;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_Project extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Person.class);
    
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
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Project.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_create() {
        try{

            // Zpracování Json
            final Form<Swagger_Project_New> form = Form.form(Swagger_Project_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Project_New help = form.get();

            Model_Product product = Model_Product.get_byId(help.product_id);
            if(product == null){return GlobalResult.result_notFound("Product not found");}

            // Vytvoření objektu
            Model_Project project  = new Model_Project();
            project.name = help.name;
            project.description = help.description;
            project.product = product;

            // Kontrola oprávnění těsně před uložením
            if (!project.create_permission())  return GlobalResult.result_forbidden();
            if (!project.financial_permission())  return GlobalResult.result_badRequest("Cannot create new project because of low financial resources.");

            // Uložení objektu
            project.save();

            project.refresh();

            Model_ProjectParticipant participant = new Model_ProjectParticipant();
            participant.person = product.payment_details.person;
            participant.project = project;
            participant.state = Enum_Participant_status.owner;

            participant.save();

            project.refresh();

            // Vrácení objektu
            return GlobalResult.result_created(Json.toJson(project));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Project.class, responseContainer = "list"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result project_getByUser(){
        try {

            // Získání seznamu
            List<Model_Project> projects = Model_Project.find.where().eq("participants.person.id", Controller_Security.get_person_id()).eq("product.active", true).order().asc("name").findList();

            // Vrácení seznamu
            return GlobalResult.result_ok(Json.toJson( projects ));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Project.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result project_get(@ApiParam(value = "project_id String path", required = true)  String project_id){

        try {

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if (project == null) return GlobalResult.result_notFound("Project project_id not found");

            // Kontrola oprávnění
            if (!project.read_permission())   return GlobalResult.result_forbidden();

            // Vraácení objektu
            return GlobalResult.result_ok(Json.toJson(project));

         } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result project_delete(@ApiParam(value = "project_id String path", required = true) String project_id){
        try {

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if (project == null) return GlobalResult.result_notFound("Project project_id not found");

            // Kontrola oprávnění
            if (!project.delete_permission())   return GlobalResult.result_forbidden();

           // Kvuli bezpečnosti abych nesmazal něco co nechceme
           for(Model_CProgram c : project.c_programs){ c.delete();}


            // Smazání objektu
            project.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Project.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_update(@ApiParam(value = "project_id String path", required = true)  String project_id){
        try {

            // Zpracování Json
            final Form<Swagger_Project_Edit> form = Form.form(Swagger_Project_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Project_Edit help = form.get();

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if (project == null) return GlobalResult.result_notFound("Project project_id not found");

            // Kontrola oprávnění
            if (!project.edit_permission() )   return GlobalResult.result_forbidden();

            // Úprava objektu
            project.name = help.project_name;
            project.description = help.project_description;

            // Uložení do DB
            project.update();

            // Vrácení změny
            return GlobalResult.result_ok(Json.toJson(project));


        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "share Project",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Project.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_invite(@ApiParam(value = "project_id String path", required = true)  String project_id){
        try {

            // Zpracování Json
            final Form<Swagger_ShareProject_Person> form = Form.form(Swagger_ShareProject_Person.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_ShareProject_Person help = form.get();

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if(project == null) return GlobalResult.result_notFound("Project project_id not found");

            // Kontrola oprávnění
            if (!project.share_permission()) return GlobalResult.result_forbidden();

            // Získání seznamu uživatelů, kteří jsou registrovaní(listIn) a kteří ne(listOut)
            List<Model_Person> listIn = new ArrayList<>();
            List<String> toRemove = new ArrayList<>();

            // Roztřídění seznamů
            for (String mail : help.persons_mail){
                Model_Person person =  Model_Person.find.where().eq("mail",mail).findUnique();
                if (person != null) {
                    listIn.add(person);
                    toRemove.add(person.mail);
                }
            }
            help.persons_mail.removeAll(toRemove);

            String full_name = Controller_Security.get_person().full_name;

            // Vytvoření pozvánky pro nezaregistrované uživatele
            for (String mail :  help.persons_mail){

                Model_Invitation invitation = Model_Invitation.find.where().eq("mail", mail).eq("project.id", project.id).findUnique();
                if(invitation == null){
                    invitation = new Model_Invitation();
                    invitation.mail = mail;
                    invitation.date_of_creation = new Date();
                    invitation.owner = Controller_Security.get_person();
                    invitation.project = project;
                    invitation.save();
                }

                String link = Server.becki_mainUrl + "/" +  Server.becki_invitationToCollaborate + URLEncoder.encode(mail, "UTF-8");

                // Odeslání emailu s linkem pro registraci
                try {

                    new Email()
                            .text("User " + Email.bold(full_name) + " invites you to collaborate on the project " + Email.bold(project.name) + ". If you would like to participate in it, register yourself via link below.")
                            .divider()
                            .link("Register here and collaborate",link)
                            .send(mail, "Invitation to Collaborate");

                } catch (Exception e) {
                    terminal_logger.internalServerError("project_invite:", e);
                }
            }

            for (Model_Person person : listIn) {

                Model_Invitation invitation = Model_Invitation.find.where().eq("mail", person.mail).eq("project.id", project.id).findUnique();
                if(invitation == null){
                    invitation = new Model_Invitation();
                    invitation.mail = person.mail;
                    invitation.date_of_creation = new Date();
                    invitation.owner = Controller_Security.get_person();
                    invitation.project = project;
                    invitation.save();
                }

                try {

                    new Email()
                            .text("User " + Email.bold(full_name) + " invites you to collaborate on the project " + Email.bold(project.name) + ". If you would like to participate in it, log in to your Byzance account.")
                            .send(person.mail, "Invitation to Collaborate");

                } catch (Exception e) {
                    terminal_logger.internalServerError("project_invite:", e);
                }

                project.notification_project_invitation(person, invitation);
            }

            // Uložení do DB
            project.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(project));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_changeParticipantStatus(@ApiParam(value = "project_id String path", required = true)  String project_id){
        try{

            // Zpracování Json
            final Form<Swagger_Project_Participant_status> form = Form.form(Swagger_Project_Participant_status.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Project_Participant_status help = form.get();

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if(project == null) return GlobalResult.result_notFound("Project no longer exists");

            // Kontrola oprávnění
            if (!project.admin_permission()) return GlobalResult.result_forbidden();

            // Kontrola objektu
            Model_ProjectParticipant participant = Model_ProjectParticipant.find.where().eq("person.id", help.person_id).eq("project.id", project_id).findUnique();
            if (participant == null) return GlobalResult.result_notFound("Participant not found");

            // Uložení změn
            participant.state = help.state;
            participant.update();

            // Odeslání notifikace uživateli
            project.notification_project_participant_change_status(participant);

            return GlobalResult.result_ok(Json.toJson(participant));
        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "unshare Project",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Project.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_removeParticipant(@ApiParam(value = "project_id String path", required = true) String project_id){
        try {

            // Zpracování Json
            final Form<Swagger_ShareProject_Person> form = Form.form(Swagger_ShareProject_Person.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_ShareProject_Person help = form.get();

            //Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if(project == null) return GlobalResult.result_notFound("Project not found");

            // Kontrola oprávnění
            if (!project.unshare_permission()) return GlobalResult.result_forbidden();

            List<Model_Person> list = new ArrayList<>();

            // Získání seznamu
            for (String mail : help.persons_mail){

                Model_Person person = Model_Person.find.where().eq("mail",mail).findUnique();
                if(person != null)
                    list.add(person);
            }

            List<Model_Invitation> invitations = new ArrayList<>();

            for (String mail : help.persons_mail){

                Model_Invitation invitation = Model_Invitation.find.where().eq("mail",mail).eq("project.id", project_id).findUnique();
                if(invitation != null)
                    invitations.add(invitation);
            }

            for (Model_Person person : list) {
                Model_ProjectParticipant participant = Model_ProjectParticipant.find.where().eq("person.id", person.id).eq("project.id", project.id).findUnique();
                if (participant != null) {

                    // Úprava objektu
                    participant.delete();
                }
            }

            for (Model_Invitation invitation : invitations) {
                invitation.delete_notification();
                invitation.delete();
            }

            // Obnovení v DB
            project.refresh();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(project));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }
}