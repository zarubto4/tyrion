package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum InvoiceStatus {

    @EnumValue("PAID")        PAID,    // Uhrazeno
    @EnumValue("PENDING")     PENDING, // Čěká na zaplacení
    @EnumValue("UNFINISHED")  UNFINISHED, // Čěká na vytvoření faktury ve Fakturiodu
    @EnumValue("OVERDUE")     OVERDUE, // Po splatnosti
    @EnumValue("CANCELED")    CANCELED, // Zrušeno
    // Objevil se problém (např. překročení limitu), faktura musí být před zveřejněním odsouhlasena administrátorem.
    @EnumValue("UNCONFIRMED") UNCONFIRMED
}
