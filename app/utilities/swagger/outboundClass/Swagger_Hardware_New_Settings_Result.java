package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_TypeOfBoardFeatures;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model with settings and firwmare and bootloader for Embedded hardware",
        value = "Hardware_New_Settings_Result")
public class Swagger_Hardware_New_Settings_Result {


    @ApiModelProperty(required = true, readOnly = true)  public String   normal_mqtt_hostname;
    @ApiModelProperty(required = true, readOnly = true)  public int      normal_mqtt_port;

    @ApiModelProperty(required = true, readOnly = true)  public String   backup_mqtt_hostname;
    @ApiModelProperty(required = true, readOnly = true)  public int      backup_mqtt_port;

    @ApiModelProperty(required = true, readOnly = true)  public String   normal_mqtt_username;
    @ApiModelProperty(required = true, readOnly = true)  public String   normal_mqtt_password;

    @ApiModelProperty(required = true, readOnly = true)  public String   backup_mqtt_username;
    @ApiModelProperty(required = true, readOnly = true)  public String   backup_mqtt_password;

    @ApiModelProperty(required = true, readOnly = true)  public String   mac_address;            // [addr in format XX:XX:XX:XX:XX:XX
    @ApiModelProperty(required = false, readOnly = true) public String   full_id;                 // [číslo procesoru - přiloží se jen když ho zašle request (oprava vypálení)

}
