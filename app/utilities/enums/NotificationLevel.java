package utilities.enums;

import io.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum NotificationLevel {

    @EnumValue("INFO")      INFO,
    @EnumValue("SUCCESS")   SUCCESS,
    @EnumValue("WARNING")   WARNING,
    @EnumValue("ERROR")     ERROR
}