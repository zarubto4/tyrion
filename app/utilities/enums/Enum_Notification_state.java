package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum Enum_Notification_state {

    @EnumValue("created")     created,
    @EnumValue("unconfirmed") unconfirmed,
    @EnumValue("updated")     updated,
    @EnumValue("deleted")     deleted;

    @JsonCreator
    public static Enum_Notification_state fromString(String key) {
        for(Enum_Notification_state state : Enum_Notification_state.values()) {
            if(state.name().equalsIgnoreCase(key)) {
                return state;
            }
        }
        return null;
    }
}