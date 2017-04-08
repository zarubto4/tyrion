package utilities.notifications;


import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.Model_Invitation;
import models.Model_Notification;
import models.Model_Person;
import play.libs.Json;
import utilities.enums.Enum_Notification_action;
import utilities.enums.Enum_Notification_importance;
import utilities.loggy.Loggy;
import web_socket.services.WS_Becki_Website;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NotificationHandler {

    protected NotificationHandler() {/* Exists only to defeat instantiation.*/}

    // Logger
    private static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public static List<Model_Notification> notifications = new ArrayList<>();

    public static void startNotificationThread(){
        logger.trace("NotificationHandler:: startNotificationThread:_ starting");
        if(!send_notification_thread.isAlive()) send_notification_thread.start();
    }

    public static void addToQueue(Model_Notification notification){

        logger.debug("NotificationHandler:: addToQueue:: adding notification to queue");

        notifications.add(notification);

        if(send_notification_thread.getState() == Thread.State.TIMED_WAITING) {
            logger.trace("NotificationHandler:: addToQueue:: thread is sleeping, waiting for interruption!");
            send_notification_thread.interrupt();
        }
    }

    private static Thread send_notification_thread = new Thread() {

        @Override
        public void run() {


            logger.trace("NotificationHandler:: send_notification_thread:: concurrent thread started on {}", new Date()) ;

            while(true){
                try{

                    if(!notifications.isEmpty()) {

                        logger.debug("Beru notifikaci z Listku:: Počet notifikací v listu je:: " + notifications.size());

                        Model_Notification notification = notifications.get(0);

                        sendNotification( notification );

                        notifications.remove( notification );

                        logger.debug("Beru notifikaci z Listku:: Smazal jsem odeslanou  notifikací z listu a teď jich je:: " + notifications.size());


                    } else {

                        logger.trace("NotificationHandler:: send_notification_thread:: no notifications, thread is going to sleep");

                        sleep(500000000);
                    }
                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    Loggy.internalServerError("NotificationHandler:: send_notification_thread: ", e);
                }
            }
        }
    };

    private static void sendNotification(Model_Notification notification){

        try {

            logger.trace("NotificationHandler:: sendNotification:: sending notification");

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


            for (String person_id : notification.list_of_ids_receivers) {

                try {
                    // Pokud je notification_importance vyšší než "low" notifikaci uložím
                    if ((notification.notification_importance != Enum_Notification_importance.low) && (notification.id == null)) {


                        notification.person = Model_Person.get_byId(person_id); // Get Person Model from Cache
                        notification.save_object();


                        message.put("id", notification.id);

                        try {
                            if ((!notification.buttons().isEmpty()) && (notification.buttons().get(0).action == Enum_Notification_action.accept_project_invitation)) {
                                Model_Invitation invitation = Model_Invitation.find.byId(notification.buttons().get(0).payload);
                                invitation.notification_id = notification.id;
                                invitation.update();
                            }
                        } catch (Exception e) {
                            Loggy.internalServerError("NotificationHandler:: sendNotification:: Error:: ", e);
                        }

                    }else {
                        message.put("id", UUID.randomUUID().toString());
                    }

                    // Pokud je uživatel přihlášený pošlu notifikaci přes websocket
                    if (Controller_WebSocket.becki_website.containsKey(person_id)) {
                        WS_Becki_Website becki = (WS_Becki_Website) Controller_WebSocket.becki_website.get(person_id);

                        becki.write_without_confirmation( message );
                    }


                    notification.id = null;

                } catch (NullPointerException e) {
                    logger.error("NotificationHandler:: SendNotification inside for void Error:: ", e);
                }
            }

        }catch (Exception e){
            logger.error("NotificationHandler:: SendNotification void Error: ", e);
        }
    }
}
