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
import utilities.model.Echo;
import utilities.model.Publishable;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.permission.Action;
import utilities.permission.JsonPermission;
import utilities.permission.Permissible;
import utilities.swagger.output.Swagger_Short_Reference;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@ApiModel( value = "Block", description = "Model of Block")
@Table(name="Block")
public class Model_Block extends TaggedModel implements Permissible, UnderProject, Publishable, Echo {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Block.class);

/* DATABASE VALUES -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore public boolean active; // U veřejných Skupin administrátor zveřejňuje skupinu - může připravit něco do budoucna

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Producer producer;

                                       @JsonIgnore public Integer order_position;
                                                   public ProgramType publish_type;

    @JsonIgnore @OneToMany(mappedBy="block", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Model_BlockVersion> versions = new ArrayList<>();

    @JsonIgnore public UUID original_id; // KDyž se vytvoří kopie nebo se publikuje program, zde se uloží původní ID pro pozdější párování

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(value = "Visible only if user has permission to know it", required = false)
    public Model_Person author()  {
        try {
            return get_author();
        } catch(NotFoundException e){
            return null;
        } catch (Exception e){
            logger.internalServerError(e);
        }
        return null;
    }

    @JsonProperty  @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by User not by Company", required = false)
    public Swagger_Short_Reference producer() {
        try {
            Model_Producer producer = getProducer();
            if (producer == null) return null;
            return new Swagger_Short_Reference(producer.id, producer.name, producer.description);
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }


    @JsonProperty @ApiModelProperty(required = true)
    public  List<Model_BlockVersion> versions() {
        try {
            return getVersions();
        } catch(Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = false, value = "Only for Community Administrator") @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean active() {
        return publish_type == ProgramType.PUBLIC || publish_type == ProgramType.DEFAULT_MAIN ? active : null;
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public boolean isPublic() {
        return publish_type == ProgramType.PUBLIC || publish_type == ProgramType.DEFAULT_MAIN;
    }

    @JsonIgnore
    public UUID get_project_id() {
        return (UUID) Model_Project.find.query().where().eq("blocks.id", id).select("id").getId();
    }

    @JsonIgnore
    public Model_Project getProject() {
        return isLoaded("project") ? this.project : Model_Project.find.query().nullable().where().eq("blocks.id", id).findOne();
    }

    @JsonIgnore
    public List<UUID> getVersionIds() {
        return Model_BlockVersion.find.query().where().eq("block.id", id).eq("deleted", false).select("id").order("created").findSingleAttributeList();
    }

    @JsonIgnore
    public List<Model_BlockVersion> getVersions() {
        return getVersionIds().stream().map(Model_BlockVersion.find::byId).collect(Collectors.toList());
    }

    @JsonIgnore
    public Model_Person get_author() throws NotFoundException {
        if(author_id != null) {
            return Model_Person.find.byId(author_id);
        } else return null;
    }

    @JsonIgnore
    public Model_Producer getProducer() {
        return isLoaded("producer") ? producer : Model_Producer.find.query().nullable().where().eq("blocks.id", id).findOne();
    }

    @JsonIgnore @Override
    public Echo getParent() {
        return this.getProject();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public boolean delete() {

        super.delete();

        // Remove from Project Cache
        if (publish_type == ProgramType.PRIVATE) {

            try {

                getProject().idCache().remove(this.getClass(), id);

            } catch (Exception e) {
                // Nothing
            }

        }

        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.BLOCK;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.PUBLISH);
    }

    @JsonPermission(Action.PUBLISH) @Transient
    public boolean community_publishing_permission;

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Block getPublicByName(String name) {
        return find.query().where().isNull("project").eq("deleted", false).eq("name", name).findOne();
    }

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_Block.class)
    public static CacheFinder<Model_Block> find = new CacheFinder<>(Model_Block.class);
}