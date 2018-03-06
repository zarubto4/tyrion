package websocket.messages.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Hardware;
import models.Model_HomerServer;
import play.libs.Json;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import java.util.List;
import java.util.UUID;

public class WS_Message_Hardware_change_server  extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String message_type = "hardware_change_server";


/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(Model_HomerServer server, List<String> device_full_ids) {

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Hardware.CHANNEL);
        request.put("mqtt_host", server.server_url);
        request.put("mqtt_port", server.mqtt_port);
        request.set("full_ids", Json.toJson(device_full_ids));

        return request;
    }

    @JsonIgnore
    public ObjectNode make_request(String mqtt_host, String mqtt_port, List<String> device_full_ids) {

        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Hardware.CHANNEL);
        request.put("mqtt_host", mqtt_host);
        request.put("mqtt_port", mqtt_port);
        request.set("full_ids", Json.toJson(device_full_ids));

        return request;
    }
}
