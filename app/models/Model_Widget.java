package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import exceptions.NotFoundException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.enums.ProgramType;
import utilities.logger.Logger;
import utilities.model.Publishable;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.models_update_echo.EchoHandler;
import utilities.permission.Action;
import utilities.permission.JsonPermission;
import utilities.permission.Permissible;
import utilities.swagger.output.Swagger_Short_Reference;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@ApiModel( value = "Widget", description = "Model of Widget")
@Table(name="Widget")
public class Model_Widget extends TaggedModel implements Permissible, UnderProject, Publishable {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Widget.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                       @JsonIgnore public Integer order_position;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Producer producer;

    public ProgramType publish_type;

    @JsonIgnore @OneToMany(mappedBy="widget", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  @OrderBy("created desc") public List<Model_WidgetVersion> versions = new ArrayList<>();

    @JsonIgnore public boolean active; // U veřejných Skupin administrátor zveřejňuje skupinu - může připravit něco do budoucna

    @JsonIgnore public UUID original_id; // KDyž se vytvoří kopie nebo se publikuje program, zde se uloží původní ID pro pozdější párování

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(value = "Visible only if user has permission to know it", required = false)
    public Model_Person author() {
        try {
            if (author_id != null) {
                return Model_Person.find.byId(author_id);
            }

            return null;
        } catch (NotFoundException e){
            //nothing
        } catch (Exception e){
            logger.internalServerError(e);
        }

        return null;
    }

    @JsonProperty
    @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by User not by Company", required = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_Short_Reference producer(){
        try {
            Model_Producer producer = getProducer();
            if (producer != null) {
                return producer.ref();
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
        return null;
    }

    @JsonProperty @ApiModelProperty(required = true)
    public  List<Model_WidgetVersion> versions() {
        try {
            return getVersions();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = false, value = "Only for Community Administrator") @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean active() {
        return publish_type == ProgramType.PUBLIC || publish_type == ProgramType.DEFAULT_MAIN ? active : null;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public boolean isPublic() {
        return publish_type == ProgramType.PUBLIC || publish_type == ProgramType.DEFAULT_MAIN;
    }

    @JsonIgnore
    public UUID getProjectId() {

        if (idCache().get(Model_Project.class) == null) {
            idCache().add(Model_Project.class, (UUID) Model_Project.find.query().where().eq("widgets.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_Project.class);
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return isLoaded("project") ? project : Model_Project.find.query().nullable().where().eq("widgets.id", id).findOne();
    }

    @JsonIgnore
    public List<UUID> getVersionIds() {
        if (idCache().gets(Model_WidgetVersion.class) == null) {
            idCache().add(Model_WidgetVersion.class, Model_WidgetVersion.find.query().where().eq("widget.id", id).ne("deleted", true).select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_WidgetVersion.class) != null ?  idCache().gets(Model_WidgetVersion.class) : new ArrayList<>();
    }

    @JsonIgnore
    public void sort_Model_Model_GridProgramVersion_ids() {

        List<Model_WidgetVersion> versions = getVersions();
        this.idCache().removeAll(Model_WidgetVersion.class);
        versions.stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())
                .forEach(o -> this.idCache().add(Model_WidgetVersion.class, o.id));

    }
    @JsonIgnore
    public List<Model_WidgetVersion> getVersions() {
        return this.getVersionIds().stream().map(Model_WidgetVersion.find::byId).collect(Collectors.toList());
    }

    @JsonIgnore
    public Model_Person get_author() {
        return Model_Person.find.byId(author_id);
    }

    @JsonIgnore
    public Model_Producer getProducer() {
        return isLoaded("producer") ? producer : Model_Producer.find.query().nullable().where().eq("widgets.id", id).findOne();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("save::Creating new Object");

        // Save Object
        super.save();

        Model_Project project = getProject();

        // Add to Cache
        if (project != null) {
            new Thread(() -> { EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, project.id, project.id)); }).start();
            project.idCache().add(this.getClass(), id);
        }
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update::Update object Id: {}",  this.id);

        // Update Object
        super.update();

        if(publish_type == ProgramType.PRIVATE) {
            new Thread(() -> {
                EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, getProjectId(), getProjectId()));
            }).start();
        }

    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete::Delete object Id: {}",  this.id);

        // Delete
        super.delete();

        if (publish_type == ProgramType.PRIVATE) {

            try {
                getProject().idCache().remove(this.getClass(), id);
            } catch (Exception e) {
                // Nothing
            }

            new Thread(() -> {
                try {
                    EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, getProjectId(), getProjectId()));
                } catch (Exception e) {
                    // Nothing
                }
            }).start();
        }

        return false;
    }

/* ORDER ---------------------------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA -----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.WIDGET;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.ACTIVATE);
    }

    @JsonPermission(Action.PUBLISH) @Transient
    public boolean community_publishing_permission;

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Widget getPublicByName(String name) {
        return find.query().where().isNull("project").eq("name", name).findOne();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_Widget.class)
    public static CacheFinder<Model_Widget> find = new CacheFinder<>(Model_Widget.class);
}