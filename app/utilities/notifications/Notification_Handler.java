package utilities.notifications;


import controllers.WebSocketController;
import models.notification.Notification;
import models.person.Person;
import utilities.enums.Notification_importance;
import utilities.webSocket.WS_Becki_Website;

import java.util.ArrayList;
import java.util.List;

public class Notification_Handler {

    protected Notification_Handler() {/* Exists only to defeat instantiation.*/}

    // Logger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public static List<Notification> notifications = new ArrayList<>();

    public static void start_notification_thread(){
        logger.debug("Notification Handler will be started");
        send_notification_thread.start();
    }

    public static void add_to_queue(Notification notification){

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

                        Notification notification = notifications.get(0);

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

    public void send_notification(Notification notification, List<Person> receivers){

        for (Person person : receivers) {

            // Pokud je notification_importance vyšší než "low" notifikaci uložím
            if (notification.notification_importance != Notification_importance.low) {

                notification.person = person;
                notification.save_object();
            }

            // Pokud je uživatel přihlášený pošlu notifikaci přes websocket
            if (WebSocketController.becki_website.containsKey(person.id)) {
                WebSocketController.becki_sendNotification((WS_Becki_Website) WebSocketController.becki_website.get(person.id), notification);
            }
        }
    }
}
