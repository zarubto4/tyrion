package utilities.web_socket.message_objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.Constraints;
import utilities.web_socket.message_objects.common.WS_AbstractMessage;

import java.io.IOException;
import java.util.Date;

public class WS_CheckHomerServerConfiguration extends WS_AbstractMessage {


    public static WS_CheckHomerServerConfiguration getObject(ObjectNode json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json.toString(), WS_CheckHomerServerConfiguration.class);
    }

    @Constraints.Required public String serverName;

    @Constraints.Required public int mqttPort;
    @Constraints.Required public String mqttUser;
    @Constraints.Required public String mqttPassword;

    @Constraints.Required public int gridPort;
    @Constraints.Required public int beckiPort;
    @Constraints.Required public int webPort;

    @Constraints.Required public String tyrionUrl; // Adresa - na kterou se Homer připojuje

    @Constraints.Required public int daysInArchive;
    @Constraints.Required public Date timeStamp;

    @Constraints.Required public boolean logging;
    @Constraints.Required public boolean interactive;
    @Constraints.Required public String logLevel;

}
