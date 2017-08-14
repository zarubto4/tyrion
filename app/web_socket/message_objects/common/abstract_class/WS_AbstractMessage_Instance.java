package web_socket.message_objects.common.abstract_class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.Model_HomerInstance;
import play.data.validation.Constraints;
import utilities.logger.Class_Logger;

import javax.persistence.Transient;

public abstract class WS_AbstractMessage_Instance {

 /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(WS_AbstractMessage_Instance.class);

 /* VALUE --------------------------------------------------------------------------------------------------------------*/

    @Constraints.Required public String websocket_identificator;   // Becki ID, Homer Server ID Etc - Dosazuje do JSONu WS_SendMesage
    @Constraints.Required public String message_type;
    @Constraints.Required public String message_id;
    @Constraints.Required public String message_channel;

                           public String instance_id;
                           public String status = "error_message";

    public String error  = null;
    public Integer error_code = null;

    @JsonIgnore @Transient public Model_HomerInstance get_instance(){

        return Model_HomerInstance.get_byId(instance_id);
    }
}