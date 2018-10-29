package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum TimePeriod {

    @EnumValue("MONTH") MONTH,
    @EnumValue("WEEK")  WEEK,
    @EnumValue("DAY")   DAY,
    @EnumValue("HOUR")  HOUR,

}
