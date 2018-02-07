package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.logger.Logger;
import utilities.model.NamedModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "Permission", description = "Model of Permission")
@Table(name="Permission")
public class Model_Permission extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Permission.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "permissions") public List<Model_Person>  persons = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "permissions") public List<Model_Role>    roles   = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore public static final String read_permission_docs         = "read: If user have M_Project.read_permission = true, you can create M_program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore public static final String create_permission_docs       = "create: If user have M_Project.update_permission = true, you can create M_Program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore public static final String read_qr_token_permission_docs = "read: Private settings for M_Program";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) public boolean edit_person_permission() {  return BaseController.person() != null && BaseController.person().has_permission("Permission_edit_person_permission");  }
    @JsonProperty @ApiModelProperty(required = true) public boolean edit_permission()        {  return BaseController.person() != null && BaseController.person().has_permission("Permission_edit"); }

    public enum Permission { Permission_edit_person_permission, Permission_edit }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Permission getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_Permission getById(UUID id) {
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static final Finder<UUID, Model_Permission> find = new Finder<>( Model_Permission.class);
}
