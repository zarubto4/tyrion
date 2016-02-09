package utilities.webSocket;

import controllers.WebSocketController_Incoming;
import play.libs.F;
import play.libs.Json;
import play.mvc.WebSocket;

import java.util.Map;

public class WebSocketPlayServer {

    public static WebSocket<String> connection(Map<String, WebSocket.Out<String>> maps, String identificator) {

        return new WebSocket<String>() {

            public void onReady(final WebSocket.In<String> in, final WebSocket.Out<String> out) {

                maps.put(identificator, out);

                in.onMessage(new F.Callback<String>() {
                    @Override
                    public void invoke(String event) throws Throwable {
                        try {
                            WebSocketController_Incoming.incomingJson_PLAY_As_Server(identificator, Json.parse(event));
                        }catch (Exception e){
                            out.write("Its not JSON!");
                        }
                    }
                });

                in.onClose(new F.Callback0() {
                    @Override
                    public void invoke() throws Throwable {
                        WebSocketController_Incoming.incomingConnections_homers.remove(identificator, out);
                    }
                });
            }
        };

    }


}
