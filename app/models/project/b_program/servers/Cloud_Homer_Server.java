package models.project.b_program.servers;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import controllers.WebSocketController_Incoming;
import models.compiler.Board;
import models.project.b_program.Homer_Instance;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Cloud_Homer_Server extends Model{

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                                public String server_name;
                                                                public String hash_certificate;
                                                                public String destination_address;

    @JsonIgnore @OneToMany(mappedBy="cloud_homer_server", cascade = CascadeType.ALL) public List<Homer_Instance> cloud_instances  = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy="latest_know_server") public List<Board>  boards  = new ArrayList<>();

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/


    @JsonProperty  public boolean server_is_online(){
        return WebSocketController_Incoming.blocko_servers.containsKey(this.server_name);
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void set_hash_certificate(){

        while(true){ // I need Unique Value
            hash_certificate = UUID.randomUUID().toString();
            if (Cloud_Homer_Server.find.where().eq("hash_certificate",hash_certificate).findUnique() == null) break;
        }
    }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: User (Admin with privileges) can read public servers, User (Customer) can read own private servers";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: User (Admin with privileges) can create public cloud cloud_blocko_server where the system uniformly creating Blocko instantiates or (Customer) can create private cloud_blocko_server for own projects";

    @JsonIgnore   @Transient public boolean create_permission()  {  return SecurityController.getPerson().has_permission("Cloud_Homer_Server_create");  }
    @JsonIgnore   @Transient public boolean read_permission()    {  return SecurityController.getPerson().has_permission("Cloud_Homer_Server_read");    }
    @JsonProperty @Transient public boolean edit_permission()    {  return SecurityController.getPerson().has_permission("Cloud_Homer_Server_edit");    }
    @JsonProperty @Transient public boolean delete_permission()  {  return SecurityController.getPerson().has_permission("Cloud_Homer_Server_delete");  }

    public enum permissions{Cloud_Homer_Server_create, Cloud_Homer_Server_read, Cloud_Homer_Server_edit, Cloud_Homer_Server_delete}


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Cloud_Homer_Server> find = new Model.Finder<>(Cloud_Homer_Server.class);


}
