package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum Enum_Notification_type {

    @EnumValue("link")    link,
    @EnumValue("object")  object,
    @EnumValue("text")    text;

    @JsonCreator
    public static Enum_Notification_type fromString(String key) {
        for(Enum_Notification_type type : Enum_Notification_type.values()) {
            if(type.name().equalsIgnoreCase(key)) {
                return type;
            }
        }
        return null;
    }
}