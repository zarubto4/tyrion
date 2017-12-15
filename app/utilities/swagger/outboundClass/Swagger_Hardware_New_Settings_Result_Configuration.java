package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.document_db.document_objects.DM_Board_Bootloader_DefaultConfig;

@ApiModel(description = "",
        value = "Hardware_New_Settings_Result_Configuration")
public class Swagger_Hardware_New_Settings_Result_Configuration {


    @ApiModelProperty(required = true, readOnly = true)  @Constraints.Required  public String   normal_mqtt_hostname;
    @ApiModelProperty(required = true, readOnly = true)  @Constraints.Required  public int      normal_mqtt_port;

    @ApiModelProperty(required = true, readOnly = true)  @Constraints.Required  public String   backup_mqtt_hostname;
    @ApiModelProperty(required = true, readOnly = true)  @Constraints.Required  public int      backup_mqtt_port;

    @ApiModelProperty(required = true, readOnly = true)  @Constraints.Required  public String   mqtt_username;
    @ApiModelProperty(required = true, readOnly = true)  @Constraints.Required  public String   mqtt_password;

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String  mac;              // [addr in format XX:XX:XX:XX:XX:XX
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public boolean autobackup;                // Default 0                    // user configurable ( 0 or 1)
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public boolean blreport;         // Default 0                    // user configurable
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public boolean wdenable;          // Default 1                    // user configurable
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String  netsource;                 // Default ethernet   // user configurable ( 0 or 1)
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public Integer backuptime;                // Default  60                  // user configurable
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public boolean webview;                   // Default  1                   // user configurable via Bootloader & Portal ( 0 or 1)
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public Integer webport;                   // Default  80                  // user configurable via Bootloader & Portal ( 80 - 9999)
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public Integer timeoffset;                // Default  0                   // user configurable
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public boolean timesync;                  // Default  1                   // user configurable
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public boolean lowpanbr;                  // Default  0                   // user configurable
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public Integer autojump;                  // Default  0                   // user configurable
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public Integer wdtime;             // Default 30                   // user configurable

}


