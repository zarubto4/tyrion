package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.NamedModel;

import javax.persistence.*;
import java.util.*;

@Entity
@ApiModel(value = "HardwareFeature", description = "Model of HardwareFeature")
@Table(name="HardwareFeature")
public class Model_HardwareFeature extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_HardwareFeature.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ManyToMany(fetch = FetchType.LAZY) @JsonIgnore public List<Model_HardwareType> hardware_types = new ArrayList<>();


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    // FOR LIST
    public static Map<UUID, String> selectOptions() {

        Map<UUID, String> options = new LinkedHashMap<>();

        for (Model_HardwareFeature features : find.all()) {
            options.put(features.id, features.name);
        }

        return options;
    }


/* CRUD CLASSES --------------------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}
    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}
    
/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_HardwareFeature> find = new Finder<>(Model_HardwareFeature.class);
}
