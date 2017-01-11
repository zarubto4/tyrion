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
    @Constraints.MinLength(value = 4, message = "The name must have at least 4 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    @ApiModelProperty(required = true, value = "Length must be between 8 and 60 characters.")
    public String version_name;


    @ApiModelProperty(required = false, value = "version_description can be null or maximum length of 255 characters.")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String version_description;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String program;

    @Valid
    @ApiModelProperty(value = "Connected groups of all hardware - User can create Blocko program without hardware.", required = false)
    public List<Hardware_group> hardware_group  = new ArrayList<>();


    @Valid
    @ApiModelProperty(value = "Connected groups of m_programs.versions under imported m_programs", required = false)
    public List<M_Project_SnapShot> m_project_snapshots  = new ArrayList<>();

// ---------------------------------------------------------------------------------------------------------------------

    @ApiModel(value = "Hardware_group_IN")
    public static class Hardware_group {

        public Hardware_group(){}

        @Valid @ApiModelProperty(value = "This board must be connectible_to_internet = true! User can create new B_Program version without Main Board, but its not possible to upload that to cloud like new Instance", required = true)
        public Connected_Board main_board_pair;

        @Valid @ApiModelProperty(required = false)
        public List<Connected_Board> device_board_pairs = new ArrayList<>();

    }

    @ApiModel(value = "Connected_Board_IN")
    public static class Connected_Board {

        public Connected_Board(){}

        @Constraints.Required @ApiModelProperty(required = true)
        public String board_id;

        @Constraints.Required @ApiModelProperty(required = true)
        public String c_program_version_id;
    }


    public static class M_Project_SnapShot {

        public M_Project_SnapShot(){}

        @Constraints.Required @ApiModelProperty(required = true)
        public String m_project_id;

        @Valid @ApiModelProperty(value = "Connected boards (padavans)", required = true)
        public List<M_Program_SnapShot> m_program_snapshots = new ArrayList<>();

    }

    public static class M_Program_SnapShot {

        public M_Program_SnapShot(){}

        @Constraints.Required @ApiModelProperty(required = true)
        public String m_program_id;

        @Constraints.Required @ApiModelProperty(required = true)
        public String version_object_id;

    }

}
