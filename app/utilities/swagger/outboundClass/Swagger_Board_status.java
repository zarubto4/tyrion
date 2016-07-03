package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for Status and all information about embedded Hardware",
        value = "Board_status")
public class Swagger_Board_status {

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String where;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String b_program_id;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String b_program_version_id;

    @ApiModelProperty(value = "It in Object only if user upload own binary firmware to hardware and server used file name for naming of this value" +
            "If user used classic build with Byzance - its not visible in Json!", readOnly = true, required = false)
    @JsonInclude(JsonInclude.Include.NON_NULL) public String actual_program;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String actual_c_program_id;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String actual_c_program_version_id;

    public String required_c_program_id;
    public String required_c_program_version_id;

}
