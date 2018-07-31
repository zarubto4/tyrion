package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import javax.validation.Constraint;
import java.util.UUID;


@ApiModel(description = "",
        value = "Instance_Mesh")
public class Swagger_Instance_MESH extends _Swagger_Abstract_Default {


    @ApiModelProperty(required = false)
    public String description;


    @Constraints.MaxLength(value = 16)
    @Constraints.MinLength(value = 4)
    @ApiModelProperty(required = true) @Constraints.Required public String short_prefix;

}
