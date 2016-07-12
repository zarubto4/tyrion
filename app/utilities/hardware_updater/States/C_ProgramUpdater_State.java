package utilities.hardware_updater.States;

public enum C_ProgramUpdater_State {

    complete,           // Stav kdy je procedura považována za trvale ukončenou!
    canceled,           // Stav kdy je procedura považována za trvale ukončenou!


    in_progress,        // Proces probíhá - němělo by do něj být zasahováno!

                        //Stav kdy je procedura považována za trvale ukončenou!
    overwritten,           // Stav který vyvolal systém, protože přišla nová "čerstvější" aktualizace


    waiting_for_device,      // Stav kdy je stále možné zařízení aktulaizovat
    instance_inaccessible,        // Stav kdy je stále možné zařízení aktulaizovat
    homer_server_is_offline, // Stav kdy je stále možné zařízení aktulaizovat


    critical_error      // Stav kdy je procedura považována za trvale ukončenou!

}
