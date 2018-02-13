package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.ProgramType;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
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

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(BaseController.person().has_permission(Permission.Role_create.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        if(BaseController.person().has_permission(Permission.Role_read.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if(BaseController.person().has_permission(Permission.Role_update.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        if(BaseController.person().has_permission(Permission.Role_delete.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    public enum Permission { Role_create, Role_read, Role_update, Role_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    // TODO Cache
    public static Model_Role getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_Role getById(UUID id) throws _Base_Result_Exception {
        Model_Role role = Model_Role.find.byId(id);
        if (role == null) throw new Result_Error_NotFound(Model_Product.class);

        // Check Permission
        role.check_read_permission();
        return role;
    }

    public static Model_Role getByName(String name) {
        return find.query().where().eq("name" , name).findOne();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static final Finder<UUID, Model_Role> find = new Finder<>(Model_Role.class);
}
