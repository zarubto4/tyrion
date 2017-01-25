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
    public boolean instance_online;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean server_online;

    @ApiModelProperty(required = false, readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String version_id;

    @ApiModelProperty(required = false, readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String version_name;


    // Instance Informace
    @ApiModelProperty(required = false, readOnly = true, value = "Id of Instance -its independent object!")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String instance_id;


    // Instance Informace
    @ApiModelProperty(required = false, readOnly = true, value = "WebSocket URL without Personal AUTH_ID for remove control and webView streaming")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String instance_remote_url;


    // Server informace
    @ApiModelProperty(required = false, readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String server_name;

    @ApiModelProperty(required = false, readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String server_id;


    @ApiModelProperty(required = false, readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String unique_identificator;

}


