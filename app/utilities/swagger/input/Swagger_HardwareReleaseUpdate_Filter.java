package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.enums.Enum_Update_group_procedure_state;
import utilities.enums.HardwareUpdateState;
import utilities.enums.UpdateType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for ActualizationProcedure Filter",
        value = "HardwareReleaseUpdate_Filter")
public class Swagger_HardwareReleaseUpdate_Filter extends _Swagger_filter_parameter{


    @ApiModelProperty(required = true,  readOnly = true)  @Constraints.Required public UUID project_id;

    @ApiModelProperty(required = false, readOnly = true)  public List<Enum_Update_group_procedure_state> states  = new ArrayList<>();


}
