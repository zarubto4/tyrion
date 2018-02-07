package utilities.swagger.output;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Swagger_UpdatePlan_brief_for_homer {

    @JsonProperty(value = "tracking_group_id") public String actualization_procedure_id;  // Procedure ID
    @JsonProperty(value = "tracking_id")       public String c_program_update_plan_id;    // Task ID


    // @JsonProperty public Enum_Update_type_of_update type_of_update;

    @JsonProperty
    public List<UUID> hardware_ids = new ArrayList<>();       // Lze updatovat víc zařízení stejným frimwarem - Podpora do budocna když nebude třeba sledovat každý update zvlášť
                                                                            // tracking_id pak nemá smysl a řídí se vše jen podle tracking_group_id.

    @JsonProperty
    public boolean progress_subscribe = false;                // Tyrion asks for information about the progress of the update

    @JsonProperty
    public Swagger_UpdatePlan_brief_for_homer_BinaryComponent binary;


}


