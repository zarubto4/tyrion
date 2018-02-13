package controllers;

import com.google.inject.Inject;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.Server;
import utilities.authentication.Authentication;
import utilities.emails.Email;
import utilities.errors.Exceptions.Result_Error_InvalidBody;
import utilities.logger.Logger;
import utilities.swagger.input.*;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Project extends BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Project.class);

    private FormFactory formFactory;

    @Inject
    public Controller_Project(FormFactory formFactory) {
        this.formFactory = formFactory;
    }
    
// GENERAL PROJECT #####################################################################################################

    @ApiOperation(value = "create Project",
            tags = {"Project"},
            notes = "create new Project",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Project_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Project.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_create() {
        try {

            // Zpracování Json
            final Form<Swagger_Project_New> form = formFactory.form(Swagger_Project_New.class).bindFromRequest();
            if (form.hasErrors()) throw new Result_Error_InvalidBody(form.errorsAsJson());

            Swagger_Project_New help = form.get();

            Model_Product product = Model_Product.getById(help.product_id);
            if (product == null) {return notFound("Product not found");}

            // Vytvoření objektu
            Model_Project project  = new Model_Project();
            project.name = help.name;
            project.description = help.description;
            project.product = product;

            // Uložení objektu
            project.save();

            project.refresh();

            for (Model_Employee employee : product.customer.getEmployees()) {

                Model_ProjectParticipant participant = new Model_ProjectParticipant();
                participant.person = employee.get_person();
                participant.project = project;
                participant.state = employee.state;

                participant.save();

                participant.person.cache_project_ids.add(project.id);
            }

            project.refresh();

            // Vrácení objektu
            return created(project.json());

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Project by logged Person",
            tags = {"Project"},
            notes = "get all Projects by logged Person",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Project.class, responseContainer = "list"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result project_getByUser() {
        try {

            // Vrácení seznamu
            return ok(Json.toJson(person().get_user_access_projects()));

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Project",
            tags = {"Project"},
            notes = "get Projects by project_id",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Project.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result project_get(@ApiParam(value = "project_id String path", required = true) String project_id) {

        try {

            // Kontrola objektu
            Model_Project project = Model_Project.getById(project_id);

            // Vraácení objektu
            return ok(project.json());

         } catch (Exception e) {
            return controllerServerError(e);
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
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result project_delete(@ApiParam(value = "project_id String path", required = true) String project_id) {
        try {

            // Kontrola objektu
            Model_Project project = Model_Project.getById(project_id);

            // Kvuli bezpečnosti abych nesmazal něco co nechceme
            for (Model_CProgram c : project.getCPrograms()) {
                c.delete();
            }

            // Smazání objektu
            project.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Project",
            tags = {"Project"},
            notes = "edit ne Project",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_NameAndDescription",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Project.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_update(@ApiParam(value = "project_id String path", required = true) String project_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_NameAndDescription help = form.get();

            // Kontrola objektu
            Model_Project project = Model_Project.getById(project_id);

            // Úprava objektu
            project.name = help.name;
            project.description = help.description;

            // Uložení do DB
            project.update();

            // Vrácení změny
            return ok(project.json());

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "share Project",
            tags = {"Project"},
            notes = "sends Invitation to all users in list: List<persons_mail>",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Invite_Person",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Project.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_invite(@ApiParam(value = "project_id String path", required = true) String project_id) {
        try {

            // Zpracování Json
            final Form<Swagger_Invite_Person> form = formFactory.form(Swagger_Invite_Person.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Invite_Person help = form.get();

            // Kontrola objektu
            Model_Project project = Model_Project.getById(project_id);

            // Kontrola oprávnění
            project.check_share_permission();

            // Získání seznamu uživatelů, kteří jsou registrovaní(listIn) a kteří ne(listOut)
            List<Model_Person> listIn = new ArrayList<>();
            List<String> toRemove = new ArrayList<>();

            // Roztřídění seznamů
            for (String mail : help.persons_mail) {
                Model_Person person =  Model_Person.find.query().where().eq("mail",mail).findOne();
                if (person != null) {
                    listIn.add(person);
                    toRemove.add(person.email);
                }
            }
            help.persons_mail.removeAll(toRemove);

            logger.debug("project_invite - registered users {}", Json.toJson(listIn));
            logger.debug("project_invite - unregistered users {}", Json.toJson(help.persons_mail));

            String full_name = person().full_name();

            // Vytvoření pozvánky pro nezaregistrované uživatele
            for (String mail :  help.persons_mail) {

                logger.debug("project_invite - creating invitation for {}", mail);

                Model_Invitation invitation = Model_Invitation.find.query().where().eq("mail", mail).eq("project.id", project.id).findOne();
                if (invitation == null) {
                    invitation = new Model_Invitation();
                    invitation.email = mail;
                    invitation.owner = person();
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
                    logger.internalServerError(e);
                }
            }

            for (Model_Person person : listIn) {

                if (project.isParticipant(person)) continue;

                logger.debug("project_invite - creating invitation for {}", person.email);

                Model_Invitation invitation = Model_Invitation.find.query().where().eq("mail", person.email).eq("project.id", project.id).findOne();
                if (invitation == null) {
                    invitation = new Model_Invitation();
                    invitation.email = person.email;
                    invitation.owner = person();
                    invitation.project = project;
                    invitation.save();
                }

                try {

                    new Email()
                            .text("User " + Email.bold(full_name) + " invites you to collaborate on the project " + Email.bold(project.name) + ". If you would like to participate in it, log in to your Byzance account.")
                            .send(person.email, "Invitation to Collaborate");

                } catch (Exception e) {
                    logger.internalServerError(e);
                }

                project.notification_project_invitation(person, invitation);
            }

            // Uložení do DB
            project.cache_refresh();

            // Vrácení objektu
            return ok(project.json());

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "change Project participant status",
            tags = {"Project"},
            notes = "Changes participant status ",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Project_Participant_status",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_ProjectParticipant.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_changeParticipantStatus(@ApiParam(value = "project_id String path", required = true) String project_id) {
        try {

            // Zpracování Json
            final Form<Swagger_Project_Participant_status> form = formFactory.form(Swagger_Project_Participant_status.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Project_Participant_status help = form.get();

            // Kontrola objektu
            Model_Project project = Model_Project.getById(project_id);

            // Kontrola oprávnění
            project.admin_permission();

            // Kontrola objektu
            Model_ProjectParticipant participant = Model_ProjectParticipant.find.query().where().eq("person.id", help.person_id).eq("project.id", project_id).findOne();
            if (participant == null) return notFound("Participant not found");

            // Uložení změn
            participant.state = help.state;
            participant.update();

            // Odeslání notifikace uživateli
            project.notification_project_participant_change_status(participant);

            return ok(Json.toJson(participant));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "unshare Project",
            tags = {"Project"},
            notes = "unshare Project with all users in list: List<person_id>",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Invite_Person",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Project.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_removeParticipant(@ApiParam(value = "project_id String path", required = true) String project_id) {
        try {

            // Zpracování Json
            final Form<Swagger_Invite_Person> form = formFactory.form(Swagger_Invite_Person.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Invite_Person help = form.get();

            //Kontrola objektu
            Model_Project project = Model_Project.getById(project_id);

            // Kontrola oprávnění
            project.check_share_permission();

            List<Model_Person> list = new ArrayList<>();

            // Získání seznamu
            for (String mail : help.persons_mail) {

                Model_Person person = Model_Person.find.query().where().eq("mail",mail).findOne();
                if (person != null)
                    list.add(person);
            }

            List<Model_Invitation> invitations = new ArrayList<>();

            for (String mail : help.persons_mail) {

                Model_Invitation invitation = Model_Invitation.find.query().where().eq("mail",mail).eq("project.id", project_id).findOne();
                if (invitation != null)
                    invitations.add(invitation);
            }

            for (Model_Person person : list) {
                Model_ProjectParticipant participant = Model_ProjectParticipant.find.query().where().eq("person.id", person.id).eq("project.id", project.id).findOne();
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
            return ok(project.json());

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "tag Project",
            tags = {"Project"},
            notes = "",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Tags",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Project.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_addTags() {
        try {

            // Zpracování Json
            final Form<Swagger_Tags> form = formFactory.form(Swagger_Tags.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Tags help = form.get();

            Model_Project project = Model_Project.getById(help.object_id);

            project.addTags(help.tags);

            // Vrácení objektu
            return ok(project.json());

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "untag Project",
            tags = {"Project"},
            notes = "",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Tags",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Project.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_removeTags() {
        try {

            // Zpracování Json
            final Form<Swagger_Tags> form = formFactory.form(Swagger_Tags.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Tags help = form.get();

            Model_Project project = Model_Project.getById(help.object_id);

            project.removeTags(help.tags);

            // Vrácení objektu
            return ok(project.json());

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "addHW Project",
            tags = {"Project"},
            notes = "add new HW to Project, creates HardwareRegistration",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Project_AddHardware",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Ok Result",                 response = Model_HardwareRegistration.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_addHardware() {
        try {

            // Zpracování Json
            final Form<Swagger_Project_AddHardware> form = formFactory.form(Swagger_Project_AddHardware.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Project_AddHardware help = form.get();

            logger.debug("registering new device with hash: {}", help.registration_hash);

            // Kotrola objektu - NAjdu v Databázi
            Model_Hardware hardware = Model_Hardware.find.query().where().eq("registration_hash", help.registration_hash).findOne();
            if (hardware == null) return notFound("Hardware not found");
            if (hardware.registration != null) return badRequest("Board is already registered");

            // Kotrola objektu
            Model_Project project = Model_Project.getById(help.project_id);

            Model_HardwareRegistration registration = new Model_HardwareRegistration();
            registration.hardware = hardware;
            registration.project = project;

            if (help.group_id != null) {
                Model_HardwareGroup group = Model_HardwareGroup.getById(help.group_id);
                if (group == null) return notFound("HardwareGroup not found");
                if (!group.update_permission()) return forbidden();

                registration.group = group;
                registration.cache_group_id = group.id;
            }

            registration.save();

            project.cache_hardware_ids.add(registration.id);

            return created(registration.json());

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "removeHW Project",
            tags = {"Project"},
            notes = "removes HW from Project",
            produces = "application/json",
            protocols = "https"
    )

    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_removeHardware(String registration_id) {
        try {

            Model_HardwareRegistration registration = Model_HardwareRegistration.getById(registration_id);
            if (registration == null) return notFound("HardwareRegistration not found");

            Model_Project project = registration.getProject();
            project.update_permission();

            if (registration.hardware == null) return badRequest("Already removed");

            registration.hardware = null;
            registration.save();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}