package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum BoardUpdateCollision {

    @EnumValue("NO_COLLISION")          NO_COLLISION,
    @EnumValue("ALREADY_IN_INSTANCE")   ALREADY_IN_INSTANCE,
    @EnumValue("PLANNED_UPDATE")        PLANNED_UPDATE,

}
