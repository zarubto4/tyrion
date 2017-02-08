package utilities.webSocket.messageObjects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.Constraints;

import java.io.IOException;
import java.util.Date;

public class WS_CheckHomerServerConfiguration {


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

    // Defualtní
    public String messageId;
    public String messageType;
    public String messageChannel;
    public String status;

}
