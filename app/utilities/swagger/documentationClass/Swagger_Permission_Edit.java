package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for change description for Permission",
             value = "Permission_Edit")
public class Swagger_Permission_Edit {

    @ApiModelProperty(required = false, value = "program_description can be null or maximum length of 255 characters.")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String description;

}
