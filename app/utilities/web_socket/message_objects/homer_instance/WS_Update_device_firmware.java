package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.project.b_program.instnace.Model_HomerInstance;
import play.libs.Json;
import utilities.hardware_updater.Actualization_procedure;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageInstance;

import java.util.List;

public class WS_Update_device_firmware extends WS_AbstractMessageInstance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "updateDevice";




    @JsonIgnore
    public ObjectNode make_request(Model_HomerInstance instance, List<Actualization_procedure> procedures) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerInstance.CHANNEL);
        request.put("instanceId", instance.blocko_instance_name);
        request.set("actualizationProcedures", Json.toJson(procedures));

        return request;
    }
}
