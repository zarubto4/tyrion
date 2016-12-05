package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController;

public class WS_Becki_Single_Connection extends  WebSCType {

    WS_Becki_Website person_connection;
    public boolean notification_subscriber = false;

    public WS_Becki_Single_Connection(String person_id, WS_Becki_Website person_connection) {
        super();
        this.person_connection = person_connection;
        super.identifikator = person_id;
        super.maps =  person_connection.all_person_Connections ;
        super.webSCtype = this;
    }

    @Override
    public void onClose() {
        System.out.println("WS_Becki_Single_Connection onClose " + super.identifikator);

        this.close();

        person_connection.all_person_Connections.remove(this.identifikator);

        if(person_connection.all_person_Connections.isEmpty()){
            WebSocketController.becki_website.remove(person_connection.identifikator);
        }
    }


    @Override
    public void onMessage(ObjectNode json) {
        json.put("single_connection_token", this.identifikator);
        person_connection.onMessage(json);
    }
}
