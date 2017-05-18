package web_socket.message_objects.homer_instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage_Instance;


public class WS_Message_AutoBackUp_progress extends WS_AbstractMessage_Instance {

    // MessageType
    @JsonIgnore public static final String messageType = "autoBackUp_progress";


    @Constraints.Required  public String phase;            // only two kind of value "start" and "done"
    @Constraints.Required  public boolean autobackup;
    @Constraints.Required  public String build_id;
    @Constraints.Required  public String deviceId;

}
