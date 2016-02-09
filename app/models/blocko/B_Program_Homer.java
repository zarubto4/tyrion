package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class B_Program_Homer extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;

                                     @JsonIgnore @ManyToOne     public B_Program b_program;
                                                                public String b_program_version;

                                     @JsonIgnore @ManyToOne     public Homer homer;
                                                                public String state;


}
