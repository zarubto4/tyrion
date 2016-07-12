package utilities.hardware_updater.States;

import io.swagger.annotations.ApiModelProperty;

public enum Actual_procedure_State {

    @ApiModelProperty(value = " State where all inner procedures was absolutely successful" ) successful_complete,       // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where all inner procedures was successfully OR overwritten with newer version" )  complete,       // Stav kdy je procedura považována za trvale ukončenou!

    @ApiModelProperty(value = " State like \"complete\" but some update plans had Error" )  complete_with_error,

    @ApiModelProperty(value = " State where user canceled procedure, but remember that some update plans might be already made (had some state) " )  canceled,       // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where procedure is in progress")  in_progress,    // Proces probíhá - němělo by do něj být zasahováno!
}
