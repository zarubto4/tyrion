package utilities.webSocket.developing;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.WebSocketController_Incoming;
import play.libs.Json;
import play.mvc.WebSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public abstract class WebSCType {

    public Map<String, WebSCType> maps;
    public List<WebSCType> subscribers = new ArrayList<>();

    public WebSCType webSCtype;
    public WebSocket.Out<String> out;
    public String identifikator;

    public abstract void onClose();

    public abstract void onMessage(JsonNode json);
    public boolean isReady(){
        return out != null;
    }

    public void close(){out.close();}

    public WebSocket<String> connection() {
        return new WebSocket<String>() {

            public void onReady(final WebSocket.In<String> in, final WebSocket.Out<String> o) {

                maps.put(identifikator, webSCtype);
                out = o;

                in.onMessage(message -> {

                        try {
                            System.out.println("příchozí zpráva na WS: " + message);
                            JsonNode json = Json.parse(message);

                            if( json.has("messageId")){

                                String messageId  = json.get("messageId").asText();

                                if(message_out.containsKey(messageId)){
                                    System.out.println("příchozí zpráva obsahuje klíč a je v zásobníku vyžadovaných odpovědí");
                                    message_out.remove(messageId);
                                    message_in.put(messageId, json);
                                    return;
                                }
                            }

                            System.out.println("příchozí buď neobsahovala klíč, nebo na ní nebylo vyžadováno potvrzení");
                            onMessage(json);

                        }catch (Exception e){
                            System.out.println("příchozí zpráva není ve validním formátu JSON: " + message);
                            WebSocketController_Incoming.invalid_json_message(webSCtype);
                        }

                });

                in.onClose(() -> {

                    System.out.println("Socket " + identifikator + " ztratil spojení!");
                    onClose();
                });
            }
        };
    }

    private  Map<String, JsonNode> message_out = new HashMap<>(); // (meessageId, JsonNode)
    private  Map<String, JsonNode> message_in  = new HashMap<>(); // (meessageId, JsonNode)

    public JsonNode write_with_confirmation(String messageId, JsonNode json) throws TimeoutException, InterruptedException {
        System.out.println("Odesílám zprávu [" + messageId + "], na kterou požaduji potvrzení. Zpráva: " + json.toString());

        message_out.put(messageId, json);
        out.write(json.toString());

        Integer breaker = 10;

        while(true){
            breaker--;
            Thread.sleep(250);

            if( message_in.containsKey(messageId)){
                JsonNode result =  message_in.get(messageId);
                message_in.remove(messageId);
                return result;
            }

            if(breaker == 0){
                System.out.println("Time out Exception");
                message_out.remove(messageId);
                throw new TimeoutException();
            }

        }
    }

    public void write_without_confirmation(JsonNode json) {
        System.out.println("Zprávu zasílám na " + identifikator);
        out.write(json.toString());
    }
}
