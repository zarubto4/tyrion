package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum Enum_Payment_mode {
    @EnumValue("free") free,
    @EnumValue("monthly")  monthly,
    @EnumValue("annual")  annual,
    @EnumValue("per_credit")  per_credit;

    @JsonCreator
    public static Enum_Payment_mode fromString(String key) {
        for(Enum_Payment_mode mode : Enum_Payment_mode.values()) {
            if(mode.name().equalsIgnoreCase(key)) {
                return mode;
            }
        }
        return null;
    }
}
