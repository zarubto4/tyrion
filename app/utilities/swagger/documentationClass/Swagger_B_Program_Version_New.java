package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.List;

@ApiModel(description = "Json Model for new Version of B_Program",
         value = "B_Program_Version_New")
public class Swagger_B_Program_Version_New {

    @Constraints.Required
    @Constraints.MinLength(value = 6, message = "The name must have at least 6 characters")
    @ApiModelProperty(required = true)
    public String version_name;


    @ApiModelProperty(required = false)
    public String version_description;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String program;

    @Valid
    public List<Connected_Board> boards;

    public static class Connected_Board {
        public Connected_Board(){}

        public String board_id;
        public String c_program_version_id;
    }

}
