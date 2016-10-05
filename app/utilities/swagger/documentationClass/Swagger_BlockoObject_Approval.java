package utilities.swagger.documentationClass;

import play.data.validation.Constraints;

public class Swagger_BlockoObject_Approval {

    @Constraints.Required
    public String object_id;

    public String reason;
}
