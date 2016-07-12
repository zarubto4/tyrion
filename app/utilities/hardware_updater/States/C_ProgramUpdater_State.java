package utilities.hardware_updater.States;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "States of update plan for each board are: ")
public enum C_ProgramUpdater_State {

    // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where procedure was absolutely successful" ) complete,

    // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where the procedure is canceled by system or board owner" )  canceled,

    // Proces probíhá - němělo by do něj být zasahováno!
    @ApiModelProperty(value = " State where system is installing new firmware to board. Its not possible terminate this procedure in this time" ) in_progress,

    //  Stav který vyvolal systém, protože přišla nová "čerstvější" aktualizace
    @ApiModelProperty(value =  " State where procedure was overwritten by newer versions" )  overwritten,

    // Stav kdy je stále možné zařízení aktulaizovat
    @ApiModelProperty(value = " State where board is not connected to Homer Server and Main Center is waiting for that"  ) waiting_for_device,

    // Stav kdy je stále možné zařízení aktulaizovat
    @ApiModelProperty(value = " State where instance in Homer wasn't accessible while update procedure"  )  instance_inaccessible,

    // Stav kdy je stále možné zařízení aktulaizovat
    @ApiModelProperty(value = " State where server where board is connected wasn't accessible while update procedure"  )  homer_server_is_offline,

    // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where shit happens - Server don't know what happens - Automatically reported to BackEnd development team" ) critical_error;

}
