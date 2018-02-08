package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.UUID;

@ApiModel(description = "Json Model for ActualizationProcedure Make Procedure - HardwareType",
          value = "ActualizationProcedure_Make_HardwareType")
public class Swagger_ActualizationProcedure_Make_HardwareType {


    public Swagger_ActualizationProcedure_Make_HardwareType() {}

    @ApiModelProperty(required = true,  readOnly = true)  @Constraints.Required public UUID hardware_type_id;
    @ApiModelProperty(required = true,  readOnly = true)  public UUID c_program_version_id;
    @ApiModelProperty(required = true,  readOnly = true)  public UUID bootloader_id;

}
