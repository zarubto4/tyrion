package utilities.webSocket;

import controllers.WebSocketController_Incoming;
import play.libs.F;
import play.mvc.WebSocket;

import java.util.Map;

public class WebSocketPlayServer {



    public static WebSocket<String> connection(Map<String, WebSocket.Out<String>> maps, String identification) {


        return new WebSocket<String>() {

            public void onReady(final WebSocket.In<String> in, final WebSocket.Out<String> out) {

                maps.put(identification, out);

                in.onMessage(new F.Callback<String>() {
                    @Override
                    public void invoke(String event) throws Throwable {
                            WebSocketController_Incoming.incomingJson_PLAY_As_Server(identification, event);
                    }
                });

                in.onClose(new F.Callback0() {
                    @Override
                    public void invoke() throws Throwable {
                        WebSocketController_Incoming.incomingConnections_homers.remove(identification, out);
                    }
                });
            }
        };

    }


}
