package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController_Incoming;
import play.mvc.WebSocket;

import java.util.HashMap;
import java.util.Map;

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
        WebSocketController_Incoming.terminal_is_disconnected(this);
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
    public void onMessage(ObjectNode json) {
        System.out.println("příchozí zpráva v WS_Grid_Terminal: " + json.toString());
         WebSocketController_Incoming.incoming_message_terminal(this, json);
    }
}


//**********************************************************************************************************************

