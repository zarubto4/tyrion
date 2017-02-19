package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "States of update plan for each board are: ")
public enum C_ProgramUpdater_State {

    // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where procedure was absolutely successful" ) @EnumValue("complete")  complete,

    // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where the procedure is canceled by system or board owner" )  @EnumValue("canceled")  canceled,

    // Proces ještě nezačal
    @ApiModelProperty(value = " State where Tyrion not yet compiled firmware for hardware" )  @EnumValue("bin_file_not_found") bin_file_not_found,

    // Proces ještě nezačal
    @ApiModelProperty(value = " State where system is installing new firmware to board. Its not possible terminate this procedure in this time" )  @EnumValue("not_start_yet") not_start_yet,

    // Proces probíhá - němělo by do něj být zasahováno!
    @ApiModelProperty(value = " State where system is installing new firmware to board. Its not possible terminate this procedure in this time" )  @EnumValue("in_progress") in_progress,

    //  Stav který vyvolal systém, protože přišla nová "čerstvější" aktualizace
    @ApiModelProperty(value =  " State where procedure was overwritten by newer versions" )  @EnumValue("overwritten")  overwritten,

    // Stav kdy je stále možné zařízení aktulaizovat
    @ApiModelProperty(value = " State where board wasn updated to right version of Firmware"  ) @EnumValue("not_updated")  not_updated,

    // Stav kdy je stále možné zařízení aktulaizovat
    @ApiModelProperty(value = " State where board is not connected to Homer Server and Main Center is waiting for that"  ) @EnumValue("waiting_for_device")  waiting_for_device,

    // Stav kdy je stále možné zařízení aktulaizovat
    @ApiModelProperty(value = " State where instance in Homer wasn't accessible while update procedure"  )   @EnumValue("instance_inaccessible") instance_inaccessible,

    // Stav kdy je stále možné zařízení aktulaizovat
    @ApiModelProperty(value = " State where server where board is connected wasn't accessible while update procedure"  )  @EnumValue("homer_server_is_offline")  homer_server_is_offline,

    // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where shit happens - Server don't know what happens - Automatically reported to BackEnd development team" )  @EnumValue("critical_error") critical_error;

}
