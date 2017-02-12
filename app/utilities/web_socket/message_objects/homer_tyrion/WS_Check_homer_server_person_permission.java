package utilities.web_socket.message_objects.homer_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.web_socket.message_objects.common.WS_AbstractMessage;

public class WS_Check_homer_server_person_permission extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore
    public static final String messageType = "checkUserPermission";

    @ApiModelProperty(required = true) @Constraints.Required  public String email;
    @ApiModelProperty(required = true) @Constraints.Required  public String password;
    @ApiModelProperty(required = true) @Constraints.Required  public String user_agent;


}
