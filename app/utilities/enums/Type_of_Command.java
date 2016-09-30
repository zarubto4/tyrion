package utilities.enums;

public enum Type_of_command {

    INFO_FIRMWARE("INFO_FIRMWARE"),
    INFO_BOOTLOADER("INFO_BOOTLOADER"),
    INFO_DATETIME("INFO_DATETIME"),
    INFO_DEVICE_COUNTER("INFO_DEVICE_COUNTER"),
    INFO_AUTO_BACKUP("INFO_AUTO_BACKUP"),
    INFO_STATE("INFO_STATE"),

    INFO_WIFI_USERNAME("INFO_WIFI_USERNAME"),
    INFO_WIFI_PASSWORD("INFO_WIFI_PASSWORD"),

    COMMAND_WIFI_USERNAME("INFO_WIFI_USERNAME"),
    COMMAND_WIFI_PASSWORD("INFO_WIFI_PASSWORD"),

    SETTINGS_DATETIME("SETTINGS_DATETIME"),
    SETTINGS_AUTOBACKUP("SETTINGS_AUTOBACKUP"),

    COMMAND_GET_DEVICE("COMMAND_GET_DEVICE"),
    COMMAND_ADD_DEVICE("COMMAND_ADD_DEVICE"),
    COMMAND_REMOVE_DEVICE("COMMAND_REMOVE_DEVICE"),
    COMMAND_ADD_MASTER_DEVICE("COMMAND_ADD_MASTER_DEVICE"),
    COMMAND_REMOVE_MASTER_DEVICE("COMMAND_REMOVE_MASTER_DEVICE"),


    COMMAND_UPLOAD_BOOTLOADER("COMMAND_UPLOAD_BOOTLOADER"),
    COMMAND_UPLOAD_FIRMWARE("COMMAND_UPLOAD_FIRMWARE"),
    COMMAND_UPLOAD_BACKUP("COMMAND_UPLOAD_BACKUP"),

    COMMAND_RESTART_DEVICE("COMMAND_RESTART_DEVICE"),
    COMMAND_PING_DEVICE("COMMAND_PING_DEVICE"),

    COMMAND_PING_INSTANCE("COMMAND_PING_INSTANCE"),
    COMMAND_DISCONECT_INSTANCE("COMMAND_DISCONECT_INSTANCE");

    private String command;

    Type_of_command(String command) {
        this.command = command;
    }


    public static Type_of_command getTypeCommand(String value){

        if(value.equalsIgnoreCase(INFO_FIRMWARE.toString()         ))   return Type_of_command.INFO_FIRMWARE;
        else if(value.equalsIgnoreCase(INFO_BOOTLOADER.toString()       ))   return Type_of_command.INFO_BOOTLOADER;
        else if(value.equalsIgnoreCase(INFO_DATETIME.toString()         ))   return Type_of_command.INFO_DATETIME;
        else if(value.equalsIgnoreCase(INFO_DEVICE_COUNTER.toString()   ))   return Type_of_command.INFO_DEVICE_COUNTER;
        else if(value.equalsIgnoreCase(INFO_AUTO_BACKUP.toString()      ))   return Type_of_command.INFO_AUTO_BACKUP;
        else if(value.equalsIgnoreCase(INFO_STATE.toString()            ))   return Type_of_command.INFO_STATE;

        // Device
        else if(value.equalsIgnoreCase(COMMAND_ADD_DEVICE.toString()            ))   return Type_of_command.COMMAND_ADD_DEVICE;
        else if(value.equalsIgnoreCase(COMMAND_REMOVE_DEVICE.toString()         ))   return Type_of_command.COMMAND_REMOVE_DEVICE;
        else if(value.equalsIgnoreCase(COMMAND_ADD_MASTER_DEVICE.toString()     ))   return Type_of_command.COMMAND_ADD_MASTER_DEVICE;
        else if(value.equalsIgnoreCase(COMMAND_REMOVE_MASTER_DEVICE.toString()  ))   return Type_of_command.COMMAND_REMOVE_MASTER_DEVICE;

        // Firmware
        else if(value.equalsIgnoreCase(COMMAND_UPLOAD_FIRMWARE.toString()       ))   return Type_of_command.COMMAND_UPLOAD_FIRMWARE;
        else if(value.equalsIgnoreCase(COMMAND_UPLOAD_BOOTLOADER.toString()     ))   return Type_of_command.COMMAND_UPLOAD_BOOTLOADER;
        else if(value.equalsIgnoreCase(COMMAND_UPLOAD_BACKUP.toString()         ))   return Type_of_command.COMMAND_UPLOAD_BACKUP;

        // Restart device
        else if(value.equalsIgnoreCase(COMMAND_RESTART_DEVICE.toString()        ))   return Type_of_command.COMMAND_RESTART_DEVICE;

        // Ping
        else if(value.equalsIgnoreCase(COMMAND_PING_DEVICE.toString()           ))   return Type_of_command.COMMAND_PING_DEVICE;

        return null;
    }

    public String get_command() {
        return command;
    }
}
