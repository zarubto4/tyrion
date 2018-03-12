package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.UUID;


@ApiModel(description = "Json Model for validation of Token for HTTP requests",
        value = "Blocko_Token_validation_result")
public class Swagger_Blocko_Token_validation_result {

    @ApiModelProperty(required = true, readOnly = true)
    public UUID token;

    @ApiModelProperty(required = true, readOnly = true)
    public Long available_requests;

}
