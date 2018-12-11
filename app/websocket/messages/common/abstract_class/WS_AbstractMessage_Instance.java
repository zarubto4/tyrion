package websocket.messages.common.abstract_class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.Model_Instance;

import javax.persistence.Transient;
import java.util.UUID;

public abstract class WS_AbstractMessage_Instance extends WS_AbstractMessage {

    public UUID instance_id;

    public String error = null;  // Historický

    @JsonIgnore @Transient
    public Model_Instance get_instance() {
        return Model_Instance.find.byId(instance_id);
    }
}