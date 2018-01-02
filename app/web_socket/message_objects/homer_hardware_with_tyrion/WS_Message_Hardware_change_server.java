package web_socket.message_objects.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Board;
import models.Model_HomerServer;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

import java.net.URL;
import java.util.List;

public class WS_Message_Hardware_change_server  extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String message_type = "hardware_change_server";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(Model_HomerServer server, List<String> device_ids) {

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Board.CHANNEL);
        request.put("mqtt_host", server.server_url);
        request.put("mqtt_port", server.mqtt_port);
        request.set("device_ids", Json.toJson(device_ids) );

        return request;
    }

    @JsonIgnore
    public ObjectNode make_request(String mqtt_host, String mqtt_port, List<String> device_ids) {

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Board.CHANNEL);
        request.put("mqtt_host", mqtt_host);
        request.put("mqtt_port", mqtt_port);
        request.set("device_ids", Json.toJson(device_ids) );

        return request;
    }
}
