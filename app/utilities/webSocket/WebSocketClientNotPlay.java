package utilities.webSocket;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.WebSocketController_OutComing;
import play.libs.Json;

import javax.websocket.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@ClientEndpoint
public class WebSocketClientNotPlay extends Thread {

    private String identificator;
    private Thread thread;

    public Session session = null;

    public  Map<String, JsonNode> message_out = new HashMap<>(); // (meessageId, JsonNode)
    public  Map<String, JsonNode> message_in  = new HashMap<>(); // (meessageId, JsonNode)


    public WebSocketClientNotPlay(String identificator, URI serverURI , Thread thread) throws Exception {
        this.identificator = identificator;
        this.thread = thread;

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, serverURI);
    }


    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        WebSocketController_OutComing.servers.put(identificator, this);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println( "You have been disconnected: " + reason + "\n" );
        WebSocketController_OutComing.servers.remove(identificator);
        thread.interrupt();
    }

    @OnMessage
    public void onMessage(String message) {
        try {

          //  System.out.println("Příchozí zpráva: " + message);
            JsonNode json = Json.parse(message);
            String messageId  = json.get("messageId").asText();

            if(message_out.containsKey(messageId)){
                message_out.remove(messageId);
                message_in.put(messageId, json);
                return;
            }

            System.out.println("Pozor!!! Přišla zpráva, která neměla uložený identifikátor!!! Server bude sám reagovat!!! Až přijdeš na to jak se to děje - smaž tenhle text! ");
            WebSocketController_OutComing.incomingJson_PLAY_As_Client(identificator, Json.parse(message));
        }catch (Exception e){
           System.out.println("Chyba!!!!");
        }
    }


    // TODO dopsat proč to je takto implementováno!!!
    public JsonNode write(String messageId, JsonNode json) throws TimeoutException, InterruptedException {

        message_out.put(messageId, json);
        System.out.println("Co odesílám: " + json.toString());
        session.getAsyncRemote().sendText(json.toString());

        Integer breaker = 10;

        while(true){

            breaker--;
            Thread.sleep(250);

            if( message_in.containsKey(messageId)){
                JsonNode result =  message_in.get(messageId);
                message_in.remove(messageId);
                System.out.println("Co příjmám: " + result.toString());
                return result;
            }

            if(breaker == 0){
                message_out.remove(messageId);
                throw new TimeoutException();
            }

        }
    }


}

