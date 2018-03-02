package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@ApiModel(description = "Json Model for new Hardware Group edit group of devices or group of hardware",
          value = "HardwareGroup_Edit")
public class Swagger_HardwareGroup_Edit {

    @Constraints.Required
    public UUID group_id;

    @Constraints.Required
    public List<UUID> hardware_ids = new ArrayList<>();
}

