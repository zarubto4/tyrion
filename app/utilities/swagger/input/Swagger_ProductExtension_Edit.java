package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for updating extension of product.",
        value = "ProductExtension_Edit")
public class Swagger_ProductExtension_Edit extends Swagger_NameAndDescription {

    @ApiModelProperty(required = false, value = "Color")
    public String color;
}
