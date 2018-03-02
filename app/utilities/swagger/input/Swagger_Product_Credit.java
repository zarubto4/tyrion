package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for buying credit for Product",
        value = "Product_Credit")
public class Swagger_Product_Credit {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Double number - amount of credit to be bought.")
    public double credit;
}
