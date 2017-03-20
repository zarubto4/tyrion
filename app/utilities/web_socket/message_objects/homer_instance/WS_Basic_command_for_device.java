package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import play.libs.Json;
import utilities.enums.Enum_type_of_command;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageInstance;

public class WS_Basic_command_for_device extends WS_AbstractMessageInstance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "basicCommand";

    @JsonIgnore
    public ObjectNode make_request(Model_HomerInstance instance, String targetId, Enum_type_of_command command) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerInstance.CHANNEL);
        request.put("instanceId", instance.blocko_instance_name);
        request.put("commandType", command.get_command());
        request.put("targetId", targetId);

        return request;
    }
}
