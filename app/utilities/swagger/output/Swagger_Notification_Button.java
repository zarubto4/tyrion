package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.NotificationAction;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

@ApiModel(description = "Json Model for notification buttons",
        value = "Notification_Button")
public class Swagger_Notification_Button extends _Swagger_Abstract_Default {

    @ApiModelProperty(required =  true) public String text;
    @ApiModelProperty(required =  true) public NotificationAction action;
    @ApiModelProperty(required =  true) public String color;
    @ApiModelProperty(required =  true) public String payload = null;

    @ApiModelProperty(required =  true) public boolean bold = false;
    @ApiModelProperty(required =  true) public boolean italic = false;
    @ApiModelProperty(required =  true) public boolean underline = false;
}
