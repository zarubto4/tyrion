package models.project.c_program.actualization;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Board;
import models.compiler.FileRecord;
import models.compiler.Version_Object;
import utilities.hardware_updater.States.C_ProgramUpdater_State;

import javax.persistence.*;

/**
 * Objekt slouží k aktualizačnímu plánu jednotlivých zařízení!
 *
 */

@Entity
public class C_Program_Update_Plan extends Model {


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id  @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true) public String id;

              @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)    public Actualization_procedure actualization_procedure;

              @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)    public Board board; // Deska k aktualizaci


                                                                // Aktualizace je vázána buď na verzi C++ kodu nebo na soubor, nahraný uživatelem
                        /** OR **/  @JsonIgnore @ManyToOne()    public Version_Object c_program_version_for_update; // C_program k aktualizaci
                        /** OR **/  @JsonIgnore @ManyToOne()    public FileRecord binary_file;

    @ApiModelProperty(required = true, value = "state_documentation")  @Enumerated(EnumType.STRING)    public C_ProgramUpdater_State state;


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)  @Transient public String board_id()             { return board.id;  }


    @ApiModelProperty(required = false, value = "Is visible only if user send compilation under C_program in system  ( OR state for binary_file)") @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public C_Program_Update_program c_program_detail(){

        if(c_program_version_for_update == null ) return null;

            C_Program_Update_program c_program_detail   = new  C_Program_Update_program();
            c_program_detail.c_program_id               = c_program_version_for_update.c_program.id;
            c_program_detail.c_program_program_name     = c_program_version_for_update.c_program.program_name;
            c_program_detail.c_program_version_id       = c_program_version_for_update.id;
            c_program_detail.c_program_version_name     = c_program_version_for_update.version_name;

            return c_program_detail;

    }

    @ApiModelProperty(required = false, value = "Is visible only if user send own binary file ( OR state for c_program_detail)") @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public FileRecord binary_file_detail(){
        return binary_file == null ? null : binary_file;
    }



/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,C_Program_Update_Plan> find = new Model.Finder<>(C_Program_Update_Plan.class);


/* POMOCNÉ TŘÍDY -------------------------------------------------------------------------------------------------------*/

    class C_Program_Update_program{
        @ApiModelProperty(required = true, value = "Can be empty") public String c_program_id;
        @ApiModelProperty(required = true, value = "Can be empty") public String c_program_version_id;
        @ApiModelProperty(required = true, value = "Can be empty") public String c_program_program_name;
        @ApiModelProperty(required = true, value = "Can be empty") public String c_program_version_name;
    }


/* DESCRIPTION - DOCUMENTATION ---------------------------------------------------------------------------------------------------------*/
    @JsonIgnore @Transient public final static String state_documentation = "States of update plan for each board is: \n\n"
            + C_ProgramUpdater_State.canceled         + " State where the procedure is canceled by system or board owner" + "\n"
            + C_ProgramUpdater_State.complete         + " State where procedure was absolutely successful" + "\n"
            + C_ProgramUpdater_State.overwritten      + " State where procedure was overwritten by newer versions" + "\n"
            + C_ProgramUpdater_State.in_progress      + " State where system is installing new firmware to board. Its not possible terminate this procedure in this time" + "\n"
            + C_ProgramUpdater_State.instance_inaccessible + " State where instance in Homer wasn't accessible while update procedure" + "\n"
            + C_ProgramUpdater_State.homer_server_is_offline + " State where server where board is connected wasn't accessible while update procedure" + "\n"
            + C_ProgramUpdater_State.waiting_for_device + " State where board is not connected to Homer Server and Main Center is waiting for that" + "\n"
            + C_ProgramUpdater_State.waiting_for_device + " State where shit happens - Server don't know what happens - Automatically reported to BackEnd development team" + "\n"
            ;


}


