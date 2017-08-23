package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for new C_Program",
          value = "C_Program_Edit")
public class Swagger_C_Program_Edit {

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 4 and 60 characters.")
    public String name;

    @ApiModelProperty(required = false, value = "program_description can be null or maximum length of 255 characters.")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String description;

    @ApiModelProperty(required = false, hidden = true)
    public boolean c_program_public_admin_create;        /// Určené Pro administraci tyriona na nastavení C_Programu do public sekce  není vidět pro Becki

}

