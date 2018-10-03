package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.EntityType;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.permission.Action;
import utilities.permission.Permissible;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.List;

@Entity
@ApiModel(value = "Article", description = "Model of Article")
@Table(name="Article")
public class Model_Article extends TaggedModel implements Permissible {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Article.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(columnDefinition = "TEXT") public String mark_down_text;

    /* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    /* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    /* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    /* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    /* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    /* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.ARTICLE;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

    /* CACHE ---------------------------------------------------------------------------------------------------------------*/

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Article.class)
    public static CacheFinder<Model_Article> find = new CacheFinder<>(Model_Article.class);


}
