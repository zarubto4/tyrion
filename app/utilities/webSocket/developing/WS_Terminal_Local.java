package utilities.webSocket.developing;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.WebSocketController_Incoming;

import java.util.Map;

public class WS_Terminal_Local extends  WebSCType {


    public WS_Terminal_Local(String mobile_mac_address, Map<String, WebSCType> incomingConnections_mobileDevice) {
        super();
        super.identifikator = mobile_mac_address;
        super.maps = incomingConnections_mobileDevice;
        super.webSCtype = this;
    }

    @Override
    public void onClose() {
        System.out.println("Local_Terminal onClose " + super.identifikator);

        WebSocketController_Incoming.terminal_is_disconnected(this);
    }


    @Override
    public void onMessage(JsonNode json) {
        System.out.println("příchozí zpráva v WS_Terminal_Local: " + json.toString());
         WebSocketController_Incoming.incoming_message_terminal(this, json);
    }
}
