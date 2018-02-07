package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum CompilationStatus {

    @EnumValue("IN_PROGRESS")               IN_PROGRESS,                // Compilace probíhá - aby se zajistilo že nebude provedeno nic dvakrát!!
    @EnumValue("FILE_NOT_FOUND")            FILE_NOT_FOUND,             // Nebyl nalezen json soubor kde je uložený kod!!
    @EnumValue("BROKEN_JSON")               BROKEN_JSON,                // Json se nepodařilo parsovat!!
    @EnumValue("SERVER_OFFLINE")            SERVER_OFFLINE,             // Server byl offline když bylo požádáno o kompilaci!!
    @EnumValue("SERVER_ERROR")              SERVER_ERROR,               // Něco se posralo na straně serveru!!!
    @EnumValue("FAILED")                    FAILED,                     // Zkompilováno úspěšně - ale uživatel tam nasral chyby!!!
    @EnumValue("SUCCESS_DOWNLOAD_FAILED")   SUCCESS_DOWNLOAD_FAILED,    // úspěšně zkompilováno - ale nepodařilo se stáhnout soubor!
    @EnumValue("SUCCESS")                   SUCCESS,                    // úspěšně zkompilováno a Tyrion stáhl a uložil Bin file soubor!!
    @EnumValue("UNSTABLE")                  UNSTABLE,                   // úspěšně zkompilováno, ale hardware s touto verzí selhal -označena jako nestabilní!!!
    @EnumValue("UNDEFINED")                 UNDEFINED                   // Pro stavy kdy je compilation == null a musím dát něco do JSonu.
}
