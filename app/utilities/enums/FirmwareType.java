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

    public static FirmwareType getFirmwareType(String value) {

             if (value.equalsIgnoreCase(FIRMWARE.toString()       ))   return FirmwareType.FIRMWARE;
        else if (value.equalsIgnoreCase(BOOTLOADER.toString()     ))   return FirmwareType.BOOTLOADER;
        else if (value.equalsIgnoreCase(BACKUP.toString()         ))   return FirmwareType.BACKUP;

        return null;
    }

    public String get_firmwareType() {
        return firmwareType;
    }
}
