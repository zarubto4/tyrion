package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Update_group_procedure_state;
import utilities.enums.Enum_Update_type_of_update;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(description = "Json Model for ActualizationProcedure Filter",
        value = "ActualizationProcedure_Filter")
public class Swagger_ActualizationProcedure_Filter {


    @ApiModelProperty(required = true,  readOnly = true) public List<String> project_ids = new ArrayList<>();

    @ApiModelProperty(required = false, readOnly = true)  public List<Enum_Update_group_procedure_state> states  = new ArrayList<>();
    @ApiModelProperty(required = false, readOnly = true)  public List<Enum_Update_type_of_update> type_of_updates  = new ArrayList<>();

}
