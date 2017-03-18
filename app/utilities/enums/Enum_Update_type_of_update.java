package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Update_type_of_update {

    @EnumValue("MANUALLY_BY_USER")                              MANUALLY_BY_USER_INDIVIDUAL,                // Uživatel nahrál firmware, bootloader, backup okamžitě a sám
    @EnumValue("MANUALLY_BY_USER_BLOCKO_GROUP")                 MANUALLY_BY_USER_BLOCKO_GROUP,              // Uživatel nasadil blocko a rozhodl se naplánovat změnu okamžitě
    @EnumValue("MANUALLY_BY_USER_BLOCKO_GROUP_ON_TIME")         MANUALLY_BY_USER_BLOCKO_GROUP_ON_TIME,      // Uživatel nasadil blocko a rozhodl se naplánovat změnu sám

    @EnumValue("AUTOMATICALLY_BY_USER_ALWAYS_UP_TO_DATE")       AUTOMATICALLY_BY_USER_ALWAYS_UP_TO_DATE,    // Pro přídady, kdy má uživatel milion kávovarů a chce je udržovat všechny aktuální

    @EnumValue("AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE")     AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE,  // V případě funkce Always up to date server updatuje podle knihoven sám...

}
