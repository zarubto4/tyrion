package utilities.web_socket.message_objects.homer_instance.Help_object;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class YodaOnlyHardwareIdList {

    public String deviceId;

    @Valid public List<String> devicesId = new ArrayList<>();

}
