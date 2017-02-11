package models.project.c_program.actualization;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Model_Board;
import models.compiler.Model_BootLoader;
import models.compiler.Model_FileRecord;
import models.compiler.Model_VersionObject;
import utilities.enums.Firmware_type;
import utilities.enums.C_ProgramUpdater_State;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * Objekt slouží k aktualizačnímu plánu jednotlivých zařízení!
 *
 */

@Entity
@ApiModel(description = "Model of CProgramUpdatePlan",
        value = "CProgramUpdatePlan")
public class Model_CProgramUpdatePlan extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                         @Id @ApiModelProperty(required = true) public String id;

                                                       @JsonIgnore @ManyToOne() public Model_ActualizationProcedure actualization_procedure;

    @ApiModelProperty(required = true, value = "UNIX time in ms",
            example = "1466163478925")                                          public Date date_of_create;
    @ApiModelProperty(required = true, value = "can be empty, which means that the procedure is not done yet. " +
                                               "UNIX time in ms",
            example = "1466163478925")                                          public Date date_of_finish;


              @JsonIgnore @ManyToOne(fetch = FetchType.EAGER)                   public Model_Board board; // Deska k aktualizaci
              @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)  public Firmware_type firmware_type;

                                                                                // Aktualizace je vázána buď na verzi C++ kodu nebo na soubor, nahraný uživatelem
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.EAGER)                 public Model_VersionObject c_program_version_for_update; // C_program k aktualizaci
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                  public Model_BootLoader bootloader;   // Když nahrávám Firmware
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                  public Model_FileRecord binary_file;  // Soubor, když firmware nahrává uživatel sám mimo flow

    @ApiModelProperty(required = true, value = "Description on Model C_ProgramUpdater_State")  @Enumerated(EnumType.STRING)    public C_ProgramUpdater_State state;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = false, value = "Is visible only if user send compilation under C_program in system  ( OR state for binary_file)")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @Transient
    public C_Program_Update_program c_program_detail(){

        if(c_program_version_for_update == null ) return null;

            C_Program_Update_program c_program_detail   = new  C_Program_Update_program();
            c_program_detail.c_program_id               = c_program_version_for_update.c_program.id;
            c_program_detail.c_program_program_name     = c_program_version_for_update.c_program.name;
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
    public Model_FileRecord binary_file_detail(){
        return binary_file == null ? null : binary_file;
    }



/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        this.state = C_ProgramUpdater_State.not_start_yet;
        this.date_of_create = new Date();

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_CProgramUpdatePlan.find.byId(this.id) == null) break;
        }
        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

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

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_CProgramUpdatePlan> find = new Model.Finder<>(Model_CProgramUpdatePlan.class);

}


