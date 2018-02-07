package utilities.models_update_echo;

import controllers.Controller_WebSocket;
import models.Model_Project;
import utilities.logger.Logger;
import websocket.interfaces.WS_Portal;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Slouží k zasílání informací o updatu do Becki kde se zašle ID společně s typem objektu.
 * Becki si objekt stromovou hierarchí přepíše a aktualizuje.
 */
public class EchoHandler {
    
/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(EchoHandler.class);

/* METHOD  -------------------------------------------------------------------------------------------------------------*/

    protected EchoHandler() {/* Exists only to defeat instantiation.*/}

    public static List<WSM_Echo> update_messages = new ArrayList<>();

    public static void startThread() {
        logger.info("startThread - starting");
        if (!send_update_messages_thread.isAlive()) send_update_messages_thread.start();
    }

    public static void addToQueue(WSM_Echo message) {

        logger.info("addToQueue - adding update to queue");

        update_messages.add(message);

        if (send_update_messages_thread.getState() == Thread.State.TIMED_WAITING) {
            logger.debug("addToQueue - thread is sleeping, waiting for interruption!");
            send_update_messages_thread.interrupt();
        }
    }

    private static Thread send_update_messages_thread = new Thread() {

        @Override
        public void run() {
            while (true) {
                try {

                    if (!update_messages.isEmpty()) {

                        logger.trace("send_update_messages_thread - in que is " + update_messages.size());

                        //Get
                        WSM_Echo message = update_messages.get(0);

                        //Send
                        sendUpdate(message);

                        //
                        sleep(10);

                        //Remove
                        update_messages.remove(message);

                    } else {

                        logger.debug("send_update_messages_thread - no notifications, thread is going to sleep");
                        sleep(500000000);
                    }
                } catch (InterruptedException i) {
                    // Do nothing
                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }
        }
    };

    private static void sendUpdate(WSM_Echo message) {
        try {
            
            logger.trace("sendUpdate - sending update");

            List<UUID> list = Model_Project.get_project_becki_person_ids_list(message.project_id);

            for (UUID person_id : list) {
                try {

                    // Pokud je uživatel přihlášený pošlu notifikaci přes websocket
                    if (Controller_WebSocket.portals.containsKey(person_id)) {

                        WS_Portal becki = Controller_WebSocket.portals.get(person_id);
                        becki.send(message.make_request());

                    } else {
                        Model_Project.becki_person_id_unsubscribe(person_id);
                    }

                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }
}
