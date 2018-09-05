package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers._BaseController;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.VersionModel;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.UUID;

@Entity
@ApiModel( value = "WidgetVersion", description = "Model of WidgetVersion")
@Table(name="WidgetVersion")
public class Model_WidgetVersion extends VersionModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_WidgetVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(columnDefinition = "TEXT") public String design_json;
    @Column(columnDefinition = "TEXT") public String logic_json;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Widget widget;

/* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_grid_widget_id() throws _Base_Result_Exception {

        if (idCache().get(Model_Widget.class) == null) {
            idCache().add(Model_Widget.class, (UUID) Model_Widget.find.query().where().eq("versions.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_Widget.class);
    }

    @JsonIgnore
    public Model_Widget get_grid_widget() throws _Base_Result_Exception {
        try {
            return Model_Widget.find.byId(get_grid_widget_id());
        }catch (Exception e) {
            return null;
        }
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
        logger.debug("save::Creating new Object");

        // Save Object
        super.save();

        Model_Widget widget = get_grid_widget();

        // Add to Cache
        if (widget != null) {
            widget.get_versionsId();
            widget.idCache().add(this.getClass(), id);
            widget.sort_Model_Model_GridProgramVersion_ids();
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, widget.getProjectId(), widget.id));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update::Update object Id: {}",  this.id);

        // Update Object
        super.update();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, get_grid_widget().getProjectId(), get_grid_widget_id()));
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
            get_grid_widget().idCache().remove(this.getClass(), id);
        } catch (_Base_Result_Exception e) {
            // Nothing
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, get_grid_widget().getProjectId(), get_grid_widget_id()));
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

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception { widget.check_update_permission();} // You have to access widget directly, because get_grid_widget() finds the widget by id of the version which is not yet created
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        try {

            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_read_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_read_" + id);
                return;
            }

            get_grid_widget().check_read_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        try {

            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_update_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_update_" + id);
                return;
            }

            get_grid_widget().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        try {

            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_delete_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_delete_" + id);
                return;
            }

            get_grid_widget().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    public enum Permission {} // Not Required here

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_WidgetVersion.class)
    public static CacheFinder<Model_WidgetVersion> find = new CacheFinder<>(Model_WidgetVersion.class);
}
