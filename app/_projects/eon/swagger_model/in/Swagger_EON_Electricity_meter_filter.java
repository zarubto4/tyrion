package _projects.eon.swagger_model.in;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.input._Swagger_filter_parameter;

import java.util.List;

@ApiModel(description = "Json Model for getting data for EON project",
        value = "EON_Electricity_meter_filter")
public class Swagger_EON_Electricity_meter_filter extends _Swagger_filter_parameter {

    @ApiModelProperty(value = "Account id of owner", required = true)
    public String owner_id;

}
