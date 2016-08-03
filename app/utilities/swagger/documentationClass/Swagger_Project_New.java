package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new General Project",
        value = "Project_New")
public class Swagger_Project_New {

    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The name must have at least 8 characters, must be unique!")
    @ApiModelProperty(required = true, value = "The name must have at least 8 characters, must be unique!")
    public String project_name;


    @Constraints.MinLength(value = 24, message = "The description must have at least 24 characters")
    @ApiModelProperty(required = false, value = "The description must have at least 24 characters")
    public String project_description;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Id of Product (Tariff) where the project will be registered")
    public Long product_id;
}
