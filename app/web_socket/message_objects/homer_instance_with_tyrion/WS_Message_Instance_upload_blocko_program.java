package web_socket.message_objects.homer_instance_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_FileRecord;
import models.Model_HomerInstance;
import models.Model_VersionObject;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

public class WS_Message_Instance_upload_blocko_program extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "load_blocko_program";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(Model_FileRecord fileRecord, Model_VersionObject b_program_version) {

        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_HomerInstance.CHANNEL);
        request.put("b_program_name", b_program_version.b_program.name);
        request.put("b_program_id", b_program_version.b_program.id);
        request.put("program_version_name", b_program_version.version_name);
        request.put("program_version_id", b_program_version.id);
        request.put("file_path", fileRecord.file_path);

        return request;
    }
}