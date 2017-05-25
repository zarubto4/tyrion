package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum  Enum_Board_BackUpMode {

    @EnumValue("STATIC_BACKUP")  STATIC_BACKUP,
    @EnumValue("AUTO_BACKUP")    AUTO_BACKUP,
    @EnumValue("NO_BACKUP")      NO_BACKUP,
}
