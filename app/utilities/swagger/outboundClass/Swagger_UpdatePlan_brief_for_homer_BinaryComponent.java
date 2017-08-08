package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonProperty;
import utilities.enums.Enum_Firmware_type;

/**
 * Created by zaruba on 06.08.17.
 */
public class Swagger_UpdatePlan_brief_for_homer_BinaryComponent {


    @JsonProperty public Enum_Firmware_type firmware_type;
    @JsonProperty public String build_id;                           // Model_CCompilation.firmware_build_id or Model_BootLoader.version_identificator
    @JsonProperty public String program_name;
    @JsonProperty public String program_version_name;
    @JsonProperty public String download_id;                        // For firmware or Backup is Model_VersionObject.id for bootloader is Model_BootLoader.version_identificator

}
