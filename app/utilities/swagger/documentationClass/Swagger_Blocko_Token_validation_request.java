package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(description = "Json Model for validation of Token for HTTP requests",
        value = "Blocko_Token_validation_request")
public class Swagger_Blocko_Token_validation_request {

    @ApiModelProperty(required = true, readOnly = true)
    public String token;

    @ApiModelProperty(required = true, readOnly = true, example = "PERSON_TOKEN, INSTANCE_TOKEN")
    public String type_of_token;

}
