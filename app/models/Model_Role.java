package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers._BaseController;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.NamedModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
        if(_BaseController.person().has_permission(Permission.Role_create.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Role_read.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Role_update.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Role_delete.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    public enum Permission { Role_create, Role_read, Role_update, Role_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Role getByName(String name) {
        return find.query().where().eq("name" , name).findOne();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Role.class)
    public static CacheFinder<Model_Role> find = new CacheFinder<>(Model_Role.class);
}
