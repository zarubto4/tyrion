package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonProperty;
import utilities.enums.Enum_Firmware_type;
import utilities.enums.Enum_Update_type_of_update;

/**
 * Created by zaruba on 30.06.17.
 */
public class Swagger_UpdatePlan_brief_for_homer {

    @JsonProperty public String actualization_procedure_id;  // Procedure ID
    @JsonProperty public String c_program_update_plan_id;    // Task ID

    @JsonProperty public String device_id;

    @JsonProperty public Enum_Update_type_of_update type_of_update;

    @JsonProperty public Enum_Firmware_type firmware_type;
    @JsonProperty public String build_id;
    @JsonProperty public String blob_link;

    @JsonProperty public String program_name;

}
