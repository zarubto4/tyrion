package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import controllers.WebSocketController_Incoming;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class Cloud_Compilation_Server extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  @ApiModelProperty(required = true)     public String id;
                                        @Column(unique=true) @ApiModelProperty(required = true)     public String server_name;
                                        @Column(unique=true) @JsonIgnore                            public String unique_identificator;
                                                             @JsonIgnore                            public String hash_certificate;
                                        @Column(unique=true) @ApiModelProperty(required = true)     public String destination_address;


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) public boolean server_is_online(){
        return WebSocketController_Incoming.compiler_cloud_servers.containsKey(this.server_name);
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void set_hash_certificate(){

        while(true){ // I need Unique Value
            hash_certificate = UUID.randomUUID().toString();
            if (Cloud_Compilation_Server.find.where().eq("hash_certificate",hash_certificate).findUnique() == null) break;
        }

        while(true){ // I need Unique Value
            unique_identificator = UUID. randomUUID().toString().substring(0,6);
            if (Cloud_Compilation_Server.find.where().eq("unique_identificator",unique_identificator).findUnique() == null) break;
        }
    }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


    @JsonIgnore   @Transient                                    public boolean create_permission(){  return SecurityController.getPerson().has_permission("Cloud_Compilation_Server_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return SecurityController.getPerson().has_permission("Cloud_Compilation_Server_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return SecurityController.getPerson().has_permission("Cloud_Compilation_Server_delete"); }

    public enum permissions{Cloud_Compilation_Server_create, Cloud_Compilation_Server_edit, Cloud_Compilation_Server_delete}


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Cloud_Compilation_Server> find = new Model.Finder<>(Cloud_Compilation_Server.class);


}
