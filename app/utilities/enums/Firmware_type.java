package utilities.enums;

public enum Firmware_type {

    FIRMWARE("FIRMWARE"),
    BOOTLOADER("BOOTLOADER"),
    BACKUP("BACKUP");

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
