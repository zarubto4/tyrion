package web_socket.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.Model_HomerServer;
import models.Model_Notification;
import models.Model_Person;
import models.Model_Project;
import play.data.Form;
import play.i18n.Lang;
import play.mvc.WebSocket;
import utilities.logger.Class_Logger;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Subscribe_Notifications;
import web_socket.message_objects.tyrion_with_becki.WS_Message_UnSubscribe_Notifications;

import java.util.HashMap;
import java.util.Map;

public class WS_Becki_Website extends WS_Interface_type {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(WS_HomerServer.class);

/* VALUES  -------------------------------------------------------------------------------------------------------------*/

    public static final String CHANNEL = "becki";

    public Map<String, WS_Interface_type> all_person_Connections = new HashMap<>();
    public Model_Person person;
    public String identifikator;

    public WS_Becki_Website(Model_Person person) {
        super();
        this.person = person;
        identifikator = person.id;
    }

    @Override
    public boolean is_online() {
        try {

            for(String key :  Controller_WebSocket.homer_servers.keySet() ){
                System.out.println("Mám v " + getClass().getSimpleName() + " Identifikator :: " + key);
            }


            out.write(" Něco posílám???");
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public void add_to_map() {
        Controller_WebSocket.becki_website.put(identifikator, this);
    }

    @Override
    public String get_identificator() {
        return identifikator;
    }

    @Override
    public WebSocket<String> connection(){return null;}

    @Override
    public void onClose() {
        terminal_logger.debug("Local_Terminal onClose " + identifikator);
        this.close();
    }


    @Override
    public void write_without_confirmation(ObjectNode json) {
        try {

            terminal_logger.debug("          Reálné odesílání notifikace na počet odběrů jednoho uživatele Becki_Website:: " + all_person_Connections.size());

            for (Map.Entry<String,WS_Interface_type> entry : all_person_Connections.entrySet()) {
                terminal_logger.debug("          Reálné odesílání notifikace na Identifikator WS odběru :: " + entry.getValue().get_identificator());

               entry.getValue().write_without_confirmation(json);
            }

        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }

    }

    @Override
    public void onMessage(ObjectNode json) {

        terminal_logger.trace("onMessage " + identifikator + " Incoming message: " + json.toString() );

        if(json.has("messageChannel")) {

            switch (json.get("messageChannel").asText()) {

                case WS_Becki_Website.CHANNEL : {

                    switch (json.get("messageType").asText()) {

                        case Model_Notification.messageType                   :   {  becki_notification_confirmation_from_becki(json); return;}    // Becki poslala odpověď, že dostala notifikaci
                        case WS_Message_Subscribe_Notifications.messageType   :   {  becki_subscribe_notification(json);               return;}    // Becki poslala odpověď, že ví že subscribe_notification
                        case WS_Message_UnSubscribe_Notifications.messageType :   {  becki_unsubscribe_notification( json);            return;}    // Becki poslala odpověď, že ví že už ne! subscribe_notification

                        default: {
                            terminal_logger.warn("onMessage::  "+ identifikator + " Incoming message on messageChannel \"becki\" has not unknown messageType!!!!" + json.toString());

                        }
                    }
                }

                case Model_HomerServer.CHANNEL : {
                    terminal_logger.warn("onMessage:: Incoming message: Tyrion: Server receive message: ");
                    terminal_logger.warn("onMessage:: Incoming message: Tyrion: Server don't know what to do!");
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
            terminal_logger.warn(""+ identifikator + " Incoming message has not messageChannel!!!!" + json.toString());
        }
    }
    
    
    public static void Messages_HomerInstance(WS_HomerServer homer, ObjectNode json){
        terminal_logger.error("Messages_HomerInstance:: Message from Homer server {} Incoming message from server to Becki has not implemented !!!! Message {}", homer.get_identificator(), json.toString());
    }


    public void becki_subscribe_notification (ObjectNode json){
        try {

            final Form<WS_Message_Subscribe_Notifications> form = Form.form(WS_Message_Subscribe_Notifications.class).bind(json);
            if(form.hasErrors()){terminal_logger.error("WS_Message_Subscribe_Notifications:: Incoming Json has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); return;}

            WS_Message_Subscribe_Notifications subscribe_notifications =  form.get();

            WS_Becki_Single_Connection single_connection = (WS_Becki_Single_Connection) all_person_Connections.get( subscribe_notifications.single_connection_token);
            single_connection.notification_subscriber = true;

            Model_Project.becki_person_id_subscribe(identifikator);

            single_connection.write_without_confirmation( subscribe_notifications.messageId ,  WS_Message_Subscribe_Notifications.approve_result() );

        }catch (Exception e){
            terminal_logger.error("becki_subscribe_notification:: Error: ", e);
        }

    }

    public void becki_unsubscribe_notification (ObjectNode json){
        try{

            final Form<WS_Message_UnSubscribe_Notifications> form = Form.form(WS_Message_UnSubscribe_Notifications.class).bind(json);
            if(form.hasErrors()){terminal_logger.error("WS_Message_Subscribe_Notifications:: Incoming Json has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString()); return;}

            WS_Message_UnSubscribe_Notifications un_subscribe_notifications =  form.get();

            WS_Becki_Single_Connection single_connection = (WS_Becki_Single_Connection) all_person_Connections.get( un_subscribe_notifications.single_connection_token);
            single_connection.notification_subscriber = false;

            Model_Project.becki_person_id_unsubscribe(identifikator);

            single_connection.write_without_confirmation( un_subscribe_notifications.messageId,  WS_Message_UnSubscribe_Notifications.approve_result() );

        }catch (Exception e){
            terminal_logger.error("becki_unsubscribe_notification:: Error: ", e);
        }
    }


    public void becki_notification_confirmation_from_becki( JsonNode json){
        // Tady dosátvám potvrzení, že becki dostala notifikaci
    }


}


//**********************************************************************************************************************

