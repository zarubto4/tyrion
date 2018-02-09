package websocket.interfaces;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import utilities.logger.Logger;
import websocket.WS_Interface;

import java.util.UUID;

public class WS_Compiler extends WS_Interface {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(WS_Compiler.class);

/* STATIC  -------------------------------------------------------------------------------------------------------------*/

    public static Props props(ActorRef out, UUID id) {
        return Props.create(WS_Compiler.class, out, id);
    }

    public WS_Compiler(ActorRef out, UUID id) {
        super(out);
        this.id = id;
        Controller_WebSocket.compilers.put(this.id, this);
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public void onMessage(ObjectNode json) {
        logger.warn("onMessage - no message should drop down here, all messages from compiler should be caught as response, message: {}", json.toString());
    }

    @Override
    public void onClose() {
        Controller_WebSocket.compilers.remove(this.id);
    }
}
