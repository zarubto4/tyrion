package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import utilities.enums.NotificationAction;

import java.util.ArrayList;
import java.util.List;

@Constraints.Validate
@ApiModel(description = "Json Model for confirming notification",
             value = "Notification_Confirm")
public class Swagger_Notification_Confirm implements Constraints.Validatable<List<ValidationError>> {

    @Constraints.Required
    @ApiModelProperty(value = "Notification_action", required = true)
    public NotificationAction action;

    @ApiModelProperty(value = "Notification payload")
    public String payload;

    @Override
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        if (!action.equals(NotificationAction.CONFIRM_NOTIFICATION) && payload == null) {
            errors.add(new ValidationError("payload", "Payload is required, can be null only if action is CONFIRM_NOTIFICATION"));
        }

        return errors.isEmpty() ? null : errors;
    }
}
