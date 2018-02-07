package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum  BackupMode {

    @EnumValue("STATIC_BACKUP")  STATIC_BACKUP,
    @EnumValue("AUTO_BACKUP")    AUTO_BACKUP,
    @EnumValue("NO_BACKUP")      NO_BACKUP,
}
