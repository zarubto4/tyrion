package webSocket.controllers;

import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.mvc.Controller;
import play.mvc.WebSocket;

public class WebSocketServer extends Controller {


    public static WebSocket<String> connection(String key){

        return new WebSocket<String>(){

            public void onReady(In<String> in, Out<String> out){
                WebSocketServer.start(in, out, key);
             }
        };

    }


    public static void start(WebSocket.In<String> in, WebSocket.Out<String> out, String key){
        System.out.println("Server:  Proběhlo spojení s prvkem");
        SocketCollector.addConnectionToThread(key, out);

        in.onMessage(new Callback<String>(){
            public void invoke(String packet){
                System.out.println("Server:  Analýza přijatého paketu = " + packet);
                out.write("Přijal jsem: " + packet);
                Distributor.turingMachine(out, packet);
            }
        });

        in.onClose(new Callback0() {
            public void invoke() {
                System.out.println("Server: Zařízení se zabilo: " + out.toString());
                SocketCollector.removeFromMasterThread(out);
            }
        });
    }


}





