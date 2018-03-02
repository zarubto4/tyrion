package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.List;

@ApiModel(description = "Json Model for new Documentation",
        value = "Documentation")
public class Swagger_Project_Documentation {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "There must be at least one article")
    public List<Swagger_Project_Documentation_Article> articles;
}
