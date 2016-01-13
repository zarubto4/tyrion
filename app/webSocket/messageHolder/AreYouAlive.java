package webSocket.messageHolder;

import com.google.gson.JsonObject;
import play.mvc.WebSocket;
import webSocket.config.PropertiesOfDevice;

import java.util.Date;

/*
    JSON PACKET pro zasílání oživovací zprávy na koncová zařízení.
    Tento JSON je neutrální pro všechna zařízení. Potřebné údaje vždy načítá
    z konfiguračního souboru viz PropertiesOfDevice v configClasses
*/


public class AreYouAlive {
    public static String serverName = PropertiesOfDevice.properties.getProperty("ServerName");
    public static String version = PropertiesOfDevice.properties.getProperty("Version");

    public static void checkDevice(WebSocket.Out<String> webSocket){

        JsonObject o = new JsonObject();
        o.addProperty("serverName"      ,    serverName);
        o.addProperty("version"         ,    version);
        o.addProperty("time"            ,    new Date().toString());
        o.addProperty("ServerMessage"   ,    "Čau houmre!! ");

        webSocket.write(o.toString());
    }

}
