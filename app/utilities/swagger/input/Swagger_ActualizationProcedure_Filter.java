package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.enums.Enum_Update_group_procedure_state;
import utilities.enums.UpdateType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for ActualizationProcedure Filter",
        value = "ActualizationProcedure_Filter")
public class Swagger_ActualizationProcedure_Filter extends _Swagger_filter_parameter{


    @ApiModelProperty(required = true,  readOnly = true)  @Constraints.Required public List<UUID> project_ids = new ArrayList<>();

    @ApiModelProperty(required = false, readOnly = true)  public List<Enum_Update_group_procedure_state> update_states  = new ArrayList<>();
    @ApiModelProperty(required = false, readOnly = true)  public List<UpdateType> type_of_updates  = new ArrayList<>();


}
