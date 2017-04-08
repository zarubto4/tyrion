package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Compile_status {

    @EnumValue("compilation_in_progress")               compilation_in_progress,            // Compilace probíhá - aby se zajistilo že nebude provedeno nic dvakrát!!
    @EnumValue("file_with_code_not_found")              file_with_code_not_found,           // Nebyl nalezen json soubor kde je uložený kod!!
    @EnumValue("json_code_is_broken")                   json_code_is_broken,                // Json se nepodařilo parsovat!!
    @EnumValue("server_was_offline")                    server_was_offline,                 // Server byl offline když bylo požádáno o kompilaci!!
    @EnumValue("compilation_server_error")              compilation_server_error,           // Něco se posralo na straně serveru!!!
    @EnumValue("compiled_with_code_errors")             compiled_with_code_errors,          // Zkompilováno úspěšně - ale uživatel tam nasral chyby!!!
    @EnumValue("successfully_compiled_not_restored")    successfully_compiled_not_restored, // úspěšně zkompilováno - ale nepodařilo se stáhnout soubor!
    @EnumValue("successfully_compiled_and_restored")    successfully_compiled_and_restored,       // úspěšně zkompilováno a Tyrion stáhl a uložil Bin file soubor!!
    @EnumValue("hardware_unstable")                     hardware_unstable,  // úspěšně zkompilováno, ale hardware s touto verzí selhal -označena jako nestabilní!!!

    @EnumValue("undefined")                             undefined            // Pro stavy kdy je c_compilation == null a musím dát něco do JSonu.
}
