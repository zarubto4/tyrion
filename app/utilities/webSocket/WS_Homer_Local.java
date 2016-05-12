package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController_Incoming;

import java.util.Map;

public class WS_Homer_Local extends WebSCType{

    public WS_Homer_Local(String homer_mac_address, Map<String, WebSCType> incomingConnections_homers) {
        super();
        super.identifikator = homer_mac_address;
        super.maps = incomingConnections_homers;
        super.webSCtype = this;
    }

    @Override
    public void onClose() {
        this.close();
        WebSocketController_Incoming.homer_is_disconnect(this);
    }

    @Override
    public void onMessage(ObjectNode json) {
        System.out.println("příchozí zpráva v WS_Homer_Local: " + json.toString());
        WebSocketController_Incoming.homer_incoming_message(this, json);


    }
}
