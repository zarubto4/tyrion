package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum PaymentStatus {

    @EnumValue("PAID")      PAID,    // Uhrazeno
    @EnumValue("PENDING")   PENDING, // Čěká na zaplacení
    @EnumValue("OVERDUE")   OVERDUE, // Po splatnosti
    @EnumValue("CANCELED")  CANCELED // Zrušeno
}
