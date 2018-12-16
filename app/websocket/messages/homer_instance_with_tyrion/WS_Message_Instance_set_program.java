package websocket.messages.homer_instance_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_BProgramVersion;
import models.Model_Instance;
import models.Model_InstanceSnapshot;
import play.libs.Json;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WS_Message_Instance_set_program extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String message_type = "instances_set_program";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(Model_InstanceSnapshot snapshot) {

        List<Instance_Update_Request> request_list = new ArrayList<>();

        Model_BProgramVersion version = snapshot.getBProgramVersion();

        Instance_Update_Request update = new Instance_Update_Request();
        update.instance_id = snapshot.getInstanceId();
        update.snapshot_id = snapshot.id;
        update.program_version_id = version.id;
        update.program_version_name = version.name;
        update.b_program_id = version.get_b_program_id();
        update.b_program_name = version.getBProgram().name;
        request_list.add(update);

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Instance.CHANNEL);
        request.set("instances", Json.toJson(request_list));

        return request;
    }



    public class Instance_Update_Request{

        public UUID instance_id;
        public UUID snapshot_id;

        public UUID program_version_id;       // Download Link
        public String program_version_name;

        public UUID b_program_id;
        public String b_program_name;

    }

}