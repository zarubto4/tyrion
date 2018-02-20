package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum PaymentMethod {
    @EnumValue("BANK_TRANSFER") BANK_TRANSFER,
    @EnumValue("CREDIT_CARD")   CREDIT_CARD,
    @EnumValue("CREDIT")        CREDIT,
    @EnumValue("FREE")          FREE
}
