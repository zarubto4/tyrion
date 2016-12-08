package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum Notification_state {

    @EnumValue("created")     created,
    @EnumValue("unconfirmed") unconfirmed,
    @EnumValue("updated")     updated,
    @EnumValue("deleted")     deleted;

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