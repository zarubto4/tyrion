package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;


@ApiModel(description = "Json Model for new Hardware Group edit group of devices or group of hardware",
          value = "Hardware_Group_DeviceListEdit")
public class Swagger_Hardware_Group_DeviceListEdit {

    public Swagger_Hardware_Group_DeviceListEdit() {}

    @Valid
    @ApiModelProperty(required = false)
    public Swagger_Hardware_Group_DeviceListEdit_Device device_synchro;

    @Valid
    @ApiModelProperty(required = false)
    public Swagger_Hardware_Group_DeviceListEdit_Group group_synchro;



}

