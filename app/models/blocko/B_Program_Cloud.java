package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;

@Entity
public class B_Program_Cloud extends Model {

        @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                        @JsonIgnore @ManyToOne      public B_Program b_program;

                                        @JsonIgnore                 public String blocko_server_name;
                                        @JsonIgnore                 public String blocko_instance_name;

                                                                    public Date     running_from;
                                                                    public boolean  runing;
                                                                    public String   state_of_progam;


}
