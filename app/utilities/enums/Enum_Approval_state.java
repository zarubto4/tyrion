package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum Enum_Approval_state {

    @EnumValue("pending")       pending,
    @EnumValue("approved")      approved,
    @EnumValue("disapproved")   disapproved,
    @EnumValue("edited")        edited;

    @JsonCreator
    public static Enum_Approval_state fromString(String key) {
        for(Enum_Approval_state type : Enum_Approval_state.values()) {
            if(type.name().equalsIgnoreCase(key)) {
                return type;
            }
        }
        return null;
    }
}