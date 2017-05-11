package utilities.notifications;


import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.Model_Invitation;
import models.Model_Notification;
import models.Model_Person;
import play.libs.Json;
import utilities.enums.Enum_Notification_action;
import utilities.enums.Enum_Notification_importance;
import utilities.logger.Class_Logger;
import web_socket.services.WS_Becki_Website;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NotificationHandler {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(NotificationHandler.class);

/* METHOD  -------------------------------------------------------------------------------------------------------------*/

    protected NotificationHandler() {/* Exists only to defeat instantiation.*/}


    public static List<Model_Notification> notifications = new ArrayList<>();

    public static void startNotificationThread(){
        terminal_logger.trace("startNotificationThread: starting");
        if(!send_notification_thread.isAlive()) send_notification_thread.start();
    }

    public static void addToQueue(Model_Notification notification){

        terminal_logger.debug("addToQueue: adding notification to queue");

        notifications.add(notification);

        if(send_notification_thread.getState() == Thread.State.TIMED_WAITING) {
            terminal_logger.trace("addToQueue: thread is sleeping, waiting for interruption!");
            send_notification_thread.interrupt();
        }
    }

    private static Thread send_notification_thread = new Thread() {

        @Override
        public void run() {


            terminal_logger.trace("send_notification_thread: concurrent thread started on {}", new Date()) ;

            while(true){
                try{

                    if(!notifications.isEmpty()) {

                        terminal_logger.debug("send_notification_thread: {} notifications to send", notifications.size());

                        Model_Notification notification = notifications.get(0);

                        sendNotification( notification );

                        notifications.remove( notification );

                    } else {

                        terminal_logger.trace("send_notification_thread:: no notifications, thread is going to sleep");

                        sleep(500000000);
                    }
                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    terminal_logger.internalServerError("send_notification_thread: ", e);
                }
            }
        }
    };

    private static void sendNotification(Model_Notification notification){
        try {

            terminal_logger.trace("sendNotification:: sending notification");

            ObjectNode message = Json.newObject();
            message.put("messageType", Model_Notification.messageType);
            message.put("messageChannel", WS_Becki_Website.CHANNEL);
            message.put("notification_type", notification.notification_type.name());
            message.put("notification_level", notification.notification_level.name());
            message.put("notification_importance", notification.notification_importance.name());
            message.put("state", notification.state.name());
            message.set("notification_body", Json.toJson(notification.notification_body()));
            message.put("confirmation_required", notification.confirmation_required);
            message.put("confirmed", notification.confirmation_required);
            message.put("was_read", notification.was_read);
            message.put("created", notification.created.getTime());
            message.set("buttons", Json.toJson(notification.buttons()) );

            terminal_logger.trace("sendNotification:: sending notification");

            terminal_logger.debug("  Počet příjemců je " + notification.list_of_ids_receivers.size());

            for (String person_id : notification.list_of_ids_receivers) {

                try {

                    terminal_logger.debug("      The number of recipients is " + person_id);

                    // Pokud je notification_importance vyšší než "low" notifikaci uložím
                    if ((notification.notification_importance != Enum_Notification_importance.low) && (notification.id == null)) {

                        notification.person = Model_Person.get_byId(person_id); // Get Person Model from Cache
                        notification.save_object();

                        message.put("id", notification.id);
                        terminal_logger.debug("      Notifikaci has own Notification ID:  " +notification.id);

                    }else {
                        if(notification.id == null) {

                            // TODO Tomáš - není zavádějící přidávat to JSONu id notifikace, když je to nesmyslná hodnota? Když se notifikace neuloží do databáze, tak není nutné znát id.
                            message.put("id", UUID.randomUUID().toString());
                            terminal_logger.debug("      Notification has not own id and its not set for save ID:" +  message.get("id").asText());
                        }else {
                            System.out.println("      Notification has own id and its not set for save ID: " +  notification.id);

                            // TODO proč 2x?
                            message.put("id", notification.id);
                            message.put("notification_id", notification.id);
                        }
                    }

                    // Pokud je uživatel přihlášený pošlu notifikaci přes websocket
                    if (Controller_WebSocket.becki_website.containsKey(person_id)) {
                        WS_Becki_Website becki = (WS_Becki_Website) Controller_WebSocket.becki_website.get(person_id);
                        becki.write_without_confirmation( message );
                    }

                } catch (NullPointerException e) {
                    terminal_logger.internalServerError("sendNotification:", e);
                }
            }

        }catch (Exception e){
            terminal_logger.internalServerError("sendNotification:", e);
        }
    }
}
