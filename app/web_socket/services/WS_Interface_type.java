package web_socket.services;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.WebSocket;
import utilities.logger.Class_Logger;
import web_socket.message_objects.common.WS_Send_message;

import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;



public abstract class WS_Interface_type {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/
    private static final Class_Logger terminal_logger = new Class_Logger(WS_Interface_type.class);

    public WS_Interface_type webSCtype;
    public WebSocket.Out<String> out;

    public abstract void onClose(); //  Určeno pro možnost výběru, přes kterou metodu v controlleru se pokyn vykoná. Především proto, aby na to mohl cloud_blocko_server globálně reagovat, uzavřel ostatní vlákna atd.
    public void close(){ if(out != null) out.close(); }
    public abstract void onMessage(ObjectNode json);
    public abstract String get_identificator();
    public boolean isReady(){
        return out != null;
    }
    public abstract void add_to_map();



    public void onMessage(String message){
        try {

            terminal_logger.debug("Incoming message: " + message);

            ObjectNode json = (ObjectNode) new ObjectMapper().readTree(message);

            // V případě že zpráva byla odeslaná Tyironem - existuje v zásobníku její objekt
            if (json.has("messageId") && sendMessageMap.containsKey(json.get("messageId").asText())) {
                sendMessageMap.get(json.get("messageId").asText()).insert_result(json);
                return;
            }

            onMessage(json);

        }catch (JsonParseException e){
            terminal_logger.internalServerError(e);

            ObjectNode result = Json.newObject();
            result.put("messageType", "JsonUnrecognized JsonParseException");
            webSCtype.write_without_confirmation(result);

        }catch (Exception e){

            terminal_logger.internalServerError(e);

            ObjectNode result = Json.newObject();
            result.put("messageType", "JsonUnrecognized " + e.getClass().getSimpleName());
            webSCtype.write_without_confirmation(result);

        }

    }

    public WebSocket<String> connection() {
        return new WebSocket<String>() {

            public void onReady(final WebSocket.In<String> in, final WebSocket.Out<String> o) {

                add_to_map();
                out = o;

                in.onMessage(message -> {  onMessage(message);  });

                in.onClose(() -> { onClose();  });
            }
        };
    }


    /**
     * Odesílání zpráv: Zprávy lze odesílat s vyžadovanou odpovědí, nebo bez ní. Pokud vyžaduji odpověď (jako potvrzení
     * že se akce povedla, nebo co se událo v reakci na zprávu), spustí se vlákno v metodě write_with_confirmation. Odeslaná
     * zpráva má unikátní číslo, které se uloží do zásobníku odeslaných odpovědí.
     * Vlákno se na chvíli uspí..  metoda onMessage, kam chodí odpovědi zjistí-li, že bylo uloženo do zásobníku odeslaných
     * zpráv nějaké ID, zprávu dále nepřeposílá a pouze danou zprávu uloží do zásobníku příchozích zpráv,
     * kde jí vlákno v intervalech hledá. Tam si jí vlákno taktéž vyzvedne. Pokud
     * nedojde k během určitého intervalu k odovědi, vláknu vyprší životnost a zavolá vyjímku TimeoutException.
     */

    public Map<String,WS_Send_message> sendMessageMap = new HashMap<>(); // MessageId, Message


    public ObjectNode write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries) throws TimeoutException, ClosedChannelException, ExecutionException, InterruptedException {

        String messageId = UUID.randomUUID().toString();
        json.put("messageId", messageId );

        WS_Send_message send_message = new WS_Send_message(webSCtype, json, messageId, time, delay, number_of_retries);
        sendMessageMap.put(messageId, send_message);

        // Může vyvolat i vyjímku o nedoručení
        return  send_message.send_with_response();

    }


    // Odeslání bez nutnosti vyčkat na potvrzení
    public void write_without_confirmation(ObjectNode json){
        if(!json.has("messageId")) json.put("messageId", UUID.randomUUID().toString() );
        out.write( json.toString() );
    }

    public void write_without_confirmation(String messageId, ObjectNode json){
        json.put("messageId", messageId );
        out.write( json.toString() );
    }





}
