package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Update_group_procedure_state;
import utilities.enums.Enum_Update_type_of_update;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for ActualizationProcedureTask Filter",
        value = "Swagger_ActualizationProcedureTask_Filter")
public class Swagger_ActualizationProcedureTask_Filter {


    @ApiModelProperty(required = false,  readOnly = true) public List<String> board_ids = new ArrayList<>();
    @ApiModelProperty(required = false,  readOnly = true) public List<String> instance_ids = new ArrayList<>();
    @ApiModelProperty(required = false,  readOnly = true) public List<String> actualization_procedure_ids = new ArrayList<>();

    @ApiModelProperty(required = false, readOnly = true)  public List<Enum_Update_group_procedure_state> update_states  = new ArrayList<>();
    @ApiModelProperty(required = false, readOnly = true)  public List<Enum_Update_type_of_update> type_of_updates  = new ArrayList<>();


}
