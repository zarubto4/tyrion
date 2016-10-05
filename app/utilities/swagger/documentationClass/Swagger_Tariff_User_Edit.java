package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model with List of Board ID",
          value = "Tariff_edit")
public class Swagger_Tariff_User_Edit {

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The product_individual_name must have at least 4 characters")
    @Constraints.MaxLength(value = 60, message = "The product_individual_name must not have more than 60 characters.")
    @ApiModelProperty(required = true, value =  "Required: always." + "The product_individual_name length must be between 4 and 60 characters")
    public String product_individual_name;

    @Constraints.Required
    @Constraints.MinLength(value = 3)
    @Constraints.MaxLength(value = 3)
    @ApiModelProperty(required = true, value =  "Length must be 3 characters")
    public String currency_type;

}
