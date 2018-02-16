package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum NetworkStatus {

    @EnumValue("NOT_YET_FIRST_CONNECTED")               NOT_YET_FIRST_CONNECTED,
    @EnumValue("FREEZED")                               FREEZED,
    @EnumValue("SHUT_DOWN")                             SHUT_DOWN,
    @EnumValue("SYNCHRONIZATION_IN_PROGRESS")           SYNCHRONIZATION_IN_PROGRESS,
    @EnumValue("OFFLINE")                               OFFLINE,
    @EnumValue("ONLINE")                                ONLINE,
    @EnumValue("UNKNOWN_LOST_CONNECTION_WITH_SERVER")   UNKNOWN_LOST_CONNECTION_WITH_SERVER
}
