package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@ApiModel(description = "Json Model for new Hardware Group edit group of devices or group of hardware",
          value = "Hardware_Group_DeviceListEdit_Group")
public class Swagger_Hardware_Group_DeviceListEdit_Group {

    public Swagger_Hardware_Group_DeviceListEdit_Group() {}

    public UUID group_id;
    public List<UUID> hardware_ids = new ArrayList<>();

}

