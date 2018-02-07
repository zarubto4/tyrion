package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum ParticipantStatus {

    @EnumValue("OWNER")     OWNER,
    @EnumValue("ADMIN")     ADMIN,
    @EnumValue("MEMBER")    MEMBER,
    @EnumValue("INVITED")   INVITED

}