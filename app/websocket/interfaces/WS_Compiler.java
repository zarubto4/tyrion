package websocket.interfaces;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.Model_CompilationServer;
import play.libs.Json;
import utilities.logger.Logger;
import websocket.WS_Interface;
import websocket.WS_Message;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WS_Compiler extends WS_Interface {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(WS_Compiler.class);

/* STATIC  -------------------------------------------------------------------------------------------------------------*/

    public static Props props(ActorRef out, UUID id) {
        return Props.create(WS_Compiler.class, out, id);
    }

    public WS_Compiler(ActorRef out, UUID id) {
        super(out);
        this.id = id;
        Controller_WebSocket.compilers.put(this.id, this);
    }

    @Override
    public boolean isOnline() {
        return false;
    }


    /**
     *
     * DOPRDELE jestli tohle někdo znovu smaže tak si podřežu žíly..
     *
     * Compilační server funguje asynchroně. Pošle se mu žádost o kompilaci a on na ní okamžitě odpoví!
     * Proto je nutné do zásobníku zprávu zavěsit znovu aby při druhé odpovědi kdy už kompilační
     * server odešle výledek kompilace, byl vrácen výsledek druhé zprávy.
     *
     * Pokud jde o zprávu WS_Message_Make_compilation.message_type pak je to první zpráva, která dává pokyn.
     * Její odpověď je zachycena a číslo Build_ID které Code server vrátil (přidělil) se uloží do HashMapy.
     *
     */
    @Override
    public ObjectNode sendWithResponse(WS_Message message) {

        // Speciální případ - Vynucený dvojnásobný request
        if(message.message_type.equals(WS_Message_Make_compilation.message_type)) {

           return this.make_Compilation(message);

        }else {
            return super.sendWithResponse(message);
        }
    }


    public ObjectNode make_Compilation(WS_Message message) {

        logger.trace("make_Compilation Start");
        // Odpověd
        ObjectNode response_one = super.sendWithResponse(message);

        logger.trace("make_Compilation: We have Response {}", response_one);

        if (!response_one.has("status") || ( response_one.has("status") && !response_one.get("status").asText().equals("success"))) {
            logger.warn("make_Compilation: status is error - Build is not in progress on Server");
            try {

                logger.warn("make_Compilation:: Incoming message has not contains state = success");

                WS_Message_Make_compilation make_compilation = new WS_Message_Make_compilation();

                make_compilation.message_id = message.getId().toString();
                make_compilation.message_channel = Model_CompilationServer.CHANNEL;
                make_compilation.websocket_identificator = this.id;
                make_compilation.status = "error";
                make_compilation.error_message = "Something was wrong";

                return (ObjectNode) new ObjectMapper().readTree(Json.toJson(make_compilation).toString());

            }catch (Exception e){
                logger.error("make_Compilation:: Shit Happens. Return Fail Response");
                logger.internalServerError(e);

                ObjectNode node = JsonNodeFactory.instance.objectNode();
                node.put("message_type", WS_Message_Make_compilation.message_type);
                node.put("status","error");
                node.put("error_message","Critical Error");
                node.put("message_id", message.getId().toString());
                node.put("message_channel", Model_CompilationServer.CHANNEL);
                node.put("websocket_identificator", this.id.toString());
                node.put("error_log",e.getLocalizedMessage());
                return node;
            }
        }



        UUID build_id = UUID.fromString(response_one.get("build_id").asText());
        response_one.put("message_id", build_id.toString());

        WS_Message get_compilation = new WS_Message(response_one, 1000 * 60, 0, 0);
        logger.trace("make_Compilation:: Add Message id {} to Buffer!", build_id.toString());
        messageBuffer.put(build_id, get_compilation);

        logger.trace("make_Compilation:: IDENTIFICATOR:: {} Message is registered in Buffer under build_id:: {} ", id, build_id);
        ObjectNode response_two = get_compilation.send();
        response_two.put("interface_code", response_one.get("interface_code").asText());
        response_two.put("build_id", build_id.toString());

        logger.debug("make_Compilation:: MESSAGE ABOUT BUILD IS DONE:: {} ", response_two);
        return response_two;
    }

    @Override
    public void onMessage(ObjectNode json) {
        try{

            logger.trace("WS_CompilerServer:: ID: {} onMessage:: Incoming message:: {} ", id, json.toString());

            if (json.has("build_id")) {
                logger.trace("WS_CompilerServer:: ID: {} onMessage:: Message has build ID", id);

                 UUID build_id = UUID.fromString(json.get("build_id").asText());

                logger.trace("WS_CompilerServer:: ID: {} onMessage:: Message build ID {} ", id, build_id);

                if(messageBuffer.containsKey(build_id)) {
                    logger.trace("WS_CompilerServer:: ID: {} onMessage:: Message with compiled build", id);

                    super.messageBuffer.get(build_id).resolve(json);
                    super.messageBuffer.remove(build_id);
                    return;
                }else {
                    logger.warn("WS_CompilerServer:: ID: {} onMessage:: Message with build_id: {} compiled build but message not found in Buffer! Buffer Size:: {} ", id, build_id, messageBuffer.size());
                }


            }


            logger.error("onMessage - no message should drop down here, all messages from compiler should be caught as response, message: {}", json.toString());


        }catch (Exception e){
            logger.internalServerError(e);
        }
    }

    

    @Override
    public void onClose() {
        Controller_WebSocket.compilers.remove(this.id);
    }
}
