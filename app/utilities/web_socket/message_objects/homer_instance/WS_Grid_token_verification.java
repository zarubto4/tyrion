package utilities.web_socket.message_objects.homer_instance;

import play.data.validation.Constraints;
import utilities.web_socket.message_objects.common.WS_AbstractMessageInstance;

public class WS_Grid_token_verification extends WS_AbstractMessageInstance {

    @Constraints.Required public String token;
}
