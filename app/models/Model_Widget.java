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
import utilities.enums.ProgramType;
import utilities.logger.Logger;
import utilities.model.NamedModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "Widget", description = "Model of Widget")
@Table(name="Widget")
public class Model_Widget extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Widget.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                       @JsonIgnore public Integer order_position;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Person author;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Producer producer;

    public ProgramType publish_type;

    @JsonIgnore @OneToMany(mappedBy="widget", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  @OrderBy("created desc")
    public List<Model_WidgetVersion> versions = new ArrayList<>();

    @ManyToMany public List<Model_Tag> tags = new ArrayList<>();

    @JsonIgnore public boolean active; // U veřejných Skupin administrátor zveřejňuje skupinu - může připravit něco do budoucna

 /* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_project_id;
    @JsonIgnore @Transient @Cached public List<UUID> cache_version_ids;
    @JsonIgnore @Transient @Cached private UUID cache_author_id;
    @JsonIgnore @Transient @Cached private UUID cache_producer_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(readOnly = true, value = "can be hidden, if Widget is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public UUID author_id() {

        if (cache_author_id != null) return cache_author_id;

        Model_Person person = get_author();
        if (person == null) return null;

        return person.id;
    }

    @ApiModelProperty(readOnly = true, value = "can be hidden, if Widget is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public String author_nick_name() {

        Model_Person person = get_author();
        if (person == null) return null;

        return person.nick_name;
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
        return getVersions();
    }


    @JsonProperty @ApiModelProperty(required = false, value = "Only for Community Administrator") @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean active() {
        return publish_type == ProgramType.PUBLIC ? true : null;
    }
/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Project getProject() {

        if (cache_project_id == null) {
            Model_Project project = Model_Project.find.query().where().eq("widgets.id", id).findOne();
            if (project == null) return null;

            cache_project_id = project.id;
            project.cache();

            return project;
        }

        return Model_Project.getById(cache_project_id);
    }

    @JsonIgnore
    public UUID getProjectId() {
        if (cache_project_id == null) {
            Model_Project project = getProject();
            if (project == null) return null;

            return project.id;
        }

        return cache_project_id;
    }

    @JsonIgnore
    public List<Model_WidgetVersion> getVersions() {
        try {

            if (cache_version_ids.isEmpty()) {

                List<Model_WidgetVersion> grid_versions =   Model_WidgetVersion.find.query().where().eq("widget.id", id).eq("deleted", false).order().desc("created").select("id").findList();

                // Získání seznamu
                for (Model_WidgetVersion grid_version : grid_versions) {
                    cache_version_ids.add(grid_version.id);
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

        if (cache_author_id == null) {
            Model_Person person = Model_Person.find.query().where().eq("widgetsAuthor.id", id).select("id").findOne();
            if (person == null) return null;
            cache_author_id = person.id;
        }

        return Model_Person.getById(cache_author_id);
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

/* ORDER  -------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void up() {
/*
        Model_Widget up = Model_Widget.find.query().where().eq("order_position", (order_position-1) ).eq("type_of_widget.id", type_of_widget_id()).findOne();
        if (up == null)return;

        up.order_position += 1;
        up.update();

        this.order_position -= 1;
        this.update();*/
    }

    @JsonIgnore @Transient
    public void down() {
/*
        Model_Widget down = Model_Widget.find.query().where().eq("order_position", (order_position+1) ).eq("type_of_widget.id", type_of_widget_id()).findOne();
        if (down == null)return;

        down.order_position -= 1;
        down.update();

        this.order_position += 1;
        this.update();*/

    }
/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user can read TypeOfWidget, than can read all GridWidgets from list of TypeOfWidget ( You get ids of list of GridWidgets in object \"GridWidgets\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have TypeOfWidget.update_permission = true, you can create new GridWidgets on this TypeOfWidget - Or you need static/dynamic permission key if user want create GridWidget in public TypeOfWidget";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore                                      public boolean create_permission() { return (this.project != null && this.project.update_permission()) || BaseController.person().has_permission(Permission.Widget_create.name()); }
    @JsonIgnore                                      public boolean read_permission()   { return publish_type == ProgramType.PUBLIC || publish_type == ProgramType.DEFAULT_MAIN || getProject().read_permission();}
    @JsonProperty @ApiModelProperty(required = true) public boolean edit_permission()   { return getProjectId() != null ? getProject().update_permission() : BaseController.person().has_permission(Permission.Widget_edit.name()) ;}
    @JsonProperty @ApiModelProperty(required = true) public boolean update_permission() { return getProjectId() != null ? getProject().update_permission() : BaseController.person().has_permission(Permission.Widget_update.name()) ;}
    @JsonProperty @ApiModelProperty(required = true) public boolean delete_permission() { return getProjectId() != null ? getProject().update_permission() : BaseController.person().has_permission(Permission.Widget_delete.name()) ;}
    @JsonProperty @ApiModelProperty("Visible only for Administrator with Permission") @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean community_publishing_permission()  { return BaseController.person().has_permission(Model_CProgram.Permission.C_Program_community_publishing_permission.name());}

    public enum Permission { Widget_create, Widget_read, Widget_edit, Widget_update, Widget_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_Widget.class)
    public static Cache<UUID, Model_Widget> cache;

    public static Model_Widget getById(String id) {
    return getById(UUID.fromString(id));
    }

    public static Model_Widget getById(UUID id) {

        Model_Widget grid_widget = cache.get(id);
        if (grid_widget == null) {

            grid_widget = Model_Widget.find.byId(id);
            if (grid_widget == null) return null;

            cache.put(id, grid_widget);
        }

        return grid_widget;
    }

    public static Model_Widget getPublicByName(String name) {
        return find.query().where().isNull("type_of_widget.project").eq("name", name).findOne();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Widget> find = new Finder<>(Model_Widget.class);
}