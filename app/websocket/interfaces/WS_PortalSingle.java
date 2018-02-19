package websocket.interfaces;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import utilities.logger.Logger;
import websocket.WS_Interface;

import java.util.UUID;

public class WS_PortalSingle extends WS_Interface {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(WS_PortalSingle.class);

/* STATIC  -------------------------------------------------------------------------------------------------------------*/

    public static Props props(ActorRef out, WS_Portal portal, UUID id) {
        return Props.create(WS_PortalSingle.class, out, portal, id);
    }

    private WS_Portal portal;

    public WS_PortalSingle(ActorRef out, WS_Portal portal, UUID id) {
        super(out);
        this.id = id;
        this.portal = portal;
        this.portal.all_person_connections.put(this.id, this);
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public void onMessage(ObjectNode json) {
        this.portal.onMessage(json);
    }

    @Override
    public void onClose() {
        logger.trace("onClose - single portal connection: {} was closed", this.id);
        this.portal.all_person_connections.remove(this.id);
    }
}
