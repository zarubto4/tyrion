package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "",
            value = "Instance_Edit")
public class Swagger_Instance_Edit {

    @Constraints.MinLength(value = 4, message = "The name must not have more than 255 characters.")
    @Constraints.MaxLength(value = 255, message = "The name must not have more than 255 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 0 and 255 characters.")
    public String name;

    @ApiModelProperty(required = false, value = "Description")
    @Constraints.MaxLength(value = 255, message = "The Description must not have more than 255 characters.")
    public String description;
}