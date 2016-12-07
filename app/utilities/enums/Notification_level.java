package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum Notification_level {

    @EnumValue("info")      info,
    @EnumValue("success")   success,
    @EnumValue("warning")   warning,
    @EnumValue("error")     error;

    @JsonCreator
    public static Notification_level fromString(String key) {
        for(Notification_level level : Notification_level.values()) {
            if(level.name().equalsIgnoreCase(key)) {
                return level;
            }
        }
        return null;
    }
}