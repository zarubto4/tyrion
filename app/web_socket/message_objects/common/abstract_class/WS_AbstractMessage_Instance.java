package web_socket.message_objects.common.abstract_class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.Model_HomerInstance;

import javax.persistence.Transient;

public abstract class WS_AbstractMessage_Instance {

                           public String instanceId;

                           public String messageType;
                           public String messageId;
                           public String messageChannel;
                           public String status = "error";

    public String error  = null;
    public Integer errorCode  = null;

    @JsonIgnore @Transient public Model_HomerInstance get_instance(){
        return Model_HomerInstance.find.byId(instanceId);
    }
}
