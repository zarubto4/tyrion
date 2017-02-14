package utilities.web_socket.message_objects.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.project.b_program.instnace.Model_HomerInstance;

import javax.persistence.Transient;

public abstract class WS_AbstractMessageInstance {

                           public String instanceId;

                           public String messageType;
                           public String messageId;
                           public String messageChannel;
                           public String status;

    public String error  = null;
    public Integer errorCode  = null;

    @JsonIgnore @Transient public Model_HomerInstance get_instance(){
        return Model_HomerInstance.find.byId(instanceId);
    }
}
