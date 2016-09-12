package utilities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Notification_Type{

    confirmation,
    link,
    object,
    bold_text,
    text;

    @JsonCreator
    public static Notification_Type fromString(String key) {
        for(Notification_Type type : Notification_Type.values()) {
            if(type.name().equalsIgnoreCase(key)) {
                return type;
            }
        }
        return null;
    }
}