package _projects.eon.swagger_model.in;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for getting data for EON project",
        value = "EON_Electricity_meter_create_edit")
public class Swagger_EON_Electricity_meter_create_edit {

    public String identification_id;
    public String name;
    public String description;

    public String owner_id;
    public String gateway_id;

    public Double latitude;
    public Double longitude;

}
