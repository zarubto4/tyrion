package controllers;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import utilities.response.GlobalResult;
import utilities.webSocket.WebSocketClientNotPlay;
import utilities.webSocket.WebSocketPlayServer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class WebSocketController_Incoming extends Controller {

// Values  ---------------------------------------------------------------------------------------------------------

    public static Map<String, WebSocket.Out<String> > incomingConnections_homers        = new HashMap<>(); // (Identificator, Websocket)
    public static Map<String, WebSocket.Out<String> > incomingConnections_mobileDevice  = new HashMap<>(); // (Identificator, Websocket)

// PUBLIC API ---------------------------------------------------------------------------------------------------------
    public WebSocket<String> homer_connection(String homer_mac_address){
        System.out.println("Příchozí připojení Homer" + homer_mac_address);
        return  WebSocketPlayServer.connection(incomingConnections_homers, homer_mac_address);
    }

    public WebSocket<String> mobile_connection(String m_program_name){
        System.out.println("Příchozí připojení mobilního telefonu na programu" + m_program_name);
        return  WebSocketPlayServer.connection(incomingConnections_mobileDevice, m_program_name);
    }

// PRIVATE API ---------------------------------------------------------------------------------------------------------


    /** incoming Json from Cliens - (users, Homers, mob. apps) */
    public static void incomingJson_PLAY_As_Server(String identificator, String string){
        System.out.println("Zpráva od " + identificator + " Obsah " + string );
    }






// Test & Control API ---------------------------------------------------------------------------------------------------------

    public Result getWebSocketStats(){

        System.out.println("IncomingConnections  homer           count= " + incomingConnections_homers.size());
        System.out.println("IncomingConnections  mobile device   count= " + incomingConnections_mobileDevice.size());
        System.out.println("OutcomingConnections servers         count= " + WebSocketController_OutComing.servers.size());


        //1
        System.out.println("\n");
        System.out.println("IncomingConnections Homers.......................................................... ");
        int k = 1;
        for (Map.Entry<String, WebSocket.Out<String>> entry : incomingConnections_homers.entrySet()) {
            System.out.println(k++ + ". Connection name = " + entry.getKey());
        }

        System.out.println("............................................................................. ");
        System.out.println("\n");



        //2
        System.out.println("\n");
        System.out.println("IncomingConnections Mobile device................................................... ");
        int j = 1;
        for (Map.Entry<String, WebSocket.Out<String>> entry : incomingConnections_mobileDevice.entrySet()) {
            System.out.println(j++ + ". Connection name = " + entry.getKey());
        }

        System.out.println("............................................................................. ");
        System.out.println("\n");


        //3
        System.out.println("\n");
        System.out.println("OutcomingConnections.......................................................... ");
        int i = 1;
        for (Map.Entry<String, WebSocketClientNotPlay> entry : WebSocketController_OutComing.servers.entrySet()) {
            System.out.println(i++ + ". Connection name = " + entry.getKey());
        }

        System.out.println("............................................................................. ");
        System.out.println("\n");

        return  GlobalResult.okResult();
    }

    public Result sendTo(String key){
        try {
            System.out.println("I am sending to " + key);
            if (!incomingConnections_homers.containsKey(key) && !WebSocketController_OutComing.servers.containsKey(key)) {
                System.out.println("No connection with this key");
                return GlobalResult.okResult();
            }

            if (incomingConnections_homers.containsKey(key)) incomingConnections_homers.get(key).write((new Date()).toString());
            if (WebSocketController_OutComing.servers.containsKey(key))
                WebSocketController_OutComing.servers.get(key).write("50000", Json.toJson(new Date()));

            return ok();
        }catch (Exception e){
            System.out.println("Něco se pokazilo");
            e.printStackTrace();
            return badRequest();
        }
    }



}
