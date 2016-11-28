package utilities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Notification_level {

    info,
    success,
    warning,
    error;

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