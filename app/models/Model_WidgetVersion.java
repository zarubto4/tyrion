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
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.UUID;

@Entity
@ApiModel( value = "GridWidgetVersion", description = "Model of GridWidgetVersion")
@Table(name="GridWidgetVersion")
public class Model_WidgetVersion extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_WidgetVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(columnDefinition = "TEXT") public String design_json;
    @Column(columnDefinition = "TEXT") public String logic_json;

    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Only if user make request for publishing") @Enumerated(EnumType.STRING) public Approval approval_state;
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Only for main / default program - and access only for administrators") @Enumerated(EnumType.STRING) public ProgramType publish_type;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Person author;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Widget widget;


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_widget_id;
    @JsonIgnore @Transient @Cached private UUID cache_author_id;

/* JSON PROPERTY VALUES -----------------------------a-------------------------------------------------------------------*/

    @JsonProperty
    public Model_Person author() {
        try {

            if (author == null) return null;

            return get_author();

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }
    
/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Person get_author() {

        if (cache_author_id == null) {
            Model_Person person = Model_Person.find.query().where().eq("widgetVersionsAuthor.id", id).select("id").findOne();
            cache_author_id = person.id;
        }

        return Model_Person.getById(cache_author_id);
    }

    @JsonIgnore
    public UUID get_grid_widget_id() {

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
    public Model_Widget get_grid_widget() {

        if (get_grid_widget_id() != null) {
            return Model_Widget.getById(cache_widget_id);
        }

        return null;
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("save :: Creating new Object");

        super.save();

        if (widget != null && get_grid_widget().getProjectId() != null) {
            new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, get_grid_widget().getProjectId(), get_grid_widget_id()))).start();
        }

        // Add to Cache
        if (widget != null) {
            widget.cache_version_ids.add(0, id);
        }
    }

    @JsonIgnore @Override public void update() {

        System.out.println("gridWidgetVersion_edit .... update() ");

        logger.debug("update :: Update object Id: {}",  this.id);
        super.update();

        if (get_grid_widget() != null && get_grid_widget().getProjectId() != null) {
            new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, get_grid_widget().getProjectId(), get_grid_widget_id()))).start();
        }
    }

    @JsonIgnore @Override public boolean delete() {

        logger.debug("delete :: Delete object Id: {}",  this.id);

        this.deleted = true;

        super.update();

        // Add to Cache
        if (get_grid_widget() != null) {
            get_grid_widget().cache_version_ids.remove(id);
        }

        if (get_grid_widget() != null && get_grid_widget().getProjectId() != null) {
            new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, get_grid_widget().getProjectId(), get_grid_widget_id()))).start();
        }

        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user can read GridWidget, than can read all Versions from list of GridWidgets ( You get ids of list of version in object \"GridWidgets\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have GridWidget.update_permission = true, you can create new version of GridWidgets on this GridWidget - Or you need static/dynamic permission key if user want create version of GridWidget in public GridWidget in public TypeOfWidget";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean create_permission()  {  return  widget.update_permission() ||  BaseController.person().has_permission("WidgetVersion_create"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean read_permission()    {  return  widget.read_permission()   ||  BaseController.person().has_permission("WidgetVersion_read");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()    {  return  widget.update_permission() ||  BaseController.person().has_permission("WidgetVersion_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission()  {  return  widget.update_permission() ||  BaseController.person().has_permission("WidgetVersion_delete"); }

    public enum Permission {WidgetVersion_create, WidgetVersion_read, WidgetVersion_edit, WidgetVersion_delete}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_WidgetVersion.class, timeToIdle = 600)
    public static Cache<UUID, Model_WidgetVersion> cache;

    public static Model_WidgetVersion getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_WidgetVersion getById(UUID id) {

        Model_WidgetVersion grid_widget_version = cache.get(id);

        if (grid_widget_version == null) {

            grid_widget_version = Model_WidgetVersion.find.byId(id);
            if (grid_widget_version == null) return null;

            cache.put(id, grid_widget_version);
        }

        return grid_widget_version;
    }

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_WidgetVersion> find = new Finder<>(Model_WidgetVersion.class);
}
