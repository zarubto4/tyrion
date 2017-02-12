package utilities.web_socket;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.compiler.Model_CompilationServer;
import play.libs.Json;
import utilities.web_socket.message_objects.homer_tyrion.WS_Rejection_homer_server;

import java.util.HashMap;
import java.util.Map;

public class WS_CompilerServer extends WebSCType{

    public String server_address;
    public Map<String, ObjectNode> compilation_results = new HashMap<>();
    public Map<String, ObjectNode> compilation_request = new HashMap<>();
    Model_CompilationServer server;
    public boolean security_token_confirm = true;


    public WS_CompilerServer(Model_CompilationServer server, Map<String, WebSCType> compiler_cloud_servers) {
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

        logger.debug("WS_CompilerServer:: close thread");

        logger.debug("WS_CompilerServer:: removing object");
        Controller_WebSocket.compiler_cloud_servers.remove(this.identifikator);

        logger.debug("WS_CompilerServer:: removing on object");
        server.compiler_server_is_disconnect();
    }

    @Override
    public void onMessage(String message) {

        try{

            logger.debug("Incoming message on compilation server: " + message);
            ObjectNode json = (ObjectNode) new ObjectMapper().readTree(message);

            if (json.has("buildId") && super.sendMessageMap.containsKey(json.get("buildId").asText())) {

                System.out.println("Jedná se o zprávu už s hotovým buildem");

                super.sendMessageMap.get(json.get("buildId").asText()).insert_result(json);
                super.sendMessageMap.remove(json.get("buildId").asText());
                return;
            }

            // V případě že zpráva byla odeslaná Tyironem - existuje v zásobníku její objekt
            if (json.has("messageId") && sendMessageMap.containsKey(json.get("messageId").asText())) {

                System.out.println("Jedná se o zprávu potvrzující začátek buildu");

                sendMessageMap.get(json.get("messageId").asText()).insert_result(json);
                return;
            }

            onMessage(json);

        }catch (JsonParseException e){

            ObjectNode result = Json.newObject();
            result.put("messageType", "JsonUnrecognized");
            webSCtype.write_without_confirmation(result);

        }catch (Exception e){

            ObjectNode result = Json.newObject();
            result.put("messageType", "JsonUnrecognized");
            webSCtype.write_without_confirmation(result);
            e.printStackTrace();

        }
    }


    @Override
    public void onMessage(ObjectNode json) {

        logger.debug("Incoming not requested message: " + json.toString());
        logger.debug("BlockoServer: "+ super.identifikator + " Incoming message: " + json.toString());

        // Pokud není token - není dovoleno zasílat nic do WebSocketu a ani nic z něj
        if(!security_token_confirm){
            logger.warn("WS_HomerServer:: onMessage:: This Websocket is not confirm");

            //security_token_confirm_procedure();
            super.write_without_confirmation(new WS_Rejection_homer_server().make_request());
            return;
        }


        if(json.has("messageChannel")){

            switch (json.get("messageChannel").asText()){

                default: logger.error("Homer Server Incoming message not recognize incoming messageChanel!!! ->" + json.get("messageChannel").asText());
            }

        }else {
            logger.error("Homer Server: "+ super.identifikator + " Incoming message has not messageChannel!!!!");
        }

    }

}
