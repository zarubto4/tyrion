package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.UUID;

@ApiModel(value = "ProjectParticipant", description = "Model of Project_participant")
public class Swagger_ProjectParticipant extends _Swagger_Abstract_Default {

    public UUID id;
    public String email;
    public String full_name;

    public Swagger_ProjectParticipant role;
}
