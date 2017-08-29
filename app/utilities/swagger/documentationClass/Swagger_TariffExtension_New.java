package utilities.swagger.documentationClass;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for updating extension of product.",
        value = "TariffExtension_New")
public class Swagger_TariffExtension_New {

    @Constraints.Required
    public String name;

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
