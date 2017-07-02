package web_socket.message_objects.homer_hardware_with_tyrion.updates;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_ActualizationProcedure;
import models.Model_Board;
import models.Model_HomerInstance;
import models.Model_HomerServer;
import play.libs.Json;
import utilities.swagger.outboundClass.Swagger_UpdatePlan_brief_for_homer;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class WS_Message_Update_device_firmware extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "update_state";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    public String actualization_procedure_id = null;
    public String c_program_update_plan_id = null;

    public String required_build_id = null;
    public String actual_build_id = null;

    public String device_id= null;
    public String update_state = null;


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(List<Swagger_UpdatePlan_brief_for_homer> tasks) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_Board.CHANNEL);
        request.set("update_tasks", Json.toJson(tasks));

        return request;
    }
}

