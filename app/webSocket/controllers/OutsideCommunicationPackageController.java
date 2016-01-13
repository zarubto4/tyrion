package webSocket.controllers;

import play.mvc.WebSocket;

public class OutsideCommunicationPackageController {

    public  WebSocket<String> connection(String key){
       System.out.println("Chci se připojit k SERVERU....");
       return WebSocketServer.connection(key);
    }


}
