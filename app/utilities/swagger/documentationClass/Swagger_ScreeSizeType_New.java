package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModelProperty;

public class Swagger_ScreeSizeType_New {

    @ApiModelProperty(required = true) public String name;
    @ApiModelProperty(required = true) public Integer height;
    @ApiModelProperty(required = true) public Integer width;
    @ApiModelProperty(required = true) public boolean height_lock;
    @ApiModelProperty(required = true) public boolean width_lock;
    @ApiModelProperty(required = true) public boolean touch_screen;
    @ApiModelProperty(value = "When you want create private screen", required = false) public String project_id;

}
