package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * All actions that could be invoked through the notifications are listed here.
 */
public enum Enum_Notification_action {

    @EnumValue("accept_project_invitation")   accept_project_invitation,
    @EnumValue("reject_project_invitation")   reject_project_invitation,
    @EnumValue("confirm_notification")        confirm_notification;

    @JsonCreator
    public static Enum_Notification_action fromString(String key) {
        for(Enum_Notification_action action : Enum_Notification_action.values()) {
            if(action.name().equalsIgnoreCase(key)) {
                return action;
            }
        }
        return null;
    }
}