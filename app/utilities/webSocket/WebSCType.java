package utilities.webSocket;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController_Incoming;
import play.mvc.WebSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public abstract class WebSCType {

    public Map<String, WebSCType> maps;
    public List<WebSCType> subscribers = new ArrayList<>();

    public WebSCType webSCtype;
    public WebSocket.Out<String> out;
    public String identifikator;

    public abstract void onClose(); //  Určeno pro možnost výběru, přes kterou metodu v controlleru se pokyn vykoná. Především proto, aby na to mohl server globálně reagovat, uzavřel ostatní vlákna atd.
    public void close(){ if(out != null) out.close(); }
    public abstract void onMessage(ObjectNode json);
    public boolean isReady(){
        return out != null;
    }


    public void onMessage(String message){
        try {

            System.out.println("Příchozí zpráva " + message);

            ObjectNode json = (ObjectNode) new ObjectMapper().readTree(message);


            if (json.has("messageId") && message_out.containsKey(json.get("messageId").asText())) {
                message_out.remove(json.get("messageId").asText());
                message_in.put(json.get("messageId").asText(), json);
                return;
            }

            onMessage(json);
        }catch (JsonParseException e){
            WebSocketController_Incoming.invalid_json_message(webSCtype);
        }catch (Exception e){
            e.printStackTrace();
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

    private  Map<String, JsonNode> message_out = new HashMap<>(); // (meessageId, JsonNode)
    private  Map<String, JsonNode> message_in  = new HashMap<>(); // (meessageId, JsonNode)


    public JsonNode write_with_confirmation(String messageId, ObjectNode json) throws TimeoutException, InterruptedException {
            return write_with_confirmation(messageId, json, (long) (250*100) );
    }

    public JsonNode write_with_confirmation(String messageId, ObjectNode json, Long time_To_TimeOutExcepting) throws TimeoutException,  InterruptedException {


        class Confirmation_Thread implements Callable<JsonNode>{

            @Override
            public JsonNode call() throws Exception {

                    System.out.println("Odesílám zprávu [" + messageId + "], na kterou požaduji potvrzení. Zpráva: " + json.toString());

                    message_out.put(messageId, json);
                    out.write(json.toString());

                    System.out.println("Zapínám vlákno");
                    Long breaker = time_To_TimeOutExcepting;

                    while (breaker > 0) {
                        breaker-=250;
                        //System.out.println("Zbejvá času " + breaker);

                        Thread.sleep(250);

                        if (message_in.containsKey(messageId)) {
                            System.out.println("Zpráva nalezena a tak zabíjím while cyklus");
                            JsonNode message = message_in.get(messageId);
                            message_in.remove(messageId);
                            return message;
                        }

                        if (breaker == 0) {
                            System.out.println("Time out Exception - Čas ve vlákně vypršel ");
                            message_out.remove(messageId);
                            throw new TimeoutException();
                        }

                    }

                    throw new InterruptedException();
            }
        }

        ExecutorService pool = Executors.newFixedThreadPool(3);

        Callable<JsonNode> callable = new Confirmation_Thread();
        Future<JsonNode> future = pool.submit(callable);

        try {
            return future.get();
        } catch (Exception e) {
            System.out.println("write_with_confirmation NEstihlo se včas");
            throw new TimeoutException();
        }

    }

    public void write_without_confirmation(ObjectNode json) {

        System.out.println("Odesílám zprávu na kterou nepožaduji potvrzení. Zpráva: " + json.toString());


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
