package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum  Payment_status {

    @EnumValue("paid") paid,                        // Uhrazeno
    @EnumValue("sent") sent,                        // Zasláno u uhrazené
    @EnumValue("created_waited") created_waited,    // Čěká na zaplacení
    @EnumValue("cancelled")  cancelled              // Zrušeno

}
