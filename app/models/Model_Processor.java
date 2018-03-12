package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import org.ehcache.Cache;
import utilities.cache.CacheField;
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
@ApiModel(description = "Model of Processor", value = "Processor")
@Table(name="Processor")
public class Model_Processor extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Processor.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public String processor_code;
    public int speed;

    @JsonIgnore @OneToMany(mappedBy="processor", cascade = CascadeType.ALL) public List<Model_HardwareType> hardware_types = new ArrayList<>();

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Processor_create.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {
        // Not limited now
        return;
    }

    @JsonIgnore @Transient @Override public void check_update_permission()  throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Processor_update.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission()  throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Processor_delete.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    public enum Permission { Processor_create, Processor_update, Processor_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_Processor.class, duration = CacheField.DayCacheConstant)
    public static Cache<UUID, Model_Processor> cache;

    public static Model_Processor getById(UUID id) throws _Base_Result_Exception {

        Model_Processor processor = cache.get(id);

        if (processor == null) {

            processor = Model_Processor.find.byId(id);
            if (processor == null) throw new Result_Error_NotFound(Model_Processor.class);

            cache.put(id, processor);
        }
        // Check Permission
        if(processor.its_person_operation()) {
            processor.check_read_permission();
        }
        return processor;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Processor> find = new Finder<>(Model_Processor.class);
}
