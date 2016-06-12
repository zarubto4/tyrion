package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController_Incoming;

import java.util.concurrent.TimeoutException;

public class WS_Homer_Cloud extends WebSCType{

    public WS_BlockoServer blockoServer;

    public WS_Homer_Cloud (String identificator,  WS_BlockoServer blockoServer) {
        super();
        super.identifikator = identificator;
        this.blockoServer = blockoServer;
        super.webSCtype = this;
    }


    @Override
    public void write_without_confirmation(ObjectNode json) {
       try {

           json.put("instanceId", super.identifikator);
           blockoServer.write_without_confirmation(json);

       }catch (Exception e){
           e.printStackTrace();
       }

    }

    @Override
    public ObjectNode write_with_confirmation(String messageId, ObjectNode json) throws TimeoutException, InterruptedException {
        try {

            json.put("instanceId", super.identifikator);
            return blockoServer.write_with_confirmation(messageId, json);

        }catch (Exception e){
            throw new InterruptedException();
        }
    }

    @Override
    public ObjectNode write_with_confirmation(String messageId, ObjectNode json, Long time_To_TimeOutExcepting) throws TimeoutException, InterruptedException {
        try {

            json.put("instanceId", super.identifikator);
            return blockoServer.write_with_confirmation(messageId, json, time_To_TimeOutExcepting);

        }catch (Exception e){
            e.printStackTrace();
            throw new InterruptedException();
        }
    }


    @Override
    public void onClose() {
        this.close();
        WebSocketController_Incoming.homer_is_disconnect(this);
    }

    @Override
    public void onMessage(ObjectNode json) {
        System.out.println("příchozí zpráva v WS_Homer_cloud: " + json.asText());
        WebSocketController_Incoming.homer_incoming_message(this, json);


    }
}
