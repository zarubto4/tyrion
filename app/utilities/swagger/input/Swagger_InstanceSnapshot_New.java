package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(value = "InstanceSnapshot_New")
public class Swagger_InstanceSnapshot_New {

    @Constraints.Required
    public UUID instance_id;

    @Constraints.Required
    public UUID version_id;

    @Constraints.Required
    public String snapshot;

    public List<UUID> hardware_ids = new ArrayList<>();
}
