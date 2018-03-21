package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.swagger.output.Swagger_Short_Reference;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "HardwareGroup", description = "Model of Hardware Group")
@Table(name="HardwareGroup")
public class Model_HardwareGroup extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Block.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY) public Model_Project project;  // Projekt, pod který Hardware Group spadá

    @JsonIgnore @ManyToMany(mappedBy = "hardware_groups", fetch = FetchType.LAZY) public List<Model_Hardware> hardware = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public Integer cache_group_size;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public int size() {
        try {
            if (cache_group_size == null) {
                cache_group_size = Model_Hardware.find.query().where().eq("hardware_groups.id", this.id).findCount();
            }

            return cache_group_size;
        }catch (Exception e) {
            logger.internalServerError(e);
            return -1;
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

        if (cache().gets(Model_HardwareType.class) == null) {
            cache().add(Model_HardwareType.class,  Model_HardwareType.find.query().where().eq("hardware.hardware_groups.id", id).order().desc("created").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_HardwareType.class);

    }


    @JsonIgnore
    public List<Model_HardwareType> getHardwareTypes() {

        try {

            List<Model_HardwareType> hardwareTypes  = new ArrayList<>();

            for (UUID types : get_HardwareTypesId()) {
                hardwareTypes.add(Model_HardwareType.getById(types));
            }

            return hardwareTypes;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public UUID get_project_id()           {

        if (cache().get(Model_Project.class) == null) {
            cache().add(Model_Project.class, (UUID) Model_Project.find.query().where().eq("hardware_groups.id", id).select("id").findSingleAttribute());
        }

        return cache().get(Model_Project.class);

    }

    @JsonIgnore @Transient public Model_Project get_project() throws _Base_Result_Exception  {
        try {
            return Model_Project.getById(get_project_id());
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }


    @JsonIgnore
    public List<UUID> getHardwareIds() {
        if (cache().gets(Model_Hardware.class) == null) {
            cache().add(Model_Hardware.class,  Model_Hardware.find.query().where().eq("hardware_groups.id", id).order().desc("created").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_Hardware.class);

    }

    @JsonIgnore
    public List<Model_Hardware> getHardware() {
        try {

            List<Model_Hardware> hardwares = new ArrayList<>();

            for (UUID types : getHardwareIds()) {
                hardwares.add(Model_Hardware.getById(types));
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

        project.cache().add(this.getClass(), id);

        //  if create something under project
        //  if (project != null ) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo(Model_Project.class, project_id(), project_id()))).start();
    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete: Delete object Id: {} ", this.id);

        this.hardware.clear();
        this.deletePermanent();

        try {
            get_project().cache().remove(this.getClass(), id);
        } catch (_Base_Result_Exception e) {
           // Nothing
        }

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

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Create Permission is always JsonIgnore
    @JsonIgnore @Override @Transient public void check_create_permission() throws _Base_Result_Exception {  project.check_update_permission(); }
    @JsonIgnore @Override @Transient public void check_read_permission()   throws _Base_Result_Exception {  get_project().check_read_permission(); }
    @JsonIgnore @Override @Transient public void check_update_permission() throws _Base_Result_Exception {  get_project().check_update_permission(); }
    @JsonIgnore @Override @Transient public void check_delete_permission() throws _Base_Result_Exception {  get_project().check_update_permission(); }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_HardwareGroup.class)
    public static Cache<UUID, Model_HardwareGroup> cache;

    public static Model_HardwareGroup getById(UUID id) throws _Base_Result_Exception {

        Model_HardwareGroup group = cache.get(id);
        if (group == null) {

            group = find.byId(id);
            if (group == null) throw new Result_Error_NotFound(Model_Product.class);
            cache.put(id, group);
        }
        // Check Permission
        if(group.its_person_operation()) {
            group.check_read_permission();
        }

        return group;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_HardwareGroup> find = new Finder<>(Model_HardwareGroup.class);
}
