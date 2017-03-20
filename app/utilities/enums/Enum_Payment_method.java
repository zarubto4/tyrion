package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum Enum_Payment_method {
    @EnumValue("bank_transfer") bank_transfer,
    @EnumValue("credit_card") credit_card,
    @EnumValue("free") free;

    @JsonCreator
    public static Enum_Payment_method fromString(String key) {
        for(Enum_Payment_method method : Enum_Payment_method.values()) {
            if(method.name().equalsIgnoreCase(key)) {
                return method;
            }
        }
        return null;
    }
}
