package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for crating new version and also content of GSM Model",
        value = "GSM_New")
public class Swagger_GSM_New extends Swagger_NameAndDescription{

    @Constraints.Required
    public String MSINumber;
}
