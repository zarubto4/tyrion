package utilities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Notification_state {

    created,
    unconfirmed,
    updated,
    deleted;

    @JsonCreator
    public static Notification_state fromString(String key) {
        for(Notification_state state : Notification_state.values()) {
            if(state.name().equalsIgnoreCase(key)) {
                return state;
            }
        }
        return null;
    }
}