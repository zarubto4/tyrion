package utilities.swagger.outboundClass;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;
import models.project.c_program.C_Compilation;

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

    @ApiModelProperty(required = false, readOnly = true )
    public JsonNode main;

   @ApiModelProperty(required = false, readOnly = true )
    public JsonNode user_files;

    @ApiModelProperty(required = false, readOnly = true )
    public JsonNode external_libraries;


    @ApiModelProperty(required = true, readOnly = true, value = "Value can be empty, Server cannot guarantee that. External documentation: " + C_Compilation.virtual_input_output_docu)
    public String virtual_input_output;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean compilation_restored;

   @ApiModelProperty(required = true, readOnly = true)
    public List<String> runing_on_board = new ArrayList<>();

}
