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
@ApiModel(value = "Role", description = "Model of Role")
@Table(name="Role")
public class Model_Role extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Role.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ManyToMany(mappedBy = "roles",fetch = FetchType.LAZY) public List<Model_Person> persons = new ArrayList<>();
    @ManyToMany() @OrderBy(value ="name") public List<Model_Permission> permissions = new ArrayList<>();

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public boolean delete() {

        this.persons = null;
        this.update();

        this.refresh();
        return super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore                                      public boolean create_permission() { return BaseController.person().has_permission("Role_create"); }
    @JsonIgnore                                      public boolean read_permission()   { return BaseController.person().has_permission("Role_read"); }
    @JsonProperty @ApiModelProperty(required = true) public boolean update_permission() { return BaseController.person().has_permission("Role_update"); }
    @JsonProperty @ApiModelProperty(required = true) public boolean delete_permission() { return BaseController.person().has_permission("Role_delete");}

    public enum Permission { Role_create, Role_read, Role_update, Role_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Role getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_Role getById(UUID id) {
        logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);
    }

    public static Model_Role getByName(String name) {
        return find.query().where().eq("name" , name).findOne();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static final Finder<UUID, Model_Role> find = new Finder<>(Model_Role.class);
}
