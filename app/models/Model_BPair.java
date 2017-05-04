package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.UUID;


@Entity
@ApiModel(description = "Model of BPair", value = "BPair")
public class Model_BPair extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_BPair.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                          @JsonIgnore @Id public UUID id;

    @JsonIgnore @ManyToOne()                              public Model_VersionObject c_program_version;      // Týká se aktualizace C_Programu na HW.
    @JsonIgnore @ManyToOne()                              public Model_Board board;

    // B_Program - Skupiny HW pod Yodou
                                                                  @JsonIgnore @ManyToOne()  public Model_BProgramHwGroup device_board_pair;  // Devices
    @JsonIgnore @OneToOne(cascade=CascadeType.ALL)  @JoinColumn(name="main_board_pair_id")  public Model_BProgramHwGroup main_board_pair;    // Master Boards - for example Yoda

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty   @Transient public String c_program_version_id()          { return c_program_version == null ? null : c_program_version.id;}
    @JsonProperty   @Transient public String c_program_version_name()        { return c_program_version == null ? null : c_program_version.version_name;}
    @JsonProperty   @Transient public String c_program_version_description() { return c_program_version == null ? null : c_program_version.version_description;}

    @JsonProperty   @Transient public String c_program_id()                  { return c_program_version.c_program == null ? null : c_program_version.c_program.id;}
    @JsonProperty   @Transient public String c_program_name()                { return c_program_version.c_program == null ? null : c_program_version.c_program.name;}
    @JsonProperty   @Transient public String c_program_description()         { return c_program_version.c_program == null ? null : c_program_version.c_program.description;}

    @JsonProperty   @Transient public String type_of_board_id()              { return board.type_of_board_id();}
    @JsonProperty   @Transient public String type_of_board_name()            { return board.type_of_board_name();  }

    @JsonProperty   @Transient public String board_id()                      { return board.id;}
    @JsonProperty   @Transient public String board_personal_description()    { return board.personal_description;}

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/
    @JsonIgnore @Override
    public void save() {
        super.save();
    }

    @JsonIgnore @Override public void update() {

        super.update();
    }

    @JsonIgnore @Override public void delete() {

        super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/
/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/
/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/
/* PERMISSION Description ----------------------------------------------------------------------------------------------*/
/* PERMISSION ----------------------------------------------------------------------------------------------------------*/
/* CACHE ---------------------------------------------------------------------------------------------------------------*/
/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Model_BPair> find = new Finder<>(Model_BPair.class);

}
