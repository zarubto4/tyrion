package utilities.swagger.input;

import io.ebean.annotation.EnumValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for Filtering Boards",
          value = "Board_filter")
public class Swagger_Board_Filter extends _Swagger_filter_parameter{

    @ApiModelProperty(value = "List of hardware_type.id", required = false)
    public List<UUID> hardware_type_ids;

    @ApiModelProperty(value = "Boolean - required in String! \"true\" or \"false \"", required = false)
    public String active;

    @ApiModelProperty(value = "List of project.id", required = false)
    public List<UUID> projects;

    @ApiModelProperty(value = "List of producer.id", required = false)
    public List<UUID> producers;

    @ApiModelProperty(value = "List of processor.id", required = false)
    public List<UUID> processors;

    @ApiModelProperty(value = "List of instance_snapshot.id", required = false)
    public UUID instance_snapshot;

    @ApiModelProperty(value = "List of hardware_group.id", required = false)
    public List<UUID>  hardware_groups_id;

    @ApiModelProperty(value = "Unix in millis - created - start_time", required = false)
    public Date start_time;

    @ApiModelProperty(value = "Unix in millis - created - end_time", required = false)
    public Date end_time;


    /** FIND BY SOME VALUE  ------------------------------------------------------------------------------------------ **/

    @ApiModelProperty(value = "Hardware Alias Name - substring supported", required = false)
    public String name;

    @ApiModelProperty(value = "Hardware Alias Name - substring supported", required = false)
    public String description;

    @ApiModelProperty(value = "Hardware Full ID", required = false)
    public String full_id;

    @ApiModelProperty(value = "Hardware ID or part of ID (not necessary to send UUID)", required = false)
    public UUID id;


    /** ORDER BY ------------------------------------------------------------------------------------------------------ **/

    @ApiModelProperty(value = "Order By Enum value", required = false)
    public Order_by order_by;

    @ApiModelProperty(value = "Order", required = false)
    public Order_Schema order_schema;


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

