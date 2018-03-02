package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.input.Swagger_Library_Library_Version_pair;
import utilities.swagger.input.Swagger_Library_Record;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for Version of C_program",
        value = "C_Program_Version_Program")
public class Swagger_C_Program_Version {

    @ApiModelProperty(required = false, readOnly = true )
    public String main;

    @Valid
    @ApiModelProperty(required = false, readOnly = true )
    public List<Swagger_Library_Record>  files = new ArrayList<>();

    @ApiModelProperty(required = false, readOnly = true , value = "List imported libraries")
    public List<Swagger_Library_Library_Version_pair> imported_libraries = new ArrayList<>();

}
