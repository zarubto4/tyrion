package utilities.swagger.input;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.enums.BoardCommand;

import java.util.UUID;

@ApiModel(description = "Json Model for developers commands to Hardware. For example restart, redirect etc. Please, use that, only if you know, what you are doing.",
          value = "Board_Command")
public class Swagger_Board_Command {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Length must be between 0 and 255 characters.")
    public UUID hardware_id;

    @ApiModelProperty(required = true, value = "Command")
    public BoardCommand command;

}
