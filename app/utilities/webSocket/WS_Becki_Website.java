package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController_Incoming;
import play.mvc.WebSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class WS_Becki_Website extends  WebSCType {

   public Map<String, WebSCType> all_person_Connections = new HashMap<>();

    public WS_Becki_Website(String person_id) {
        super();
        super.identifikator = person_id;
    }

    @Override
    public WebSocket<String> connection(){return null;}

    @Override
    public void onClose() {
        System.out.println("Local_Terminal onClose " + super.identifikator);
        this.close();
        WebSocketController_Incoming.becki_disconnect(this);
    }

    public void person_connection_onClose(WS_Becki_Single_Connection becki){

        System.out.println("person_connection_onClose");
        if(all_person_Connections.containsKey(becki.identifikator)) {
            System.out.println("Contains becki a tak jí smažu");
            all_person_Connections.remove(becki.identifikator);
        }
        if(all_person_Connections.isEmpty()){
            System.out.println("Už žádné připojení uživatele id=" + super.identifikator );
            System.out.println("Odmazávám z centrální mapy" );
            // smažu z mapy připojení
            WebSocketController_Incoming.becki_website.remove(super.identifikator);

            // a informuji odběratele o tom že je nikdo neodebírá
            for(WebSCType ws : super.subscribers_becki){
                System.out.println("Upozornuju odběratele becki že už tu žádná není identificator: " + ws.identifikator );
                WebSocketController_Incoming.becki_echo_that_becki_was_disconnect(ws);
            }

            this.onClose();
        }

    }

    @Override
    public void write_without_confirmation(ObjectNode json) {
        try {

            for (Map.Entry<String,WebSCType> entry : all_person_Connections.entrySet()) {
               entry.getValue().write_without_confirmation(json);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public ObjectNode write_with_confirmation(ObjectNode json, Long time_To_TimeOutExcepting) throws TimeoutException,  InterruptedException {

        System.out.println("write_with_confirmation na centrální becki není implementováno!!!!!!!!!...................!!!!!!!");

        return null;
    }
    @Override
    public void onMessage(ObjectNode json) {
        System.out.println("příchozí zpráva z Becki: " + json.toString());
         WebSocketController_Incoming.becki_incoming_message(this, json);
    }
}


//**********************************************************************************************************************

