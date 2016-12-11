package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for Version of C_program",
        value = "C_Program_Version_For_Public_Decision")
public class Swagger_C_Program_Version_For_Public_Decision {

    @ApiModelProperty(required = true, readOnly = true)
    public Swagger_C_Program_Version c_program_version;


    @ApiModelProperty(required = false, readOnly = true )
    public String c_program_id;

    @ApiModelProperty(required = false, readOnly = true )
    public String c_program_name;

    @ApiModelProperty(required = false, readOnly = true )
    public String c_program_description;


}
