package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import models.project.b_program.B_Program_Cloud;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Cloud_Blocko_Server extends Model{

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                                public String server_name;
                                                                public String hash_certificate;
                                                                public String destination_address;

    @JsonIgnore @OneToMany(mappedBy="server", cascade = CascadeType.ALL) public List<B_Program_Cloud> cloud_programs  = new ArrayList<>();

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void set_hash_certificate(){

        while(true){ // I need Unique Value
            hash_certificate = UUID.randomUUID().toString();
            if (Cloud_Blocko_Server.find.where().eq("hash_certificate",hash_certificate).findUnique() == null) break;
        }
    }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: User (Admin with privileges) can read public servers, User (Customer) can read own private servers";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: User (Admin with privileges) can create public cloud server where the system uniformly creating Blocko instantiates or (Customer) can create private server for own projects";

    @JsonIgnore   public Boolean create_permission()  {  return SecurityController.getPerson().has_permission("Cloud_Blocko_Server_create");  }
    @JsonIgnore   public Boolean read_permission()    {  return SecurityController.getPerson().has_permission("Cloud_Blocko_Server_read");    }
    @JsonProperty public Boolean edit_permission()    {  return SecurityController.getPerson().has_permission("Cloud_Blocko_Server_edit");    }
    @JsonProperty public Boolean delete_permission()  {  return SecurityController.getPerson().has_permission("Cloud_Blocko_Server_delete");  }

    public enum permissions{Cloud_Blocko_Server_create, Cloud_Blocko_Server_read, Cloud_Blocko_Server_edit, Cloud_Blocko_Server_delete}


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Cloud_Blocko_Server> find = new Model.Finder<>(Cloud_Blocko_Server.class);


}
