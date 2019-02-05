package utilities.project;

import com.google.inject.Inject;
import exceptions.NotFoundException;
import models.*;
import play.libs.Json;
import utilities.Server;
import utilities.emails.Email;
import utilities.enums.NetworkStatus;
import utilities.homer.HomerService;
import utilities.logger.Logger;
import utilities.network.NetworkStatusService;
import utilities.notifications.NotificationService;
import utilities.swagger.input.Swagger_Invite_Person;
import utilities.swagger.output.Swagger_ProjectStats;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.net.URLEncoder;

public class ProjectService {

    private static final Logger logger = new Logger(ProjectService.class);

    private final NetworkStatusService networkStatusService;
    private final NotificationService notificationService;
    private final HomerService homerService;

    @Inject
    public ProjectService(NetworkStatusService networkStatusService, NotificationService notificationService, HomerService homerService) {
        this.networkStatusService = networkStatusService;
        this.notificationService = notificationService;
        this.homerService = homerService;
    }

    public Swagger_ProjectStats getOverview(Model_Project project) {

        Swagger_ProjectStats stats = new Swagger_ProjectStats();
        stats.hardware = project.getHardware().size();
        stats.b_programs = project.getBProgramsIds().size();
        stats.c_programs = project.getCProgramsIds().size();
        stats.libraries = project.getLibrariesIds().size();
        stats.grid_projects = project.getGridProjectsIds().size();
        stats.hardware_groups = project.getHardwareGroupsIds().size();
        stats.widgets = project.getWidgetsIds().size();
        stats.blocks = project.getBlocksIds().size();
        stats.instances = project.getInstancesIds().size();
        stats.servers = project.getHomerServerIds().size();

        stats.hardware_online = 0;
        stats.instance_online = 0;
        stats.servers_online = 0;

        /*List<UUID> serverIds = Model_Hardware.find.query().where()
                .eq("project.id", project.getId())
                .eq("dominant_entity", true)
                .isNotNull("connected_server_id")
                .select("connected_server_id")
                .setDistinct(true)
                .findSingleAttributeList();

        for (UUID id : serverIds) {
            try {
                Model_HomerServer server = Model_HomerServer.find.byId(id);
                HomerInterface homerInterface = this.homerService.getInterface(server);
                WS_Message_Hardware_online_status response = homerInterface.device_online_synchronization_ask(Model_Hardware.find.query().where().eq("project.id", id).eq("dominant_entity", true).eq("connected_server_id", id).select("id").findSingleAttributeList());

                for (WS_Message_Hardware_online_status.DeviceStatus status : response.hardware_list) {

                    if (status.online_status) {
                        ++stats.hardware_online;
                    }

                    try {
                        Model_Hardware hardware = Model_Hardware.find.byId(UUID.fromString(status.uuid));
                        this.networkStatusService.setStatus(hardware, status.online_status ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE);
                    } catch (Exception e){
                        // just skip
                    }
                }

            } catch (NotFoundException | ServerOfflineException | FailedMessageException e) {
                // Nothing
            }
        }*/

        for (Model_Hardware hardware : project.getHardware()) {
            if (this.networkStatusService.getStatus(hardware) == NetworkStatus.ONLINE) {
                ++stats.hardware_online;
            }
        }

        for (Model_HomerServer server : project.getHomerServers()) {
            if (this.networkStatusService.getStatus(server) == NetworkStatus.ONLINE) {
                ++stats.servers_online;
            }
        }

        for (Model_Instance instance : project.getInstances()) {
            if (this.networkStatusService.getStatus(instance) == NetworkStatus.ONLINE) {
                ++stats.instance_online;
            }
        }

        return stats;
    }

    public void activate(Model_Project project) {}

    public void deactivate(Model_Project project) {}

    public void invite(Model_Project project, List<String> emails, Model_Person who_invite) throws UnsupportedEncodingException {

        // Získání seznamu uživatelů, kteří jsou registrovaní(listIn) a kteří ne(listOut)
        List<Model_Person> listIn = new ArrayList<>();
        List<String> listOut = new ArrayList<>();

        // Roztřídění seznamů
        for (String mail : emails) {
            Model_Person person =  Model_Person.find.query().nullable().where().eq("email",mail).findOne();
            if (person != null) {
                listIn.add(person);
                listOut.add(person.email);
            }
        }

        emails.removeAll(listOut);

        logger.debug("project_invite - registered users {}", Json.toJson(listIn));
        logger.debug("project_invite - unregistered users {}", Json.toJson(emails));

        String full_name = who_invite.full_name();

        // Vytvoření pozvánky pro nezaregistrované uživatele
        for (String mail : emails) {

            logger.debug("project_invite - creating invitation for {}", mail);

            Model_Invitation invitation = Model_Invitation.find.query().nullable().where().eq("email", mail).eq("project.id", project.id).findOne();
            if (invitation == null) {
                invitation = new Model_Invitation();
                invitation.email = mail;
                invitation.owner = who_invite;
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

            Model_Invitation invitation = Model_Invitation.find.query().nullable().where().eq("email", person.email).eq("project.id", project.id).findOne();
            if (invitation == null) {
                invitation = new Model_Invitation();
                invitation.email = person.email;
                invitation.owner = who_invite;
                invitation.project = project;
                invitation.save();
            }

            project.idCache().add(Model_Invitation.class, invitation.id);

            try {

                new Email()
                        .text("User " + Email.bold(full_name) + " invites you to collaborate on the project ")
                        .link(project.name, Server.becki_mainUrl + "/projects")
                        .text(". If you would like to participate in it, log in to your Byzance account.")
                        .send(person.email, "Invitation to Collaborate");

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            this.notificationService.send(person, project.notificationInvitation(who_invite, invitation));
        }

        // Uložení do DB
        project.refresh();
    }

    public void acceptInvitation(Model_Invitation invitation) {

        Model_Person person = Model_Person.find.query().where().eq("email", invitation.email).findOne();
        Model_Project project = invitation.getProject();

        try {

            Model_Role role = Model_Role.find.query().where().eq("project.id", project.id).eq("default_role", true).findOne();

            if (!role.persons.contains(person)) {

                role.persons.add(person);
                role.update();
            }
        } catch (NotFoundException e) {
            logger.warn("onProjectInvitationAccepted - unable to find default role for project, id {}", project.id);
        }

        try {

            System.out.println("onProjectInvitationAccepted - person " + invitation.who_invite().email);
            System.out.println("onProjectInvitationAccepted - person " + person.email);

            this.notificationService.send(invitation.who_invite(), project.notificationInvitationAccepted(person));

        } catch (Exception e) {
            e.printStackTrace();
        }

        // TODO new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, project_not_cached.id, project_not_cached.id))).start();

        invitation.delete();
        project.refresh();
    }

    public void rejectInvitation(Model_Invitation invitation) {
        Model_Person person = Model_Person.find.query().where().eq("email", invitation.email).findOne();

        Model_Project project = invitation.getProject();

        this.notificationService.send(invitation.owner, project.notificationInvitationRejected(person));

        invitation.delete();
    }
}
