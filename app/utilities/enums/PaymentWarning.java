package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum PaymentWarning {

    @EnumValue("NONE")          NONE,
    @EnumValue("FIRST")         FIRST,
    @EnumValue("SECOND")        SECOND,
    @EnumValue("ZERO_BALANCE")  ZERO_BALANCE,
    @EnumValue("DEACTIVATION")  DEACTIVATION
}
