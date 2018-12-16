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

    public static final String CHANNEL = "becki";

    private UUID personId;

    private boolean notificationSubscribed;

    private final NotificationService notificationService;

    @Inject
    public Portal(Materializer materializer, _BaseFormFactory formFactory, NotificationService notificationService) {
        super(materializer, formFactory);
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
                    case "ping": break;
                    default: {
                        logger.error("onMessagePorta - incoming message not recognized: {}", message.getMessage().toString());
                        if (!message.isErroneous()) {
                            this.send(message.getMessage().put("error_message", "message_type not recognized").put("error_code", 400));
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getDefaultChannel() {
        return CHANNEL;
    }

    @Override
    protected void onClose() {
        super.onClose();
        this.notificationService.unsubscribe(this);
    }

    private void onSubscribeNotification(Message message) {
        logger.trace("onSubscribeNotification - id: {}", this.id);

        this.notificationService.subscribe(this);

        this.notificationSubscribed = true;

        this.send(WS_Message_Subscribe_Notifications.approve_result(message.getId().toString()));
    }

    private void onUnsubscribeNotification(Message message) {
        logger.trace("onUnsubscribeNotification - id: {}", this.id);

        this.notificationService.unsubscribe(this);

        this.notificationSubscribed = false;

        this.send(WS_Message_UnSubscribe_Notifications.approve_result(message.getId().toString()));
    }
}
