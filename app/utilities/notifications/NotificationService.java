package utilities.notifications;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import exceptions.NotFoundException;
import models.*;
import play.libs.Json;
import utilities.enums.NetworkStatus;
import utilities.enums.NotificationImportance;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.model.UnderProject;
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

    public synchronized void send(List<Model_Person> receivers, Model_Notification notification) {
        receivers.forEach(receiver -> {

            Model_Notification notification1;

            if (notification.id == null) {
                notification1 = notification.copy();
                if (!notification1.notification_importance.equals(NotificationImportance.LOW)) {
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
            } else {
                message.put("id", notification1.id.toString());
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

    public synchronized void subscribe(Portal portal) {
        logger.info("subscribe - subscribing portal: {} for person: {}", portal.getId(), portal.getPersonId());
        if (!this.subscriptions.containsKey(portal.getPersonId())) {
            this.subscriptions.put(portal.getPersonId(), new ArrayList<>());
        }

        this.subscriptions.get(portal.getPersonId()).add(portal);

        Model_Person person = Model_Person.find.byId(portal.getPersonId());
        person.get_user_access_projects().forEach(project -> {
            if (!this.projectSubscriptions.containsKey(project.getId())) {
                this.projectSubscriptions.put(project.getId(), new ArrayList<>());
            }
            List<UUID> personIds = this.projectSubscriptions.get(project.getId());
            if (!personIds.contains(portal.getPersonId())) {
                personIds.add(portal.getPersonId());
            }
        });
    }

    public synchronized void unsubscribe(Portal portal) {
        logger.info("unsubscribe - removing subscription of portal: {} for person: {}", portal.getId(), portal.getPersonId());
        if (this.subscriptions.containsKey(portal.getPersonId())) {
            this.subscriptions.get(portal.getPersonId()).remove(portal);

            if (this.subscriptions.get(portal.getPersonId()).isEmpty()) {
                this.subscriptions.remove(portal.getPersonId());
            }
        }

        Model_Person person = Model_Person.find.byId(portal.getPersonId());
        person.get_user_access_projects().forEach(project -> {
            if (this.projectSubscriptions.containsKey(project.getId())) {
                this.projectSubscriptions.get(project.getId()).remove(portal.getPersonId());

                if (this.projectSubscriptions.get(project.getId()).isEmpty()) {
                    this.projectSubscriptions.remove(project.getId());
                }
            }
        });
    }

    // TODO maybe move somewhere else
    public synchronized void networkStatusChanged(Class<?> cls, UUID id, NetworkStatus status, UUID projectId) {
        logger.info("networkStatusChanged - send status {} for {}, id: {}, project id: {}", status, cls.getSimpleName(), id, projectId);
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

    public synchronized void modelUpdated(Class<?> cls, UUID id, UUID projectId) {
        logger.debug("modelUpdated - send echo for {} with id: {} and project id: {}", cls.getSimpleName(), id, projectId);
        if (projectId != null) {
            if (this.projectSubscriptions.containsKey(projectId)) {
                this.projectSubscriptions.get(projectId).forEach(personId -> {
                    if (this.subscriptions.containsKey(personId)) {
                        this.subscriptions.get(personId).forEach(portal -> portal.send(new WSM_Echo(cls, id).make_request()));
                    }
                });
            }
        } else {
            this.subscriptions.values().forEach(portals -> portals.forEach(portal -> portal.send(new WSM_Echo(cls, id).make_request())));
        }
    }
}
