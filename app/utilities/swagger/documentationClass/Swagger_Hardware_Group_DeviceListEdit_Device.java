package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;

import java.util.List;


@ApiModel(description = "Json Model for new Hardware Group edit group of devices or group of hardware",
          value = "Hardware_Group_DeviceListEdit_Device")
public class Swagger_Hardware_Group_DeviceListEdit_Device {

    public Swagger_Hardware_Group_DeviceListEdit_Device() {}

    public String device_id;
    public List<String> group_ids;

}

