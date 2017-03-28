package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_ProductExtension.Config;
import play.data.validation.Constraints;
import utilities.enums.Enum_ExtensionType;

@ApiModel(description = "Json Model for creating new extension of product.",
        value = "ProductExtension_New")
public class Swagger_ProductExtension_New {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Id of product to extend")
    public String product_id;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Name of extension")
    public String name;

    @Constraints.MaxLength(value = 255)
    @ApiModelProperty(required = false, value = "Description must not have more than 255 characters")
    public String description;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Name of extension")
    public String color;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Enumerated type of extension")
    public Enum_ExtensionType type;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Configuration of individual extension")
    public Config config;
}
