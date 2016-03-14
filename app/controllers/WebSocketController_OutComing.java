package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.Controller;
import utilities.webSocket.WebSocketClientNotPlay;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeoutException;


public class WebSocketController_OutComing extends Controller {

    //public static Map<String, WebSocketClientNotPlay> servers = new HashMap<>(); // (Identificator, Websocket)


    /**
     * TODO
     * Důležitý komentář. Momentálně je bez otestování dopsáno jak se má chovat připojený terminál když je B_program
     * nahrán v cloudu nebo na lokálním PC (RPI). Umí se různě odpojovat připojovat, navazovat spojení atd... Program v clodu
     * je překryt vlastním WS_HOMER_Cloud objektem (který by se měl pro vnější obsluhu chovat naprosto totožně. Takže je do něj
     * potřeba dopsat jednak vazbu na server, vazbu na instanci a další ptákoviny...
     *
     * Takže zde uvedené metody postrádají zatím smysl protože WebSocketClientNotPlay by měl být obsah WS_HOMER_Cloud objektu!!
     *
     *
     *
     */


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
        try {
            System.out.println("Pokus o vytvoření nové instance");
            String messageId =  UUID.randomUUID().toString();

            ObjectNode result = Json.newObject();
            result.put("messageType", "createInstance");
            result.put("messageId", messageId);
            result.put("instanceId", instance_name);

            JsonNode answare = WebSocketController_Incoming.cloud_servers.get(server_name).get(instance_name).write_with_confirmation(messageId, result);

        } catch (TimeoutException e){
            System.out.println("TimeoutException");
        } catch (InterruptedException e){
            System.out.println("InterruptedException");
        }
    }

    public static void blockoServerKillInstance(String server_name, String instance_name) throws TimeoutException, InterruptedException{
        try {

            System.out.println("Pokus o zabití předchozí instance");

            String messageId =  UUID.randomUUID().toString();

            ObjectNode result = Json.newObject();
            result.put("messageType", "destroyInstance");
            result.put("messageId", messageId);
            result.put("instanceId", instance_name);

            JsonNode answare = WebSocketController_Incoming.cloud_servers.get(server_name).get(instance_name).write_with_confirmation(messageId, result);

        } catch (TimeoutException e){
            System.out.println("TimeoutException");
        } catch (InterruptedException e){
            System.out.println("InterruptedException");
        }

    }

    public static void blockoServerUploadProgram(String server_name, String instance_name, String program_in_string) throws TimeoutException, InterruptedException{
        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "loadProgram");
        result.put("instanceId", instance_name);
        result.put("messageId", messageId);
        result.put("program", program_in_string);

        JsonNode answare = WebSocketController_Incoming.cloud_servers.get(server_name).get(instance_name).write_with_confirmation(messageId, result);
    }

    public static boolean blockoServer_is_Instance_Running(String server_name, String instance_name) {
        try {

            String messageId = UUID.randomUUID().toString();

            ObjectNode result = Json.newObject();
            result.put("messageType", "instanceExist");
            result.put("messageId", messageId);
            result.put("instanceId", instance_name);

            JsonNode answare = WebSocketController_Incoming.cloud_servers.get(server_name).get(instance_name).write_with_confirmation(messageId, result);
            return answare.get("exist").asBoolean();

        } catch (TimeoutException e){
            System.out.println("TimeoutException");
            return false;
        } catch (InterruptedException e){
            System.out.println("InterruptedException");
             return false;
        }
    }

    public static void blockoServer_set_DigitalValue (String server_name, String instance_name, String hwId, boolean value ) throws TimeoutException, InterruptedException{
        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "setDigitalValue");
        result.put("messageId", messageId);
        result.put("instanceId", instance_name);
        result.put("hwId", hwId);
        result.put("value", value);

        JsonNode answare = WebSocketController_Incoming.cloud_servers.get(server_name).get(instance_name).write_with_confirmation(messageId, result);
    }

    public static void blockoServer_set_AnalogValue (String server_name, String instance_name, String hwId, String value ) throws TimeoutException, InterruptedException{
        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "setAnalogValue");
        result.put("messageId", messageId);
        result.put("instanceId", instance_name);
        result.put("hwId", hwId);
        result.put("value", value);

        JsonNode answare = WebSocketController_Incoming.cloud_servers.get(server_name).get(instance_name).write_with_confirmation(messageId, result);
    }

    public static JsonNode blockoServer_get_Inputs(String server_name, String instance_name) throws TimeoutException, InterruptedException{
        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "getInputs");
        result.put("messageId", messageId);
        result.put("instanceId", instance_name);

        JsonNode answare = WebSocketController_Incoming.cloud_servers.get(server_name).get(instance_name).write_with_confirmation(messageId, result);
        return answare;
    }

    public static JsonNode blockoServer_get_Outputs(String server_name, String instance_name) throws TimeoutException, InterruptedException{
        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "getOutputs");
        result.put("messageId", messageId);
        result.put("instanceId", instance_name);

        JsonNode answare = WebSocketController_Incoming.cloud_servers.get(server_name).get(instance_name).write_with_confirmation(messageId, result);
        return answare;
    }

    public static void send_blocko_Instruction(String server_name, String instance_name, JsonNode json) {
        System.out.println("Tato metoda je prázdná, ale poslala by na Cloud server toto " + json.toString());
    }
}
