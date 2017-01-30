package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Model_VersionObject;
import models.project.c_program.Model_CCompilation;
import utilities.enums.Compile_Status;
import utilities.swagger.documentationClass.Swagger_C_Program_Version_New;

import java.util.List;

@ApiModel(description = "Json Model for Version of C_program",
        value = "C_Program_Version")
public class Swagger_C_Program_Version {


    @ApiModelProperty(required = true, readOnly = true)
    public Model_VersionObject version_object;


    @ApiModelProperty(required = true, readOnly = true)
    public Compile_Status status;

    @ApiModelProperty(required = false, readOnly = true )
    public String main;

   @ApiModelProperty(required = false, readOnly = true )
    public List<Swagger_C_Program_Version_New.User_File>  user_files;

    @ApiModelProperty(required = false, readOnly = true )
    public List<Swagger_C_Program_Version_New.Library_File> library_files;


    @ApiModelProperty(required = true, readOnly = true, value = "Value can be empty, Server cannot guarantee that. External documentation: " + Model_CCompilation.virtual_input_output_docu)
    public String virtual_input_output;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean remove_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean edit_permission;

}
