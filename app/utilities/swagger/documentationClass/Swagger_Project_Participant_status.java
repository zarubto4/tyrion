package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.enums.Participant_status;

@ApiModel(description = "Json Model for changing Project_participant status",
        value = "Project_Participant_status")
public class Swagger_Project_Participant_status {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Participant id")
    public String person_id;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Status", allowableValues = "admin, member")
    public Participant_status state;





}
