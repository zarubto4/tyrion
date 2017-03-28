package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_ProductExtension.Config;
import play.data.validation.Constraints;
import utilities.enums.Enum_ExtensionType;

@ApiModel(description = "Json Model for updating extension of product.",
        value = "ProductExtension_Edit")
public class Swagger_ProductExtension_Edit {

    @ApiModelProperty(required = false, value = "Name of extension")
    public String name;

    @Constraints.MaxLength(value = 255)
    @ApiModelProperty(required = false, value = "Description must not have more than 255 characters")
    public String description;

    @ApiModelProperty(required = false, value = "Color")
    public String color;
}
