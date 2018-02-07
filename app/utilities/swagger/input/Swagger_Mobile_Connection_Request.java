package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for update M_Project",
        value = "Mobile_Connection_Request")
public class Swagger_Mobile_Connection_Request {

    @Constraints.Required
    public String instance_record_id;

    @Constraints.Required
    public String version_object_id;

}
