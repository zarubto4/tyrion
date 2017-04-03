package utilities.enums;


import com.avaje.ebean.annotation.EnumValue;

public enum  Enum_Notification_type {

    @EnumValue("CHAIN_START")     CHAIN_START,
    @EnumValue("CHAIN_UPDATE")    CHAIN_UPDATE,
    @EnumValue("CHAIN_END")       CHAIN_END,

    @EnumValue("INDIVIDUAL")       INDIVIDUAL;

}
