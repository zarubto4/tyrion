package websocket.messages.homer_hardware_with_tyrion;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WS_Message_Hardware_overview_Board {

    public WS_Message_Hardware_overview_Board() {}

    @Constraints.Required public UUID uuid;
    @Constraints.Required public boolean online_status;
    @Constraints.Required public String mac;
                          public List<UUID> hardware_group_ids;



    @Valid public WS_Message_Hardware_overview_Board_Binaries binaries;

    @Constraints.Required  public String target;  // třeba Yoda G3
                           public String alias; // "John 33" - can be null
    @Constraints.Required  public String ip;
    @Constraints.Required  public boolean console;
    @Constraints.Required  public boolean autobackup;                // Default 0                    // user configurable ( 0 or 1)
    @Constraints.Required  public boolean blreport;                  // Default 0                    // user configurable
    @Constraints.Required  public boolean wdenable;                  // Default 1                    // user configurable
    @Constraints.Required  public String  netsource;                 // Default ethernet   // user configurable ( 0 or 1)
    @Constraints.Required  public boolean webview;                   // Default  1                   // user configurable via Bootloader & Portal ( 0 or 1)
    @Constraints.Required  public Integer webport;                   // Default  80                  // user configurable via Bootloader & Portal ( 80 - 9999)
    @Constraints.Required  public Integer timeoffset;                // Default  0                   // user configurable
    @Constraints.Required  public boolean timesync;                  // Default  1                   // user configurable
    @Constraints.Required  public boolean lowpanbr;                  // Default  0                   // user configurable
    @Constraints.Required  public Integer autojump;                  // Default  0                   // user configurable
    @Constraints.Required  public Integer wdtime;             // Default 30                   // user configurable


    @Constraints.Required  public String normal_mqtt_connection;       // ip addressa:port
    @Constraints.Required  public String backup_mqtt_connection;       // ip addressa:port



    // -- Kopírované parametry  z WS_AbstractMessage

    public UUID websocket_identificator;   // Becki ID, Homer Server ID Etc - Dosazuje do JSONu WS_SendMesage
    public String message_type;
    public String message_id;
    public String message_channel;
    public String status = "error";
    public String error_message = null;
    public Integer error_code = null;
}
