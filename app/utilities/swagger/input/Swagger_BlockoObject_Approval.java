package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

import java.util.UUID;

@ApiModel(description = "",
        value = "BlockoObject_Approval")
public class Swagger_BlockoObject_Approval {

    @Constraints.Required
    public UUID object_id;

    public String reason;
}
