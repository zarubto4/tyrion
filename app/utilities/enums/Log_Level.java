package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Log_Level {

    @EnumValue("error")   error,
    @EnumValue("warn")    warn,
    @EnumValue("info")    info,
    @EnumValue("debug")   debug,
    @EnumValue("trace")   trace;


    public static Log_Level fromString(String key) {
        for(Log_Level mode : Log_Level.values()) {
            if(mode.name().equalsIgnoreCase(key)) {
                return mode;
            }
        }
        return null;
    }
}
