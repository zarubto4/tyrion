package web_socket.message_objects.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Board;
import models.Model_HomerServer;
import play.data.validation.Constraints;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

import java.util.List;

public class WS_Message_Hardware_change_server  extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String messageType = "hardware_change_server";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(Model_HomerServer server, List<String> device_ids) {

        ObjectNode request = Json.newObject();
        request.put("messageType", messageType);
        request.put("messageChannel", Model_Board.CHANNEL);
        request.put("main_server_url", server.server_url);
        request.put("mqtt_port", server.mqtt_port);
        request.put("mqtt_password", server.mqtt_password);
        request.put("mqtt_user_name", server.mqtt_username);
        request.set("device_ids", Json.toJson(device_ids) );

        return request;
    }
}
