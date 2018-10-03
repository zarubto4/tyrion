package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.logger.Logger;
import utilities.model.NamedModel;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@ApiModel(description = "Model of Log",
        value = "Log")
@Table(name="Log")
public class Model_Log extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Log.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                            public String type; // "tyrion", "homer"
      @JsonIgnore @OneToOne public Model_Blob file;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


    @JsonIgnore @Override
    public boolean delete() { // TODO better

        Model_Blob file = this.file;

        this.file = null;
        this.update();

        file.refresh();
        file.delete();

        return super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Log.class)
    public static CacheFinder<Model_Log> find = new CacheFinder<>(Model_Log.class);
}
