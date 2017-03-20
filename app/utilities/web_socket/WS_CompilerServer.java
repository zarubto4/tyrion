package utilities.web_socket;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.Model_CompilationServer;
import utilities.web_socket.message_objects.homer_tyrion.WS_Rejection_homer_server;

import java.util.HashMap;
import java.util.Map;

public class WS_CompilerServer extends WS_Interface_type {

    public String server_address;
    public Map<String, ObjectNode> compilation_results = new HashMap<>();
    public Map<String, ObjectNode> compilation_request = new HashMap<>();
    public Model_CompilationServer server;
    public boolean security_token_confirm = true;


    public WS_CompilerServer(Model_CompilationServer server, Map<String, WS_Interface_type> compiler_cloud_servers) {
        super();
        this.server_address = server.server_url;
        super.identifikator = server.unique_identificator;
        super.maps = compiler_cloud_servers;
        super.webSCtype = this;
    }


    @Override
    public void onClose() {

        logger.debug("WS_CompilerServer:: onClose ");
        this.close();

        logger.trace("WS_CompilerServer:: close thread");

        logger.trace("WS_CompilerServer:: removing object");
        Controller_WebSocket.compiler_cloud_servers.remove(this.identifikator);

        logger.trace("WS_CompilerServer:: removing on object");
        server.compiler_server_is_disconnect();
    }



    @Override
    public void onMessage(String message) {

        try{

            logger.trace("WS_CompilerServer:: onMessage:: Incoming message:: " + message);
            ObjectNode json = (ObjectNode) new ObjectMapper().readTree(message);

            if (json.has("buildId") && super.sendMessageMap.containsKey(json.get("buildId").asText())) {

                logger.trace("WS_CompilerServer:: onMessage:: Message with compiled build");

                super.sendMessageMap.get(json.get("buildId").asText()).insert_result(json);
                super.sendMessageMap.remove(json.get("buildId").asText());
                return;
            }

            // V případě že zpráva byla odeslaná Tyironem - existuje v zásobníku její objekt
            if (json.has("messageId") && sendMessageMap.containsKey(json.get("messageId").asText())) {

                logger.trace("WS_CompilerServer:: onMessage:: Message approve compilation start");
                sendMessageMap.get(json.get("messageId").asText()).insert_result(json);
                return;
            }

            onMessage(json);

        }catch (JsonParseException e){

            logger.error("WS_CompilerServer:: onMessage:: JsonParseException:: Message:: " + message);


        }catch (Exception e){

            logger.error("WS_CompilerServer:: onMessage:: JsonParseException:: Message:: " + message , e);

        }
    }


    @Override
    public void onMessage(ObjectNode json) {

        logger.trace("WS_CompilerServer:: onMessage:: Not requested message:: " + json.toString());

        // Pokud není token - není dovoleno zasílat nic do WebSocketu a ani nic z něj
        if(!security_token_confirm){
            logger.warn("WS_HomerServer:: onMessage:: This Websocket is not confirm");

            //security_token_confirm_procedure();
            super.write_without_confirmation(new WS_Rejection_homer_server().make_request());
            return;
        }


        if(json.has("messageChannel")){

            switch (json.get("messageChannel").asText()){

                default: logger.error("WS_CompilerServer:: onMessage:: not recognize incoming messageChanel!!! ->" + json.get("messageChannel").asText());
            }

        }else {
            logger.error("WS_CompilerServer:: onMessage:: "+ super.identifikator + " Incoming message has not messageChannel!!!!");
        }

    }

}
