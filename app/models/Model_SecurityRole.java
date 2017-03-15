
package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@ApiModel(description = "Model of SecurityRole",
        value = "SecurityRole")
public class Model_SecurityRole extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
    @Id @ApiModelProperty(required = true) public String id;
        @ApiModelProperty(required = true) public String name;
        @ApiModelProperty(required = true) public String description;

    @JsonIgnore @ManyToMany(mappedBy = "roles")  @JoinTable(name = "person_roles") public List<Model_Person> persons = new ArrayList<>();
    @JsonIgnore @ManyToMany() public List<Model_Permission> person_permissions = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> persons_id()           {  List<String> l = new ArrayList<>();  for( Model_Person m  : persons)   l.add(m.id); return l;  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String> person_permissions_id(){  List<String> l = new ArrayList<>();  for( Model_Permission m   : person_permissions)   l.add(m.value); return l;  }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_SecurityRole.find.byId(this.id) == null) break;
        }
        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return  Controller_Security.getPerson().has_permission("SecurityRole_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return  Controller_Security.getPerson().has_permission("SecurityRole_read"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission(){  return  Controller_Security.getPerson().has_permission("SecurityRole_update"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return  Controller_Security.getPerson().has_permission("SecurityRole_delete");}

    public enum permissions{SecurityRole_create, SecurityRole_read, SecurityRole_update , SecurityRole_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model_SecurityRole findByName(String name) {return find.where().eq("name", name).findUnique();}
    public static final Model.Finder<String, Model_SecurityRole> find = new Finder<>(Model_SecurityRole.class);
}
