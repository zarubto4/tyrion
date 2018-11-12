package websocket.interfaces;

import akka.stream.Materializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import controllers._BaseFormFactory;
import utilities.network.NetworkStatusService;
import websocket.Interface;
import websocket.Message;
import websocket.Request;

public class Compiler extends Interface {

    @Inject
    public Compiler(NetworkStatusService networkStatusService, Materializer materializer, _BaseFormFactory formFactory) {
        super(networkStatusService, materializer, formFactory);
    }

    public ObjectNode requestCompilation(Request message) {
        return null;
    }

    @Override
    public void onMessage(Message message) {
        switch (message.getType()) {

        }
    }
}
