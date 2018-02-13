package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.NamedModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Producer", description = "Model of Producer")
@Table(name="Producer")
public class Model_Producer extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Producer.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @OneToMany(mappedBy="producer", cascade = CascadeType.ALL) public List<Model_HardwareType> hardware_types = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="producer", cascade = CascadeType.ALL) public List<Model_Block> blocks = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="producer", cascade = CascadeType.ALL) public List<Model_Widget> widgets = new ArrayList<>();

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(BaseController.person().has_permission(Permission.Producer_create.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {
        // Now all are public
        return;
    }
    @JsonIgnore @Transient @Override public void check_update_permission()  {
        if(BaseController.person().has_permission(Permission.Producer_update.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void  check_delete_permission() throws _Base_Result_Exception  {
        if(BaseController.person().has_permission(Permission.Producer_delete.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    public enum Permission { Producer_create, Producer_read, Producer_update, Producer_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_Producer.class, timeToIdle = CacheField.DayCacheConstant)
    public static Cache<UUID, Model_Producer> cache;

    public static Model_Producer getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_Producer getById(UUID id) throws _Base_Result_Exception {
        logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Producer> find = new Finder<>(Model_Producer.class);
}
