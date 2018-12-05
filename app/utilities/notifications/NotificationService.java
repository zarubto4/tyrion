package utilities.notifications;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import exceptions.BadRequestException;
import exceptions.NotFoundException;
import exceptions.NotSupportedException;
import models.*;
import play.libs.Json;
import utilities.enums.NetworkStatus;
import utilities.enums.NotificationAction;
import utilities.enums.NotificationImportance;
import utilities.enums.NotificationState;
import utilities.logger.Logger;
import websocket.interfaces.Portal;
import websocket.messages.tyrion_with_becki.WSM_Echo;
import websocket.messages.tyrion_with_becki.WS_Message_Online_Change_status;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class NotificationService {

    private static final Logger logger = new Logger(NotificationService.class);

    private final Map<UUID, List<Portal>> subscriptions = new HashMap<>();

    private final Map<UUID, List<UUID>> projectSubscriptions = new HashMap<>();

    public void send(Model_Person receiver, Model_Notification notification) {
        this.send(Collections.singletonList(receiver), notification);
    }

    public void send(List<Model_Person> receivers, Model_Notification notification) {
        receivers.forEach(receiver -> {

            Model_Notification notification1;

            if (notification.id == null) {
                notification1 = notification.copy();
                if (notification1.notification_importance.equals(NotificationImportance.LOW)) {
                    notification1.person = receiver;
                    notification1.save();
                }
            } else {
                notification1 = notification;
            }

            // TODO maybe move somewhere else

            ObjectNode message = Json.newObject();
            message.put("message_type", Model_Notification.message_type);
            message.put("message_channel", Portal.CHANNEL);
            message.put("notification_type", notification1.notification_type.name());
            message.put("notification_level", notification1.notification_level.name());
            message.put("notification_importance", notification1.notification_importance.name());
            message.put("state", notification1.state.name());
            message.set("notification_body", Json.toJson(notification1.notification_body()));
            message.put("confirmation_required", notification1.confirmation_required);
            message.put("confirmed", notification1.confirmed);
            message.put("was_read", notification1.was_read);
            message.put("created", notification1.created.getTime());
            message.set("buttons", Json.toJson(notification1.buttons()) );

            if (notification1.id == null) {
                message.put("id", UUID.randomUUID().toString());
                message.put("notification_id", UUID.randomUUID().toString());
            } else {
                message.put("id", notification1.id.toString());
                message.put("notification_id", notification1.id.toString());
            }

            if (this.subscriptions.containsKey(receiver.id)) {
                this.subscriptions.get(receiver.id).forEach(portal -> portal.send(message));
            }
        });
    }

    public void send(Model_Project project, Model_Notification notification) {
        if (this.projectSubscriptions.containsKey(project.getId())) {
            this.send(this.projectSubscriptions.get(project.getId()).stream().map(Model_Person.find::byId).collect(Collectors.toList()), notification);
        }
    }

    public void confirm(Model_Notification notification, NotificationAction action, String payload) {
        if (notification.confirmed) {
            throw new BadRequestException("Notification is already confirmed");
        } else {
            notification.confirm();
            this.send(notification.getPerson(), notification.setState(NotificationState.UPDATED));
        }

        switch (action) {
            case CONFIRM_NOTIFICATION: break;
            case ACCEPT_PROJECT_INVITATION: this.onProjectInvitationAccepted(UUID.fromString(payload)); break;
            case REJECT_PROJECT_INVITATION: this.onProjectInvitationRejected(UUID.fromString(payload)); break;
            default: throw new NotSupportedException("Unsupported action: " + action.name());
        }
    }

    public void subscribe(Portal portal) {
        if (!this.subscriptions.containsKey(portal.getPersonId())) {
            this.subscriptions.put(portal.getPersonId(), new ArrayList<>());
        }

        this.subscriptions.get(portal.getPersonId()).add(portal);

        Model_Person person = Model_Person.find.byId(portal.getPersonId());
        person.get_user_access_projects().forEach(project -> {
            if (!this.projectSubscriptions.containsKey(project.getId())) {
                this.projectSubscriptions.put(project.getId(), new ArrayList<>());
            }
            this.projectSubscriptions.get(project.getId()).add(portal.getPersonId());
        });
    }

    public void unsubscribe(Portal portal) {
        if (this.subscriptions.containsKey(portal.getPersonId())) {
            this.subscriptions.get(portal.getPersonId()).remove(portal);

            if (this.subscriptions.get(portal.getPersonId()).isEmpty()) {
                this.subscriptions.remove(portal.getPersonId());
            }
        }

        Model_Person person = Model_Person.find.byId(portal.getPersonId());
        person.get_user_access_projects().forEach(project -> {
            if (!this.projectSubscriptions.containsKey(project.getId())) {
                this.projectSubscriptions.get(project.getId()).remove(portal.getPersonId());

                if (this.projectSubscriptions.get(project.getId()).isEmpty()) {
                    this.projectSubscriptions.remove(project.getId());
                }
            }
        });
    }

    // TODO maybe move somewhere else
    public void networkStatusChanged(Class<?> cls, UUID id, NetworkStatus status, UUID projectId) {
        if (projectId == null) {
            this.subscriptions.values().forEach(portals -> portals.forEach(portal -> portal.send(new WS_Message_Online_Change_status(cls, id, status).make_request())));
        } else {
            if (this.projectSubscriptions.containsKey(projectId)) {
                this.projectSubscriptions.get(projectId).forEach(personId -> {
                    if (this.subscriptions.containsKey(personId)) {
                        this.subscriptions.get(personId).forEach(portal -> portal.send(new WS_Message_Online_Change_status(cls, id, status).make_request()));
                    }
                });
            }
        }
    }

    public void modelUpdated(Class<?> cls, UUID id, UUID projectId) {
        if (this.projectSubscriptions.containsKey(projectId)) {
            this.projectSubscriptions.get(projectId).forEach(personId -> {
                if (this.subscriptions.containsKey(personId)) {
                    this.subscriptions.get(personId).forEach(portal -> portal.send(new WSM_Echo(cls, projectId, id).make_request()));
                }
            });
        }
    }

    private void onProjectInvitationAccepted(UUID invitationId) {

        Model_Invitation invitation;

        try {
            invitation = Model_Invitation.find.byId(invitationId);
        } catch (NotFoundException e) {
            throw new BadRequestException("Failed to add you to the project. Invitation no longer exists, it might have been drawn back.");
        }

        Model_Person person = Model_Person.find.query().where().eq("email", invitation.email).findOne();

        Model_Project project = invitation.getProject();

        if (!project.persons.contains(person)) {
            project.persons.add(person);
            project.update();

            try {
                Model_Role role = Model_Role.find.query().where().eq("project.id", project.id).eq("default_role", true).findOne();
                if (!role.persons.contains(person)) {
                    role.persons.add(person);
                    role.update();
                }
            } catch (NotFoundException e) {
                logger.warn("onProjectInvitationAccepted - unable to find default role for project, id {}", project.id);
            }
        }

        person.idCache().add(Model_Project.class, project.id);
        // TODO project.notification_project_invitation_accepted(person, invitation.owner);

        // TODO new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, project_not_cached.id, project_not_cached.id))).start();

        invitation.delete();
        project.refresh();
    }

    private void onProjectInvitationRejected(UUID invitationId) {

        Model_Invitation invitation;

        try {
            invitation = Model_Invitation.find.byId(invitationId);
        } catch (NotFoundException e) {
            throw new BadRequestException("Failed to add you to the project. Invitation no longer exists, it might have been drawn back.");
        }

        Model_Person person = Model_Person.find.query().where().eq("email", invitation.email).findOne();

        Model_Project project = invitation.getProject();

        // TODO project.notification_project_invitation_rejected(invitation.owner);

        invitation.delete();
    }
}
