package utilities.webSocket.developing;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.WebSocketController_Incoming;
import play.libs.Json;
import play.mvc.WebSocket;

import java.io.File;
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

    public abstract void onClose(); //  Určeno pro možnost výběru, přes kterou metodu v controlleru se pokyn vykoná. Především proto, aby na to mohl server globálně reagovat, uzavřel ostatní vlákna atd.
    public void close(){out.close();}
    public abstract void onMessage(JsonNode json);
    public boolean isReady(){
        return out != null;
    }


    public void onMessage(String message){
        try {
            JsonNode json = Json.parse(message);


             if(json.has("messageId") && message_out.containsKey( json.get("messageId").asText())){
                    message_out.remove(json.get("messageId").asText() );
                    message_in.put(json.get("messageId").asText(), json);
                    return;
             }

            onMessage(json);

        }catch (Exception e){
            WebSocketController_Incoming.invalid_json_message(webSCtype);
        }

    }


    public WebSocket<String> connection() {
        return new WebSocket<String>() {

            public void onReady(final WebSocket.In<String> in, final WebSocket.Out<String> o) {

                maps.put(identifikator, webSCtype);
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

    public  Map<String, JsonNode> message_out = new HashMap<>(); // (meessageId, JsonNode)
    public  Map<String, JsonNode> message_in  = new HashMap<>(); // (meessageId, JsonNode)

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

            if(breaker == 0) {
                System.out.println("Time out Exception");
                message_out.remove(messageId);
                throw new TimeoutException();
            }

        }
    }

    public void send_file(File file){
        out.write("asdfsd");

    }

    public void write_without_confirmation(JsonNode json) {

            Thread thread = new Thread(){

                @Override
                public void run() {
                    try {

                        Integer breaker = 10;

                        while(breaker > 0){
                            breaker--;
                            if(isReady()) {
                                out.write( json.toString() );
                                break;
                            }

                            Thread.sleep(250);
                        }

                    }catch (Exception e){
                        System.out.println("JSON Nebylo možné odeslat: " + json.toString());
                    }

                }
            };

            thread.start();

    }
}
