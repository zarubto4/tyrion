package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.Controller;
import utilities.webSocket.WebSocketClientNotPlay;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;


public class WebSocketController_OutComing extends Controller {

    public static Map<String, WebSocketClientNotPlay> servers = new HashMap<>(); // (Identificator, Websocket)




// PRIVATE API ---------------------------------------------------------------------------------------------------------

    /** incoming Json from Server where Play is only client */
    public static void incomingJson_PLAY_As_Client(String identificator, JsonNode jsonNode){
        // Pro testování - jsem přijdou nečíslované zprávy - číslované jsou odchyceny
        // Co sem případně přijde by mělo být dále zpracováno -> přeposláno dál na jendotlivé zařízení
        System.out.println("Zpráva od " + identificator + " Obsah " + jsonNode.toString() );
    }

    /** if Play Server is only client - for example when connecting to Blocko Server */
    public static void connectToServer(String identificator, String adress, Thread thread) throws Exception {
        // Ověření netřeba - připojuje se vždy jen a pouze server
        System.out.println("WebSocketController_Incoming startuje clienta \n");
        new WebSocketClientNotPlay(identificator, new URI(adress), thread);
    }



    public static void blockoServerCreateInstance(String server_name, String instance_name) throws TimeoutException, InterruptedException{
            String messageId =  UUID.randomUUID().toString();

            ObjectNode result = Json.newObject();
            result.put("messageType", "createInstance");
            result.put("messageId", messageId);
            result.put("instanceId", instance_name);

            JsonNode answare = WebSocketController_OutComing.servers.get(server_name).write(messageId, result);
    }


    public static void blockoServerUploadProgram(String server_name, String instance_name, String program_in_string) throws TimeoutException, InterruptedException{
            String messageId =  UUID.randomUUID().toString();

            ObjectNode result = Json.newObject();
            result.put("messageType", "loadProgram");
            result.put("instanceId", instance_name);
            result.put("messageId", messageId);
            result.put("program", program_in_string);

            JsonNode answare = WebSocketController_OutComing.servers.get(server_name).write(messageId, result);
    }



}
