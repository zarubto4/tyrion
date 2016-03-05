
package models.persons;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import utilities.Server;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class SecurityRole extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;
                                                            public String name;
                                                            public String description;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "roles")  @JoinTable(name = "person_roles") public List<Person> persons = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL) public List<PersonPermission>  permissions    = new ArrayList<>();



    public String persons(){  return Server.serverAddress + "/secure/role/person/"+ id; }



    public String getName()
    {
        return name;
    }
    public static SecurityRole findByName(String name) {return find.where().eq("name", name).findUnique();}

    public static final Finder<String, SecurityRole> find = new Finder<>(SecurityRole.class);
}
