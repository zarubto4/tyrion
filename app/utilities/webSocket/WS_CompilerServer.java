package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController_Incoming;

import java.util.HashMap;
import java.util.Map;

public class WS_CompilerServer extends WebSCType{

    public String server_address;
    public Map<String, ObjectNode> compilation_results = new HashMap<>();
    public Map<String, ObjectNode> compilation_request = new HashMap<>();

    public WS_CompilerServer(String server_name, String server_address, Map<String, WebSCType> compiler_cloud_servers) {
        super();
        this.server_address = server_address;
        super.identifikator = server_name;
        super.maps = compiler_cloud_servers;
        super.webSCtype = this;
    }


    @Override
    public void onClose() {
        this.close();
        WebSocketController_Incoming.compiler_server_is_disconnect(this);
    }

    @Override
    public void onMessage(ObjectNode json) {

        if(json.has("buildUrl") || json.has("buildErrors")){
            if(compilation_request.containsKey(json.get("buildId").asText())) compilation_results.put(json.get("buildId").asText(), json );
            else {
                System.out.println("Kompilace neproběhla včas a tak jí zahazuji protože Tyiron na ní nepočkal a uživateli už vrátil odpověď");
            }
        }
        else {
            System.out.println("příchozí zpráva v WS_CompilerServer: " + json.toString());
            WebSocketController_Incoming.compilation_server_incoming_message(this, json);
        }
    }


}
