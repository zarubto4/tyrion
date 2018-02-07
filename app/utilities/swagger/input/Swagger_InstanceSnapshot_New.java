package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "InstanceSnapshot_New")
public class Swagger_InstanceSnapshot_New {

    @Constraints.Required
    public String instance_id;

    @Constraints.Required
    public String version_id;

    @Constraints.Required
    public String snapshot;

    public List<String> hardware_ids = new ArrayList<>();
}
