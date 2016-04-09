
package models.persons;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PersonPermission extends Model {

    @Id      public String value;
             public String description;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "permissions")  @JoinTable(name = "join_prs_prm") public List<Person> persons = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "permissions")  @JoinTable(name = "join_group_prm") public List<SecurityRole> roles = new ArrayList<>();


    public String getValue() {
        return value;
    }

    public static PersonPermission findByValue(String vale){
        return PersonPermission.find.byId(vale);
    }


    // Creating new permission if system not contains that
    public PersonPermission(String key, String description){

        if(PersonPermission.find.byId(key) != null) return;
        this.value = key;
        this.description = description;
        this.save();
    }




    // Finder **********************************************************************************************************
    public static final Finder<String, PersonPermission> find = new Finder<>( PersonPermission.class);



}
