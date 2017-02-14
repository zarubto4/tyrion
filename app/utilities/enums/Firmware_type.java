package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Firmware_type {

    @EnumValue("FIRMWARE")   FIRMWARE("FIRMWARE"),
    @EnumValue("BOOTLOADER") BOOTLOADER("BOOTLOADER"),
    @EnumValue("BACKUP")     BACKUP("BACKUP"),
    @EnumValue("WIFI")       WIFI("WIFI");

    private String firmwareType;

    Firmware_type(String firmwareType) {
        this.firmwareType = firmwareType;
    }

    public static Firmware_type getFirmwareType(String value){

             if(value.equalsIgnoreCase(FIRMWARE.toString()       ))   return Firmware_type.FIRMWARE;
        else if(value.equalsIgnoreCase(BOOTLOADER.toString()     ))   return Firmware_type.BOOTLOADER;
        else if(value.equalsIgnoreCase(BACKUP.toString()         ))   return Firmware_type.BACKUP;

        return null;
    }

    public String get_firmwareType() {
        return firmwareType;
    }
}
