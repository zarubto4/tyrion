package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for new Version of C_Program",
          value = "C_Program_Version_Update")
public class Swagger_C_Program_Version_Update {

    // Nutný fiktivní contructor pro inicializaci vnitřních tříd
    public Swagger_C_Program_Version_Update() {}

    @ApiModelProperty(required = false, value = "Required only if user compile code not under C++ code version (where compilation can found hardware_type)")
    public UUID hardware_type_id;

    @ApiModelProperty(required = false, value = "The Library Version tag_name from HardwareType.supported_libraries")
    @Constraints.MaxLength(value = 60)
    @Constraints.Required
    public String library_compilation_version;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String main;

    @Valid
    @ApiModelProperty(required = false, readOnly = true )
    public List<Swagger_Library_Record> files = new ArrayList<>();

    @ApiModelProperty(required = false, readOnly = true , value = "List ID of libraries version ID")
    public List<String> imported_libraries = new ArrayList<>();


    @ApiModelProperty(required = false, value = "Only if user want update hardware with compilation immediately.")
    public boolean immediately_hardware_update;

    @ApiModelProperty(required = false)
    public List<UUID> hardware_ids = new ArrayList<>();


}
