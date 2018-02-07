package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.NotificationAction;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@ApiModel(description = "Json Model for notification buttons",
        value = "Notification_Button")
public class Swagger_Notification_Button {

    @ApiModelProperty(required =  true) public String text;
    @ApiModelProperty(required =  true) @Enumerated(EnumType.STRING) public NotificationAction action;
    @ApiModelProperty(required =  true) public String color;
    @ApiModelProperty(required =  true) public String payload = null;

    @ApiModelProperty(required =  true) public boolean bold = false;
    @ApiModelProperty(required =  true) public boolean italic = false;
    @ApiModelProperty(required =  true) public boolean underline = false;
}
