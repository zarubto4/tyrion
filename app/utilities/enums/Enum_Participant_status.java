package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Participant_status {

    @EnumValue("owner")   owner,
    @EnumValue("admin")   admin,
    @EnumValue("member")  member,
    @EnumValue("invited") invited
}