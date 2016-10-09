package models.project.c_program.actualization;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Board;
import models.compiler.FileRecord;
import models.compiler.Version_Object;
import utilities.enums.Firmware_type;
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

              @JsonIgnore @ManyToOne()                                          public Actualization_procedure actualization_procedure;

              @JsonIgnore @ManyToOne(fetch = FetchType.EAGER)                   public Board board; // Deska k aktualizaci
              @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)  public Firmware_type firmware_type;

                                                                                // Aktualizace je vázána buď na verzi C++ kodu nebo na soubor, nahraný uživatelem
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.EAGER)                 public Version_Object c_program_version_for_update; // C_program k aktualizaci
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                  public FileRecord binary_file; // Soubor, když firmware nahrává uživatel sám mimo flow

    @ApiModelProperty(required = true, value = "Description on Model C_ProgramUpdater_State")  @Enumerated(EnumType.STRING)    public C_ProgramUpdater_State state;

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/



    @ApiModelProperty(required = false, value = "Is visible only if user send compilation under C_program in system  ( OR state for binary_file)")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @Transient
    public C_Program_Update_program c_program_detail(){

        if(c_program_version_for_update == null ) return null;

            C_Program_Update_program c_program_detail   = new  C_Program_Update_program();
            c_program_detail.c_program_id               = c_program_version_for_update.c_program.id;
            c_program_detail.c_program_program_name     = c_program_version_for_update.c_program.program_name;
            c_program_detail.c_program_version_id       = c_program_version_for_update.id;
            c_program_detail.c_program_version_name     = c_program_version_for_update.version_name;

            return c_program_detail;
    }


    @JsonProperty @ApiModelProperty(required = true, readOnly = true) @Transient
    public Board_detail board_detail(){

        Board_detail board_detail = new Board_detail();
        board_detail.board_id = board.id;
        board_detail.personal_description = board.personal_description;
        board_detail.type_of_board_id = board.type_of_board.id;
        board_detail.type_of_board_name = board.type_of_board.name;

        return board_detail;
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
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_id;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_version_id;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_program_name;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_version_name;
    }

    class Server_detail{
        @ApiModelProperty(required = true, readOnly = true) public String  server_id;
        @ApiModelProperty(required = true, readOnly = true) public boolean is_private;
        @ApiModelProperty(required = true, readOnly = true) public String  server_name;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String instnace_id;
    }


    class Board_detail{
        @ApiModelProperty(required = true, readOnly = true) public String board_id;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String personal_description;
        @ApiModelProperty(required = true, readOnly = true) public String type_of_board_id;
        @ApiModelProperty(required = true, readOnly = true) public String type_of_board_name;
    }
}


