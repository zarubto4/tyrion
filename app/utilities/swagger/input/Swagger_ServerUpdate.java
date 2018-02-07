package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(value = "ServerUpdate", description = "Json Model for scheduling server update.")
public class Swagger_ServerUpdate {

    @Constraints.Required
    public String version;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "UNIX time in millis", example = "1466163478925", dataType = "integer")
    public Long update_time;

    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        if (update_time < new Date().getTime() + 5000) {
            errors.add(new ValidationError("update_time","Must be time in the future"));
        }

        return errors.isEmpty() ? null : errors;
    }
}
