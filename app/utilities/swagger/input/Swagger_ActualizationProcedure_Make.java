package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for ActualizationProcedure Make Procedure",
          value = "ActualizationProcedure_Make")
public class Swagger_ActualizationProcedure_Make {


    @Constraints.Required @ApiModelProperty(required = true,  readOnly = true) public UUID project_id;
    @Constraints.Required @ApiModelProperty(required = true,  readOnly = true) public String firmware_type;       // Enum_Firmware_type

    @Constraints.Required @ApiModelProperty(required = true, readOnly = true)  public String hardware_group_id;

    @Valid
    @ApiModelProperty(required = true,  readOnly = true) public List<Swagger_ActualizationProcedure_Make_HardwareType> hardware_type_settings = new ArrayList<>();

    @ApiModelProperty(required = false,  readOnly = true, value = "If  value is null - its a command for immediately update ") public Long time;
    @ApiModelProperty(required = false,  readOnly = true, value = "If  value is null - its a command for immediately update - Default Value 0") public Integer timeOffset = 0;

}
