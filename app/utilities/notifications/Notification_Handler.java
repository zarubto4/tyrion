package utilities.notifications;


import controllers.Controller_WebSocket;
import models.Model_Notification;
import models.Model_Invitation;
import models.Model_Person;
import utilities.enums.Enum_Notification_action;
import utilities.enums.Enum_Notification_importance;
import web_socket.services.WS_Becki_Website;

import java.util.ArrayList;
import java.util.List;

public class Notification_Handler {

    protected Notification_Handler() {/* Exists only to defeat instantiation.*/}

    // Logger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public static List<Model_Notification> notifications = new ArrayList<>();

    public static void start_notification_thread(){
        logger.debug("Notification Handler will be started");
        if(!send_notification_thread.isAlive()) send_notification_thread.start();
    }

    public static void add_to_queue(Model_Notification notification){

        logger.debug("Notification - new incoming procedure");

        notifications.add(notification);

        if(send_notification_thread.getState() == Thread.State.TIMED_WAITING) {
            logger.debug("Thread is sleeping - wait for interrupt!");
            send_notification_thread.interrupt();
        }
    }

    static Thread send_notification_thread = new Thread() {

        @Override
        public void run() {


            logger.info("Independent Thread in Notification Handler now working") ;

            while(true){
                try{

                    if(!notifications.isEmpty()) {

                        logger.debug("Notification Handler Thread is running. Tasks to solve: " + notifications.size() );

                        Model_Notification notification = notifications.get(0);

                        new Notification_Handler().send_notification( notification , notification.receivers );
                        notifications.remove( notification );

                    }

                    else{
                        logger.debug("Notification Handler Thread has no other tasks. Going to sleep!");
                        sleep(500000000);
                    }



                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    logger.error("Notification Handler Error", e);
                }
            }
        }
    };

    public void send_notification(Model_Notification notification, List<Model_Person> receivers){

        for (Model_Person person : receivers) {

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
                    logger.error("Notification Handler Error: Cannot find project invitation about which is this notification.");
                }
            }

            // Pokud je uživatel přihlášený pošlu notifikaci přes websocket
            if (Controller_WebSocket.becki_website.containsKey(person.id)) {
                WS_Becki_Website becki = (WS_Becki_Website) Controller_WebSocket.becki_website.get(person.id);
                becki.becki_sendNotification(notification);
            }

            notification.id = null;
        }
    }
}
