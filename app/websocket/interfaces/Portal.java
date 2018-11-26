package websocket.interfaces;

import akka.stream.Materializer;
import com.google.inject.Inject;
import controllers._BaseFormFactory;
import models.Model_Garfield;
import models.Model_Project;
import utilities.logger.Logger;
import utilities.network.NetworkStatusService;
import utilities.notifications.NotificationService;
import websocket.Interface;
import websocket.Message;
import websocket.messages.tyrion_with_becki.WS_Message_Subscribe_Notifications;
import websocket.messages.tyrion_with_becki.WS_Message_UnSubscribe_Notifications;

import java.util.UUID;

public class Portal extends Interface {

    private static final Logger logger = new Logger(Portal.class);

    private UUID personId;

    private boolean notificationSubscribed;

    private final NotificationService notificationService;

    @Inject
    public Portal(NetworkStatusService networkStatusService, Materializer materializer, _BaseFormFactory formFactory, NotificationService notificationService) {
        super(networkStatusService, materializer, formFactory);
        this.notificationService = notificationService;
    }

    public void setPersonId(UUID personId) {
        if (this.personId == null) {
            this.personId = personId;
        } else {
            throw new RuntimeException("Cannot set parent twice");
        }
    }

    public UUID getPersonId() {
        return personId;
    }

    @Override
    public void onMessage(Message message) {
        switch (message.getChannel()) {
            case Model_Garfield.CHANNEL: { // TODO make interface for garfield
                break;
            }
            default: {
                switch (message.getType()) {
                    case WS_Message_Subscribe_Notifications.message_type: onSubscribeNotification(message); break;
                    case WS_Message_UnSubscribe_Notifications.message_type: onUnsubscribeNotification(message); break;
                    default: // TODO
                }
            }
        }
    }

    public void onSubscribeNotification(Message message) {
        try {

            logger.trace("onSubscribeNotification - id: {}", this.id);

            this.notificationService.subscribe(this);

            this.notificationSubscribed = true;

            Model_Project.becki_person_id_subscribe(this.personId); // TODO ugly -> rework

            this.send(WS_Message_Subscribe_Notifications.approve_result(message.getId().toString()));

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void onUnsubscribeNotification(Message message) {
        try {

            logger.trace("onUnsubscribeNotification - id: {}", this.id);

            this.notificationService.unsubscribe(this);

            this.notificationSubscribed = false;

            Model_Project.becki_person_id_unsubscribe(this.personId); // TODO ugly -> rework

            this.send(WS_Message_UnSubscribe_Notifications.approve_result(message.getId().toString()));

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }
}
