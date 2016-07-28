package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
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


// ---------------------------------------------------------------------------------------------------------------------

    @Constraints.Required
    @ApiModelProperty(value = "This board must be connectible_to_internet = true! (Its out Yoda)", required = true)
    @Valid
    public Main_Board main_board;

    // TODO http://youtrack.byzance.cz/youtrack/issue/TYRION-263 Main_Board by se mělo transformovat na pole a integrovat do sebe zařízení které jsou na něj vázané (to jest obsahovat Connected_Board)

    public static class Main_Board {
        public Main_Board(){}

        @Constraints.Required @ApiModelProperty(required = true, value = "TypeofBoard of this Board must be connectible_to_internet = true ") public String board_id;
        @Constraints.Required @ApiModelProperty(required = true) public String c_program_version_id;
    }


// ---------------------------------------------------------------------------------------------------------------------

    @Valid
    @ApiModelProperty(value = "Connected boards (padavans)", required = false)
    public List<Connected_Board> boards = new ArrayList<>();

    public class Connected_Board {
        public Connected_Board(){}
        @Constraints.Required @ApiModelProperty(required = true) public String board_id;
        @Constraints.Required @ApiModelProperty(required = true) public String c_program_version_id;
    }

}
