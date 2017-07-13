package web_socket.message_objects.homer_instance_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import models.Model_VersionObject;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;

import java.util.ArrayList;
import java.util.List;

public class WS_Message_Instance_upload_blocko_program extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String message_type = "instances_set_program";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(Model_HomerInstance instance , Model_VersionObject b_program_version) {  // TODO předělat na pole


        List<Instance_Update_Request> request_list = new ArrayList<>();

        Instance_Update_Request update = new Instance_Update_Request();
        update.instance_id = instance.id;
        update.program_version_id = b_program_version.id;
        update.program_version_name = b_program_version.version_name;
        update.b_program_id = b_program_version.get_b_program().id;
        update.b_program_name = b_program_version.get_b_program().name;
        request_list.add(update);

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_HomerInstance.CHANNEL);
        request.set("instances", Json.toJson(request_list));

        return request;
    }



    public class Instance_Update_Request{

        public String instance_id;

        public String program_version_id;       // Download Link
        public String program_version_name;

        public String b_program_id;
        public String b_program_name;



    }

}