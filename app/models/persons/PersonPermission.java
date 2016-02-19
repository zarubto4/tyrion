
package models.persons;

import be.objectify.deadbolt.core.models.Permission;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PersonPermission extends Model implements Permission {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                                public String value;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "permissions")  @JoinTable(name = "join_prs_prm") public List<Person> persons = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "permissions")  @JoinTable(name = "join_group_prm") public List<SecurityRole> roles = new ArrayList<>();


    public String getValue() {
        return value;
    }

    public static PersonPermission findByValue(String vale){
        return PersonPermission.find.where().eq("value",vale).findUnique();
    }


    // Finder **********************************************************************************************************
    public static final Finder<String, PersonPermission> find = new Finder<>( PersonPermission.class);
}
