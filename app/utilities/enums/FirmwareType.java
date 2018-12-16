package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum FirmwareType {

    @EnumValue("FIRMWARE")   FIRMWARE("FIRMWARE"),
    @EnumValue("BOOTLOADER") BOOTLOADER("BOOTLOADER"),
    @EnumValue("BACKUP")     BACKUP("BACKUP"),
    @EnumValue("WIFI")       WIFI("WIFI");

    private String firmwareType;

    FirmwareType(String firmwareType) {
        this.firmwareType = firmwareType;
    }

}
