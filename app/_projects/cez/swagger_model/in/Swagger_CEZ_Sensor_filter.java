package _projects.cez.swagger_model.in;

import io.ebean.annotation.EnumValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.input.Swagger_Board_Filter;
import utilities.swagger.input._Swagger_filter_parameter;

import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for getting data for EON project",
        value = "EON_Electricity_meter_filter")
public class Swagger_CEZ_Sensor_filter extends _Swagger_filter_parameter {

    @ApiModelProperty(value = "Hardware Alias Name - substring supported", required = false)
    public String name;

    @ApiModelProperty(value = "Hardware Alias Name - substring supported", required = false)
    public String description;

    @ApiModelProperty(value = "Hardware Full ID", required = false)
    public String full_id;

    @ApiModelProperty(value = "Hardware ID or part of ID (not necessary to send UUID)", required = false)
    public UUID id;

    @ApiModelProperty(value = "List of hardware_group.id", required = false)
    public List<UUID> hardware_groups_id;

    /** ORDER BY ------------------------------------------------------------------------------------------------------ **/

    @ApiModelProperty(value = "Order By Enum value", required = false)
    public Swagger_Board_Filter.Order_by order_by;

    @ApiModelProperty(value = "Order", required = false)
    public Swagger_Board_Filter.Order_Schema order_schema;


    public enum Order_by {
        @EnumValue("NAME")      NAME,
        @EnumValue("FULL_ID")   FULL_ID,
        @EnumValue("ID")        ID,
        @EnumValue("DATE_OF_REGISTER")        DATE_OF_REGISTER
    }

    public enum Order_Schema {
        @EnumValue("ASC")    ASC,
        @EnumValue("DESC")   DESC,
    }

}
