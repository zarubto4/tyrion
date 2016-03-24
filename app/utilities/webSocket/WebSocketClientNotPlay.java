package utilities.webSocket;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.WebSocketController_Incoming;
import utilities.webSocket.developing.WebSCType;

import javax.websocket.*;
import java.net.URI;

@ClientEndpoint
public class WebSocketClientNotPlay extends WebSCType {

    private String identificator;
    private Thread thread;
    public Session session = null;

    public WebSocketClientNotPlay(String identificator, URI serverURI , Thread thread) throws Exception {
        this.identificator = identificator;
        this.thread = thread;

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, serverURI);

    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        WebSocketController_Incoming.cloud_servers.put(identificator, null);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println( "You have been disconnected: " + reason + "\n" );
        WebSocketController_Incoming.cloud_servers.remove(identificator);
        thread.interrupt();
    }

    @Override
    public void onClose() { System.out.println("Nikdy nevyužívaná metoda!!"); }

    @OnMessage
    public void onMessage(String message) { super.onMessage(message); }

    @Override
    public void onMessage(JsonNode json) {
        WebSocketController_Incoming.incoming_message_homer(this, json);
    }

}

