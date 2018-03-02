package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.HardwareUpdateState;
import utilities.enums.Enum_Update_group_procedure_state;
import utilities.enums.UpdateType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for ActualizationProcedureTask Filter",
        value = "ActualizationProcedureTask_Filter")
public class Swagger_ActualizationProcedureTask_Filter {


    @ApiModelProperty(required = false,  readOnly = true) public List<UUID> hardware_ids = new ArrayList<>();
    @ApiModelProperty(required = false,  readOnly = true) public List<UUID> instance_ids = new ArrayList<>();
    @ApiModelProperty(required = false,  readOnly = true) public List<UUID> actualization_procedure_ids = new ArrayList<>();

    @ApiModelProperty(required = false, readOnly = true)  public List<HardwareUpdateState> update_states  = new ArrayList<>();
    @ApiModelProperty(required = false, readOnly = true)  public List<Enum_Update_group_procedure_state> update_status  = new ArrayList<>();
    @ApiModelProperty(required = false, readOnly = true)  public List<UpdateType> type_of_updates  = new ArrayList<>();


}
