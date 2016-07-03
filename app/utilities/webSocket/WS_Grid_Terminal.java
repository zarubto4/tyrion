package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController_Incoming;

import java.util.Map;

public class WS_Grid_Terminal extends  WebSCType {

    public String m_project_id; // Určeno pouze pro Vývojový WebSocket portál


    public WS_Grid_Terminal(String mobile_mac_address, String m_project_id, Map<String, WebSCType> incomingConnections_mobileDevice) {
        super();
        super.identifikator = mobile_mac_address;
        this.m_project_id = m_project_id;
        super.maps = incomingConnections_mobileDevice;
        super.webSCtype = this;
    }

    @Override
    public void onClose() {
        System.out.println("Local_Terminal onClose " + super.identifikator);
        this.close();
        WebSocketController_Incoming.terminal_is_disconnected(this);
    }


    @Override
    public void onMessage(ObjectNode json) {
        System.out.println("příchozí zpráva v WS_Grid_Terminal: " + json.toString());
         WebSocketController_Incoming.terminal_incoming_message(this, json);
    }




}
