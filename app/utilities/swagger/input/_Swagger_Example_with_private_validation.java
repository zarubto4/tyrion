package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.ArrayList;
import java.util.List;


@ApiModel(
        value = "Example_with_private_validation",
        description = "Json Model for validation on object with required parameters"
)
public class _Swagger_Example_with_private_validation extends  _Swagger_Abstract_Default implements  Constraints.Validatable<List<ValidationError>> {

        @Constraints.Required
        @ApiModelProperty(value = "Name of something", required = true, example = "Terminator")
        public String example_name;

        @Constraints.Required
        @ApiModelProperty(value = "Salary - Must be positive and under", required = true,  example = "2400", dataType = "Double")
        @Constraints.Min(value = 0)
        public Double example_salary;


        @Constraints.Required
        @ApiModelProperty(value = "Salary - Must be positive", required = true,  example = "21", dataType = "Integer")
        @Constraints.Min(value = 0)
        @Constraints.Max(value = 120)
        public Integer example_age;

        @ApiModelProperty(value = "Must be unique!!!, The hardware_id must have 24 hexadecimal characters!", required = true, example = "ABBBABBABF09192ABCD")
        @Constraints.Required
        @Constraints.MinLength(value = 14)
        @Constraints.MaxLength(value = 30)
        public String something;

        @Constraints.Required
        @ApiModelProperty(value = "Required Super Boolean", required = true, example = "false", dataType = "Boolean")
        public boolean example_boolean;


        @Override
        public List<ValidationError> validate() {

                List<ValidationError> errors = new ArrayList<>();

                if (something != null) {
                        if (!something.matches("^[0-9A-F]+$")) {

                                errors.add(new ValidationError("something", "Something can contain only hex characters."));
                        }

                }

                return errors.isEmpty() ? null : errors;
        }

}
