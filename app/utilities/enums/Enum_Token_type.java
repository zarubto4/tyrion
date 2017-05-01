package utilities.enums;

public enum Enum_Token_type {

    PERSON_TOKEN,
    INSTANCE_TOKEN;

    public static Enum_Token_type getType(String value) {

        if (value.equalsIgnoreCase(PERSON_TOKEN.toString())) return Enum_Token_type.PERSON_TOKEN;
        else if (value.equalsIgnoreCase(INSTANCE_TOKEN.toString())) return Enum_Token_type.INSTANCE_TOKEN;
        return null;
    }
}
