package utilities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Notification_importance {

    low,
    normal,
    high;

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
