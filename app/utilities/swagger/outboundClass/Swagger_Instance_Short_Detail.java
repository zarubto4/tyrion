package utilities.swagger.outboundClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Instance Light (only few properties)",
        value = "Instance_Short_Detail")
public class Swagger_Instance_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true) public String id;
    @ApiModelProperty(required = true, readOnly = true) public String b_program_id;
    @ApiModelProperty(required = true, readOnly = true) public String b_program_name;
    @ApiModelProperty(required = true, readOnly = true) public String b_program_description;


    @ApiModelProperty(required = true, readOnly = true) public String server_name;
    @ApiModelProperty(required = true, readOnly = true) public String server_id;
    @ApiModelProperty(required = true, readOnly = true) public boolean server_is_online;
    @ApiModelProperty(required = true, readOnly = true) public boolean instance_is_online;
}
