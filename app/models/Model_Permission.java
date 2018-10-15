package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.EntityType;
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

    public Action action;
    public EntityType entity_type;

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

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Permission.class)
    public static final CacheFinder<Model_Permission> find = new CacheFinder<>(Model_Permission.class);
}
