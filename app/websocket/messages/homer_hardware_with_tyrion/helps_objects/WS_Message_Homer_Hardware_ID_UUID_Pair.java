package websocket.messages.homer_hardware_with_tyrion.helps_objects;

import play.data.validation.Constraints;

import java.util.UUID;

public class WS_Message_Homer_Hardware_ID_UUID_Pair {

    public WS_Message_Homer_Hardware_ID_UUID_Pair() {}

    @Constraints.Required  public String full_id;
    @Constraints.Required  public String uuid;  // Warning!!!!!! - It must be String not UUID
    @Constraints.Required  public boolean online_state;  // Warning!!!!!! - It must be String not UUID

    /**
     * Why String:
     * Sometime - Homer has not connection to Tyrion for get translate from full_id to uuid.
     * So it used full id as uuid for a while to first connection and check operation
     */
}
