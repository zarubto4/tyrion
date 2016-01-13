package webSocket.incomingMessage;


import com.google.gson.JsonObject;
import play.mvc.WebSocket;
import webSocket.controllers.SocketCollector;

/*
    JSON PACKET pro zaslání prvního potvrzovacího packetu o připojení
    Tento JSON je neutrální pro všechna zařízení. Potřebné údaje vždy načítá
    z konfiguračního souboru viz PropertiesOfDevice v configClasses
*/
public class NewConnection {

    public  String clientCode;
    public  String typeOfDevice;
    public  String projectId;
    public  String version;
    public  String macAddress;


    /** Seřazujeme podle frekvence volání! Od nejčastějšího po nejméňě! */
    public void runable(String method, WebSocket.Out<String> out){
        switch (method){
            case "newConnection" : newConnection(out);
        }
    }

    private void newConnection(WebSocket.Out<String> out){
        SocketCollector.addConnectionToThread(macAddress, out);

        JsonObject o = new JsonObject();
        o.addProperty("Aktualizuj"      ,    "true");
        out.write(o.toString());
    }

}
