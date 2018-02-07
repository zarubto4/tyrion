package websocket.interfaces;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import utilities.logger.Logger;
import websocket.WS_Interface;

public class WS_PortalSingle extends WS_Interface {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(WS_PortalSingle.class);

/* STATIC  -------------------------------------------------------------------------------------------------------------*/

    public static Props props(ActorRef out, WS_Portal portal) {
        return Props.create(WS_PortalSingle.class, out, portal);
    }

    private WS_Portal portal;

    public WS_PortalSingle(ActorRef out, WS_Portal portal) {
        super(out);
        this.portal = portal;
        this.portal.singles.put(this.id, this);
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public void onMessage(ObjectNode json) {
        this.portal.onMessage(json);
    }

    @Override
    public void onClose() {
        this.portal.singles.remove(this.id);
    }
}
