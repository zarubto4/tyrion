package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import models.blocko.Homer;
import play.mvc.Controller;
import play.mvc.Security;
import play.mvc.WebSocket;
import utilities.loginEntities.Secured;
import utilities.webSocket.WebSocketPlayServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Security.Authenticated(Secured.class)
public class WebSocketController extends Controller {

// Values  ---------------------------------------------------------------------------------------------------------

    public static Map<String, WebSocket.Out<String>  > maps = new HashMap<>(); // (Identificator, Websocket)


// PUBLIC API ---------------------------------------------------------------------------------------------------------
    @Security.Authenticated(Secured.class)
    public WebSocket<String> connection(String mail){
        if(SecurityController.getPerson() == null) System.out.print("Nepřihlášený uživatel");

        return  WebSocketPlayServer.connection(mail);
    }


    public static void incomingJson(String identificator, JsonNode jsonNode){

    }

    // Voalné z WebSocketPlayServer - Automaticky se zavolá tato metoda, když se cokoliv připojí!
    public static void addConnection(String identificator, WebSocket.Out<String> out){
        maps.put( identificator , out);
    }

    // Voalné z WebSocketPlayServer - Automaticky se zavolá tato metoda, když se cokoliv odpojí
    public static void removeConnection(String identificator){
        maps.remove(identificator);
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
