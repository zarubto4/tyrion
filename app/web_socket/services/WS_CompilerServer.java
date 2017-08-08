package web_socket.services;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.Model_CompilationServer;
import utilities.logger.Class_Logger;
import web_socket.message_objects.compilator_with_tyrion.WS_Message_Ping_compilation_server;
import web_socket.message_objects.homer_with_tyrion.verification.WS_Message_Homer_Rejection;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Becki_Ping;

public class WS_CompilerServer extends WS_Interface_type {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(WS_CompilerServer.class);

/* VALUES  -------------------------------------------------------------------------------------------------------------*/

    public String server_address;
    //public Map<String, ObjectNode> compilation_results = new HashMap<>();
    //public Map<String, ObjectNode> compilation_request = new HashMap<>();
    public Model_CompilationServer server;
    public boolean security_token_confirm = true;
    public String identifikator;

    public WS_CompilerServer(Model_CompilationServer server) {
        super();
        this.server = server;
        this.server_address = server.server_url;
        identifikator = server.unique_identificator;
        super.webSCtype = this;
    }

    @Override
    public boolean is_online() {
        try {

            ObjectNode status = write_with_confirmation( new WS_Message_Ping_compilation_server().make_request(), 1000 * 10, 0, 1);
            return status.get("status").asText().equals("success");

        }catch (Exception e){
            return false;
        }
    }

    @Override
    public void add_to_map() {
        Controller_WebSocket.compiler_cloud_servers.put(identifikator, this);
    }

    @Override
    public String get_identificator() {
        return identifikator;
    }

    @Override
    public void onClose() {

        terminal_logger.debug("WS_CompilerServer:: onClose ");
        this.close();

        terminal_logger.trace("WS_CompilerServer:: close thread");

        terminal_logger.trace("WS_CompilerServer:: removing object");
        Controller_WebSocket.compiler_cloud_servers.remove(this.identifikator);

        terminal_logger.trace("WS_CompilerServer:: removing on object");
        server.compiler_server_is_disconnect();
    }

    @Override
    public void onMessage(String message) {

        try{

            terminal_logger.trace("WS_CompilerServer:: onMessage:: Incoming message:: " + message);
            ObjectNode json = (ObjectNode) new ObjectMapper().readTree(message);

            if (json.has("build_id") && super.sendMessageMap.containsKey(json.get("build_id").asText())) {

                terminal_logger.trace("WS_CompilerServer:: onMessage:: Message with compiled build");

                super.sendMessageMap.get(json.get("build_id").asText()).insert_result(json);
                super.sendMessageMap.remove(json.get("build_id").asText());
                return;
            }

            // V případě že zpráva byla odeslaná Tyironem - existuje v zásobníku její objekt
            if (json.has("message_id") && sendMessageMap.containsKey(json.get("message_id").asText())) {

                terminal_logger.trace("WS_CompilerServer:: onMessage:: Message approve compilation start");
                sendMessageMap.get(json.get("message_id").asText()).insert_result(json);
                return;
            }

            onMessage(json);

        }catch (JsonParseException e){
            terminal_logger.internalServerError(new Exception("Error while parsing json.", e));
        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    @Override
    public void onMessage(ObjectNode json) {

        terminal_logger.trace("WS_CompilerServer:: onMessage:: Not requested message:: " + json.toString());

        // Pokud není token - není dovoleno zasílat nic do WebSocketu a ani nic z něj
        if(!security_token_confirm){
            terminal_logger.warn("WS_HomerServer:: onMessage:: This Websocket is not confirm");

            //security_token_confirm_procedure();
            super.write_without_confirmation(new WS_Message_Homer_Rejection().make_request());
            return;
        }

        if(json.has("message_channel")){

            switch (json.get("message_channel").asText()){

                default: terminal_logger.internalServerError(new Exception("WS_CompilerServer: messageChanel not recognized -> " + json.get("message_channel").asText()));
            }

        }else {
            terminal_logger.internalServerError(new Exception("WS_CompilerServer: " + identifikator + ". Incoming message has not message_channel!!!!"));
        }
    }
}