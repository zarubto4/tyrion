package utilities.enums;

public enum FirmwareType {

    FIRMWARE_YODA_FIRMWARE("FIRMWARE_YODA_FIRMWARE"),
    FIRMWARE_YODA_BOOTLOADER("FIRMWARE_YODA_BOOTLOADER"),
    FIRMWARE_YODA_BACKUP("FIRMWARE_YODA_BACKUP"),
    FIRMWARE_DEVICE_FIRMWARE("FIRMWARE_DEVICE_FIRMWARE"),
    FIRMWARE_DEVICE_BOOTLOADER("FIRMWARE_DEVICE_BOOTLOADER");

    private String firmwareType;

    FirmwareType(String firmwareType) {
        this.firmwareType = firmwareType;
    }

    public static FirmwareType getFirmwareType(String value){

             if(value.equalsIgnoreCase(FIRMWARE_YODA_FIRMWARE.toString()       ))   return FirmwareType.FIRMWARE_YODA_FIRMWARE;
        else if(value.equalsIgnoreCase(FIRMWARE_YODA_BOOTLOADER.toString()     ))   return FirmwareType.FIRMWARE_YODA_BOOTLOADER;
        else if(value.equalsIgnoreCase(FIRMWARE_YODA_BACKUP.toString()         ))   return FirmwareType.FIRMWARE_YODA_BACKUP;
        else if(value.equalsIgnoreCase(FIRMWARE_DEVICE_FIRMWARE.toString()     ))   return FirmwareType.FIRMWARE_DEVICE_FIRMWARE;
        else if(value.equalsIgnoreCase(FIRMWARE_DEVICE_BOOTLOADER.toString()   ))   return FirmwareType.FIRMWARE_DEVICE_BOOTLOADER;

        return null;
    }

    public String get_firmwareType() {
        return firmwareType;
    }
}
