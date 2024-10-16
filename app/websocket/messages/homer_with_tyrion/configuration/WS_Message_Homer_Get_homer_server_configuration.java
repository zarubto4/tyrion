package websocket.messages.homer_with_tyrion.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;
import websocket.interfaces.Homer;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import java.util.Date;

public class WS_Message_Homer_Get_homer_server_configuration extends WS_AbstractMessage {

    @JsonIgnore public static final String message_type = "homer_get_configuration";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    // @Constraints.Required   ?
    @Constraints.Required public String server_version; // v.1.0.4

    @Constraints.Required public String server_id;

    @Constraints.Required public int mqtt_port;
    @Constraints.Required public int grid_port;
    @Constraints.Required public int web_view_port;
    @Constraints.Required public int hw_logger_port;
    @Constraints.Required public int rest_api_port;
    @Constraints.Required public String server_url; // localhost, 123.412.123.111, ...

    // @Constraints.Required public Long time_stamp_configuration;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectNode make_request() {

        ObjectNode request_conf = Json.newObject();
        request_conf.put("message_type", message_type);
        request_conf.put("message_channel", Homer.CHANNEL);

        return request_conf;
    }

}
