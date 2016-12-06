package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController;
import models.person.Person;
import play.libs.Json;
import play.mvc.WebSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class WS_Becki_Website extends  WebSCType {

    public Map<String, WebSCType> all_person_Connections = new HashMap<>();
    public Person person;

    public WS_Becki_Website(Person person) {
        super();
        this.person = person;
        super.identifikator = person.id;
    }

    @Override
    public WebSocket<String> connection(){return null;}

    @Override
    public void onClose() {
        System.out.println("Local_Terminal onClose " + super.identifikator);
        this.close();
    }


    @Override
    public void write_without_confirmation(ObjectNode json) {
        try {

            for (Map.Entry<String,WebSCType> entry : all_person_Connections.entrySet()) {
               entry.getValue().write_without_confirmation(json);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onMessage(ObjectNode json) {
         WebSocketController.becki_incoming_message(this, json);
    }
}


//**********************************************************************************************************************

