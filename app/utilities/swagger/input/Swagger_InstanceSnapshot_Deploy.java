package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.Date;
import java.util.UUID;

@ApiModel(description = "Json Model for new Version of B_Program",
          value = "InstanceSnapshot_Deploy")
public class Swagger_InstanceSnapshot_Deploy {

    @Constraints.Required
    public UUID snapshot_id;

    @ApiModelProperty(required = false, value = "UNIX time in millis - Date: number of milliseconds elapsed since  Thursday, 1 January 1970", example = "1466163478925")
    public Long upload_time;

}