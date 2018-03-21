package websocket.messages.common.abstract_class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.Model_Hardware;
import play.data.validation.Constraints;
import utilities.logger.Logger;

import java.util.UUID;

public abstract class WS_AbstractMessage_Hardware {

 /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(WS_AbstractMessage_Hardware.class);

 /* VALUE --------------------------------------------------------------------------------------------------------------*/

    @Constraints.Required public String websocket_identificator;   // Becki ID, Homer Server ID Etc - Dosazuje do JSONu WS_SendMesage
    @Constraints.Required public String message_type;
    @Constraints.Required public String message_id;
    @Constraints.Required public String message_channel;

    @Constraints.Required public UUID uuid;
                          public String status = "error";

    public String error  = null;
    public Integer error_code = null;


}