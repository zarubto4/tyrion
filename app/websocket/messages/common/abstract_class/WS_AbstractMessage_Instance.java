package websocket.messages.common.abstract_class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.Model_Instance;
import play.data.validation.Constraints;
import utilities.logger.Logger;

import javax.persistence.Transient;
import java.util.UUID;

public abstract class WS_AbstractMessage_Instance {

 /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger terminal_logger = new Logger(WS_AbstractMessage_Instance.class);

 /* VALUE --------------------------------------------------------------------------------------------------------------*/

    @Constraints.Required public String websocket_identificator;   // Becki ID, Homer Server ID Etc - Dosazuje do JSONu WS_SendMesage
    @Constraints.Required public String message_type;
    @Constraints.Required public String message_id;
    @Constraints.Required public String message_channel;

                           public UUID instance_id;
                           public String status = "error";

    public String error  = null;
    public Integer error_code = null;

    @JsonIgnore @Transient public Model_Instance get_instance() {

        return Model_Instance.getById(UUID.fromString(instance_id));
    }
}