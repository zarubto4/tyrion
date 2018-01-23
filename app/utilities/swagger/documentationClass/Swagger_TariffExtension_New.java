package utilities.swagger.documentationClass;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for updating extension of product.",
        value = "TariffExtension_New")
public class Swagger_TariffExtension_New {

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 3 characters. Recommended be unique if its not private!")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters. Recommended be unique if not private!")
    @ApiModelProperty(required = true, value = "The name must not have more than 60 characters and minimal length is 4")
    public String name;

    @Constraints.MaxLength(value = 254, message = "The description must not have more than 255 characters.")
    @ApiModelProperty(required = true, value = "The name must not have more than 60 characters and minimal length is 4")
    public String description;

    @Constraints.Required
    public String color;

    @Constraints.Required
    public boolean included;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Enumerated type of extension")
    public String extension_type;

    @Constraints.Required @ApiModelProperty(required = true, value = "Json in String")
    public String config;
}
