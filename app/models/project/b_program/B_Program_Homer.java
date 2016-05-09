package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;

import javax.persistence.*;
import java.util.Date;

@Entity
public class B_Program_Homer extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                @OneToOne @JoinColumn(name="vrs_obj_id")        public Version_Object version_object;

                @OneToOne @JoinColumn(name="BProgramHomer_id")  public Homer homer;

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1461854312") public Date running_from;
    @JsonProperty @Transient public boolean homer_online()  { return homer.online();}


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/



/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,B_Program_Homer> find = new Finder<>(B_Program_Homer.class);


}
