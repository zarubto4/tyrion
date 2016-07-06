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

    @Id  @GeneratedValue(strategy = GenerationType.SEQUENCE)    public String id; // Vlastní id je přidělováno

              @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)    public Actualization_procedure actualization_procedure;

              @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)    public Board board; // Deska k aktualizaci


                                                                // Aktualizace je vázána buď na verzi C++ kodu nebo na soubor, nahraný uživatelem
                        /** OR **/  @JsonIgnore @ManyToOne()    public Version_Object c_program_version_for_update; // C_program k aktualizaci
                        /** OR **/  @JsonIgnore @ManyToOne()    public FileRecord binary_file;

                                @Enumerated(EnumType.STRING)    public C_ProgramUpdater_State state;


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient public String board_id()             { return board.id;  }


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
        public String c_program_id;
        public String c_program_version_id;
        public String c_program_program_name;
        public String c_program_version_name;
    }

}





