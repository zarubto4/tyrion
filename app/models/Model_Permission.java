package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.EntityType;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.permission.Action;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@ApiModel(value = "Permission", description = "Model of Permission")
@Table(name="Permission")
public class Model_Permission extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Permission.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Enumerated(EnumType.STRING) public Action action;
    @Enumerated(EnumType.STRING) public EntityType entity_type;

    @JsonIgnore @ManyToMany(mappedBy = "permissions") public List<Model_Person>  persons = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "permissions") public List<Model_Role>    roles   = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        // Netřeba komentář - záměrně bez logger.error
        throw new Result_Error_NotSupportedException();
    }

    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {
        // Not limited now
        return;
    }

    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        // if(_BaseController.person().has_permission(Permission.Permission_update.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        throw new Result_Error_NotSupportedException();
    }

    // public enum Permission { Permission_crate, Permission_edit_person_permission, Permission_update }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Permission.class)
    public static final CacheFinder<Model_Permission> find = new CacheFinder<>(Model_Permission.class);
}
