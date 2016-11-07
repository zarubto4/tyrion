package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Payment_method{
    @EnumValue("bank_transfer") bank_transfer,
    @EnumValue("credit_card") credit_card,
    @EnumValue("free") free
}
