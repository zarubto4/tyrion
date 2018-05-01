package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum BoardCommand {

    @EnumValue("RESTART")               RESTART,
    @EnumValue("SWITCH_TO_BOOTLOADER")  SWITCH_TO_BOOTLOADER,
    @EnumValue("BLINK")  BLINK
}
