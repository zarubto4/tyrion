package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.compiler.Model_FileRecord;
import models.project.b_program.instnace.Model_HomerInstance;
import play.libs.Json;
import utilities.enums.Firmware_type;
import utilities.web_socket.message_objects.common.WS_AbstractMessageInstance;

import java.util.List;

public class WS_Update_device_firmware extends WS_AbstractMessageInstance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "updateDevice";

    @JsonIgnore
    public ObjectNode make_request(Model_HomerInstance instance, String actualization_procedure_id, Firmware_type firmware_type, List<String> devicesId, Model_FileRecord record) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerInstance.CHANNEL);
        request.put("instanceId", instance.blocko_instance_name);
        request.put("actualization_procedure_id", actualization_procedure_id);
        request.set("devicesId", Json.toJson(devicesId) );
        request.put("firmware_type", firmware_type.get_firmwareType());
        request.set("targetIds", Json.toJson(devicesId));
        request.put("program", record.get_fileRecord_from_Azure_inString());

        if (record.boot_loader != null) request.put("build_id", record.boot_loader.version_identificator);              // Nahrávám Bootloader
        else                            request.put("build_id", record.c_compilations_binary_file.firmware_build_id);  // Nahrávám klasický Firmware




        return request;
    }
}
