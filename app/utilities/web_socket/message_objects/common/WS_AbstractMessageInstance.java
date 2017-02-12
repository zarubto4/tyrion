package utilities.web_socket.message_objects.common;

import models.project.b_program.instnace.Model_HomerInstance;
import play.data.validation.Constraints;

public abstract class WS_AbstractMessageInstance extends WS_AbstractMessage {

    @Constraints.Required public String instanceId;

    public Model_HomerInstance get_instance(){
        return Model_HomerInstance.find.byId(instanceId);
    }
}
