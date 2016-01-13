package webSocket.models_JSON;

import webSocket.controllers.SocketCollector;

import java.util.ArrayList;

public class WebSocketStats {

    public String name = "WebSocketStats";
    public int size = 0;
    public ArrayList<String> connection = new ArrayList<>();



    public WebSocketStats() {

        size = SocketCollector.map.size();

        for (String key : SocketCollector.map.keySet()) {
             connection.add(key);
        }
    }


}
