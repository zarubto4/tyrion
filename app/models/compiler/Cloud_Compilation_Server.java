package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

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


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void set_hash_certificate(){

        while(true){ // I need Unique Value
            hash_certificate = UUID.randomUUID().toString();
            if (Cloud_Compilation_Server.find.where().eq("hash_certificate",hash_certificate).findUnique() == null) break;
        }
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Cloud_Compilation_Server> find = new Model.Finder<>(Cloud_Compilation_Server.class);


}
