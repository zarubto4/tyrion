package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Board_Status {

    @EnumValue("not_yet_first_connected")  not_yet_first_connected,
    @EnumValue("offline")  offline,
    @EnumValue("online")  online,
}
