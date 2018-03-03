package websocket.interfaces;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import controllers.Controller_WebSocket;
import controllers._BaseFormFactory;
import models.Model_Garfield;
import models.Model_HomerServer;
import models.Model_Notification;
import models.Model_Project;
import utilities.logger.Logger;
import websocket.messages.tyrion_with_becki.WS_Message_Subscribe_Notifications;
import websocket.messages.tyrion_with_becki.WS_Message_UnSubscribe_Notifications;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WS_Portal {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(WS_Portal.class);

/* STATIC  -------------------------------------------------------------------------------------------------------------*/

@Inject public static _BaseFormFactory baseFormFactory; // Its Required to set this in Server.class Component

    public static final String CHANNEL = "becki";
    public UUID person_id;

    public WS_Portal(UUID person_id) {
        this.person_id = person_id;
        Controller_WebSocket.portals.put(person_id, this);
    }

    /**
     * Holds all connections of Becki portals (Same user with multiple connection!)
     */
    public Map<UUID, WS_PortalSingle> all_person_connections = new HashMap<>();


    public void send(ObjectNode message) {
        all_person_connections.forEach((id, single) -> single.send(message));
    }


    public boolean isOnline() {
        return true;
    }


    public void onMessage(WS_PortalSingle ws, ObjectNode json) {
       
        logger.trace("onMessage:: {} :: Incoming message: {} ", person_id,  json.toString() );

        if(json.has("message_channel")) {

            switch (json.get("message_channel").asText()) {

                case WS_Portal.CHANNEL: {

                    switch (json.get("message_type").asText()) {

                        case Model_Notification.messageType: {
                            becki_notification_confirmation_from_becki(ws, json);
                            return;
                        }    // Becki poslala odpověď, že dostala notifikaci
                        case WS_Message_Subscribe_Notifications.message_type: {
                            becki_subscribe_notification(ws, json);
                            return;
                        }    // Becki poslala odpověď, že ví že subscribe_notification
                        case WS_Message_UnSubscribe_Notifications.message_type: {
                            becki_unsubscribe_notification(ws, json);
                            return;
                        }    // Becki poslala odpověď, že ví že už ne! subscribe_notification

                        default: {
                            logger.warn("onMessage:: {} :: Incoming message on message_channel \"becki\" has not unknown message_type!!!! Message:: {}", person_id,  json.toString());

                        }
                    }
                }

                // Podpora Garfielda - Přeposílání mezi Websockety
                case Model_Garfield.CHANNEL: {

                    // Není komu co zasílat - zahazuji - Je připojen jen tento kanál
                    if (all_person_connections.size() < 2) {
                        return;
                    }

                    for (UUID key : all_person_connections.keySet()) {
                        if (key.equals(UUID.fromString(json.get("single_connection_token").asText()))) continue;
                        all_person_connections.get(key).send(json);
                    }

                }

                case Model_HomerServer.CHANNEL: {
                    logger.warn("onMessage:: Incoming message: Tyrion: Server receive message: ");
                    logger.warn("onMessage:: Incoming message: Tyrion: Server don't know what to do!");
                    return;
                }

                default: {

                    // Přepošlu to na všehcny odběratele Becki
                    if (all_person_connections != null && !all_person_connections.isEmpty()) {
                        for (UUID key : all_person_connections.keySet()) {
                            all_person_connections.get(key).send(json);
                        }
                    }

                }
            }
        } else {
            logger.error("onMessage:: {} :: Incoming message has not message_channel!!!!",person_id, json.toString());
        }
    }

    public void becki_subscribe_notification (WS_PortalSingle ws, ObjectNode json){
        try {

            logger.trace("becki_subscribe_notification:: Content:: {}", json.toString());
            WS_Message_Subscribe_Notifications subscribe_notifications = baseFormFactory.formFromJsonWithValidation(ws, WS_Message_Subscribe_Notifications.class, json);

            WS_PortalSingle single_connection = all_person_connections.get( subscribe_notifications.single_connection_token);
            single_connection.notification_subscriber = true;

            Model_Project.becki_person_id_subscribe(person_id);

            single_connection.send(WS_Message_Subscribe_Notifications.approve_result(subscribe_notifications.message_id));

        }catch (Exception e){
            logger.internalServerError(e);
        }
    }

    public void becki_unsubscribe_notification (WS_PortalSingle ws, ObjectNode json){
        try{

            logger.trace("becki_unsubscribe_notification:: Content:: {}", json.toString());
            WS_Message_UnSubscribe_Notifications un_subscribe_notifications = baseFormFactory.formFromJsonWithValidation(ws, WS_Message_UnSubscribe_Notifications.class, json);

            WS_PortalSingle single_connection = all_person_connections.get(un_subscribe_notifications.single_connection_token);
            single_connection.notification_subscriber = false;

            Model_Project.becki_person_id_unsubscribe(person_id);

            single_connection.send(WS_Message_UnSubscribe_Notifications.approve_result(un_subscribe_notifications.message_id));

        }catch (Exception e){
            logger.internalServerError(e);
        }
    }

    public void becki_notification_confirmation_from_becki(WS_PortalSingle ws, ObjectNode json){
        // Tady dosátvám potvrzení, že becki dostala notifikaci
    }

    public void close() {
        this.all_person_connections.forEach((id, single) -> single.close());
    }

    public void close(UUID token) {
        if (this.all_person_connections.containsKey(token)) {
            this.all_person_connections.get(token).close();
            this.all_person_connections.remove(token);
        }
    }

    public void onClose() {
        Controller_WebSocket.portals.remove(this.person_id);
    }
}
