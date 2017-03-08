package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Board_update_collision {

    @EnumValue("NO_COLLISION") NO_COLLISION,
    @EnumValue("ALREADY_IN_INSTANCE") ALREADY_IN_INSTANCE,
    @EnumValue("PLANNED_UPDATE") PLANNED_UPDATE,

}
