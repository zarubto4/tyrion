package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum LibraryState {

    @EnumValue("NEW")        NEW,
    @EnumValue("TESTED")     TESTED,
    @EnumValue("DEPRECATED") DEPRECATED
}