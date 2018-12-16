package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum UpdateType {

    @EnumValue("MANUALLY_BY_USER_INDIVIDUAL")                   MANUALLY_BY_USER_INDIVIDUAL,                // Uživatel nahrál firmware, bootloader, backup okamžitě a sám
    @EnumValue("MANUALLY_RELEASE_MANAGER")                      MANUALLY_RELEASE_MANAGER,                   // Uživatel nahrál firmware, bootloader, pomocí Release Manageru
    @EnumValue("MANUALLY_BY_INSTANCE")                          MANUALLY_BY_INSTANCE,                       // Uživatel nasadil blocko a rozhodl se naplánovat změnu okamžitě
    @EnumValue("AUTOMATICALLY_BY_INSTANCE")                     AUTOMATICALLY_BY_INSTANCE,                  // Uživatel nasadil blocko a rozhodl se naplánovat změnu sám

    @EnumValue("AUTOMATICALLY_BY_USER_ALWAYS_UP_TO_DATE")       AUTOMATICALLY_BY_USER_ALWAYS_UP_TO_DATE,    // Pro přídady, kdy má uživatel milion kávovarů a chce je udržovat všechny aktuální

    @EnumValue("AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE")     AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE,  // V případě funkce Always up to date server updatuje podle knihoven sám...

}
