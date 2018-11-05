package websocket.interfaces;

import models.Model_Project;
import utilities.logger.Logger;
import websocket.Interface;
import websocket.Message;
import websocket.WebSocketInterface;
import websocket.messages.tyrion_with_becki.WS_Message_Subscribe_Notifications;
import websocket.messages.tyrion_with_becki.WS_Message_UnSubscribe_Notifications;

import java.util.UUID;

public class SinglePortal extends Interface {

    private static final Logger logger = new Logger(SinglePortal.class);

    private final WebSocketInterface parent;

    private boolean notificationSubscribed;

    public SinglePortal(UUID id, WebSocketInterface parent) {
        super(id);

        this.parent = parent;
    }

    @Override
    public void onMessage(Message message) {
        switch (message.getType()) {
            case WS_Message_Subscribe_Notifications.message_type: onSubscribeNotification(message); break;
            case WS_Message_UnSubscribe_Notifications.message_type: onUnsubscribeNotification(message); break;
            default: this.parent.onMessage(message); break;
        }
    }

    public void onSubscribeNotification(Message message) {
        try {

            logger.trace("onSubscribeNotification - id: {}", this.id);

            this.notificationSubscribed = true;

            Model_Project.becki_person_id_subscribe(this.parent.getId()); // TODO ugly -> rework

            this.send(WS_Message_Subscribe_Notifications.approve_result(message.getId().toString()));

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void onUnsubscribeNotification(Message message) {
        try {

            logger.trace("onUnsubscribeNotification - id: {}", this.id);

            this.notificationSubscribed = false;

            Model_Project.becki_person_id_unsubscribe(this.parent.getId()); // TODO ugly -> rework

            this.send(WS_Message_UnSubscribe_Notifications.approve_result(message.getId().toString()));

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }
}
