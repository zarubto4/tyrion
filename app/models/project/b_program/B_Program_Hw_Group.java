package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class B_Program_Hw_Group extends Model {

   @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true)  public Long  id;

    @OneToMany(mappedBy="device_board_pair",cascade= CascadeType.ALL)   public List<B_Pair> device_board_pairs = new ArrayList<>();
    @OneToOne(mappedBy="main_board_pair",cascade=CascadeType.ALL)       public B_Pair main_board_pair;



    @JsonIgnore  @ManyToOne() public Version_Object b_program_version_group;
}
