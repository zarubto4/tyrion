package utilities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Approval_state {

    pending,
    approved,
    disapproved,
    edited;

    @JsonCreator
    public static Approval_state fromString(String key) {
        for(Approval_state type : Approval_state.values()) {
            if(type.name().equalsIgnoreCase(key)) {
                return type;
            }
        }
        return null;
    }
}