package web_socket.message_objects.common.abstract_class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.Model_HomerInstance;
import utilities.logger.Class_Logger;

import javax.persistence.Transient;

public abstract class WS_AbstractMessage_Instance {

 /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(WS_AbstractMessage_Instance.class);

 /* VALUE --------------------------------------------------------------------------------------------------------------*/

                           public String instanceId;
                           public String serverId = null;

                           public String messageType;
                           public String messageId;
                           public String message_channel;
                           public String status = "error";

    public String error  = null;
    public Integer error_code = null;

    @JsonIgnore @Transient public Model_HomerInstance get_instance(){

        return Model_HomerInstance.get_byId(instanceId);
    }
}