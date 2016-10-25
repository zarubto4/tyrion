package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

@ApiModel(description = "Json Model for new Version of B_Program",
        value = "B_Program_Version_New")
public class Swagger_B_Program_Uploud_Instance {

    @ApiModelProperty(required = false, value = "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970", example = "1466163478925")
    public Date upload_time;

}