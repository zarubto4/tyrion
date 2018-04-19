package utilities.swagger.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Model of Project Statistic - Its Asynchronous Object created after all values in project are cached.",
        value = "ProjectStats")
public class Swagger_ProjectStats{
    @ApiModelProperty(required = true) @JsonProperty() public int hardware;
    @ApiModelProperty(required = true) @JsonProperty() public int hardware_online;
    @ApiModelProperty(required = true) @JsonProperty() public int b_programs;
    @ApiModelProperty(required = true) @JsonProperty() public int c_programs;
    @ApiModelProperty(required = true) @JsonProperty() public int libraries;
    @ApiModelProperty(required = true) @JsonProperty() public int grid_projects;
    @ApiModelProperty(required = true) @JsonProperty() public int hardware_groups;
    @ApiModelProperty(required = true) @JsonProperty() public int widgets;
    @ApiModelProperty(required = true) @JsonProperty() public int blocks;
    @ApiModelProperty(required = true) @JsonProperty() public int instances;
}
