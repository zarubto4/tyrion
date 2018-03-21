package utilities.enums;

import io.ebean.annotation.EnumValue;
import io.swagger.annotations.ApiModelProperty;

/**
 *  Stav pro označení RM objektu v databázi u Model_ActualizationProcedure.state
 */
public enum Enum_Update_group_procedure_state {

    // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where all inner procedures was absolutely successful" )
    @EnumValue("SUCCESSFULLY_COMPLETE")
    SUCCESSFULLY_COMPLETE,

    // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where all inner procedures was successfully OR overwritten with newer version" )
    @EnumValue("COMPLETE")
    COMPLETE,

    @ApiModelProperty(value = " State like \"complete\" but some update plans had Error" )
    @EnumValue("COMPLETE_WITH_ERROR")
    COMPLETE_WITH_ERROR,

    // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where user canceled procedure, but remember that some update plans might be already made (had some state) " )
    @EnumValue("CANCELED")
    CANCELED,

    // Proces probíhá - němělo by do něj být zasahováno!
    @ApiModelProperty(value = " State where procedure is in progress")
    @EnumValue("IN_PROGRESS")
    IN_PROGRESS,

    //Proces ještě nezačal!!!
    @ApiModelProperty(value = " State where procedure is in progress")
    @EnumValue("NOT_START_YET")
    NOT_START_YET,

}
