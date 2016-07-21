
package models.person;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@ApiModel(value = "Person permission static key object with description")
public class PersonPermission extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(value = "Permission key - \"(static key)\"", required = true, readOnly = true)
    @Id      public String value;

    @ApiModelProperty(value = "Description for \"(static key)\"", required = true, readOnly = true)
             public String description;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "person_permissions")  @JoinTable(name = "join_prs_prm")   public List<Person>       persons = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "person_permissions")  @JoinTable(name = "join_group_prm") public List<SecurityRole> roles   = new ArrayList<>();

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


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs         = "read: If user have M_Project.read_permission = true, you can create M_program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs       = "create: If user have M_Project.update_permission = true, you can create M_Program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String read_qrToken_permission_docs = "read: Private settings for M_Program";

    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean edit_person_permission() {  return  SecurityController.getPerson() == null ? null :  SecurityController.getPerson().has_permission("PersonPermission_edit_person_permission");  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean edit_permission()        {  return  SecurityController.getPerson() == null ? null :  SecurityController.getPerson().has_permission("PersonPermission_edit"); }

    public enum permissions{ PersonPermission_edit_person_permission, PersonPermission_edit }


/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static final Finder<String, PersonPermission> find = new Finder<>( PersonPermission.class);

}
