package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for updating blocko code.js in Instance",
        value = "Instance_Temporary")
public class Swagger_Instance_Temporary {

    @Constraints.Required
    @Constraints.MinLength(value = 5)
    public String instance_name;

    @Constraints.Required
    public String unique_identificator;
}
