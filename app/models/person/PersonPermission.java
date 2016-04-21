
package models.person;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PersonPermission extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id      public String value;
             public String description;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "person_permissions")  @JoinTable(name = "join_prs_prm") public List<Person> persons = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "permissions")  @JoinTable(name = "join_group_prm") public List<SecurityRole> roles = new ArrayList<>();

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    // Creating new permission if system not contains that
    @JsonIgnore
    public PersonPermission(String key, String description){
        if(PersonPermission.find.byId(key) != null) return;
        this.value = key;
        this.description = description;
        this.save();
    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static final Finder<String, PersonPermission> find = new Finder<>( PersonPermission.class);

}
