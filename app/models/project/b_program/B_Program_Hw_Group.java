package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@ApiModel(value = "Hardware_group") // POZOR - Je zde záměrně sjednocen objekt s dokumentační třídou pro swagger Hardware_group.class
public class B_Program_Hw_Group extends Model {

    @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true)  public Long  id;

    @OneToMany(mappedBy="device_board_pair",cascade= CascadeType.ALL)   public List<B_Pair> device_board_pairs = new ArrayList<>();
    @OneToOne(mappedBy="main_board_pair",cascade=CascadeType.ALL)       public B_Pair main_board_pair;



    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "b_program_hw_groups")  @JoinTable(name = "version_b_group_id") public List<Version_Object> b_program_version_groups = new ArrayList<>();


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/



/* JSON IGNORE METHOD --------------------------------------------------------------------------------------------------*/



/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, B_Program_Hw_Group> find = new Finder<>(B_Program_Hw_Group.class);

}
