package utilities.models_update_echo;

import controllers.Controller_WebSocket;
import models.Model_Project;
import utilities.logger.Class_Logger;
import utilities.login_entities.Secured_Admin;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;
import web_socket.services.WS_Becki_Website;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Update_echo_handler {

    
/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Update_echo_handler.class);

/* METHOD  -------------------------------------------------------------------------------------------------------------*/

    protected Update_echo_handler() {/* Exists only to defeat instantiation.*/}

    public static List<WS_Message_Update_model_echo> update_messages = new ArrayList<>();

    public static void startEchoUpdateThread(){
        terminal_logger.info("UpdateEcho:: startNotificationThread: starting");
        if(!send_update_messages_thread.isAlive()) send_update_messages_thread.start();
    }

    public static void addToQueue(WS_Message_Update_model_echo message){

        terminal_logger.info("UpdateEcho:: addToQueue: adding notification to queue");

        update_messages.add(message);

        if(send_update_messages_thread.getState() == Thread.State.TIMED_WAITING) {
            terminal_logger.trace("NotificationHandler:: addToQueue: thread is sleeping, waiting for interruption!");
            send_update_messages_thread.interrupt();
        }
    }

    private static Thread send_update_messages_thread = new Thread() {

        @Override
        public void run() {


            terminal_logger.info("UpdateEcho:: send_notification_thread: concurrent thread started on {}", new Date()) ;

            while(true){
                try{

                    if(!update_messages.isEmpty()) {

                        terminal_logger.trace("UpdateEcho:: send_update_messages_thread: in que is " + update_messages.size());

                        //Get
                        WS_Message_Update_model_echo message = update_messages.get(0);

                        //Send
                        sendUpdate( message );

                        //
                        sleep(10);

                        //Remove
                        update_messages.remove(message);

                    } else {

                        terminal_logger.debug("UpdateEcho:: send_update_messages_thread: no notifications, thread is going to sleep");
                        sleep(500000000);
                    }
                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    terminal_logger.internalServerError("UpdateEcho:: send_update_messages_thread:", e);
                }
            }
        }
    };


    private static void sendUpdate(WS_Message_Update_model_echo message){

        terminal_logger.trace("NotificationHandler:: sendNotification:: sending notification");


        List<String> list = Model_Project.get_project_becki_person_ids_list(message.project_id);

        for (String person_id : list) {

                try {

                    // Pokud je uživatel přihlášený pošlu notifikaci přes websocket
                    if (Controller_WebSocket.becki_website.containsKey(person_id)) {

                        WS_Becki_Website becki = (WS_Becki_Website) Controller_WebSocket.becki_website.get(person_id);
                        becki.write_without_confirmation(message.get_request());

                    }else {
                        Model_Project.becki_person_id_unsubscribe(person_id);
                    }

                }catch (Exception e){
                    terminal_logger.internalServerError("NotificationHandler:: sendNotification:: Error", e);
                }
        }

    }



}
