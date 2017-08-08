package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import io.swagger.annotations.ApiModelProperty;

/**
 *  Stav pro označení RM objektu v databázi u Model_ActualizationProcedure.state
 */
public enum Enum_Update_group_procedure_state {

    // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where all inner procedures was absolutely successful" )
    @EnumValue("successful_complete")
    successful_complete,

    // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where all inner procedures was successfully OR overwritten with newer version" )
    @EnumValue("complete")
    complete,

    @ApiModelProperty(value = " State like \"complete\" but some update plans had Error" )
    @EnumValue("complete_with_error")
    complete_with_error,

    // Stav kdy je procedura považována za trvale ukončenou!
    @ApiModelProperty(value = " State where user canceled procedure, but remember that some update plans might be already made (had some state) " )
    @EnumValue("canceled")
    canceled,

    // Proces probíhá - němělo by do něj být zasahováno!
    @ApiModelProperty(value = " State where procedure is in progress")
    @EnumValue("in_progress")
    in_progress,

    //Proces ještě nezačal!!!
    @ApiModelProperty(value = " State where procedure is in progress")
    @EnumValue("not_start_yet")
    not_start_yet,

}
