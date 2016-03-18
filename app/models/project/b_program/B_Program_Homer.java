package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;
import models.project.global.Homer;

import javax.persistence.*;
import java.util.Date;

@Entity
public class B_Program_Homer extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                @OneToOne @JoinColumn(name="vrs_obj_id")        public Version_Object version_object;

                @OneToOne @JoinColumn(name="BProgramHomer_id")  public Homer homer;
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1458315085338") public Date running_from;


    @JsonProperty public boolean homer_online()  { return homer.online();}

 //********************************************************************************************************************

    public static Finder<String,B_Program_Homer> find = new Finder<>(B_Program_Homer.class);


}
