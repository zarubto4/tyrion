package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for Filtering Boards",
          value = "Board_filter")
public class Swagger_Board_Filter {

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

    @ApiModelProperty(value = "Unix in millis - created - start_time", required = false)
    public Date start_time;

    @ApiModelProperty(value = "Unix in millis - created - end_time", required = false)
    public Date end_time;


}
