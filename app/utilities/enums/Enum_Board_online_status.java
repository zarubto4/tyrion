package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Board_online_status {

    @EnumValue("not_yet_first_connected")  not_yet_first_connected,
    @EnumValue("synchronization_in_progress")  synchronization_in_progress,
    @EnumValue("offline")  offline,
    @EnumValue("online")  online,
    @EnumValue("unknown_lost_connection_with_server")  unknown_lost_connection_with_server,
}
