package utilities.notifications;


import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.Model_Notification;
import models.Model_Invitation;
import models.Model_Person;
import org.codehaus.jackson.map.ObjectMapper;
import utilities.enums.Enum_Notification_action;
import utilities.enums.Enum_Notification_importance;
import utilities.loggy.Loggy;
import web_socket.services.WS_Becki_Website;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        logger.trace("NotificationHandler:: addToQueue:: adding notification to queue");

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

                        Model_Notification notification = notifications.get(0);

                        sendNotification( notification );
                        notifications.remove( notification );

                    } else {

                        logger.trace("NotificationHandler:: send_notification_thread:: no notifications, thread is going to sleep");

                        sleep(500000000);
                    }
                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    Loggy.internalServerError("NotificationHandler:: send_notification_thread:", e);
                }
            }
        }
    };

    private static void sendNotification(Model_Notification notification){

        logger.trace("NotificationHandler:: sendNotification:: sending notification");

        for (Model_Person person : notification.receivers) {

            // Pokud je notification_importance vyšší než "low" notifikaci uložím
            if ((notification.notification_importance != Enum_Notification_importance.low)&&(notification.id == null)) {

                notification.person = person;
                notification.save_object();

                try {
                    if((!notification.buttons().isEmpty())&&(notification.buttons().get(0).action == Enum_Notification_action.accept_project_invitation)){

                        Model_Invitation invitation = Model_Invitation.find.byId(notification.buttons().get(0).payload);
                        invitation.notification_id = notification.id;
                        invitation.update();
                    }
                }catch (Exception e){
                    Loggy.internalServerError("NotificationHandler:: sendNotification:: Error", e);
                }
            }

            // Pokud je uživatel přihlášený pošlu notifikaci přes websocket
            if (Controller_WebSocket.becki_website.containsKey(person.id)) {
                WS_Becki_Website becki = (WS_Becki_Website) Controller_WebSocket.becki_website.get(person.id);
                becki.write_without_confirmation(new ObjectMapper().convertValue(notification, ObjectNode.class));
            }

            notification.id = null;
        }
    }
}
