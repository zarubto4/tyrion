package _projects.cez.swagger_model.in;

import _projects.cez.enums.Enum_Sensor_type;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(description = "Json Model for getting data for CEZ project",
        value = "CEZ_data_request ")
public class Swagger_CEZ_data_request {

    @ApiModelProperty(value = "begin of time range, in unix time", dataType = "number", required = true, example = "1549223285")
    public Long start_date = 1549223285L;

    @ApiModelProperty(value = "end of time range, in unix time", required = true, dataType = "number", example = "1704067200")
    public Long end_date = 1704067200L;

    @ApiModelProperty(value = "code of value", required = true, example = "temperature")
    public Enum_Sensor_type data_typ = Enum_Sensor_type.temperature;

    @ApiModelProperty(value = "interval in seconds",required = true)
    public Integer interval = 900;

    @ApiModelProperty(value = "list of hardwares", required = true)
    public List<String> hardwares;
}
