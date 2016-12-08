package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum Notification_importance {

    @EnumValue("low")     low,
    @EnumValue("normal")  normal,
    @EnumValue("high")    high;

    @JsonCreator
    public static Notification_importance fromString(String key) {
        for(Notification_importance importance : Notification_importance.values()) {
            if(importance.name().equalsIgnoreCase(key)) {
                return importance;
            }
        }
        return null;
    }

}
