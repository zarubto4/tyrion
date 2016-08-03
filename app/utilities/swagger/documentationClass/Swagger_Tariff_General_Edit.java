package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

public class Swagger_Tariff_General_Edit {

    @Constraints.Required
    @Constraints.MinLength(value = 5, message = "The tariff_individual_name must have at least 4 characters")
    @ApiModelProperty(required = true, value =  "Required: always." +
            "The street must have at least 4 characters")
    public String product_individual_name;

    @Constraints.Required
    @Constraints.MinLength(value = 3)
    @Constraints.MaxLength(value = 3) public String currency_type;

}
