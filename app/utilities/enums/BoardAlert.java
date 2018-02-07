package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum BoardAlert {

    @EnumValue("BOOTLOADER_REQUIRED")   BOOTLOADER_REQUIRED,
    @EnumValue("RESTART_REQUIRED")      RESTART_REQUIRED,
}
