package utilities.webSocket.developing;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.WebSocketController_Incoming;
import play.libs.Json;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

public class WS_Homer_Cloud extends WebSCType {

    String server_name;
    private Thread thread;

    public Session session = null;

    public WS_Homer_Cloud (String server_name, String identifikator, URI serverURI , Thread thread) throws Exception {
        super.identifikator = identifikator;
        this.thread = thread;

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, serverURI);
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        WebSocketController_Incoming.cloud_servers.get(server_name).put(super.identifikator, this);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println( "Cloud Homer been disconnected: " + reason + "\n" );
        WebSocketController_Incoming.cloud_servers.remove(server_name);
        thread.interrupt();
    }

    @OnMessage
    public void onMessage(String message) {
        try {

            //  System.out.println("Příchozí zpráva: " + message);
            JsonNode json = Json.parse(message);
            String messageId  = json.get("messageId").asText();

            if(super.message_out.containsKey(messageId)){
                super.message_out.remove(messageId);
                super.message_in.put(messageId, json);
                return;
            }

            System.out.println("Pozor!!! Přišla zpráva, která neměla uložený identifikátor!!! Server bude sám reagovat!!! Až přijdeš na to jak se to děje - smaž tenhle text! ");
            WebSocketController_Incoming.incoming_message_homer(this, json);
        }catch (Exception e){
            System.out.println("Chyba!!!!");
        }
    }

    @Override
    public void write_without_confirmation(JsonNode json) {
       try {
            System.out.println("Zprávu zasílám na " + server_name + " : " + identifikator);
            session.getBasicRemote().sendText( json.toString() );


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public void onClose() {}
    @Override public void onMessage(JsonNode json) {}
}
