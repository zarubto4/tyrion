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


    @Valid
    @ApiModelProperty(value = "Connected groups of hardware - User can create Blocko program without hardware.", required = false)
    public List<Hardware_group> hardware_group  = new ArrayList<>();

// ---------------------------------------------------------------------------------------------------------------------

    public static class Hardware_group {

        public Hardware_group(){}

        @ApiModelProperty(value = "This board must be connectible_to_internet = true! User can create new B_Program version without Main Board, but its not possible to upload that to cloud like new Instance" +
                                  "",
                          required = true)
        @Valid
        public Connected_Board main_board;

        @Valid
        @ApiModelProperty(value = "Connected boards (padavans)", required = false)
        public List<Connected_Board> boards = new ArrayList<>();


    }

    public static class Connected_Board {
        public Connected_Board(){}
        @Constraints.Required @ApiModelProperty(required = true) public String board_id;
        @Constraints.Required @ApiModelProperty(required = true) public String c_program_version_id;
    }

}
