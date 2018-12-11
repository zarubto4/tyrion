package websocket.messages.common.abstract_class;

import play.data.validation.Constraints;

import java.util.UUID;

public abstract class WS_AbstractMessage_Hardware extends WS_AbstractMessage {

    @Constraints.Required public UUID uuid;

    public String error  = null;
}