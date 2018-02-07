package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

@ApiModel(description = "",
        value = "BlockoObject_Approval")
public class Swagger_BlockoObject_Approval {

    @Constraints.Required
    public String object_id;

    public String reason;
}
