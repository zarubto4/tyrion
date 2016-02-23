package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.compiler.Version_Object;
import models.project.global.Homer;

import javax.persistence.*;
import java.util.Date;

@Entity
public class B_Program_Homer extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                    @JsonIgnore  @ManyToOne     public Version_Object version_object;

    @JsonIgnore @OneToOne @JoinColumn(name="BProgramHomer_id")  public Homer homer;
                                                                public Date running_from;
                                                                public String state_of_progam;


    @JsonProperty public String state()       { return  "Nedoplněný stav - TODO"; }


}
