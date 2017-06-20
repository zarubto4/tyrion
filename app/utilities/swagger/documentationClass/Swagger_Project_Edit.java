package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for Edit Project",
        value = "Project_Edit")
public class Swagger_Project_Edit {

    @Constraints.Required
    @Constraints.MinLength(value = 8,  message = "The name must have at least 8 characters, must be unique!")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters, must be unique!")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters, must be unique!")
    public String project_name;


    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 24 and 255 characters.")
    public String project_description;

}
