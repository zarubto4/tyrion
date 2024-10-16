package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for new Working Copy of C_Program",
          value = "C_Program_Version_Refresh")
public class Swagger_C_Program_Version_Refresh {

    @ApiModelProperty(required = false, value = "The Library Version tag_name from HardwareType.supported_libraries")
    @Constraints.MaxLength(value = 60)
    public String library_compilation_version;

    @ApiModelProperty(required = true)
    public String main;

    @ApiModelProperty(required = false)
    @Valid public List<Swagger_Library_Record> files = new ArrayList<>();

    @ApiModelProperty(required = false, value = "Contains IDs of imported Library versions")
    @Valid public List<UUID>  imported_libraries = new ArrayList<>();

}



