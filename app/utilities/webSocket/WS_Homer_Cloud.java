package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController;
import models.project.b_program.instnace.Homer_Instance;
import utilities.swagger.outboundClass.Swagger_Instance_HW_Group;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class WS_Homer_Cloud extends WebSCType{

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    // Obslužné objekty pro tym Websocketu
    public WS_BlockoServer blockoServer;
    public String version_id = "ERROR"; // Pro první inicializaci a pro případ, že se version_id nedosadí a aby to bylo v komunikaci všude jasně vidět
    public boolean virtual_instance;

    public List<Swagger_Instance_HW_Group> group = new ArrayList<>(); // Seznam HW - Který by měl na instanci běžet!


    // Kontstruktor sloužící pro vytvoření objektu
    public WS_Homer_Cloud (String identificator, boolean virtual_instance, String version_id,  WS_BlockoServer blockoServer) {
        super();
        this.version_id = version_id;
        super.identifikator = identificator;
        this.blockoServer = blockoServer;
        this.virtual_instance = virtual_instance;
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
    public ObjectNode write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries) throws ExecutionException, TimeoutException, InterruptedException {

        json.put("instanceId", super.identifikator);
        return blockoServer.write_with_confirmation(json, time, delay, number_of_retries);

    }


    @Override
    public void onClose() {
        this.close();
        WebSocketController.homer_instance_is_disconnect(this);
    }

    @Override
    public void onMessage(ObjectNode json) {
        logger.debug("Cloud Homer: "+ super.identifikator + " on blocko version: " + version_id + " Incoming message: " + json.toString());

        json.put("version_id", version_id);
        json.remove("instanceId");
        WebSocketController.homer_instance_incoming_message(this, json);
    }
}
