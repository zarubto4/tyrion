package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Hardware_group") // POZOR - Je zde záměrně sjednocen objekt s dokumentační třídou pro swagger Hardware_group.class
@Table(name="BProgramHwGroup")
public class Model_BProgramHwGroup extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_BProgramHwGroup.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Id public UUID id;

    @OneToMany(mappedBy="device_board_pair",cascade= CascadeType.ALL, fetch = FetchType.EAGER)   public List<Model_BPair> device_board_pairs = new ArrayList<>();
    @OneToOne(mappedBy="main_board_pair",cascade=CascadeType.ALL, fetch = FetchType.EAGER)       public Model_BPair main_board_pair;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "b_program_hw_groups")  @JoinTable(name = "version_b_group_id") public List<Model_VersionObject> b_program_version_groups = new ArrayList<>();


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public boolean contains_HW(String board_id) {
        try {

            // Složený SQL dotaz pro nalezení funkční běžící instance (B_Pair)

            for(Model_BPair model_bPair : device_board_pairs){

                if(model_bPair.board.id.equals(board_id)) return true;
            }

            return false;

        } catch (Exception e) {
            terminal_logger.internalServerError("contains_HW:", e);
            return false;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        super.save();
    }

    @JsonIgnore @Override public void update() {

        super.update();
    }


    @JsonIgnore @Override public void delete() {
        terminal_logger.internalServerError(new Exception("This object is not legitimate to remove."));
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_BProgramHwGroup> find = new Finder<>(Model_BProgramHwGroup.class);

}
