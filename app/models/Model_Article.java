package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers._BaseController;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@ApiModel(value = "Article", description = "Model of Article")
@Table(name="Article")
public class Model_Article extends TaggedModel {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Article.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(columnDefinition = "TEXT") public String mark_down_text;

    /* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    /* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore
    @Override
    public void save() {

        logger.debug("save :: Creating new Object");
        super.save();

    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);
        super.update();

    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete: Delete object Id: {} ", this.id);
        return super.delete();
    }

    /* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    /* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    /* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    /* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @javax.persistence.Transient
    @Override public void check_read_permission()   throws _Base_Result_Exception {
        // True
    }

    @JsonIgnore @javax.persistence.Transient
    @Override public void check_create_permission() throws _Base_Result_Exception {
        // Podmínka pro administrátora
        if (_BaseController.person().has_permission(Permission.Article_create.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    @JsonIgnore @javax.persistence.Transient
    @Override public void check_update_permission() throws _Base_Result_Exception {
        // Podmínka pro administrátora
        if (_BaseController.person().has_permission(Permission.Article_update.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    /**
     * Example jak tvořit oprávnění!
     *
     * @throws _Base_Result_Exception
     */
    @JsonIgnore @javax.persistence.Transient
    @Override public void check_delete_permission() throws _Base_Result_Exception {

        // Podmínka pro administrátora
        if (_BaseController.person().has_permission(Permission.Article_delete.name())) return;
        throw new Result_Error_PermissionDenied();

    }

    public enum Permission { Article_create, Article_read, Article_update, Article_delete }

    /* CACHE ---------------------------------------------------------------------------------------------------------------*/

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Article.class)
    public static CacheFinder<Model_Article> find = new CacheFinder<>(Model_Article.class);
}
