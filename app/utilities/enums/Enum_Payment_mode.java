package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Payment_mode {

    @EnumValue("free")          free,
    @EnumValue("monthly")       monthly,
    @EnumValue("annual")        annual,
    @EnumValue("per_credit")    per_credit
}
