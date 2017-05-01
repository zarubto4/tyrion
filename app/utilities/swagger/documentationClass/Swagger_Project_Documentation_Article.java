package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new Article",
        value = "Article")
public class Swagger_Project_Documentation_Article {

    @Constraints.Required
    @Constraints.MinLength(value = 8,  message = "The name must have at least 8 characters.")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String name;

    @Constraints.Required
    @Constraints.MinLength(value = 24, message = "The text must have at least 24 characters")
    @Constraints.MaxLength(value = 255, message = "The text must not have more than 255 characters.")
    @ApiModelProperty(required = true, value = "Length must be between 24 and 255 characters.")
    public String text;
}
