package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Firmware_type {

    @EnumValue("FIRMWARE")   FIRMWARE("FIRMWARE"),
    @EnumValue("BOOTLOADER") BOOTLOADER("BOOTLOADER"),
    @EnumValue("BACKUP")     BACKUP("BACKUP"),
    @EnumValue("WIFI")       WIFI("WIFI");

    private String firmwareType;

    Enum_Firmware_type(String firmwareType) {
        this.firmwareType = firmwareType;
    }

    public static Enum_Firmware_type getFirmwareType(String value){

             if(value.equalsIgnoreCase(FIRMWARE.toString()       ))   return Enum_Firmware_type.FIRMWARE;
        else if(value.equalsIgnoreCase(BOOTLOADER.toString()     ))   return Enum_Firmware_type.BOOTLOADER;
        else if(value.equalsIgnoreCase(BACKUP.toString()         ))   return Enum_Firmware_type.BACKUP;

        return null;
    }

    public String get_firmwareType() {
        return firmwareType;
    }
}
