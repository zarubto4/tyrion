package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.logger.Logger;
import utilities.model.UnderProject;
import utilities.model.VersionModel;
import utilities.permission.Action;
import utilities.permission.Permissible;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "WidgetVersion", description = "Model of WidgetVersion")
@Table(name="WidgetVersion")
public class Model_WidgetVersion extends VersionModel implements Permissible, UnderProject {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_WidgetVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(columnDefinition = "TEXT") public String design_json;
    @Column(columnDefinition = "TEXT") public String logic_json;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Widget widget;

/* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_grid_widget_id() {

        if (idCache().get(Model_Widget.class) == null) {
            idCache().add(Model_Widget.class, (UUID) Model_Widget.find.query().where().eq("versions.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_Widget.class);
    }

    @JsonIgnore
    public Model_Widget getWidget() {
        return isLoaded("widget") ? widget : Model_Widget.find.query().nullable().where().eq("versions.id", id).findOne();
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return this.getWidget().getProject();
    }

    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        // Save Object
        super.save();

        Model_Widget widget = getWidget();

        // Add to Cache
        if (widget != null) {
            widget.getVersionIds();
            widget.idCache().add(this.getClass(), id);
            widget.sort_Model_Model_GridProgramVersion_ids();
        }
    }

    @JsonIgnore @Override
    public boolean delete() {

        getWidget().idCache().remove(this.getClass(), id);

        return super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.WIDGET_VERSION;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

    public enum Permission {} // Not Required here

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_WidgetVersion.class)
    public static CacheFinder<Model_WidgetVersion> find = new CacheFinder<>(Model_WidgetVersion.class);
}
