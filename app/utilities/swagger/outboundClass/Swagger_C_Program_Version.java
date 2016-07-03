package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for Version of C_program",
        value = "C_Program_Version")
public class Swagger_C_Program_Version {


    @ApiModelProperty(required = true, readOnly = true)
    public Version_Object version_object;


    @ApiModelProperty(required = true, readOnly = true)
    public boolean successfully_compiled;


    @ApiModelProperty(required = true, readOnly = true)
    public boolean compilation_in_progress;


    @ApiModelProperty(required = true, readOnly = true)
    public boolean compilable;

    @ApiModelProperty(required = true, readOnly = true, value = "Code in Json - same structure like when user saved that!")
    public String version_code;


    @ApiModelProperty(required = true, readOnly = true)
    public String virtual_input_output;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean compilation_restored;

   @ApiModelProperty(required = true, readOnly = true)
    public List<String> runing_on_board = new ArrayList<>();

}
