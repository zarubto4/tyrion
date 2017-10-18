package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum Enum_Notification_level {

    @EnumValue("info")      info,
    @EnumValue("success")   success,
    @EnumValue("warning")   warning,
    @EnumValue("error")     error;

    @JsonCreator
    public static Enum_Notification_level fromString(String key) {
        for(Enum_Notification_level level : Enum_Notification_level.values()) {
            if(level.name().equalsIgnoreCase(key)) {
                return level;
            }
        }
        return null;
    }
}