package _projects.eon.swagger_model.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.Date;
import java.util.List;

@ApiModel(description = "Json Model for EON",
        value = "EON_data_value")
public class Swagger_EON_data_values extends _Swagger_Abstract_Default {
    @ApiModelProperty(required = true, readOnly = true )
    public String _id;
    @ApiModelProperty(required = true, readOnly = true )
    public List<Swagger_EON_measurment> data;
}
