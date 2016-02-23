package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.WebSocketController_OutComing;
import models.compiler.Version_Object;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class B_Program_Cloud extends Model {

        @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
              @JsonIgnore  @OneToOne(mappedBy="b_program_cloud")    public B_Program b_program;

                                                     @JsonIgnore    public String blocko_server_name;
                                                     @JsonIgnore    public String blocko_instance_name;
          @JsonIgnore @OneToOne   @JoinColumn(name="vrs_obj_id")    public Version_Object version_object;

                                                                    public Date     running_from;
                                                                    public String   state_of_progam;

    @JsonProperty public boolean state()       { return  WebSocketController_OutComing.blockoServer_is_Instance_Running(blocko_server_name , blocko_instance_name ); }



    public void setUnique_blocko_instance_name() {

            while(true){ // I need Unique Value
                this.blocko_instance_name = UUID.randomUUID().toString();
                if (B_Program_Cloud.find.where().eq("blocko_instance_name", blocko_instance_name ).findUnique() == null) break;
            }

    }
    public static Finder<String, B_Program_Cloud> find = new Finder<>(B_Program_Cloud.class);
}
