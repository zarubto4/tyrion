package utilities.enums;


import io.ebean.annotation.EnumValue;

public enum NotificationType {

    @EnumValue("CHAIN_START")   CHAIN_START,
    @EnumValue("CHAIN_UPDATE")  CHAIN_UPDATE,
    @EnumValue("CHAIN_END")     CHAIN_END,
    @EnumValue("INDIVIDUAL")    INDIVIDUAL
}
