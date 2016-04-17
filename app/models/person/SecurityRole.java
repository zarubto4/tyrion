
package models.person;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class SecurityRole extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;
                                                            public String name;
                                                            public String description;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "roles")  @JoinTable(name = "person_roles") public List<Person> persons = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL) public List<PersonPermission>  permissions    = new ArrayList<>();



/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty public List<String> persons_id(){  List<String> l = new ArrayList<>();  for( Person m  : persons)   l.add(m.id); return l;  }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty public Boolean create_permission(){  return  SecurityController.getPerson() == null ? false : SecurityController.getPerson().has_permission("SecurityRole_create");}
    @JsonProperty public Boolean read_permission()  {  return  SecurityController.getPerson() == null ? false : SecurityController.getPerson().has_permission("SecurityRole_read"); }
    @JsonProperty public Boolean edit_permission()  {  return  SecurityController.getPerson() == null ? false : SecurityController.getPerson().has_permission("SecurityRole_edit"); }
    @JsonProperty public Boolean delete_permission(){  return  SecurityController.getPerson() == null ? false : SecurityController.getPerson().has_permission("SecurityRole_delete");}

    public enum permissions{SecurityRole_create, SecurityRole_read, SecurityRole_edit , SecurityRole_delete}


/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static SecurityRole findByName(String name) {return find.where().eq("name", name).findUnique();}
    public static final Finder<String, SecurityRole> find = new Finder<>(SecurityRole.class);
}
