package web_socket.message_objects.homer_instance.helps_objects;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class WS_Message_Help_Yoda_only_hardware_Id_list {

    public String deviceId;

    @Valid public List<String> devicesId = new ArrayList<>();

}
