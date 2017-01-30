package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Library_state {

    @EnumValue("NEW")        NEW,
    @EnumValue("TESTED")     TESTED,
    @EnumValue("DEPRECATED") DEPRECATED
}