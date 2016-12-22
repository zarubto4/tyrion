package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import models.compiler.Model_Board;
import models.compiler.Model_VersionObject;

import javax.persistence.*;
import java.util.UUID;


@Entity
@ApiModel(description = "Model of BPair",
        value = "BPair")
public class Model_BPair extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                          @JsonIgnore @Id public String id;

    @JsonIgnore @ManyToOne()                              public Model_VersionObject c_program_version;      // Týká se aktualizace C_Programu na HW.
    @JsonIgnore @ManyToOne()                              public Model_Board board;

    // B_Program - Skupiny HW pod Yodou
                                                                  @JsonIgnore @ManyToOne()  public Model_BProgramHwGroup device_board_pair;  // Devices
    @JsonIgnore @OneToOne(cascade=CascadeType.ALL)  @JoinColumn(name="main_board_pair_id")  public Model_BProgramHwGroup main_board_pair;    // Master Boards - třeba Yoda

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty   @Transient public String c_program_version_id()     { return c_program_version == null ? null : c_program_version.id;}
    @JsonProperty   @Transient public String c_program_version_name()   { return c_program_version == null ? null : c_program_version.version_name;}


    @JsonProperty   @Transient public String c_program_name()           { return c_program_version.c_program.name;}
    @JsonProperty   @Transient public String c_program_description()    { return c_program_version.c_program.description;}

    @JsonProperty   @Transient public String type_of_board_id()         { return board.type_of_board_name();}
    @JsonProperty   @Transient public String type_of_board_name()       { return board.type_of_board_id();  }

    @JsonProperty   @Transient public String board_id()                     { return board.id;}
    @JsonProperty   @Transient public String board_personal_description()   { return board.personal_description;}

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_BPair.find.byId(this.id) == null) break;
        }
        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Model_BPair> find = new Finder<>(Model_BPair.class);

}
