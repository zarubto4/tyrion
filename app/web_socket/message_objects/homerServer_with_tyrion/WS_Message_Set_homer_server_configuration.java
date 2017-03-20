package web_socket.message_objects.homerServer_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.libs.Json;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

public class WS_Message_Set_homer_server_configuration extends WS_AbstractMessage {

    @JsonIgnore
    public static final String messageType = "setServerConfiguration";

    @JsonIgnore
    public ObjectNode make_request(Model_HomerServer server) {

        ObjectNode request_conf = Json.newObject();
        request_conf.put("messageType", messageType);
        request_conf.put("messageChannel", Model_HomerServer.CHANNEL);
        request_conf.put("status", "success");
        request_conf.put("serverName", server.personal_server_name);
        request_conf.put("mqttPort", server.mqtt_port);
        request_conf.put("mqttPassword", server.mqtt_password);
        request_conf.put("mqttUser", server.mqtt_username);
        request_conf.put("gridPort", server.grid_port);
        request_conf.put("webPort", server.server_remote_port);
        request_conf.put("beckiPort", server.webView_port);
        request_conf.put("timeStamp", server.time_stamp_configuration.getTime());
        request_conf.put("daysInArchive", server.days_in_archive);
        request_conf.put("logging", server.logging);
        request_conf.put("interactive", server.interactive);
        request_conf.put("logLevel", server.logLevel.toString());

        return request_conf;
    }
}
