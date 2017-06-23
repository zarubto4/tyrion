
package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@ApiModel( value = "Permission", description = "Model of Permission")
public class Model_Permission extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

private static final Class_Logger terminal_logger = new Class_Logger(Model_Permission.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(value = "Permission key - \"(static key)\"", required = true, readOnly = true)
    @Id      public String value;

    @ApiModelProperty(value = "Description for \"(static key)\"", required = true, readOnly = true)
             public String description;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "person_permissions")  @JoinTable(name = "join_prs_prm")   public List<Model_Person>       persons = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "person_permissions")  @JoinTable(name = "join_group_prm") public List<Model_SecurityRole> roles   = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    // Creating new permission if system not contains that
    @JsonIgnore
    public Model_Permission(String key, String description){
        if(Model_Permission.find.byId(key) != null) return;
        this.value = key;
        this.description = description;
        this.save();
    }



/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        super.save();

    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object value: {}",  this.value);

        super.update();

    }

    @JsonIgnore @Override public void delete() {
        terminal_logger.internalServerError(new Exception("This object is not legitimate to remove."));
    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs         = "read: If user have M_Project.read_permission = true, you can create M_program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs       = "create: If user have M_Project.update_permission = true, you can create M_Program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String read_qr_token_permission_docs = "read: Private settings for M_Program";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_person_permission() {  return Controller_Security.get_person() != null && Controller_Security.get_person().has_permission("Permission_edit_person_permission");  }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()        {  return Controller_Security.get_person() != null && Controller_Security.get_person().has_permission("Permission_edit"); }

    public enum permissions{ Permission_edit_person_permission, Permission_edit }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static final Model.Finder<String, Model_Permission> find = new Finder<>( Model_Permission.class);

}
