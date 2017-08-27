package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;

@ApiModel(description = "Json Model for Filtering Boards",
          value = "Board_filter")
public class Swagger_Board_Filter {

    @ApiModelProperty(value = "List of typeOfBoard.id", required = false)
    public List<String> type_of_board_ids;

    @ApiModelProperty(value = "Boolean - required in String! \"true\" or \"false \"", required = false)
    public String active;

    @ApiModelProperty(value = "List of project.id", required = false)
    public List<String> projects;

    @ApiModelProperty(value = "List of producer.id", required = false)
    public List<String> producers;

    @ApiModelProperty(value = "List of processor.id", required = false)
    public List<String> processors;

    @ApiModelProperty(value = "Unix in millis - date_of_create - start_time", required = false)
    public Date start_time;

    @ApiModelProperty(value = "Unix in millis - date_of_create - end_time", required = false)
    public Date end_time;


}
