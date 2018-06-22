package utilities.swagger.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import utilities.enums.FirmwareType;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.Date;
import java.util.UUID;

@ApiModel(description = "Private",
        value = "UpdatePlan_brief_for_homer_BinaryComponent")
public class Swagger_UpdatePlan_brief_for_homer_BinaryComponent extends _Swagger_Abstract_Default {


    @JsonProperty
    public FirmwareType firmware_type;
    @JsonProperty
    public String build_id;                           // Model_CCompilation.firmware_build_id or Model_BootLoader.version_identifier
    @JsonProperty
    public String program_name;                       // C_Program.name or Model_Bootloader.name
    @JsonProperty
    public String program_version_name;               // C_Program.versions[1].name  for Model_BootLoader.version_identifier
    @JsonProperty
    public UUID download_id;                        // For firmware or Backup is Model Version for bootloader is Model_BootLoader.version_identifier

    @JsonProperty
    public Date time_stamp;

    // Only for Enum_Firmware_type .FIRMWARE or .BACKUP
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public String compilation_lib_version;            // Model_HardwareType.supported_libraries.tag_name    (try to find Swagger_CompilationLibrary)
}
