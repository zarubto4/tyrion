package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.awt.*;
import java.util.*;

@ApiModel(description = "Json Model for new Article",
        value = "Article")
public class Swagger_Project_Documentation_Article {

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @ApiModelProperty(required = true)
    public String name;

    @Constraints.Required
    @Constraints.MinLength(value = 24, message = "The text must have at least 24 characters")
    @ApiModelProperty(required = true)
    public String text;
}
