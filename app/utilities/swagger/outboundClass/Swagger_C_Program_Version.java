package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_VersionObject;
import models.Model_CCompilation;
import utilities.enums.Enum_Compile_status;
import utilities.swagger.documentationClass.Swagger_C_Program_Version_New;
import utilities.swagger.documentationClass.Swagger_Library_Library_Version_pair;
import utilities.swagger.documentationClass.Swagger_Library_Record;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for Version of C_program",
        value = "C_Program_Version")
public class Swagger_C_Program_Version {


    @ApiModelProperty(required = true, readOnly = true)
    public Model_VersionObject version_object;


    @ApiModelProperty(required = true, readOnly = true)
    public Enum_Compile_status status;

    @ApiModelProperty(required = false, readOnly = true )
    public String main;

    @Valid
    @ApiModelProperty(required = false, readOnly = true )
    public List<Swagger_Library_Record>  files = new ArrayList<>();

    @ApiModelProperty(required = false, readOnly = true , value = "List imported libraries")
    public List<Swagger_Library_Library_Version_pair> imported_libraries = new ArrayList<>();


    @ApiModelProperty(required = true, readOnly = true, value = "Value can be empty, Server cannot guarantee that. External documentation: " + Model_CCompilation.virtual_input_output_docu)
    public String virtual_input_output;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean remove_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean edit_permission;




}
