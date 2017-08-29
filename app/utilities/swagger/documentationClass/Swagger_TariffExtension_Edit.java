package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for updating extension of product.",
        value = "TariffExtension_Edit ")
public class Swagger_TariffExtension_Edit {

    @Constraints.Required
    public String name;

    public String description;

    @Constraints.Required
    public String color;

    @Constraints.Required
    public boolean included;

    @Constraints.Required @ApiModelProperty(required = true, value = "Json in String")
    public String config;

}
