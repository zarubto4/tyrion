package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for ActualizationProcedure Make Procedure - TypeOfBoard",
          value = "ActualizationProcedure_Make_TypeOfBoard")
public class Swagger_ActualizationProcedure_Make_TypeOfBoard {


    public Swagger_ActualizationProcedure_Make_TypeOfBoard(){}

    @ApiModelProperty(required = true,  readOnly = true)  @Constraints.Required public String type_of_board_id;
    @ApiModelProperty(required = true,  readOnly = true)  public String c_program_version_id;
    @ApiModelProperty(required = true,  readOnly = true)  public String bootloader_id;

}
