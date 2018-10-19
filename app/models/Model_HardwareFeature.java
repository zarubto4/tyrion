package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
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


    // Here we can probably add Icons?
    // Wait for more than one YODA type of board...


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

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/
    
/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_HardwareFeature> find = new Finder<>(Model_HardwareFeature.class);
}
