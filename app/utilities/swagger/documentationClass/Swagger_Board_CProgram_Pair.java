package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(value = "Connected_Board_IN")
public class Swagger_Board_CProgram_Pair {

    public Swagger_Board_CProgram_Pair(){}

    @Constraints.Required @ApiModelProperty(required = true)
    public String board_id;

    @Constraints.Required @ApiModelProperty(required = true)
    public String c_program_version_id;

}
