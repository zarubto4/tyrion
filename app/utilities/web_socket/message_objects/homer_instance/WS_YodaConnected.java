package utilities.web_socket.message_objects.homer_instance;

import io.swagger.annotations.ApiModelProperty;
import utilities.web_socket.message_objects.WS_BoardStats_AbstractClass;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class WS_YodaConnected extends WS_BoardStats_AbstractClass {

    @ApiModelProperty(required = true) public String instanceId;
    @ApiModelProperty(required = true) public String deviceId;
    @ApiModelProperty(required = true) public boolean autobackup;

    @Valid
    public List<WS_DeviceConnected> devices_summary  = new ArrayList<>();
}
