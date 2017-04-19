package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Payment_status {

    @EnumValue("paid") paid,                        // Uhrazeno
    @EnumValue("pending") pending,                  // Čěká na zaplacení
    @EnumValue("overdue") overdue,                  // Po splatnosti
    @EnumValue("canceled")canceled                  // Zrušeno

}
