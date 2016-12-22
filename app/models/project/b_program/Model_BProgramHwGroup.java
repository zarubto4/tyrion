package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Model_VersionObject;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Hardware_group") // POZOR - Je zde záměrně sjednocen objekt s dokumentační třídou pro swagger Hardware_group.class
public class Model_BProgramHwGroup extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true)  public Long  id;

    @OneToMany(mappedBy="device_board_pair",cascade= CascadeType.ALL, fetch = FetchType.EAGER)   public List<Model_BPair> device_board_pairs = new ArrayList<>();
    @OneToOne(mappedBy="main_board_pair",cascade=CascadeType.ALL, fetch = FetchType.EAGER)       public Model_BPair main_board_pair;



    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "b_program_hw_groups")  @JoinTable(name = "version_b_group_id") public List<Model_VersionObject> b_program_version_groups = new ArrayList<>();


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_BProgramHwGroup> find = new Finder<>(Model_BProgramHwGroup.class);

}
