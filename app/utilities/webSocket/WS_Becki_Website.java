package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.person.Model_Person;
import play.mvc.WebSocket;

import java.util.HashMap;
import java.util.Map;

public class WS_Becki_Website extends  WebSCType {

    public Map<String, WebSCType> all_person_Connections = new HashMap<>();
    public Model_Person person;

    public WS_Becki_Website(Model_Person person) {
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
         Controller_WebSocket.becki_incoming_message(this, json);
    }
}


//**********************************************************************************************************************

