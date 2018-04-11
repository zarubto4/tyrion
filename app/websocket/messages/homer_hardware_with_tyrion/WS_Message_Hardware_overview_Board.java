package websocket.messages.homer_hardware_with_tyrion;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WS_Message_Hardware_overview_Board implements Constraints.Validatable<List<ValidationError>> {

    public WS_Message_Hardware_overview_Board() {}

    @Constraints.Required public UUID uuid;
    @Constraints.Required public boolean online_status;
                           public String mac;
                          public List<UUID> hardware_group_ids;



      @Valid public WS_Message_Hardware_overview_Board_Binaries binaries;

      public String target;  // třeba Yoda G3
                           public String alias; // "John 33" - can be null
      public String ip;
      public boolean console;
      public boolean autobackup;                // Default 0                    // user configurable ( 0 or 1)
      public boolean blreport;                  // Default 0                    // user configurable
      public boolean wdenable;                  // Default 1                    // user configurable
      public String  netsource;                 // Default ethernet   // user configurable ( 0 or 1)
      public boolean webview;                   // Default  1                   // user configurable via Bootloader & Portal ( 0 or 1)
      public Integer webport;                   // Default  80                  // user configurable via Bootloader & Portal ( 80 - 9999)
      public Integer timeoffset;                // Default  0                   // user configurable
      public boolean timesync;                  // Default  1                   // user configurable
      public boolean lowpanbr;                  // Default  0                   // user configurable
      public Integer autojump;                  // Default  0                   // user configurable
      public Integer wdtime;             // Default 30                   // user configurable


      public String normal_mqtt_connection;       // ip addressa:port
      public String backup_mqtt_connection;       // ip addressa:port



    // -- Kopírované parametry  z WS_AbstractMessage

    public UUID websocket_identificator;   // Becki ID, Homer Server ID Etc - Dosazuje do JSONu WS_SendMesage
    public String message_type;
    public String message_id;
    public String message_channel;
    public String status = "error";
    public String error_message = null;
    public Integer error_code = null;

    @Override
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();
        if(error_message != null && error_message.equals("ERROR_HARDWARE_COMMAND_OFFLINE_DEVICE")) {
            return null;
        } else  {

            if(ip == null) {
                errors.add(new ValidationError("ip","This field is required"));
            }
            if(netsource == null) {
                errors.add(new ValidationError("netsource","This field is required"));
            }
        }

        return errors.isEmpty() ? null : errors;
    }
}
