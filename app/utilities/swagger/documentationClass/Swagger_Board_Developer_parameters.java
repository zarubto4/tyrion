package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for update personal description of Board",
          value = "Board_Personal_Description")
public class Swagger_Board_Developer_parameters {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Parameter names fo developer settings")
    public String parameter_type;


    @ApiModelProperty(required = false, value = "only for boolean parameters")
    public boolean boolean_value;

    @ApiModelProperty(required = false, value = "only for text label parameters")
    public String string_value;

    @ApiModelProperty(required = false, value = "only for text label parameters")
    public Integer integer_value;
}
