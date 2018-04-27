package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for register SIMCard Modul to project",
        value = "GSM_Register")
public class Swagger_GSM_Register extends Swagger_NameAndDescription{

    @Constraints.Required
    public UUID registration_hash;

    @Constraints.Required
    public UUID project_id;
}
