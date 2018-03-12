package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.UUID;


@ApiModel(description = "Json Model for validation of Token for HTTP requests",
        value = "Blocko_Token_validation_request")
public class Swagger_Blocko_Token_validation_request {

    @Constraints.Required
    @ApiModelProperty(required = true, readOnly = true)
    public UUID token;

    @Constraints.Required
    @ApiModelProperty(required = true, readOnly = true, example = "PERSON_TOKEN, INSTANCE_TOKEN")
    public String type_of_token;

}
