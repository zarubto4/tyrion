
package models.person;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class SecurityRole extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true) public String id;
                                                            @ApiModelProperty(required = true) public String name;
                                                            @ApiModelProperty(required = true) public String description;

    @JsonIgnore @ManyToMany(mappedBy = "roles")  @JoinTable(name = "person_roles") public List<Person> persons = new ArrayList<>();
    @JsonIgnore @ManyToMany() public List<PersonPermission> person_permissions = new ArrayList<>();



/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> persons_id()           {  List<String> l = new ArrayList<>();  for( Person m  : persons)   l.add(m.id); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> person_permissions_id(){  List<String> l = new ArrayList<>();  for( PersonPermission m   : person_permissions)   l.add(m.value); return l;  }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return  SecurityController.getPerson().has_permission("SecurityRole_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return  SecurityController.getPerson().has_permission("SecurityRole_read"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission(){  return  SecurityController.getPerson().has_permission("SecurityRole_update"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return  SecurityController.getPerson().has_permission("SecurityRole_delete");}

    public enum permissions{SecurityRole_create, SecurityRole_read, SecurityRole_update , SecurityRole_delete}


/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static SecurityRole findByName(String name) {return find.where().eq("name", name).findUnique();}
    public static final Model.Finder<String, SecurityRole> find = new Finder<>(SecurityRole.class);
}
