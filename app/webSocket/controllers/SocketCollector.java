package webSocket.controllers;

import com.google.gson.JsonObject;
import models.blocko.Homer;
import play.mvc.WebSocket;

import java.util.*;


// Soukromá třída schromaždující v poli connections všechny navázaná spojení,
// ve kterých je uchovávána komunikace s připjenými zařízeními.
// V pravidelných intervalech se probudí hlavní vlákno a sepne
// uložená vlákna a tím pinkne na zařízení

public class SocketCollector {


/* VALUES ---------------------------------------------------------------------------------------------- */
    /** Typ Klíče  ( klíč + webSocket k odeslání ) */
    /** MacAdresa + webSocket k odeslání */
    public static Map<String, WebSocket.Out<String>> map = new HashMap<>();


    /* SINGLETON ---------------------------------------------------------------------------------------------- */
    private SocketCollector() {}
    private static SocketCollector instance;
    public static SocketCollector getInstance() {
        if (instance == null) instance = new SocketCollector(); return instance;
    }

/* METHODS ---------------------------------------------------------------------------------------------- */

    public static void addConnectionToThread(String key, WebSocket.Out<String> webSocket) {
        System.out.println("Vložil jsem do map vazbu s klíčem");
        if(!map.containsKey(key)) map.put(key, webSocket);
    }

    public static void disconnectFromMasterThread(String macAddress){
        map.get(macAddress).close();
        map.remove(macAddress);
    }

    public static void removeFromMasterThread(WebSocket.Out<String> webSocket){
        map.values().remove(webSocket);
    }


    public static WebSocket.Out<String> getConnectionFromMasterThread(String key)throws Exception{
        if(!map.containsKey(key)) throw new Exception("Device is not connected now");
        else return map.get(key);
    }

    public static void sendJson(String key, JsonObject json) throws Exception{
        if(!map.containsKey(key)) throw new Exception("Device is not connected now");
        map.get(key).write(json.toString());
    }

    public static List<Homer> getAllConnectedDevice() {

        List<Homer> homers = new ArrayList<>();
        Set<String> macAddress = map.keySet();
        Iterator<String> iter = macAddress.iterator();

        while (iter.hasNext()) {
            try {

                String code = iter.next();
                if (!code.contains("@")) homers.add( Homer.find.byId(code));

            }catch (Exception e){
                System.out.println(instance.getClass().getName() + "Error with houmerId");
            }
        }

        return homers;
    }



    public static boolean isConnected(String key) {
         return map.containsKey(key);
    }


}
