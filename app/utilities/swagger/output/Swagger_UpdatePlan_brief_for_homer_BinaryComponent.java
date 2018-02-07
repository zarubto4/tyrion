package utilities.swagger.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import utilities.enums.Enum_Firmware_type;

import java.util.Date;

/**
 * Created by zaruba on 06.08.17.
 */
public class Swagger_UpdatePlan_brief_for_homer_BinaryComponent {


    @JsonProperty
    public Enum_Firmware_type firmware_type;
    @JsonProperty
    public String build_id;                           // Model_CCompilation.firmware_build_id or Model_BootLoader.version_identifier
    @JsonProperty
    public String program_name;                       // C_Program.name or Model_Bootloader.name
    @JsonProperty
    public String program_version_name;               // C_Program.versions[1].name  for Model_BootLoader.version_identifier
    @JsonProperty
    public String download_id;                        // For firmware or Backup is Model_VersionObject.id for bootloader is Model_BootLoader.version_identifier


    @JsonProperty
    public Date time_stamp;

    // Only for Enum_Firmware_type .FIRMWARE or .BACKUP
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public String compilation_lib_version;            // Model_TypeOfBoard.supported_libraries.tag_name    (try to find Swagger_CompilationLibrary)
}
