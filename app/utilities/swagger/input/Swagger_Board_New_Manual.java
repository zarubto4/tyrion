package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Constraints.Validate
@ApiModel(description = "Json Model for create new Board",
          value = "Board_New_Manual")
public class Swagger_Board_New_Manual implements Constraints.Validatable<List<ValidationError>> {

        @Constraints.Required
        @ApiModelProperty(value = "Required valid hardware_type_id", required = true)
        public UUID hardware_type_id;

        @ApiModelProperty(value = "Must be unique!!!, The hardware_id must have 24 hexadecimal characters!", required = true)
        @Constraints.Required
        public String full_id;

        @Override
        public List<ValidationError> validate() {

                List<ValidationError> errors = new ArrayList<>();

                if (!full_id.matches("^[0-9A-F]+$")) {

                        errors.add(new ValidationError("full_id","Full ID can contain only hex characters."));
                }

                if (full_id.length() != 24) {
                        errors.add(new ValidationError("full_id","Full ID must have 24 characters."));
                }

                return errors.isEmpty() ? null : errors;
        }
}