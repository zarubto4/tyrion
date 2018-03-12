package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.ProgramType;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "Widget", description = "Model of Widget")
@Table(name="Widget")
public class Model_Widget extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Widget.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                       @JsonIgnore public Integer order_position;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Producer producer;

    public ProgramType publish_type;

    @JsonIgnore @OneToMany(mappedBy="widget", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  @OrderBy("created desc") public List<Model_WidgetVersion> versions = new ArrayList<>();

    @JsonIgnore public boolean active; // U veřejných Skupin administrátor zveřejňuje skupinu - může připravit něco do budoucna

 /* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_project_id;
    @JsonIgnore @Transient @Cached public List<UUID> cache_version_ids = new ArrayList<>();
    @JsonIgnore @Transient @Cached private UUID cache_producer_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(value = "Visible only if user has permission to know it", required = false)
    public Model_Person author() throws _Base_Result_Exception {
        try {

            if (author_id != null) {
                return Model_Person.getById(author_id);
            }

            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @ApiModelProperty(readOnly = true, value = "can be hidden, if Widget is created by User not by Company")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public UUID producer_id() {

        if (cache_producer_id != null) return cache_producer_id;

        Model_Producer producer = get_producer();
        if (producer == null) return null;

        return producer.id;
    }

    @ApiModelProperty(readOnly = true, value = "can be hidden, if Widget is created by User not by Company")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public String producer_name() {

        Model_Producer producer = get_producer();
        if (producer == null) return null;

        return producer.name;
    }

    @JsonProperty @ApiModelProperty(required = true)
    public  List<Model_WidgetVersion> versions() {
        return get_versions();
    }


    @JsonProperty @ApiModelProperty(required = false, value = "Only for Community Administrator") @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean active() {
        return publish_type == ProgramType.PUBLIC ? true : null;
    }
/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_project_id() throws _Base_Result_Exception {

        if (cache_project_id == null) {

            Model_Project project = Model_Project.find.query().where().eq("widgets.id", id).findOne();
            if (project == null) throw new Result_Error_NotFound(Model_Project.class);

            cache_project_id = project.id;
            return project.id;
        }

        return cache_project_id;
    }

    @JsonIgnore
    public Model_Project get_project() throws _Base_Result_Exception {

        if (cache_project_id == null) {
            return Model_Project.getById(get_project_id());
        }else {
            return Model_Project.getById(cache_project_id);
        }
    }

    @JsonIgnore
    public List<Model_WidgetVersion> get_versions() {
        try {

            if (cache_version_ids.isEmpty()) {

                List<UUID> uuids =   Model_WidgetVersion.find.query().where().eq("widget.id", id).eq("deleted", false).order().desc("created").findIds();

                // Získání seznamu
                for (UUID uuid : uuids) {
                    cache_version_ids.add(uuid);
                }

            }

            List<Model_WidgetVersion> grid_versions  = new ArrayList<>();

            for (UUID version_id : cache_version_ids) {
                grid_versions.add(Model_WidgetVersion.getById(version_id));
            }

            return grid_versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }

    }

    @JsonIgnore
    public Model_Person get_author() {
        return Model_Person.getById(author_id);
    }

    @JsonIgnore
    public Model_Producer get_producer() {

        if (cache_producer_id == null) {
            Model_Producer producer = Model_Producer.find.query().where().eq("widgets.id", id).select("id").findOne();
            if (producer == null) return null;

            cache_producer_id = producer.id;
        }

        return Model_Producer.getById(cache_producer_id);
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("save::Creating new Object");

        // Save Object
        super.save();

        // Add to Cache
        if (project != null) {
            new Thread(() -> { EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, project.id, project.id)); }).start();
            project.cache_widget_ids.add(0,id);
        }
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update::Update object Id: {}",  this.id);

        // Update Object
        super.update();


        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, get_project_id(), get_project_id()));
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

        // Remove from Project Cache
        try {
            get_project().cache_widget_ids.remove(id);
        } catch (_Base_Result_Exception e) {
            // Nothing
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, get_project_id(), get_project_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

        return false;
    }

/* ORDER  -------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void up() throws _Base_Result_Exception {

        check_update_permission();

        /*
        Model_Widget up = Model_Widget.find.query().where().eq("order_position", (order_position-1) ).eq("type_of_widget.id", type_of_widget_id()).findOne();
        if (up == null) return;

        up.order_position += 1;
        up.update();

        this.order_position -= 1;
        this.update();
        */
    }

    @JsonIgnore @Transient
    public void down() throws _Base_Result_Exception {

        check_update_permission();
        /*
        Model_Widget down = Model_Widget.find.query().where().eq("order_position", (order_position+1) ).eq("type_of_widget.id", type_of_widget_id()).findOne();
        if (down == null) return;

        down.order_position -= 1;
        down.update();

        this.order_position += 1;
        this.update();
        */
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Widget_create.name())) return;
        if(this.project == null) throw new Result_Error_PermissionDenied();
        this.project.check_update_permission();
    }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        if(publish_type == ProgramType.PUBLIC || publish_type == ProgramType.DEFAULT_MAIN ) return;
        get_project().check_read_permission();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Widget_update.name())) return;
        get_project().check_update_permission();
    }

    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Widget_delete.name())) return;
        get_project().check_delete_permission();
    }

    @JsonIgnore @Transient public void check_community_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Model_CProgram.Permission.C_Program_community_publishing_permission.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    @JsonProperty @ApiModelProperty("Visible only for Administrator with Special Permission") @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean community_publishing_permission()  {
        try {
            _BaseController.person().has_permission(Model_CProgram.Permission.C_Program_community_publishing_permission.name());
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public enum Permission { Widget_create, Widget_read, Widget_update, Widget_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_Widget.class)
    public static Cache<UUID, Model_Widget> cache;

    public static Model_Widget getById(UUID id) throws _Base_Result_Exception {

        Model_Widget grid_widget = cache.get(id);
        if (grid_widget == null) {

            grid_widget = Model_Widget.find.byId(id);
            if (grid_widget == null) throw new Result_Error_NotFound(Model_Widget.class);

            cache.put(id, grid_widget);
        }
        // Check Permission
        if(grid_widget.its_person_operation()) {
            grid_widget.check_read_permission();
        }
        return grid_widget;
    }

    public static Model_Widget getPublicByName(String name) {
        return find.query().where().isNull("type_of_widget.project").eq("name", name).findOne();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Widget> find = new Finder<>(Model_Widget.class);
}