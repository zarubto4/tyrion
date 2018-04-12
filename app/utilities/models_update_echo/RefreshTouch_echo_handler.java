package utilities.models_update_echo;

import controllers.Controller_WebSocket;
import models.Model_Project;
import utilities.logger.Logger;
import websocket.interfaces.WS_Portal;
import websocket.messages.tyrion_with_becki.WS_Message_RefreshTouch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Slouží k zasílání informací o updatu do Becki kde se zašle ID společně s typem objektu.
 * Becki si objekt stromovou hierarchí přepíše a aktualizuje.
 */
public class RefreshTouch_echo_handler {

    
/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(RefreshTouch_echo_handler.class);

/* METHOD  -------------------------------------------------------------------------------------------------------------*/

    protected RefreshTouch_echo_handler() {/* Exists only to defeat instantiation.*/}

    public static List<WS_Message_RefreshTouch> update_messages = new ArrayList<>();

    public static void startThread() {
        logger.info("startEchoUpdateThread - starting");
        if (!send_update_messages_thread.isAlive()) send_update_messages_thread.start();
    }

    public static void addToQueue(WS_Message_RefreshTouch message) {

        logger.info("addToQueue - adding touch to queue");

        update_messages.add(message);

        if (send_update_messages_thread.getState() == Thread.State.TIMED_WAITING) {
            logger.debug("addToQueue - thread is sleeping, waiting for interruption!");
            send_update_messages_thread.interrupt();
        } else {
            logger.debug("addToQueue - thread not sleeping");
        }
    }

    private static Thread send_update_messages_thread = new Thread() {

        @Override
        public void run() {


            logger.info("send_update_messages_thread - concurrent thread started on {}", new Date()) ;

            while(true) {
                try {

                    if (!update_messages.isEmpty()) {

                        logger.trace("send_update_messages_thread - queue size {}", update_messages.size());

                        //Get
                        WS_Message_RefreshTouch message = update_messages.get(0);

                        //Send
                        sendUpdate( message );

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

    private static void sendUpdate(WS_Message_RefreshTouch message) {

        logger.trace("sendUpdate - sending notification");

        for (UUID person_id : message.person_ids) {
            try {

                System.out.println("Kontroluji zda je uživatel přihlášen");
                // Pokud je uživatel přihlášený pošlu notifikaci přes websocket
                if (Controller_WebSocket.portals.containsKey(person_id)) {
                    System.out.println("Ano  - uživatel přihlášen je ");

                    WS_Portal becki = Controller_WebSocket.portals.get(person_id);

                    System.out.println("Našel jsem Becki Entitu ");

                    System.out.println("Posílám");
                    becki.send(message.make_request());

                } else {
                    System.out.println("Ne uživatel přihlášen není");
                    Model_Project.becki_person_id_unsubscribe(person_id);
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }
    }
}
