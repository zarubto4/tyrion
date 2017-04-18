package utilities.enums;

public enum Enum_Token_type {

    PERSON_TOKEN("PERSON_TOKEN"),
    INSTANCE_TOKEN("INSTANCE_TOKEN");



    private String command;

    Enum_Token_type(String command) {
        this.command = command;
    }

    public static Enum_Token_type getType(String value) {

        if (value.equalsIgnoreCase(PERSON_TOKEN.toString())) return Enum_Token_type.PERSON_TOKEN;
        else if (value.equalsIgnoreCase(INSTANCE_TOKEN.toString())) return Enum_Token_type.INSTANCE_TOKEN;
        return null;
    }
}
