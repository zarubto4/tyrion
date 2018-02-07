package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum BoardConnection {

    @EnumValue("IN_PERSON_INSTANCE")                IN_PERSON_INSTANCE,
    @EnumValue("CONNECTED_TO_SERVER_UNREGISTERED")  CONNECTED_TO_SERVER_UNREGISTERED,
    @EnumValue("UNDER_PROJECT_VIRTUAL_INSTANCE")    UNDER_PROJECT_VIRTUAL_INSTANCE,
    @EnumValue("CONNECTED_TO_BYZANCE")              CONNECTED_TO_BYZANCE
}
