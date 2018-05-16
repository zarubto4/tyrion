package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for create or update Article",
        value = "Article_CreateUpdate")
public class Swagger_Article_CreateUpdate extends Swagger_NameAndDescription {

    @Constraints.Required
    @Constraints.MinLength(value = 2, message = "The name must have at least 2 characters")
    public String mark_down_text;
}
