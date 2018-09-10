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
        if(_BaseController.person().has_permission(Permission.Producer_create.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {
        // Now all are public
        return;
    }
    @JsonIgnore @Transient @Override public void check_update_permission()  {
        if(_BaseController.person().has_permission(Permission.Producer_update.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void  check_delete_permission() throws _Base_Result_Exception  {
        if(_BaseController.person().has_permission(Permission.Producer_delete.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    public enum Permission { Producer_create, Producer_read, Producer_update, Producer_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Producer.class)
    public static CacheFinder<Model_Producer> find = new CacheFinder<>(Model_Producer.class);
}
