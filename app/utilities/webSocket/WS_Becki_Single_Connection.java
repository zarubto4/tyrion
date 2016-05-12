package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController_Incoming;

public class WS_Becki_Single_Connection extends  WebSCType {

    WS_Becki_Website person_connection;

    public WS_Becki_Single_Connection(String person_id, WS_Becki_Website person_connection) {
        super();
        this.person_connection = person_connection;
        super.identifikator = person_id;
        super.maps = person_connection.all_person_Connections;
        super.webSCtype = this;
    }

    @Override
    public void onClose() {
        System.out.println("WS_Becki_Single_Connection onClose " + super.identifikator);
        this.close();
        super.maps.remove(super.identifikator);
        if(maps.isEmpty()) WebSocketController_Incoming. becki_website.remove(person_connection.identifikator);
    }


    @Override
    public void onMessage(ObjectNode json) {
        System.out.println("příchozí zpráva v WS_Grid_Terminal: " + json.toString());
        WebSocketController_Incoming.incoming_message_terminal(this, json);
    }
}
