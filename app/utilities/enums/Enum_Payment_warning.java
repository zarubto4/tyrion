package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Payment_warning {

    @EnumValue("none")          none,
    @EnumValue("first")         first,
    @EnumValue("second")        second,
    @EnumValue("zero_balance")  zero_balance,
    @EnumValue("deactivation")  deactivation
}
