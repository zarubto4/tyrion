package websocket.messages.homer_with_tyrion.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.libs.Json;
import websocket.interfaces.Homer;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Homer_Get_configuration extends WS_AbstractMessage {

    @JsonIgnore public static final String message_type = "homer_set_configuration";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/


/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request(Model_HomerServer server) {

        ObjectNode request_conf = Json.newObject();
        request_conf.put("message_type", message_type);
        request_conf.put("message_channel", Homer.CHANNEL);
        request_conf.put("status", "success");
        request_conf.put("server_name", server.name);
        request_conf.put("mqtt_port", server.mqtt_port);
        request_conf.put("grid_port", server.grid_port);
        request_conf.put("web_port", server.hardware_logger_port);
        request_conf.put("becki_port", server.web_view_port);
        request_conf.put("timeStamp", server.time_stamp_configuration.getTime());
        request_conf.put("daysInArchive", server.days_in_archive);
        request_conf.put("logging", server.logging);
        request_conf.put("interactive", server.interactive);
        request_conf.put("logLevel", server.log_level.toString());

        return request_conf;
    }
}
