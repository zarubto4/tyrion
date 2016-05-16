package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import models.blocko.Cloud_Blocko_Server;
import models.compiler.Version_Object;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class B_Program_Cloud extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

        @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;

                                          @JsonIgnore @ManyToOne    public Cloud_Blocko_Server server;
                                                     @JsonIgnore    public String blocko_instance_name;
          @JsonIgnore @OneToOne   @JoinColumn(name="vrs_obj_id")    public Version_Object version_object;

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1461854312") public Date     running_from;


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void setUnique_blocko_instance_name() {

            while(true){ // I need Unique Value
                this.blocko_instance_name = UUID.randomUUID().toString();
                if (B_Program_Cloud.find.where().eq("blocko_instance_name", blocko_instance_name ).findUnique() == null) break;
            }
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, B_Program_Cloud> find = new Finder<>(B_Program_Cloud.class);

}
