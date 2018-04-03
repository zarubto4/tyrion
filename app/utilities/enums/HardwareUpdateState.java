package utilities.enums;

import io.ebean.annotation.EnumValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "HardwareUpdateState")
public enum HardwareUpdateState {

    // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where procedure was absolutely successful") @EnumValue("COMPLETE") COMPLETE,

    // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where the procedure is CANCELED by system or hardware owner")  @EnumValue("CANCELED") CANCELED,

    // Proces ještě nezačal
    @ApiModelProperty(value = " State where Tyrion not yet compiled firmware for hardware" )  @EnumValue("BIN_FILE_MISSING") BIN_FILE_MISSING,

    // Proces ještě nezačal
    @ApiModelProperty(value = " State where system is installing new firmware to hardware. Its not possible terminate this procedure in this time")  @EnumValue("NOT_YET_STARTED") NOT_YET_STARTED,

    // Proces ještě nezačal
    @ApiModelProperty(value = "Prohibited by configuration on hardware, probably the Database Synchronization is on OFF!")  @EnumValue("PROHIBITED_BY_CONFIG") PROHIBITED_BY_CONFIG,

    // Proces probíhá - němělo by do něj být zasahováno!
    @ApiModelProperty(value = " State where system is installing new firmware to hardware. Its not possible terminate this procedure in this time")  @EnumValue("IN_PROGRESS") IN_PROGRESS,

    //  Stav který vyvolal systém, protože přišla nová "čerstvější" aktualizace
    @ApiModelProperty(value =  " State where procedure was overwritten by newer versions")  @EnumValue("OBSOLETE") OBSOLETE,

    // Stav kdy je stále možné zařízení aktulaizovat
    @ApiModelProperty(value = " State where hardware wasn updated to right version of Firmware" ) @EnumValue("NOT_UPDATED") NOT_UPDATED,

    // Stav kdy je stále možné zařízení aktulaizovat
    @ApiModelProperty(value = " State where hardware is not connected to Homer Server and Main Center is waiting for that") @EnumValue("WAITING_FOR_DEVICE") WAITING_FOR_DEVICE,

    // Stav kdy je stále možné zařízení aktulaizovat
    @ApiModelProperty(value = " State where instance in Homer wasn't accessible while update procedure")   @EnumValue("INSTANCE_INACCESSIBLE") INSTANCE_INACCESSIBLE,

    // Stav kdy je stále možné zařízení aktulaizovat
    @ApiModelProperty(value = " State where server where hardware is connected wasn't accessible while update procedure")  @EnumValue("HOMER_SERVER_IS_OFFLINE") HOMER_SERVER_IS_OFFLINE,

    @ApiModelProperty(value = " State where Main server has not any records about device connection")  @EnumValue("HOMER_SERVER_NEVER_CONNECTED") HOMER_SERVER_NEVER_CONNECTED,

    // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where shit happens - Server don't know what happens - Automatically reported to BackEnd development team")  @EnumValue("CRITICAL_ERROR") CRITICAL_ERROR;

}
