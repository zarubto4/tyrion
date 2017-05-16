package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.enums.Enum_Notification_action;

@ApiModel(description = "Json Model for confirming notification",
             value = "Notification_Confirm")
public class Swagger_Notification_Confirm {

    @Constraints.Required
    @ApiModelProperty(value = "Notification_action", required = true)
    public Enum_Notification_action action;

    @Constraints.Required
    @ApiModelProperty(value = "Notification payload", required = true)
    public String payload;


}
