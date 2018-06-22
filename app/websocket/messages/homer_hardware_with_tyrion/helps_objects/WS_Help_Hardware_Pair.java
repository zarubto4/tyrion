package websocket.messages.homer_hardware_with_tyrion.helps_objects;

import models.Model_Blob;
import models.Model_BootLoader;
import models.Model_CProgramVersion;
import models.Model_Hardware;

public class WS_Help_Hardware_Pair {

    // Required
    public Model_Hardware hardware;


    // Optional
    public Model_CProgramVersion c_program_version;
    public Model_BootLoader bootLoader;
    public Model_Blob blob;
}
