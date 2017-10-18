package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Log_level {

    @EnumValue("error")   error,
    @EnumValue("warn")    warn,
    @EnumValue("info")    info,
    @EnumValue("debug")   debug,
    @EnumValue("trace")   trace;


    public static Enum_Log_level fromString(String key) {
        for(Enum_Log_level mode : Enum_Log_level.values()) {
            if(mode.name().equalsIgnoreCase(key)) {
                return mode;
            }
        }
        return null;
    }
}
