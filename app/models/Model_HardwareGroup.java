package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.cache.Cached;
import utilities.enums.EntityType;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.model.UnderProject;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.swagger.output.Swagger_Short_Reference;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "HardwareGroup", description = "Model of Hardware Group")
@Table(name="HardwareGroup")
public class Model_HardwareGroup extends NamedModel implements Permissible, UnderProject {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Block.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY) public Model_Project project;  // Projekt, pod který Hardware Group spadá

    @JsonIgnore @ManyToMany(mappedBy = "hardware_groups", fetch = FetchType.LAZY) public List<Model_Hardware> hardware = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public Integer cache_group_size;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public Integer size() {
        try {
            if (cache_group_size == null) {
                cache_group_size = Model_Hardware.find.query().where().eq("hardware_groups.id", this.id).findCount();
            }

            return cache_group_size;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public List<Swagger_Short_Reference> hardware_types() {
        List<Swagger_Short_Reference> short_references = new ArrayList<>();

        for(Model_HardwareType type : getHardwareTypes()){
            short_references.add(new Swagger_Short_Reference(type.id, type.name, type.description));
        }

        return short_references;
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/


    //get ids
    @JsonIgnore
    public List<UUID> get_HardwareTypesId() {

        if (idCache().gets(Model_HardwareType.class) == null) {
            idCache().add(Model_HardwareType.class,  Model_HardwareType.find.query().where().eq("hardware.hardware_groups.id", id).select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_HardwareType.class) != null ?  idCache().gets(Model_HardwareType.class) : new ArrayList<>();
    }


    @JsonIgnore
    public List<Model_HardwareType> getHardwareTypes() {

        try {

            List<Model_HardwareType> hardwareTypes  = new ArrayList<>();

            for (UUID types : get_HardwareTypesId()) {
                hardwareTypes.add(Model_HardwareType.find.byId(types));
            }

            return hardwareTypes;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public UUID get_project_id()           {

        if (idCache().get(Model_Project.class) == null) {
            idCache().add(Model_Project.class, (UUID) Model_Project.find.query().where().eq("hardware_groups.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_Project.class);
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return isLoaded("project") ? project : Model_Project.find.query().nullable().where().eq("hardware_groups.id", id).findOne();
    }

    @JsonIgnore
    public List<UUID> getHardwareIds() {
        if (idCache().gets(Model_Hardware.class) == null) {
            idCache().add(Model_Hardware.class, (UUID) Model_Hardware.find.query().where().eq("hardware_groups.id", id).select("id").findSingleAttribute());
        }

        return idCache().gets(Model_Hardware.class) != null ?  idCache().gets(Model_Hardware.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_Hardware> getHardware() {
        try {

            List<Model_Hardware> hardwares = new ArrayList<>();

            for (UUID types : getHardwareIds()) {
                hardwares.add(Model_Hardware.find.byId(types));
            }

            return hardwares;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }



/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        super.save();

        project.idCache().add(this.getClass(), id);

        //  if create something under project
        //  if (project != null ) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo(Model_Project.class, project_id(), project_id()))).start();
    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete: Delete object Id: {} ", this.id);

        this.getHardware().clear();


        super.delete();

        getProject().idCache().remove(this.getClass(), id);


        // TODO opravit Info o updatu
        // Case 1.2 :: After Delete - we send notification to frontend (Only if it is desirable)
        //new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, "project.id", "model.id"))).start();

        // Case 3 :: In some cases, it is not possible to delete an object - it is therefore impossible to delete the object by the method
        //logger.internalServerError(new Exception("This object is not legitimate to remove."));
        //throw new IllegalAccessError("Delete is not supported under " + getClass().getSimpleName());

        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.HARDWARE_GROUP;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_HardwareGroup.class)
    public static CacheFinder<Model_HardwareGroup> find = new CacheFinder<>(Model_HardwareGroup.class);
}
