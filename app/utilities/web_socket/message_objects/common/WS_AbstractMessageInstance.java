package utilities.web_socket.message_objects.common;

import models.project.b_program.instnace.Model_HomerInstance;

public class WS_AbstractMessageInstance extends WS_AbstractMessage {

    public String instanceId;

    public Model_HomerInstance get_instance(){
        return Model_HomerInstance.find.byId(instanceId);
    }
}
