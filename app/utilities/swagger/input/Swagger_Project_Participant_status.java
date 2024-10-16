package utilities.swagger.input;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.enums.ParticipantStatus;

import java.util.UUID;

@ApiModel(description = "Json Model for changing Project_participant status",
        value = "Project_Participant_status")
public class Swagger_Project_Participant_status {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Participant id")
    public UUID person_id;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Status", allowableValues = "ADMIN, MEMBER")
    public ParticipantStatus state;





}
