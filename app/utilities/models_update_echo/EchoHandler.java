package utilities.models_update_echo;

import utilities.logger.Logger;
import websocket.messages.tyrion_with_becki.WSM_Echo;

/**
 * Slouží k zasílání informací o updatu do Becki kde se zašle ID společně s typem objektu.
 * Becki si objekt stromovou hierarchí přepíše a aktualizuje.
 */
public class EchoHandler {
    
/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(EchoHandler.class);

/* METHOD  -------------------------------------------------------------------------------------------------------------*/

    protected EchoHandler() {/* Exists only to defeat instantiation.*/}


    public static void addToQueue(WSM_Echo message) {

    }

}
