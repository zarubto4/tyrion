package utilities.enums;

public enum Enum_type_of_command {

    REMOVE("REMOVE"),
    ADD("ADD"),
    SET("SET");

    private String command;

    Enum_type_of_command(String command) {
        this.command = command;
    }


    public static Enum_type_of_command getTypeCommand(String value){

        // Ping
             if(value.equalsIgnoreCase(REMOVE.toString()           ))   return Enum_type_of_command.REMOVE;
        else if(value.equalsIgnoreCase(ADD.toString()           ))   return Enum_type_of_command.ADD;
        else if(value.equalsIgnoreCase(SET.toString()           ))   return Enum_type_of_command.SET;

        return null;
    }

    public String get_command() {
        return command;
    }
}
