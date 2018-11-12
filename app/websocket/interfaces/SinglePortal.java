package websocket.interfaces;

import akka.stream.Materializer;
import com.google.inject.Inject;
import controllers._BaseFormFactory;
import models.Model_Project;
import utilities.logger.Logger;
import utilities.network.NetworkStatusService;
import websocket.Interface;
import websocket.Message;
import websocket.WebSocketInterface;
import websocket.messages.tyrion_with_becki.WS_Message_Subscribe_Notifications;
import websocket.messages.tyrion_with_becki.WS_Message_UnSubscribe_Notifications;

public class SinglePortal extends Interface {

    private static final Logger logger = new Logger(SinglePortal.class);

    private WebSocketInterface parent;

    private boolean notificationSubscribed;

    @Inject
    public SinglePortal(NetworkStatusService networkStatusService, Materializer materializer, _BaseFormFactory formFactory) {
        super(networkStatusService, materializer, formFactory);
    }

    public void setParent(WebSocketInterface parent) {
        if (this.parent == null) {
            this.parent = parent;
        } else {
            throw new RuntimeException("Cannot set parent twice");
        }
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
