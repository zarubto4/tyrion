package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.UUID;


@ApiModel(description = "Json Model for validation of Token for HTTP requests",
        value = "Blocko_Token_validation_result")
public class Swagger_Blocko_Token_validation_result extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = true, readOnly = true)
    public UUID token;

    @ApiModelProperty(required = true, readOnly = true)
    public Long available_requests;

}
