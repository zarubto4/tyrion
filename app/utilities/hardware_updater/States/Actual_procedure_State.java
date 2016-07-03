package utilities.hardware_updater.States;

public enum Actual_procedure_State {

    complete,       // Stav kdy je procedura považována za trvale ukončenou!
    canceled,       // Stav kdy je procedura považována za trvale ukončenou!
    in_progress,    // Proces probíhá - němělo by do něj být zasahováno!
    override,       // ????????????????????? - zatím nemám představu jak věc integrovat
}
