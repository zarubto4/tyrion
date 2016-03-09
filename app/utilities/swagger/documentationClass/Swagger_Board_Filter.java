package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(description = "Json Model for Filtering Boards",
          value = "Board_Filter")
public class Swagger_Board_Filter {

    @ApiModelProperty(value = "List of typeOfBoard.id", required = false)
    public List<String> typeOfBoards;


    @ApiModelProperty(value = "Boolean - required in String! \"true\" or \"false \"", required = false)
    public String active;

    @ApiModelProperty(value = "List of project.id", required = false)
    public List<String> projects;

    @ApiModelProperty(value = "List of producer.id", required = false)
    public List<String> producers;

    @ApiModelProperty(value = "List of processor.id", required = false)
    public List<String> processors;


}
