package utilities.enums;

public enum TypeOfCommand {

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

    COMMAND_RESTART_DEVICE("COMMAND_RESTART_DEVICE"),
    COMMAND_PING_DEVICE("COMMAND_PING_DEVICE");

    private String command;

    TypeOfCommand(String command) {
        this.command = command;
    }


    public static TypeOfCommand getTypeCommand(String value){

        if(value.equalsIgnoreCase(INFO_FIRMWARE.toString()         ))   return TypeOfCommand.INFO_FIRMWARE;
        else if(value.equalsIgnoreCase(INFO_BOOTLOADER.toString()       ))   return TypeOfCommand.INFO_BOOTLOADER;
        else if(value.equalsIgnoreCase(INFO_DATETIME.toString()         ))   return TypeOfCommand.INFO_DATETIME;
        else if(value.equalsIgnoreCase(INFO_DEVICE_COUNTER.toString()   ))   return TypeOfCommand.INFO_DEVICE_COUNTER;
        else if(value.equalsIgnoreCase(INFO_AUTO_BACKUP.toString()      ))   return TypeOfCommand.INFO_AUTO_BACKUP;
        else if(value.equalsIgnoreCase(INFO_STATE.toString()            ))   return TypeOfCommand.INFO_STATE;

        else if(value.equalsIgnoreCase(COMMAND_ADD_DEVICE.toString()            ))   return TypeOfCommand.COMMAND_ADD_DEVICE;
        else if(value.equalsIgnoreCase(COMMAND_REMOVE_DEVICE.toString()         ))   return TypeOfCommand.COMMAND_REMOVE_DEVICE;
        else if(value.equalsIgnoreCase(COMMAND_ADD_MASTER_DEVICE.toString()     ))   return TypeOfCommand.COMMAND_ADD_MASTER_DEVICE;
        else if(value.equalsIgnoreCase(COMMAND_REMOVE_MASTER_DEVICE.toString()  ))   return TypeOfCommand.COMMAND_REMOVE_MASTER_DEVICE;
        else if(value.equalsIgnoreCase(COMMAND_UPLOAD_FIRMWARE.toString()       ))   return TypeOfCommand.COMMAND_UPLOAD_FIRMWARE;
        else if(value.equalsIgnoreCase(COMMAND_UPLOAD_BOOTLOADER.toString()     ))   return TypeOfCommand.COMMAND_UPLOAD_BOOTLOADER;
        else if(value.equalsIgnoreCase(COMMAND_RESTART_DEVICE.toString()        ))   return TypeOfCommand.COMMAND_RESTART_DEVICE;
        else if(value.equalsIgnoreCase(COMMAND_PING_DEVICE.toString()           ))   return TypeOfCommand.COMMAND_PING_DEVICE;

        return null;
    }

    public String get_command() {
        return command;
    }
}
