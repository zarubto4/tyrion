package models.project.b_program.instnace;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.WebSocketController;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Board;
import models.project.b_program.B_Program;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.b_program.servers.Private_Homer_Server;
import utilities.swagger.outboundClass.Swagger_B_Program_Instance;
import utilities.webSocket.WebSCType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Homer_Instance extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

        @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;

                                         @JsonIgnore @ManyToOne     public Cloud_Homer_Server cloud_homer_server;
                                         @JsonIgnore @OneToOne      public Private_Homer_Server private_server; // Nevyužívané

                                                    @JsonIgnore     public String blocko_instance_name;

    @JsonIgnore @OneToOne(mappedBy="instance", fetch = FetchType.LAZY)                  public B_Program b_program; // BLocko program ke kterému se Homer Instance váže

    @JsonIgnore @OneToOne(mappedBy="actual_running_instance", cascade=CascadeType.ALL)  public Homer_Instance_Record actual_instance; // Aktuálně běžící instnace na Serveri

                @OneToMany(mappedBy="main_instance_history", cascade=CascadeType.ALL) @OrderBy("id ASC") public List<Homer_Instance_Record> instance_history = new ArrayList<>(); // Setříděné pořadí různě nasazovaných verzí Blocko programu



    // Pomocný objekt pro "Fiktivní instnaci pro připojenej device" - TODO asi to odstraníme
    @JsonIgnore @OneToOne(mappedBy="private_instance",  cascade = CascadeType.MERGE, fetch = FetchType.LAZY)   public Board private_instance_board;




/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/


    @Transient @JsonProperty @ApiModelProperty(required = true) public  String b_program_id()             {  return b_program.id;}
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String b_program_name()           {  return b_program.name;}


    @Transient @JsonProperty @ApiModelProperty(required = true) public  String server_name()             {  return cloud_homer_server.server_name;}
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String server_id()               {  return cloud_homer_server.id;}





    @Transient @JsonProperty @ApiModelProperty(required = false, value = "Only if instance is upload in Homer") public Swagger_B_Program_Instance actual_summary() {


        Swagger_B_Program_Instance instance = new Swagger_B_Program_Instance();

        instance.instance_record_id = actual_instance.id;
        instance.date_of_created    = actual_instance.date_of_created;
        instance.running_from       = actual_instance.running_from;
        instance.running_to         = actual_instance.running_to;
        instance.planed_when        = actual_instance.planed_when;


        instance.b_program_id           = actual_instance.version_object.b_program.id;
        instance.b_program_name         = actual_instance.version_object.b_program.name;
        instance.b_program_version_name = actual_instance.b_program_version_name();
        instance.b_program_version_id   = actual_instance.b_program_version_id();

        instance.server_is_online       = cloud_homer_server.server_is_online();
        instance.server_name            = cloud_homer_server.server_name;
        instance.server_id              = cloud_homer_server.id;

        instance.hardware_group                 = actual_instance.version_object.b_program_hw_groups;
        instance.m_project_program_snapshots    = actual_instance.version_object.m_project_program_snapShots;

        return instance;
    }





/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public boolean instance_online(){ return WebSocketController.homer__instance_online_state(blocko_instance_name);}

    @JsonIgnore @Transient
    private void setUnique_blocko_instance_name() {

            while(true){ // I need Unique Value
                this.blocko_instance_name = UUID.randomUUID().toString();
                if (Homer_Instance.find.where().eq("blocko_instance_name", blocko_instance_name ).findUnique() == null) break;
            }
    }


    @Override
    public void save(){
        this.setUnique_blocko_instance_name();
        super.save();
    }


    @Override
    public void delete(){

        if(cloud_homer_server.server_is_online()){
            try {

                WebSocketController.homer_server_remove_instance( cloud_homer_server.get_server_webSocket_connection() ,blocko_instance_name);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.delete();
    }


    @JsonIgnore @Transient
    public WebSCType get_instance(){
        return WebSocketController.incomingConnections_homers.get(blocko_instance_name);
    }


/* ENUMS PARAMETERS ----------------------------------------------------------------------------------------------------*/



/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Homer_Instance> find = new Finder<>(Homer_Instance.class);

}
