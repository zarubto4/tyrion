package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


@ApiModel(description = "Json Model for new Version of C_Program",
          value = "C_Program_Version_Update")
public class Swagger_C_Program_Version_Update {

    // Nutný fiktivní contructor pro inicializaci vnitřních tříd
    public Swagger_C_Program_Version_Update() {}

    @ApiModelProperty(required = false, value = "Required only if user compile code not under C++ code version (where compilation can found type_of_board)")
    public String type_of_board_id;

    @ApiModelProperty(required = false, value = "The Library Version tag_name from TypeOfBoard.supported_libraries")
    @Constraints.MaxLength(value = 60)
    public String library_compilation_version;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String main;

    @Valid
    @ApiModelProperty(required = false, readOnly = true )
    public List<Swagger_Library_Record> files = new ArrayList<>();

    @ApiModelProperty(required = false, readOnly = true , value = "List ID of libraries version ID")
    public List<String> imported_libraries = new ArrayList<>();

}
