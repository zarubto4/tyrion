package websocket.messages.common.abstract_class;

import play.data.validation.Constraints;

public abstract class  WS_AbstractMessage {

    @Constraints.Required public String message_id;
    @Constraints.Required public String message_type;
    @Constraints.Required public String message_channel;

    public String status = "error";            // Defaultně eror - pokud není přepsáno příchozím success
    public String error_message = null;
    public Integer error_code = null;
}


