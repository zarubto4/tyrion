package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Participant_status {

    @EnumValue("owner")   owner,
    @EnumValue("admin")   admin,
    @EnumValue("member")  member,
    @EnumValue("invited") invited
}