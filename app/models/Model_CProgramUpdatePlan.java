package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_CProgram_updater_state;
import utilities.enums.Enum_Firmware_type;
import utilities.swagger.outboundClass.Swagger_C_Program_Update_plan_Short_Detail;

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

    @ApiModelProperty(required = true, value = "UNIX time in ms", example = "1466163478925")   public Date date_of_create;
    @ApiModelProperty(required = true, value = "UNIX time in ms", example = "1466163478925")   public Date date_of_finish;


              @JsonIgnore @ManyToOne(fetch = FetchType.EAGER)                   public Model_Board board;                           // Deska k aktualizaci
              @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)  public Enum_Firmware_type firmware_type;                 // Typ Firmwaru

                                                                                // Aktualizace je vázána buď na verzi C++ kodu nebo na soubor, nahraný uživatelem
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.EAGER)                 public Model_VersionObject c_program_version_for_update; // C_program k aktualizaci
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                  public Model_BootLoader bootloader;                      // Když nahrávám Firmware
    /** OR **/  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                  public Model_FileRecord binary_file;                     // Soubor, když firmware nahrává uživatel sám mimo flow

    @ApiModelProperty(required = true, value = "Description on Model C_ProgramUpdater_State")  @Enumerated(EnumType.STRING)    public Enum_CProgram_updater_state state;

    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty( value = "Only if state is critical_error or Homer record some error", required = false)  public String error;
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty( value = "Only if state is critical_error or Homer record some error", required = false)  public Integer errorCode;
/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


    @JsonProperty @Transient
    public Date date_of_planing() { return actualization_procedure.date_of_planing;}

    @ApiModelProperty(required = false, value = "Is visible only if update is for Firmware or Backup")
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

    @ApiModelProperty(required = false, value = "Is visible only if update is for Bootloader")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @Transient
    public Bootloader_Update_program bootloader_detail(){

        if(bootloader == null ) return null;

        Bootloader_Update_program bootloader_update_detail  = new  Bootloader_Update_program();
        bootloader_update_detail.bootloader_id                      = bootloader.id;
        bootloader_update_detail.bootloader_name                    = bootloader.name;
        bootloader_update_detail.version_identificator   = bootloader.version_identificator;

        return bootloader_update_detail;
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


    @JsonIgnore
    public Swagger_C_Program_Update_plan_Short_Detail get_short_version_for_board(){

        Swagger_C_Program_Update_plan_Short_Detail detail = new Swagger_C_Program_Update_plan_Short_Detail();
        detail.id = this.id;
        detail.date_of_create = date_of_create;
        detail.date_of_finish = date_of_finish;
        detail.firmware_type = firmware_type;
        detail.state = state;

        if(detail.firmware_type == Enum_Firmware_type.FIRMWARE || detail.firmware_type == Enum_Firmware_type.BACKUP){
            detail.c_program_id               = c_program_version_for_update.c_program.id;
            detail.c_program_program_name     = c_program_version_for_update.c_program.name;
            detail.c_program_version_id       = c_program_version_for_update.id;
            detail.c_program_version_name     = c_program_version_for_update.version_name;
        }

        if(detail.firmware_type == Enum_Firmware_type.BOOTLOADER ){
            detail.bootloader_id           = bootloader.id;
            detail.bootloader_name         = bootloader.name;
            detail.version_identificator   = bootloader.version_identificator;
        }

        return detail;
    }

    @JsonIgnore @Override
    public void save() {

        if(this.state == null) this.state = Enum_CProgram_updater_state.not_start_yet;
        this.date_of_create = new Date();

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_CProgramUpdatePlan.find.byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override
    public void update() {

        super.update();
        actualization_procedure.update_state();

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    class C_Program_Update_program{
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_id;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_version_id;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_program_name;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String c_program_version_name;
    }

    class Bootloader_Update_program{
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String bootloader_id;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String bootloader_name;
        @ApiModelProperty(required = true, value = "Can be empty", readOnly = true) public String version_identificator;

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


