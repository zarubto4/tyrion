package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

import java.util.UUID;

@ApiModel(description = "Json Model for changing parameters on Gridd App in instance",
        value = "Instance_GridApp_Settings")
public class Swagger_Instance_GridApp_Settings {

    @Constraints.Required public UUID m_program_parameter_id;
    @Constraints.Required public String snapshot_settings;

}
