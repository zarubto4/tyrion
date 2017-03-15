package utilities.web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_FileRecord;
import models.Model_HomerInstance;
import play.libs.Json;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageInstance;

public class WS_Upload_blocko_program extends WS_AbstractMessageInstance {

    // MessageType
    @JsonIgnore
    public static final String messageType = "loadProgram";

    @JsonIgnore
    public ObjectNode make_request(Model_HomerInstance instance, Model_FileRecord fileRecord, String program_id) {

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerInstance.CHANNEL);
        request.put("instanceId", instance.blocko_instance_name);
        request.put("programId", program_id);
        request.put("program", fileRecord.get_fileRecord_from_Azure_inString());

        return request;
    }
}