package utilities.enums;

public enum TokenType {

    PERSON_TOKEN,
    INSTANCE_TOKEN;

    public static TokenType getType(String value) {

        if (value.equalsIgnoreCase(PERSON_TOKEN.toString())) return TokenType.PERSON_TOKEN;
        else if (value.equalsIgnoreCase(INSTANCE_TOKEN.toString())) return TokenType.INSTANCE_TOKEN;
        return null;
    }
}
