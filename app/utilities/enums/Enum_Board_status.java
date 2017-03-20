package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Board_status {

    @EnumValue("not_yet_first_connected")  not_yet_first_connected,
    @EnumValue("offline")  offline,
    @EnumValue("online")  online,
}
