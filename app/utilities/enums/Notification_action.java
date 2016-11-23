package utilities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * All actions that could be invoked through the notifications are listed here.
 */
public enum Notification_action {

    accept_project_invitation,
    reject_project_invitation,
    confirm_notification;

    @JsonCreator
    public static Notification_action fromString(String key) {
        for(Notification_action type : Notification_action.values()) {
            if(type.name().equalsIgnoreCase(key)) {
                return type;
            }
        }
        return null;
    }
}