package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.UUID;


@ApiModel(description = "",
        value = "Instance_Token")
public class Swagger_Instance_Token extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = true) @Constraints.Required public String description;

}
