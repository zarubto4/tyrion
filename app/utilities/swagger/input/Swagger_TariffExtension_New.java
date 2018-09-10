package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for updating extension of product.",
        value = "TariffExtension_New")
public class Swagger_TariffExtension_New extends Swagger_NameAndDescription {

    @Constraints.Required
    public String color;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Enumerated type of extension")
    public String extension_type;

    @Constraints.Required @ApiModelProperty(required = true, value = "Json in String")
    public String config;

    @Constraints.Required @ApiModelProperty(required = true, value = "Json in String")
    public String consumption;
}
