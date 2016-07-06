package utilities.hardware_updater.States;

public enum Actual_procedure_State {

    successful_complete,       // Stav kdy je procedura považována za trvale ukončenou!
    complete,       // Stav kdy je procedura považována za trvale ukončenou!
    complete_with_error,

    canceled,       // Stav kdy je procedura považována za trvale ukončenou!
    in_progress,    // Proces probíhá - němělo by do něj být zasahováno!
}
