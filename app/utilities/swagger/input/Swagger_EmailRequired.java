package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model with email",
        value = "EmailRequired")
public class Swagger_EmailRequired {

    @Constraints.Email
    @Constraints.Required
    public String email;
}
