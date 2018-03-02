package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum NotificationElement {

    @EnumValue("LINK")      LINK,
    @EnumValue("OBJECT")    OBJECT,
    @EnumValue("TEXT")      TEXT,
    @EnumValue("DATE")      DATE,
    @EnumValue("NEW_LINE")  NEW_LINE
}