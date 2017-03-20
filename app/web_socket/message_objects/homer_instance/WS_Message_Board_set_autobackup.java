package web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Board;
import models.Model_HomerInstance;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

public class WS_Message_Board_set_autobackup extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "setAutoBackup";


    @JsonIgnore
    public ObjectNode make_request(Model_HomerInstance instance, Model_Board board) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerInstance.CHANNEL);
        request.put("instanceId", instance.blocko_instance_name);
        request.put("targetId", board.id);

        return request;

    }
}
