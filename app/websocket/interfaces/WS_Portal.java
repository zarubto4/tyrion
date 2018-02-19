package websocket.interfaces;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import utilities.logger.Logger;
import websocket.WS_Interface;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WS_Portal {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(WS_Portal.class);

/* STATIC  -------------------------------------------------------------------------------------------------------------*/

    public static final String CHANNEL = "becki";
    public UUID person_id;

    public WS_Portal(UUID person_id) {
        this.person_id = person_id;
        Controller_WebSocket.portals.put(person_id, this);
    }

    /**
     * Holds all connections of Becki portals (Same user with multiple connection!)
     */
    public Map<UUID, WS_PortalSingle> all_person_connections = new HashMap<>();


    public void send(ObjectNode message) {
        all_person_connections.forEach((id, single) -> single.send(message));
    }


    public boolean isOnline() {
        return true;
    }


    public void onMessage(ObjectNode json) {
        // Nothing
        logger.error("onMessage:: illegal And not supported request from Becki:: ", json.toString());
    }


    public void close() {
        this.all_person_connections.forEach((id, single) -> single.close());
    }

    public void close(UUID token) {
        if (this.all_person_connections.containsKey(token)) {
            this.all_person_connections.get(token).close();
            this.all_person_connections.remove(token);
        }
    }

    public void onClose() {
        Controller_WebSocket.portals.remove(this.person_id);
    }
}
