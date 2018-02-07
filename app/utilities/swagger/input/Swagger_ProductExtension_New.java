package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for creating new extension of product.",
        value = "ProductExtension_New")
public class Swagger_ProductExtension_New extends Swagger_NameAndDescription {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Color of extension")
    public String color;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Enumerated type of extension")
    public String extension_type;

    @Constraints.Required
    public String config;
}