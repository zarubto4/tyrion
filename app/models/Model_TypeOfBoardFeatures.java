package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.*;

@Entity
@ApiModel(description = "Model of TypeOfBoard Features ",
         value = "BoardFeature")
public class Model_TypeOfBoardFeatures extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_TypeOfWidget.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
    @Id @ApiModelProperty(required = true)   public String id;
                    @Constraints.Required    public String name;

    @ManyToMany(fetch = FetchType.LAZY) @JsonIgnore public List<Model_TypeOfBoard> type_of_boards = new ArrayList<>();


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    // FOR LIST
    @JsonIgnore @Transient
    public static Map<String, String> selectOptions() {

        Map<String, String> options = new LinkedHashMap<>();

        for (Model_TypeOfBoardFeatures features : find.all()) {
            options.put(features.id, features.name);
        }

        return options;
    }


/* CRUD CLASSES --------------------------------------------------------------------------------------------------------*/


    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");
        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_TypeOfBoard.find.byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object value: {}",  this.id);
        super.update();

    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object Id: {} ", this.id);
        super.update();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static  Model.Finder<String, Model_TypeOfBoardFeatures> find = new Finder<>(Model_TypeOfBoardFeatures.class);



}
