package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModelProperty;
import models.Model_ProductExtension.Config;
import play.data.validation.Constraints;
import utilities.enums.Enum_ExtensionType;

public class Swagger_Tariff_General_Extension_Create {

    @Constraints.Required
    public String tariff_id;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Name of extension")
    public String name;

    @ApiModelProperty(required = false, value = "Description")
    public String description;

    @Constraints.Required
    public boolean included;

    @Constraints.Required
    public Enum_ExtensionType type;

    @Constraints.Required
    public Config config;
}
