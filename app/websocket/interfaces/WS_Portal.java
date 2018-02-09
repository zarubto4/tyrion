package websocket.interfaces;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import utilities.logger.Logger;
import websocket.WS_Interface;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WS_Portal extends WS_Interface {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(WS_Portal.class);

/* STATIC  -------------------------------------------------------------------------------------------------------------*/

    public static final String CHANNEL = "becki";

    public static Props props(ActorRef out) {
        return Props.create(WS_Portal.class, out);
    }

    public WS_Portal(UUID id) {
        super(null);
        this.id = id;
        Controller_WebSocket.portals.put(this.id, this);
    }

    /**
     * Holds all connections of Becki portals
     */
    public Map<UUID, WS_PortalSingle> singles = new HashMap<>();

    @Override
    public void send(ObjectNode message) {
        singles.forEach((id, single) -> single.send(message));
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public void onMessage(ObjectNode json) {
        // TODO
    }

    @Override
    public void close() {
        this.singles.forEach((id, single) -> single.close());
    }

    public void close(UUID token) {
        if (this.singles.containsKey(token)) {
            this.singles.get(token).close();
            this.singles.remove(token);
        }
    }

    @Override
    public void onClose() {
        Controller_WebSocket.portals.remove(this.id);
    }
}
