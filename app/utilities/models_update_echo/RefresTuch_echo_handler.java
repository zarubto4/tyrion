package utilities.models_update_echo;

import controllers.Controller_WebSocket;
import models.Model_Project;
import utilities.logger.Class_Logger;
import web_socket.message_objects.tyrion_with_becki.WS_Message_RefreshTuch;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;
import web_socket.services.WS_Becki_Website;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Slouží k zasílání informací o updatu do Becki kde se zašle ID společně s typem objektu.
 * Becki si objekt stromovou hierarchí přepíše a aktualizuje.
 */
public class RefresTuch_echo_handler {

    
/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(RefresTuch_echo_handler.class);

/* METHOD  -------------------------------------------------------------------------------------------------------------*/

    protected RefresTuch_echo_handler() {/* Exists only to defeat instantiation.*/}

    public static List<WS_Message_RefreshTuch> update_messages = new ArrayList<>();

    public static void startEchoUpdateThread(){
        terminal_logger.info("RefresTuch_echo:: startEchoUpdateThread: starting");
        if(!send_update_messages_thread.isAlive()) send_update_messages_thread.start();
    }

    public static void addToQueue(WS_Message_RefreshTuch message){

        terminal_logger.info("RefresTuch_echo:: addToQueue: adding notification to queue");

        update_messages.add(message);

        if(send_update_messages_thread.getState() == Thread.State.TIMED_WAITING) {
            terminal_logger.debug("NotificationHandler:: addToQueue: thread is sleeping, waiting for interruption!");
            send_update_messages_thread.interrupt();
        }else {
            terminal_logger.debug("NotificationHandler:: addToQueue: thread not sleeping");
        }
    }

    private static Thread send_update_messages_thread = new Thread() {

        @Override
        public void run() {


            terminal_logger.info("RefresTuch_echo:: send_notification_thread: concurrent thread started on {}", new Date()) ;

            while(true){
                try{

                    if(!update_messages.isEmpty()) {

                        terminal_logger.trace("RefresTuch_echo:: send_update_messages_thread: in que is " + update_messages.size());

                        //Get
                        WS_Message_RefreshTuch message = update_messages.get(0);

                        //Send
                        sendUpdate( message );

                        //
                        sleep(10);

                        //Remove
                        update_messages.remove(message);

                    } else {

                        terminal_logger.debug("RefresTuch_echo:: send_update_messages_thread: no notifications, thread is going to sleep");
                        sleep(500000000);
                    }
                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    terminal_logger.internalServerError(e);
                }
            }
        }
    };


    private static void sendUpdate(WS_Message_RefreshTuch message){

        terminal_logger.trace("NotificationHandler:: sendNotification:: sending notification");

        for (String person_id : message.person_ids) {

                try {

                    // Pokud je uživatel přihlášený pošlu notifikaci přes websocket
                    if (Controller_WebSocket.becki_website.containsKey(person_id)) {

                        WS_Becki_Website becki = Controller_WebSocket.becki_website.get(person_id);
                        becki.write_without_confirmation(message.make_request());

                    }else {
                        Model_Project.becki_person_id_unsubscribe(person_id);
                    }

                }catch (Exception e){
                    terminal_logger.internalServerError(e);
                }
        }

    }



}
