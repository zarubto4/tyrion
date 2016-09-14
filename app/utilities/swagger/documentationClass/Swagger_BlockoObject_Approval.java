package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for approving or disapproving Blocko Object",
        value = "BlockoObject_Approval")
public class Swagger_BlockoObject_Approval {

    @Constraints.Required
    public String object_name;

    @Constraints.Required
    public String object_id;

    @Constraints.Required
    public boolean approval;
}
