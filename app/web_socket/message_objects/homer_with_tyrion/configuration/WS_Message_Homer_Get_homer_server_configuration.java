package web_socket.message_objects.homer_with_tyrion.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.data.validation.Constraints;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

import java.util.Date;

public class WS_Message_Homer_Get_homer_server_configuration extends WS_AbstractMessage {

    @JsonIgnore public static final String message_type = "homer_get_configuration";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required public String server_name;

    @Constraints.Required public int mqtt_port;
    @Constraints.Required public String mqtt_user;
    @Constraints.Required public String mqtt_password;

    @Constraints.Required public int grid_port;

    @Constraints.Required public int becki_port;

    @Constraints.Required public int web_port;


    @Constraints.Required public Long time_stamp_configuration;

    public Date get_Date(){
        return new Date(time_stamp_configuration * 1000);
    }


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request() {

        ObjectNode request_conf = Json.newObject();
        request_conf.put("message_type", message_type);
        request_conf.put("message_channel", Model_HomerServer.CHANNEL);

        return request_conf;
    }

}
