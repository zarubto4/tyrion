package utilities.webSocket;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.WebSocketController;
import play.libs.Json;

import javax.websocket.*;
import java.net.URI;

@ClientEndpoint
public class WebSocketClientNotPlay  {

    private String identificator;
    private Thread thread;

    public Session session = null;
    public MessageHandler messageHandler;


    public WebSocketClientNotPlay(String identificator, URI serverURI , Thread thread) throws Exception {

        this.identificator = identificator;
        this.thread = thread;

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, serverURI);

    }


    @OnOpen
    public void onOpen(Session session) {
        this.session = session;

        WebSocketController.outcomingConnections.put(identificator, this);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println( "You have been disconnected: " + reason + "\n" );
        WebSocketController.outcomingConnections.remove(identificator);
        thread.interrupt();
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            WebSocketController.incomingJson_PLAY_As_Client(identificator, Json.parse(message));
        }catch (Exception e){
            session.getAsyncRemote().sendText("Its not JSON! -> " + message);
        }
    }

    public void write(JsonNode json) {
        session.getAsyncRemote().sendText(json.asText());
    }
    public void write(String text)   { session.getAsyncRemote().sendText(text);}


}

