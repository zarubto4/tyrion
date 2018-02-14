package utilities.swagger.input;

import io.swagger.annotations.ApiModel;

import java.util.List;
import java.util.UUID;


@ApiModel(description = "Json Model for new Hardware Group edit group of devices or group of hardware",
          value = "Hardware_Group_DeviceListEdit_Device")
public class Swagger_Hardware_Group_DeviceListEdit {

    public Swagger_Hardware_Group_DeviceListEdit() {}

    public UUID device_id;
    public List<UUID> group_ids;

}

