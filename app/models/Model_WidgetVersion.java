package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.Approval;
import utilities.enums.ProgramType;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.model.VersionModel;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.UUID;

@Entity
@ApiModel( value = "GridWidgetVersion", description = "Model of GridWidgetVersion")
@Table(name="GridWidgetVersion")
public class Model_WidgetVersion extends VersionModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_WidgetVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(columnDefinition = "TEXT") public String design_json;
    @Column(columnDefinition = "TEXT") public String logic_json;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Widget widget;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_widget_id;

/* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_grid_widget_id() throws _Base_Result_Exception {

        if (cache_widget_id == null) {

            Model_Widget widget = Model_Widget.find.query().where().eq("versions.id", id).select("id").findOne();
            if (widget != null) {
                cache_widget_id = widget.id;
            } else {
                cache_widget_id = null;
            }
        }

        return cache_widget_id;
    }

    @JsonIgnore
    public Model_Widget get_grid_widget() throws _Base_Result_Exception {

        if (get_grid_widget_id() != null) {
            return Model_Widget.getById(cache_widget_id);
        }

        return null;
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("save::Creating new Object");

        // Save Object
        super.save();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, widget.get_project_id(), widget.id));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

        // Add to Cache
        if (widget != null) {
            widget.cache_version_ids.add(0, id);
        }
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update::Update object Id: {}",  this.id);

        // Update Object
        super.update();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, get_grid_widget().get_project_id(), get_grid_widget_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete::Delete object Id: {}",  this.id);

        // Delete
        super.delete();

        // Remove from Cache Cache
        try {
            get_grid_widget().cache_version_ids.remove(id);
        } catch (_Base_Result_Exception e) {
            // Nothing
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, get_grid_widget().get_project_id(), get_grid_widget_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception { get_grid_widget().check_update_permission();}
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception { get_grid_widget().check_read_permission();}
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception { get_grid_widget().check_update_permission();}
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception { get_grid_widget().check_update_permission();}

    public enum Permission {} // Not Required here

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_WidgetVersion.class)
    public static Cache<UUID, Model_WidgetVersion> cache;

    public static Model_WidgetVersion getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_WidgetVersion getById(UUID id) throws _Base_Result_Exception {

        Model_WidgetVersion grid_widget_version = cache.get(id);

        if (grid_widget_version == null) {

            grid_widget_version = Model_WidgetVersion.find.byId(id);
            if (grid_widget_version == null) throw new Result_Error_NotFound(Model_WidgetVersion.class);

            cache.put(id, grid_widget_version);
        }

        grid_widget_version.check_read_permission();
        return grid_widget_version;
    }

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_WidgetVersion> find = new Finder<>(Model_WidgetVersion.class);
}
