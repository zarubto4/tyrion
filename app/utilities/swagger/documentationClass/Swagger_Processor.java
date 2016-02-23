package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModelProperty;

public class Swagger_Processor {

    @ApiModelProperty(required = true) public String  description;
    @ApiModelProperty(required = true) public String  processor_code;
    @ApiModelProperty(required = true) public String  processor_name;
    @ApiModelProperty(required = true) public Integer speed;
}
