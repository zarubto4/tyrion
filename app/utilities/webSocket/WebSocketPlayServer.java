package utilities.webSocket;

import controllers.WebSocketController;
import play.libs.F;
import play.libs.Json;
import play.mvc.WebSocket;

public class WebSocketPlayServer {

    public static WebSocket<String> connection(String identificator) {

        return new WebSocket<String>() {

            public void onReady(final WebSocket.In<String> in, final WebSocket.Out<String> out) {

                WebSocketController.addConnection( identificator , out);

                in.onMessage(new F.Callback<String>() {
                    @Override
                    public void invoke(String event) throws Throwable {
                        try {
                            WebSocketController.incomingJson(identificator, Json.parse(event));
                        }catch (Exception e){
                            out.write("Its not JSON!");
                        }
                    }
                });

                in.onClose(new F.Callback0() {
                    @Override
                    public void invoke() throws Throwable {
                        WebSocketController.removeConnection(identificator);
                    }
                });
            }
        };

    }


}
