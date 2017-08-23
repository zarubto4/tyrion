package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for create Bootloader",
        value = "BootLoader_Create")
public class Swagger_BootLoader_New {

    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String name;

    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String version_identificator;

    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 2500, message = "The name must not have more than 2500 characters")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 2500 characters.")
    public String changing_note;

    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 2500, message = "The name must not have more than 2500 characters")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 2500 characters.")
    public String description;

}
