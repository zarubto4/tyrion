package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.WebSocketController;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Board;
import models.compiler.Version_Object;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.b_program.servers.Private_Homer_Server;
import models.project.global.Project;
import utilities.webSocket.WebSCType;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class Homer_Instance extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

        @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;

                                         @JsonIgnore @ManyToOne     public Cloud_Homer_Server cloud_homer_server;
                                         @JsonIgnore @OneToOne      public Private_Homer_Server private_server;

                                                    @JsonIgnore     public String blocko_instance_name;
        @JsonIgnore @OneToOne   @JoinColumn(name="vrs_obj_id")      public Version_Object version_object;
                                         @JsonIgnore @ManyToOne()   public Project project;


    @ApiModelProperty(required = true, dataType = "integer", readOnly = true,  value = "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970", example = "1466163478925")         public Date running_from;



    @OneToOne(mappedBy="private_instance",  cascade = CascadeType.MERGE, fetch = FetchType.LAZY)   public Board private_instance_board;

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void setUnique_blocko_instance_name() {

            while(true){ // I need Unique Value
                this.blocko_instance_name = UUID.randomUUID().toString();
                if (Homer_Instance.find.where().eq("blocko_instance_name", blocko_instance_name ).findUnique() == null) break;
            }
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
    public boolean is_online(){
       return cloud_homer_server.server_is_online() && WebSocketController.homer__instance_online_state(blocko_instance_name);
    }

    @JsonIgnore @Transient
    public WebSCType get_instance(){
        return WebSocketController.incomingConnections_homers.get(blocko_instance_name);
    }


/* ENUMS PARAMETERS ----------------------------------------------------------------------------------------------------*/

    /**
     Definuje typy commandů - které se dají přes tyrion zaslat do Homer serveru na konkrétní instanci.
     Homer server má terminálové příkazy pro vývojáře HW. Tak aby si mohli zkoušet svůj HW. Jelikož vzniklo grafické
     rozhraní pod tyrionem, je nutné ty samé příkazy co chodí z konzole zasílat i z tyriona. A aby jako v Homer serveru
     nebylo třeba mít pro každý příkaz metodu - zasílá se to definovanými Enum objekty,. (Na Homer serveru se udržuje
     identická struktura)
    */


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Homer_Instance> find = new Finder<>(Homer_Instance.class);

}
