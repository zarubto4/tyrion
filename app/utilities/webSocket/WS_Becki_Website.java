package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController;
import models.person.Person;
import play.mvc.WebSocket;

import java.util.HashMap;
import java.util.Map;
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

    public void person_connection_onClose(WS_Becki_Single_Connection becki){

        System.out.println("person_connection_onClose");
        if(all_person_Connections.containsKey(becki.identifikator)) {
            System.out.println("Contains becki a tak jí smažu");
            all_person_Connections.remove(becki.identifikator);
        }

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
    public ObjectNode write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries) throws TimeoutException,  InterruptedException {

        System.out.println("write_with_confirmation na centrální becki není implementováno!!!!!!!!!...................!!!!!!!");
        throw new InterruptedException("Není implementováno");
    }

    @Override
    public void onMessage(ObjectNode json) {
         WebSocketController.becki_incoming_message(this, json);
    }
}


//**********************************************************************************************************************

