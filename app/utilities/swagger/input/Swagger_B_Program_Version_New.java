package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for new Version of B_Program",
             value = "B_Program_Version_New")
public class Swagger_B_Program_Version_New extends Swagger_NameAndDescription {

    @Constraints.Required
    @ApiModelProperty(required = true)
    @Constraints.MaxLength(value = 4550000, message = "The program must not have more than 4550000 characters.")
    public String program;

    @Valid
    @ApiModelProperty(value = "Connected groups of m_programs.versions under imported m_programs", required = false)
    public List<M_Project_SnapShot> m_project_snapshots  = new ArrayList<>();

// ---------------------------------------------------------------------------------------------------------------------

    public static class M_Project_SnapShot {

        public M_Project_SnapShot() {}

        @Constraints.Required @ApiModelProperty(required = true)
        public UUID m_project_id;

        @Valid @ApiModelProperty(value = "Connected hardware", required = true)
        public List<M_Program_SnapShot> m_program_snapshots = new ArrayList<>();

    }

    public static class M_Program_SnapShot {

        public M_Program_SnapShot() {}

        @Constraints.Required @ApiModelProperty(required = true)
        public UUID m_program_id;

        @Constraints.Required @ApiModelProperty(required = true)
        public UUID version_id;

    }
}