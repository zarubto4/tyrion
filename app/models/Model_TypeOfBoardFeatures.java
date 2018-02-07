package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.logger.Logger;
import utilities.model.NamedModel;

import javax.persistence.*;
import java.util.*;

@Entity
@ApiModel(value = "BoardFeature", description = "Model of TypeOfBoard Features ")
@Table(name="BoardFeature")
public class Model_TypeOfBoardFeatures extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_TypeOfBoardFeatures.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ManyToMany(fetch = FetchType.LAZY) @JsonIgnore public List<Model_TypeOfBoard> type_of_boards = new ArrayList<>();


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    // FOR LIST
    public static Map<UUID, String> selectOptions() {

        Map<UUID, String> options = new LinkedHashMap<>();

        for (Model_TypeOfBoardFeatures features : find.all()) {
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

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_TypeOfBoardFeatures> find = new Finder<>(Model_TypeOfBoardFeatures.class);
}
