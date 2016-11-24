package utilities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Notification_type {

    link,
    object,
    text;

    @JsonCreator
    public static Notification_type fromString(String key) {
        for(Notification_type type : Notification_type.values()) {
            if(type.name().equalsIgnoreCase(key)) {
                return type;
            }
        }
        return null;
    }
}