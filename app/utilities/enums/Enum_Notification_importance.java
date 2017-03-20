package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum Enum_Notification_importance {

    @EnumValue("low")     low,
    @EnumValue("normal")  normal,
    @EnumValue("high")    high;

    @JsonCreator
    public static Enum_Notification_importance fromString(String key) {
        for(Enum_Notification_importance importance : Enum_Notification_importance.values()) {
            if(importance.name().equalsIgnoreCase(key)) {
                return importance;
            }
        }
        return null;
    }

}
