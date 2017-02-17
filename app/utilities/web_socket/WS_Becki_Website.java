package utilities.web_socket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.notification.Model_Notification;
import models.person.Model_Person;
import models.project.b_program.servers.Model_HomerServer;
import play.libs.Json;
import play.mvc.WebSocket;

import java.util.HashMap;
import java.util.Map;

public class WS_Becki_Website extends  WebSCType {

    public static final String CHANNEL = "becki";

    public Map<String, WebSCType> all_person_Connections = new HashMap<>();
    public Model_Person person;

    public WS_Becki_Website(Model_Person person) {
        super();
        this.person = person;
        super.identifikator = person.id;
    }

    @Override
    public WebSocket<String> connection(){return null;}

    @Override
    public void onClose() {
        logger.trace("Local_Terminal onClose " + super.identifikator);
        this.close();
    }


    @Override
    public void write_without_confirmation(ObjectNode json) {
        try {

            for (Map.Entry<String,WebSCType> entry : all_person_Connections.entrySet()) {
               entry.getValue().write_without_confirmation(json);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onMessage(ObjectNode json) {

        logger.debug("WS_Becki_Website:: onMessage " + identifikator + " Incoming message: " + json.toString() );

        if(json.has("messageChannel")) {

            switch (json.get("messageChannel").asText()) {

                case WS_Becki_Website.CHANNEL : {

                    switch (json.get("messageType").asText()) {

                        case "notification"             :   {  becki_notification_confirmation_from_becki(json); return;}    // Becki poslala odpověď, že dostala notifikaci
                        case "subscribe_notification"   :   {  becki_subscribe_notification(json);               return;}    // Becki poslala odpověď, že ví že subscribe_notification
                        case "unsubscribe_notification" :   {  becki_unsubscribe_notification( json);            return;}    // Becki poslala odpověď, že ví že už ne! subscribe_notification

                        default: {

                            logger.error("WS_Becki_Website:: onMessage::  "+ identifikator + " Incoming message on messageChannel \"becki\" has not unknown messageType!!!!" + json.toString());

                        }
                    }
                }
                case Model_HomerServer.CHANNEL : {
                    logger.warn("WS_Becki_Website:: onMessage:: Incoming message: Tyrion: Server receive message: ");
                    logger.warn("WS_Becki_Website:: onMessage:: Incoming message: Tyrion: Server don't know what to do!");
                    return;
                }

                default: {
                    // Přepošlu to na všehcny odběratele Becki
                    if (all_person_Connections != null && ! all_person_Connections.isEmpty()) {
                        for (String key : all_person_Connections.keySet()) {
                            all_person_Connections.get(key).write_without_confirmation(json);
                        }
                    }
                }

            }

        }else {
            logger.error("WS_Becki_Website:: "+ identifikator + " Incoming message has not messageChannel!!!!" + json.toString());
        }
    }


    // Odebírání streamu notifikací z Tytiona
    public void becki_subscribe_notification (ObjectNode json){
        try {

            WS_Becki_Single_Connection single_connection = (WS_Becki_Single_Connection) all_person_Connections.get(json.get("single_connection_token").asText());
            single_connection.notification_subscriber = true;

            becki_approve_subscription_notification_success(single_connection, json.get("messageId").asText());

        }catch (Exception e){
            logger.error("becki_subscribe_notification", e);
        }

    }

    public void becki_unsubscribe_notification (ObjectNode json){
        try{

            WS_Becki_Single_Connection single_connection = (WS_Becki_Single_Connection) all_person_Connections.get( json.get("single_connection_token").asText());
            single_connection.notification_subscriber = true;

            becki_approve_unsubscription_notification_success(single_connection, json.get("messageId").asText() );

        }catch (Exception e){
            logger.error("becki_unsubscribe_notification", e);
        }
    }

    // Json Messages
    public void becki_approve_subscription_notification_success(WS_Becki_Single_Connection single_connection, String messageId){
        ObjectNode result = Json.newObject();
        result.put("messageType", "subscribe_notification");
        result.put("messageChannel", "becki");
        result.put("status", "success");

        single_connection.write_without_confirmation( messageId, result);
    }

    public void becki_approve_unsubscription_notification_success(WS_Becki_Single_Connection single_connection, String messageId){
        ObjectNode result = Json.newObject();
        result.put("messageType", "unsubscribe_notification");
        result.put("messageChannel", "becki");
        result.put("status", "success");

        single_connection.write_without_confirmation( messageId, result);
    }

    public void becki_sendNotification(Model_Notification notification){

        ObjectNode result = Json.newObject();
        result.put("messageType", "notification");
        result.put("messageChannel", "becki");
        result.put("id", notification.id);
        result.put("notification_level",   notification.notification_level.name());
        result.put("notification_importance", notification.notification_importance.name());
        result.set("notification_body", Json.toJson(notification.notification_body()));
        result.set("buttons", Json.toJson(notification.buttons()));
        result.put("confirmation_required", notification.confirmation_required);
        result.put("confirmed", notification.confirmed);
        result.put("was_read", notification.was_read);
        result.put("created", notification.created.getTime());
        result.put("state", notification.state.name());

        for(String person_connection_token : all_person_Connections.keySet()){
            WS_Becki_Single_Connection single_connection =  (WS_Becki_Single_Connection) all_person_Connections.get(person_connection_token);
            if(single_connection.notification_subscriber) single_connection.write_without_confirmation(result);
        }

    }

    public void becki_notification_confirmation_from_becki( JsonNode json){
        // TODO
        // Tady dosátvám potvrzení, že becki dostala notifikaci
    }


}


//**********************************************************************************************************************

