package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for B_Program state",
        value = "B_Program_State")
public class Swagger_B_Program_State {

    @ApiModelProperty(required = true, readOnly = true)
    public boolean uploaded;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean online;


    @ApiModelProperty(required = false, readOnly = true, value = "Id of B_Program version whitch is running on Homer Server")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String version_id;



    // Instance Informace
    @ApiModelProperty(required = false, readOnly = true, value = "Id of Instance - independent object")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String instance_id;




    // Server informace
    @ApiModelProperty(required = false, readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String server_name;

    @ApiModelProperty(required = false, readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String server_id;

}


