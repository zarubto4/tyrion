package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
        if(BaseController.person().has_permission(Permission.Processor_create.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {
        // Not limited now
        return;
    }

    @JsonIgnore @Transient @Override public void check_update_permission()  throws _Base_Result_Exception {
        if(BaseController.person().has_permission(Permission.Processor_update.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission()  throws _Base_Result_Exception {
        if(BaseController.person().has_permission(Permission.Processor_delete.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    public enum Permission { Processor_create, Processor_update, Processor_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Processor getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_Processor getById(UUID id) throws _Base_Result_Exception {

        Model_Processor processor = Model_Processor.find.byId(id);
        if (processor == null) throw new Result_Error_NotFound(Model_Product.class);

        // Check Permission
        processor.check_read_permission();
        return processor;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Processor> find = new Finder<>(Model_Processor.class);
}
