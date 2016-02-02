package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.blocko.Homer;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.WebSocket;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;
import utilities.webSocket.WebSocketClientNotPlay;
import utilities.webSocket.WebSocketPlayServer;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Security.Authenticated(Secured.class)
public class WebSocketController extends Controller {

// Values  ---------------------------------------------------------------------------------------------------------

    public static Map<String, WebSocket.Out<String> > incomingConnections = new HashMap<>(); // (Identificator, Websocket)
    public static Map<String, WebSocketClientNotPlay> outcomingConnections       = new HashMap<>(); // (Identificator, Websocket)

// PUBLIC API ---------------------------------------------------------------------------------------------------------
    @Security.Authenticated(Secured.class)
    public WebSocket<String> connection(String mail){
        // if(SecurityController.getPerson() == null) System.out.print("Nepřihlášený uživatel");
        return  WebSocketPlayServer.connection(mail);
    }


// PRIVATE API ---------------------------------------------------------------------------------------------------------
    /** if Play Server is only client - for example when connecting to Blocko Server */
    public static void connectToServer(String identificator, String adress, Thread thread) throws Exception {
        // Ověření netřeba - připojuje se vždy jen a pouze server
        System.out.println("WebSocketController startuje clienta \n");
        new WebSocketClientNotPlay(identificator, new URI(adress), thread);
    }

    /** incoming Json from Cliens - (users, Homers, mob. apps) */
    public static void incomingJson_PLAY_As_Server(String identificator, JsonNode jsonNode){
            System.out.println("Zpráva od " + identificator + " Obsah " + jsonNode.asText() );
    }

    /** incoming Json from Server where Play is only client */
    public static void incomingJson_PLAY_As_Client(String identificator, JsonNode jsonNode){
        System.out.println("Zpráva od " + identificator + " Obsah " + jsonNode.toString() );
    }


// Test & Control API ---------------------------------------------------------------------------------------------------------

    public Result getWebSocketStats(){

        System.out.println("IncomingConnections     count= " + incomingConnections.size());
        System.out.println("OutcomingConnections    count= " + outcomingConnections.size());


        //1
        System.out.println("\n");
        System.out.println("IncomingConnections.......................................................... ");
        int j = 1;
        for (Map.Entry<String, WebSocket.Out<String>> entry : incomingConnections.entrySet()) {
            System.out.println(j++ + ". Connection name = " + entry.getKey());
        }

        System.out.println("............................................................................. ");
        System.out.println("\n");

        //2
        System.out.println("\n");
        System.out.println("OutcomingConnections.......................................................... ");
        int i = 1;
        for (Map.Entry<String, WebSocketClientNotPlay> entry : outcomingConnections.entrySet()) {
            System.out.println(i++ + ". Connection name = " + entry.getKey());
        }

        System.out.println("............................................................................. ");
        System.out.println("\n");

        return  GlobalResult.okResult();
    }

    public Result sendTo(String key){

        System.out.println("I am sending to " + key);
       if(!incomingConnections.containsKey(key) && !outcomingConnections.containsKey(key) ) {
           System.out.println("No connection with this key");
           return ok();
       }

        if(incomingConnections.containsKey(key)) incomingConnections.get(key).write( (new Date()).toString());
        if(outcomingConnections.containsKey(key)) outcomingConnections.get(key).write( (new Date()).toString());

        return ok();
    }



    // Historicky podporavné metody?? --------------------------------------------
    public static List<Homer> getAllConnectedDevice(){
        return null;
    }

    public static WebSocket.Out<String> getConnection(Homer homer){
        return null;
    }


    public static boolean isConnected(Homer homer){
        return false;
    }
}
