package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.List;


@ApiModel(description = "Json Model for new Hardware Group edit group of devices or group of hardware",
          value = "Hardware_Group_DeviceListEdit")
public class Swagger_Hardware_Group_DeviceListEdit {


    @Valid
    @ApiModelProperty(required = false)
    public DeviceGroupSynchco device_synchro;

    @Valid
    @ApiModelProperty(required = false)
    public GroupDeviceSynchro group_synchro;




    public class DeviceGroupSynchco {
        public DeviceGroupSynchco(){}
        public String device_id;
        public List<String> group_ids;
    }

    public class GroupDeviceSynchro {
        public GroupDeviceSynchro(){}
        public String group_id;
        public List<String> device_ids;
    }
}



