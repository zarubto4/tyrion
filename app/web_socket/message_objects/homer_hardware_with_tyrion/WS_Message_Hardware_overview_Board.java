package web_socket.message_objects.homer_hardware_with_tyrion;

import play.data.validation.Constraints;
import web_socket.message_objects.common.abstract_class.WS_AbstractMessage;

import javax.validation.Valid;

public class WS_Message_Hardware_overview_Board {

    public WS_Message_Hardware_overview_Board(){}

    @Constraints.Required public String hardware_id;
    @Constraints.Required public boolean online_state;
    public String mac;

    public String target;                       // třeba Yoda G3
    public String alias;                        // "pepa"

    @Valid public WS_Message_Hardware_overview_Board_Binaries binaries;

    public String ip;
    public boolean console;
    public boolean webview;
    public Integer webport;

    public String normal_mqtt_connection;       // ip addressa:port
    public String backup_mqtt_connection;       // ip addressa:port

    public boolean autobackup;



    // -- Kopírované parametry  z WS_AbstractMessage

    public String websocket_identificator;   // Becki ID, Homer Server ID Etc - Dosazuje do JSONu WS_SendMesage
    public String message_type;
    public String message_id;
    public String message_channel;
    public String status = "error";
    public String error_message = null;
    public Integer error_code = null;
}
