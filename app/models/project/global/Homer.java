package models.project.global;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import models.project.b_program.B_Program_Homer;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class Homer extends Model {

/* DATABASE VALUES ------------------------------------------------------------------------------------------------------ */
        @Id         public String homer_id;
                    public String type_of_device;
                    public String  version;

    @JsonIgnore @ManyToOne  public Project project;

    @JsonIgnore  @OneToOne(mappedBy="homer")
    B_Program_Homer b_program_homer;

/* FINDER & WEBSOCKET --------------------------------------------------------------------------------------------------------*/
        public static Finder<String,Homer> find = new Finder<>(Homer.class);

/* METHODS ----------------------------------------------------------------------------------------------------------------*/


}



