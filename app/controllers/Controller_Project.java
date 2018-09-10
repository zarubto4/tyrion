package controllers;

import com.typesafe.config.Config;
import io.swagger.annotations.*;
import models.*;
import play.Environment;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.Server;
import utilities.authentication.Authentication;
import utilities.emails.Email;
import utilities.enums.NetworkStatus;
import utilities.enums.NotificationImportance;
import utilities.enums.NotificationLevel;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.models_update_echo.EchoHandler;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.scheduler.SchedulerController;
import utilities.swagger.input.*;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_uuid_converter_cleaner;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Model_Hardware_Temporary_NotDominant_record;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Project extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Project.class);

// CONTROLLER CONFIGURATION ############################################################################################

    @javax.inject.Inject
    public Controller_Project(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerController scheduler) {
        super(environment, ws, formFactory, youTrack, config, scheduler);
    }

// GENERAL PROJECT #######-##############################################################################################

    @ApiOperation(value = "create Project",
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

            // Get and Validate Object
            Swagger_Project_New help  = formFromRequestWithValidation(Swagger_Project_New.class);

            // Kontrola objektu
            Model_Product product = Model_Product.find.byId(help.product_id);

            // Vytvoření objektu
            Model_Project project  = new Model_Project();
            project.name        = help.name;
            project.description = help.description;
            project.product     = product;

            // Uložení objektu
            project.save();

            for (Model_Employee employee : product.owner.getEmployees()) {

                Model_ProjectParticipant participant = new Model_ProjectParticipant();
                participant.person = employee.get_person();
                participant.project = project;
                participant.state = employee.state;

                participant.save();
                participant.person.idCache().add(Model_Project.class, project.id);
            }

            project.refresh();

            // Vrácení objektu
            return created(project);

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
            return ok(person().get_user_access_projects());

        } catch (Exception e) {
            e.printStackTrace();
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
    public Result project_get(UUID project_id) {
        try {

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);

            // Vraácení objektu
            return ok(project);

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
    public Result project_delete(UUID project_id) {
        try {
            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);

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
    public Result project_update(UUID project_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);

            // Úprava objektu
            project.name = help.name;
            project.description = help.description;

            // Uložení do DB
            project.update();

            // Vrácení změny
            return ok(project);

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
    public Result project_invite(UUID project_id) {
        try {

            // Get and Validate Object
            Swagger_Invite_Person help = formFromRequestWithValidation(Swagger_Invite_Person.class);

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);

            // Kontrola oprávnění
            project.check_share_permission();

            // Získání seznamu uživatelů, kteří jsou registrovaní(listIn) a kteří ne(listOut)
            List<Model_Person> listIn = new ArrayList<>();
            List<String> toRemove = new ArrayList<>();

            // Roztřídění seznamů
            for (String mail : help.persons_mail) {
                Model_Person person =  Model_Person.find.query().where().eq("email",mail).findOne();
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

                Model_Invitation invitation = Model_Invitation.find.query().where().eq("email", mail).eq("project.id", project.id).findOne();
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

            // Pro Registrované uživatele
            for (Model_Person person : listIn) {

                if (project.isParticipant(person)) continue;

                logger.debug("project_invite - creating invitation for {}", person.email);

                Model_Invitation invitation = Model_Invitation.find.query().where().eq("email", person.email).eq("project.id", project.id).findOne();
                if (invitation == null) {
                    invitation = new Model_Invitation();
                    invitation.email = person.email;
                    invitation.owner = person();
                    invitation.project = project;
                    invitation.save();
                }

                try {

                    new Email()
                            .text("User " + Email.bold(full_name) + " invites you to collaborate on the project ")
                            .link(project.name, Server.becki_mainUrl + "/projects")
                            .text(". If you would like to participate in it, log in to your Byzance account.")
                            .send(person.email, "Invitation to Collaborate");

                } catch (Exception e) {
                    logger.internalServerError(e);
                }

                project.notification_project_invitation(person, invitation);
            }

            // Uložení do DB
            project.refresh();

            // Vrácení objektu
            return ok(project);

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
    public Result project_changeParticipantStatus(UUID project_id) {
        try {

            // Get and Validate Object
            Swagger_Project_Participant_status help = formFromRequestWithValidation(Swagger_Project_Participant_status.class);

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);

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

            return ok(participant);
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
    public Result project_removeParticipant(UUID project_id) {
        try {

            // Get and Validate Object
            Swagger_Invite_Person help = formFromRequestWithValidation(Swagger_Invite_Person.class);

            //Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);

            // Kontrola oprávnění
            project.check_share_permission();

            List<Model_Person> list = new ArrayList<>();

            // Získání seznamu
            for (String mail : help.persons_mail) {

                Model_Person person = Model_Person.find.query().where().eq("email",mail).findOne();
                if (person != null)
                    list.add(person);
            }

            List<Model_Invitation> invitations = new ArrayList<>();

            for (String mail : help.persons_mail) {

                Model_Invitation invitation = Model_Invitation.find.query().where().eq("email",mail).eq("project.id", project_id).findOne();
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
            return ok(project);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "tag Project",
            tags = {"Project"},
            notes = "",     //TODO
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

            // Get and Validate Object
            Swagger_Tags help = formFromRequestWithValidation(Swagger_Tags.class);

            Model_Project project = Model_Project.find.byId(help.object_id);

            project.addTags(help.tags);

            // Vrácení objektu
            return ok(project);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "untag Project",
            tags = {"Project"},
            notes = "",     //TODO
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

            // Get and Validate Object
            Swagger_Tags help = formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola Objektu
            Model_Project project = Model_Project.find.byId(help.object_id);

            // Odstranění Tagu
            project.removeTags(help.tags);

            // Vrácení objektu
            return ok(project);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "addHW Project",
            tags = {"Project"},
            notes = "add new HW to Project, creates HardwareRegistration",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https",
            code = 201
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
            @ApiResponse(code = 201, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_addHardware() {
        try {

            // Get and Validate Object
            Swagger_Project_AddHardware help = formFromRequestWithValidation(Swagger_Project_AddHardware.class);

            logger.debug("registering new device with hash: {}", help.registration_hash);

            // First - We have to find object in central Hardware Registration Atuhority
            // Second Make a copy to local database for actual Hardware and if hardware is not active in any other project, automatically activated that

            Model_Project project = Model_Project.find.byId(help.project_id);
            project.check_update_permission();

            System.out.println("project_addHardware - check_update_permission done");

            Model_HardwareRegistrationEntity registration_authority = Model_HardwareRegistrationEntity.getbyFull_hash(help.registration_hash);

            // Hash not exist
            if(registration_authority == null){
               return notFound("Hash not found.");
            }

            // Already not registred under project!
            if (Model_Hardware.find.query().where().eq("full_id", registration_authority.full_id).eq("project.id", help.project_id).findCount() > 0) {
                return badRequest("Already registered under this project");
            }

            // Copy is done - Hardware is saved in database, but without any connections for projec, groups etc..
            Model_Hardware hardware = Model_HardwareRegistrationEntity.make_copy_of_hardware_to_local_database(help.registration_hash);
            hardware.project = project;

            // Set name if help contains it
            if(help.name != null && !help.name.equals("")) {
                hardware.name = help.name;
            }

            // Set group if help contains it
            if (help.group_ids != null && !help.group_ids.isEmpty()) {

                for(UUID group_id : help.group_ids) {

                    Model_HardwareGroup group = Model_HardwareGroup.find.byId(group_id);
                    group.check_update_permission();

                    group.getHardware().add(hardware);

                    hardware.hardware_groups.add(group);

                    if(hardware.idCache().get(Model_HardwareGroup.class) == null)  hardware.idCache().add(Model_HardwareGroup.class,  new ArrayList<>()) ;
                    hardware.idCache().add(Model_HardwareGroup.class, (group.id));
                }
            }

            // Set Dominance if its possible (Not dominant in diferent project!)
            if (Model_Hardware.find.query().where().eq("full_id", hardware.full_id).eq("dominant_entity", true).findCount() == 0) {
                hardware.dominant_entity = true;

                // Najdi všechny kterří mají stejné FUll Full-ID

                List<Model_Hardware> hardware_for_cache_clean =  Model_Hardware.find.query().where().eq("full_id", hardware.full_id).select("id").findList();
                for(Model_Hardware clean_hw: hardware_for_cache_clean) {
                    System.out.println("Posílám update o zaplnění HW na HW: " + clean_hw.id);
                    EchoHandler.addToQueue(new WSM_Echo(Model_Hardware.class, Model_Project.find.query().where().eq("hardware.id", clean_hw.id).select("id").findSingleAttribute(), clean_hw.id));
                }
            }

            // Update
            hardware.update();

            // Set hardware as Dominant if is not dominant in another project
            project.idCache().add(Model_Hardware.class, hardware.id);


            logger.warn("project_addHardware. Step 1 - is_online_get_from_cache");

            // Person - where we send notification
            Model_Person person = this.person();

            // Try to find hardware by full_id
            logger.warn("project_addHardware. Step 2 - Try to find in cache of not dominant hardware");
            if(Model_Hardware.cache_not_dominant_hardware.containsKey(hardware.full_id)) {

                new Thread(() -> {

                    logger.warn("project_addHardware. Step 2 - Yes we have not dominant hardware record");

                    WS_Model_Hardware_Temporary_NotDominant_record record = Model_Hardware.cache_not_dominant_hardware.get(hardware.full_id);
                    Model_HomerServer server = Model_HomerServer.find.byId(record.homer_server_id);

                    // Remove if exist in not dominant record on public server
                    Model_Hardware.cache_not_dominant_hardware.remove(hardware.full_id);

                    logger.warn("project_addHardware. Step 2 - No we will try to change that on server");
                    // Send restart for reallocate hardware to new UUID
                    if (server != null && server.online_state() == NetworkStatus.ONLINE) {

                        Model_Notification notification = new Model_Notification();
                        notification.setImportance(NotificationImportance.LOW);
                        notification.setLevel(NotificationLevel.INFO);
                        notification.setText(new Notification_Text().setText("Thank you for Activation Hardware. Now, its time to make a magic. Give us a few seconds or restart the device"));
                        notification.send(person);

                        logger.warn("project_addHardware. Step 2 - Server is online and know so we will do it");

                        hardware.connected_server_id = record.homer_server_id;
                        hardware.update();

                        logger.warn("project_addHardware. Step 2 - Command Send");

                        WS_Message_Hardware_uuid_converter_cleaner change = hardware.device_converted_id_clean_switch_on_server(record.random_temporary_hardware_id.toString());
                        logger.warn("project_addHardware:: Step 2 - Response: Change on Homer Server: ", Json.toJson(change).toString());

                    }

                }).start();
            }


            return created(hardware);

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
    public Result project_removeHardware(UUID id) {
        try {

            Model_Hardware hardware = Model_Hardware.find.byId(id);

            hardware.delete();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "deactiveHW Project",
            tags = {"Project"},
            notes = "freze HW from Project",
            produces = "application/json",
            protocols = "https"
    )

    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_deactiveHardware(UUID id) {
        try {

            Model_Hardware hardware = Model_Hardware.find.byId(id);

            hardware.check_deactivate_permission();

            hardware.dominant_entity = false;
            hardware.update();

            // log Hard disconection
            if(hardware.online_state() == NetworkStatus.ONLINE) {
                hardware.make_log_deactivated();
                // If device is online, restart it. So Device will connect immediately and it will find probably a new activated alternative of Device
                hardware.device_converted_id_clean_remove_from_server();
            }

            Model_Hardware.cache_status.remove(hardware.id);


            return ok(hardware);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "activeHW Project",
            tags = {"Project"},
            notes = "freze HW from Project",
            produces = "application/json",
            protocols = "https"
    )

    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result project_activeHardware(UUID id) {
        try {

            Model_Hardware hardware = Model_Hardware.find.byId(id);

            hardware.check_activate_permission();
            hardware.dominant_entity = true;
            hardware.update();


            //
            hardware.is_online_get_from_cache();

            logger.warn("project_activeHardware. Step 1 - is_online_get_from_cache");

            // Try to find hardware by full_id
            logger.warn("project_activeHardware. Step 2 - Try to find in cache of not dominant hardware");
            if(Model_Hardware.cache_not_dominant_hardware.containsKey(hardware.full_id)) {

                logger.warn("project_activeHardware. Step 2 - Yes we have not dominant hardware record");

                WS_Model_Hardware_Temporary_NotDominant_record record = Model_Hardware.cache_not_dominant_hardware.get(hardware.full_id);
                Model_HomerServer server = Model_HomerServer.find.byId(record.homer_server_id);

                // Remove if exist in not dominant record on public server
                Model_Hardware.cache_not_dominant_hardware.remove(hardware.full_id);

                logger.warn("project_activeHardware. Step 2 - No we will try to change that on server");
                // Send restart for reallocate hardware to new UUID
                if(server != null && server.online_state() == NetworkStatus.ONLINE) {

                    logger.warn("project_activeHardware. Step 2 - Server is offline and know so we will do it");

                    hardware.connected_server_id = record.homer_server_id;
                    hardware.update();

                    logger.warn("project_activeHardware. Step 2 - Command Send");

                    WS_Message_Hardware_uuid_converter_cleaner change = hardware.device_converted_id_clean_switch_on_server(record.random_temporary_hardware_id.toString());
                    logger.warn("project_activeHardware:: Step 2 - Response: Change on Homer Server: ", Json.toJson(change).toString());

                }
            }

            return ok(hardware);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}