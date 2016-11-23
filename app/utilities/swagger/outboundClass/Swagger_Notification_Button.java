package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Notification_action;

public class Swagger_Notification_Button {

    public Swagger_Notification_Button(String text, Notification_action action, String color, String payload){
        this.text = text;
        this.action = action;
        this.color = color;
        this.payload = payload;
    }

    @ApiModelProperty(required =  true) public String text;
    @ApiModelProperty(required =  true) public Notification_action action;
    @ApiModelProperty(required =  true) public String color;
    @ApiModelProperty(required =  true) public String payload;
}
