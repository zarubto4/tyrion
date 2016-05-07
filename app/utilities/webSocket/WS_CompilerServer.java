package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController_Incoming;

public class WS_CompilerServer extends WebSCType{

    public String server_address;

    public WS_CompilerServer(String server_name, String server_address) {
        super();
        this.server_address = server_address;
        super.identifikator = server_name;
        super.webSCtype = this;
    }


    @Override
    public void onClose() {
        WebSocketController_Incoming.compiler_server_is_disconnect(this);
    }

    @Override
    public void onMessage(ObjectNode json) {
        System.out.println("příchozí zpráva v WS_Homer_Local: " + json.toString());
        WebSocketController_Incoming.homer_incoming_message(  maps.get( json.get("instanceId").asText() ) , json);


    }


}
