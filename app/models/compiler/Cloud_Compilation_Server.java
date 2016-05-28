package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import controllers.WebSocketController_Incoming;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class Cloud_Compilation_Server extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                            public String server_name;
                                                            public String hash_certificate;
                                                            public String destination_address;


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty  public boolean server_is_online(){
        return WebSocketController_Incoming.compiler_cloud_servers.containsKey(this.server_name);
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void set_hash_certificate(){

        while(true){ // I need Unique Value
            hash_certificate = UUID.randomUUID().toString();
            if (Cloud_Compilation_Server.find.where().eq("hash_certificate",hash_certificate).findUnique() == null) break;
        }
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


    @JsonIgnore   @Transient public Boolean create_permission(){  return SecurityController.getPerson().has_permission("Cloud_Compilation_Server_create"); }
    @JsonIgnore   @Transient public Boolean read_permission()  {  return true; }
    @JsonProperty @Transient public Boolean edit_permission()  {  return SecurityController.getPerson().has_permission("Cloud_Compilation_Server_edit");   }
    @JsonProperty @Transient public Boolean delete_permission(){  return SecurityController.getPerson().has_permission("Cloud_Compilation_Server_delete"); }

    public enum permissions{Cloud_Compilation_Server_create, Cloud_Compilation_Server_edit, Cloud_Compilation_Server_delete}


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Cloud_Compilation_Server> find = new Model.Finder<>(Cloud_Compilation_Server.class);


}
