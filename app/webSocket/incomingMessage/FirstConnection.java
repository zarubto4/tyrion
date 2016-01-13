package webSocket.incomingMessage;

import com.google.gson.JsonObject;
import play.mvc.WebSocket;
import webSocket.controllers.SocketCollector;

public class FirstConnection {

    public  String typeOfDevice;
    public  String version;
    public  String macAddress;


    /** Seřazujeme podle frekvence volání! Od nejčastějšího po nejméňě! */
    public void runable(String method, WebSocket.Out<String> out){
        switch (method){
            case "newConnection" : firstConnectionNewDevice(out);
        }
    }

    private void firstConnectionNewDevice( WebSocket.Out<String> out) {
        SocketCollector.addConnectionToThread(macAddress, out);

        JsonObject o = new JsonObject();
        o.addProperty("Poprve", "tyhle Udaje Si Uloz");
        out.write(o.toString());
    }


}
