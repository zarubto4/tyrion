package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Version;
import models.Model_Compilation;
import utilities.enums.CompilationStatus;
import utilities.swagger.input.Swagger_C_Program_Version_New;
import utilities.swagger.input.Swagger_Library_Library_Version_pair;
import utilities.swagger.input.Swagger_Library_Record;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for Version of C_program",
        value = "C_Program_Version")
public class Swagger_C_Program_Version {


    @ApiModelProperty(required = true, readOnly = true)
    public Model_Version version;

    @ApiModelProperty(required = true, readOnly = true)
    public CompilationStatus status;

    @ApiModelProperty(required = false, readOnly = true )
    public String main;

    @Valid
    @ApiModelProperty(required = false, readOnly = true )
    public List<Swagger_Library_Record>  files = new ArrayList<>();

    @ApiModelProperty(required = false, readOnly = true , value = "List imported libraries")
    public List<Swagger_Library_Library_Version_pair> imported_libraries = new ArrayList<>();


    @ApiModelProperty(required = true, readOnly = true, value = "Value can be empty, Server cannot guarantee that. External documentation: " + Model_Compilation.virtual_input_output_docu)
    public String virtual_input_output;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean remove_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean edit_permission;




}
