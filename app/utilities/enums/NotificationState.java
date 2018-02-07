package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum NotificationState {

    @EnumValue("CREATED")       CREATED,
    @EnumValue("UNCONFIRMED")   UNCONFIRMED,
    @EnumValue("UPDATED")       UPDATED,
    @EnumValue("DELETED")       DELETED
}