package websocket.interfaces;

import com.fasterxml.jackson.databind.node.ObjectNode;
import websocket.Interface;
import websocket.Message;
import websocket.Request;

import java.util.UUID;

public class Compiler extends Interface {

    public Compiler(UUID id) {
        super(id);
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
