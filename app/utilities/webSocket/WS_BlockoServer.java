package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController_Incoming;

import java.util.HashMap;
import java.util.Map;

public class WS_BlockoServer extends WebSCType{

    // Zde si udržuju referenci na tímto serverem vytvořené virtuální homery, které jsem dal do globální mapy (incomingConnections_homers)
    public Map<String, WebSCType> virtual_homers = new HashMap<>();

    public WS_BlockoServer(String server_name, Map<String, WebSCType> blocko_servers) {
        super();
        super.identifikator = server_name;
        super.maps = blocko_servers;
        super.webSCtype = this;
    }




    @Override
    public void onClose() {

        System.out.println("Server se odpojil a tak je nutné zabít všechny jeho instnace Homerů v globální mapě");
        for (Map.Entry<String, WebSCType> entry : virtual_homers.entrySet())
        {
            System.out.println("Zabíjím virtuální instanci " +entry.getKey());
            entry.getValue().onClose();
        }

        WebSocketController_Incoming.blocko_server_is_disconnect(this);
    }

    @Override
    public void onMessage(ObjectNode json) {
        System.out.println("příchozí zpráva v WS_BlockoServer: " + json.toString());

        // Zpráva je z virtuální instance
        if(json.has ("instanceId") ){
            WebSCType ws = virtual_homers.get( json.get("instanceId").asText() );
            ws.onMessage(json);
        }

        // Zpráva je ze serveru
        else {
            WebSocketController_Incoming.blocko_server_incoming_message(this, json);
        }

    }


}
