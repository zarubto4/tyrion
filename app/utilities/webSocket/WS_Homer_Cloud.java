package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController_Incoming;

import java.util.concurrent.TimeoutException;

public class WS_Homer_Cloud extends WebSCType{

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    // Obslužné objekty pro tym Websocketu
    public WS_BlockoServer blockoServer;
    public String version_id = "ERROR"; // Pro první inicializaci a pro případ, že se version_id nedosadí a aby to bylo v komunikaci všude jasně vidět

    // Kontstruktor sloužící pro vytvoření objektu
    public WS_Homer_Cloud (String identificator,  String version_id,  WS_BlockoServer blockoServer) {
        super();
        this.version_id = version_id;
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
    public ObjectNode write_with_confirmation(ObjectNode json) throws TimeoutException, InterruptedException {
        try {

            json.put("instanceId", super.identifikator);
            return blockoServer.write_with_confirmation(json);

        }catch (Exception e){
            throw new InterruptedException();
        }
    }

    @Override
    public ObjectNode write_with_confirmation( ObjectNode json, Long time_To_TimeOutExcepting) throws TimeoutException, InterruptedException {
        try {

            json.put("instanceId", super.identifikator);
            return blockoServer.write_with_confirmation(json, time_To_TimeOutExcepting);

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
        logger.debug("Cloud Homer: "+ super.identifikator + " on blocko version: " + version_id + " Incoming message: " + json.toString());

        json.put("version_id", version_id);
        json.remove("instanceId");
        WebSocketController_Incoming.homer_incoming_message(this, json);
    }
}
