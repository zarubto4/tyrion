package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import play.libs.Json;
import utilities.hardware_updater.Actualization_procedure;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageInstance;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class WS_Update_device_firmware extends WS_AbstractMessageInstance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "updateDevice";

    @Valid
    public List<UpdateDeviceInformation> procedure_list = new ArrayList<>();



    public static class UpdateDeviceInformation {

        public UpdateDeviceInformation(){}

        public String actualizationProcedureId;
        @Valid public List<UpdateDeviceInformation_Device> device_state_list = new ArrayList<>();
    }

    public static class UpdateDeviceInformation_Device{

        public UpdateDeviceInformation_Device(){}

        public String c_program_update_plan_id = null;

        public String required_buildId = null;

        public String actual_build_id = null;
        public String firmwareType = null;

        public String deviceId= null;
        public String update_state = null;

        public String error = null;
        public Integer errorCode = null;

    }



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
