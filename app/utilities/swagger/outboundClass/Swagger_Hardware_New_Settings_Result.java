package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.NetSource;

@ApiModel(description = "Json Model with settings and firwmare and bootloader for Embedded hardware",
        value = "Hardware_New_Settings_Result")
public class Swagger_Hardware_New_Settings_Result {

    @ApiModelProperty(required = true, readOnly = true)  public String   full_id;

    @ApiModelProperty(required = true, readOnly = true)  public String   normal_mqtt_hostname;
    @ApiModelProperty(required = true, readOnly = true)  public String   normal_mqtt_port;
    @ApiModelProperty(required = true, readOnly = true)  public String   backup_mqtt_hostname;
    @ApiModelProperty(required = true, readOnly = true)  public String   backup_mqtt_port;
    @ApiModelProperty(required = true, readOnly = true)  public String   normal_mqtt_username;
    @ApiModelProperty(required = true, readOnly = true)  public String   normal_mqtt_password;
    @ApiModelProperty(required = true, readOnly = true)  public String   backup_mqtt_username;
    @ApiModelProperty(required = true, readOnly = true)  public String   backup_mqtt_password;
    @ApiModelProperty(required = true, readOnly = true)  public String   wifi_ssid;
    @ApiModelProperty(required = true, readOnly = true)  public String   wifi_username;
    @ApiModelProperty(required = true, readOnly = true)  public String   wifi_password;
    @ApiModelProperty(required = true, readOnly = true)  public Integer  devlist_counter = 0;
    @ApiModelProperty(required = true, readOnly = true)  public boolean  bootloader_report;      // hodnota 0 - vypnuto, nebo 1 zapnuto debug vypis bootloaderu)
    @ApiModelProperty(required = true, readOnly = true)  public String   mac_address;            // [addr in format XX:XX:XX:XX:XX:XX
    @ApiModelProperty(required = true, readOnly = true)  public boolean  autobackup;             // - hodnota 0, nebo 1 (povolit/zakázat)
    @ApiModelProperty(required = true, readOnly = true)  public NetSource netsource;              // 0 = nic, 1 = ethernet, 2 = wifi

    @ApiModelProperty(required = true, readOnly = true)  public String   firmware_base64;        // Základní program firmware vybraného hardwaru shodný formátem, jaký se vypaluje na device přes update
    @ApiModelProperty(required = true, readOnly = true)  public String   bootloader_base64;      // Základní program bootloaderu vybraného hardwaru shodný formátem, jaký se vypaluje na device přes update


}
