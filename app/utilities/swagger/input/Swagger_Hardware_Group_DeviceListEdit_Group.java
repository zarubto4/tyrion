package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.List;


@ApiModel(description = "Json Model for new Hardware Group edit group of devices or group of hardware",
          value = "Hardware_Group_DeviceListEdit_Group")
public class Swagger_Hardware_Group_DeviceListEdit_Group {

    public Swagger_Hardware_Group_DeviceListEdit_Group() {}

    public String group_id;
    public List<String> device_ids;

}

