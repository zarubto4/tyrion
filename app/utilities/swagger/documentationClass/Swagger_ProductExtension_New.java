package utilities.swagger.documentationClass;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import utilities.enums.Enum_ExtensionType;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for creating new extension of product.",
        value = "ProductExtension_New")
public class Swagger_ProductExtension_New {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Name of extension")
    public String name;

    @Constraints.MaxLength(value = 255)
    @ApiModelProperty(required = false, value = "Description must not have more than 255 characters")
    public String description;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Color of extension")
    public String color;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Enumerated type of extension")
    public String extension_type;

    @Constraints.Required
    public String config;

}