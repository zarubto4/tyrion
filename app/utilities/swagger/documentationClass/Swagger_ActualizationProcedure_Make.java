package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Update_group_procedure_state;
import utilities.enums.Enum_Update_type_of_update;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for ActualizationProcedure Make Procedure",
          value = "ActualizationProcedure_Make")
public class Swagger_ActualizationProcedure_Make {


    @ApiModelProperty(required = true,  readOnly = true) public String project_id;
    @ApiModelProperty(required = true,  readOnly = true) public String firmware_type;       // Enum_Firmware_type

    @ApiModelProperty(required = true, readOnly = true)  public List<String> hardware_group_ids  = new ArrayList<>();

    @ApiModelProperty(required = true,  readOnly = true) public String c_program_version_id;
    @ApiModelProperty(required = false,  readOnly = true, value = "If  value is null - its a command for immediately update ") public Long time;

}
