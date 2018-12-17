package utilities.enums;

import io.ebean.annotation.EnumValue;

/**
 * All actions that could be invoked through the notifications are listed here.
 */
public enum NotificationAction {

    @EnumValue("ACCEPT_PROJECT_INVITATION")   ACCEPT_PROJECT_INVITATION,
    @EnumValue("REJECT_PROJECT_INVITATION")   REJECT_PROJECT_INVITATION,
    @EnumValue("CONFIRM_NOTIFICATION")        CONFIRM_NOTIFICATION,

    @EnumValue("ACCEPT_RESTORE_FIRMARE")   ACCEPT_RESTORE_FIRMWARE,
    @EnumValue("REJECT_RESTORE_FIRMARE")   REJECT_RESTORE_FIRMWARE,
}