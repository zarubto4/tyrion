package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@ApiModel(description = "",
        value = "Instance_New")
public class Swagger_Instance_New extends Swagger_NameAndDesc_ProjectIdRequired{

    @ApiModelProperty(required = true) @Constraints.Required public UUID b_program_id;
    @ApiModelProperty(required = true) @Constraints.Required public UUID main_server_id;
    @ApiModelProperty(required = false, value = "Optional and not supported value now") public UUID backup_server_id;


}
