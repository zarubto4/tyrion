package utilities.web_socket.message_objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.web_socket.message_objects.common.WS_AbstractMessage;

import java.io.IOException;

public class WS_Unregistred_device_connected extends WS_AbstractMessage {


        public static WS_Unregistred_device_connected getObject(ObjectNode json) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json.toString(), WS_Unregistred_device_connected.class);
        }

        @ApiModelProperty(required = true) @Constraints.Required  public String deviceId;


}

