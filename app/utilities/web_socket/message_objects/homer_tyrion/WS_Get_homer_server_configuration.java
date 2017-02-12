package utilities.web_socket.message_objects.homer_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.project.b_program.servers.Model_HomerServer;
import play.data.validation.Constraints;
import play.libs.Json;
import utilities.web_socket.message_objects.common.WS_AbstractMessage;

import java.util.Date;

public class WS_Get_homer_server_configuration extends WS_AbstractMessage {

    @JsonIgnore
    public static final String messageType = "getServerConfiguration";

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


    @JsonIgnore
    public ObjectNode make_request() {

        ObjectNode request_conf = Json.newObject();
        request_conf.put("messageType", "getServerConfiguration");
        request_conf.put("messageChannel", Model_HomerServer.CHANNEL);

        return request_conf;
    }

}
