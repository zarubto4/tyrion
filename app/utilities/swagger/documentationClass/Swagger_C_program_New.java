package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for new C_Program",
          value = "C_Program_New")
public class Swagger_C_program_New {

    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String project_id;

    @Constraints.Required
    @Constraints.MinLength(value = 8, message = "The name must have at least 8 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String name;


    @ApiModelProperty(required = false, value = "program_description can be null or maximum length of 255 characters.")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String description;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String type_of_board_id;


    @ApiModelProperty(required = false, hidden = true)
    public boolean c_program_type_of_board_default;         /// Určené Pro administraci tyriona na nastavení C_Programu defaultního pro typ desky - není vidět pro Becki

    @ApiModelProperty(required = false, hidden = true)
    public boolean c_program_public_admin_create;        /// Určené Pro administraci tyriona na nastavení C_Programu do public sekce  není vidět pro Becki

}

