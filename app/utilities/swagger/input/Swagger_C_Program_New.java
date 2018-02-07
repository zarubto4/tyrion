package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for new C_Program",
          value = "C_Program_New")
public class Swagger_C_Program_New extends Swagger_NameAndDesc_ProjectIdOptional {

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String type_of_board_id;

    @ApiModelProperty(required = false, hidden = true)
    public boolean c_program_public_admin_create;        /// Určené Pro administraci tyriona na nastavení C_Programu do public sekce  není vidět pro Becki

}

