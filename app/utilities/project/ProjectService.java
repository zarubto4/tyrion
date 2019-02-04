package utilities.project;

import com.google.inject.Inject;
import exceptions.NotFoundException;
import models.*;
import utilities.enums.NetworkStatus;
import utilities.homer.HomerService;
import utilities.logger.Logger;
import utilities.network.NetworkStatusService;
import utilities.notifications.NotificationService;
import utilities.swagger.output.Swagger_ProjectStats;

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

    public void invite(Model_Person person) {

    }

    public void acceptInvitation(Model_Invitation invitation) {

        Model_Person person = Model_Person.find.query().where().eq("email", invitation.email).findOne();

        Model_Project project = invitation.getProject();

        if (!project.persons.contains(person)) {
            project.persons.add(person);
            project.update();

            try {

                System.out.println("acceptInvitation hledam default role");
                Model_Role role = Model_Role.find.query().where().eq("project.id", project.id).eq("default_role", true).findOne();

                if (!role.persons.contains(person)) {

                    System.out.println("Role neobsahuje uživatele a přidávám ho");

                    role.persons.add(person);
                    role.update();
                }
            } catch (NotFoundException e) {
                logger.warn("onProjectInvitationAccepted - unable to find default role for project, id {}", project.id);
            }
        }

        person.idCache().add(Model_Project.class, project.id);

        try {

            System.out.println("onProjectInvitationAccepted - person " + invitation.who_invite().email);
            System.out.println("onProjectInvitationAccepted - person " + person.email);

            this.notificationService.send(invitation.who_invite(), project.notificationInvitationAccepted(person));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Posralo se něco v notifikaci");
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
