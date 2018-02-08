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
import utilities.logger.Logger;
import utilities.model.NamedModel;

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

    @JsonIgnore @OneToMany(mappedBy = "group", fetch = FetchType.LAZY) public List<Model_HardwareRegistration> hardware = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public Integer cache_group_size;
    @JsonIgnore @Transient @Cached public UUID cache_project_id;
    @JsonIgnore @Transient @Cached public List<UUID> cache_hardware_type_ids;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public int size() {

        if (cache_group_size == null) {
            cache_group_size = Model_HardwareRegistration.find.query().where().eq("group.id", this.id).findCount();
        }

        return cache_group_size;
    }

    @JsonProperty @ApiModelProperty(required = true)
    public List<Model_HardwareType> hardware_types() {
        return getHardwareTypes();
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<Model_HardwareType> getHardwareTypes() {
        try {

            // Cache
            if (cache_hardware_type_ids.isEmpty()) {

                List<Model_HardwareType> hardwareTypes = Model_HardwareType.find.query().where().eq("hardware.registration.group.id", id).orderBy("UPPER(name) ASC").select("id").findList();

                // Získání seznamu
                for (Model_HardwareType hardwareType : hardwareTypes) {
                    cache_hardware_type_ids.add(hardwareType.id);
                }
            }

            List<Model_HardwareType> hardwareTypes = new ArrayList<>();

            for (UUID hardware_type_id : cache_hardware_type_ids) {
                hardwareTypes.add(Model_HardwareType.getById(hardware_type_id));
            }

            return hardwareTypes;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public UUID project_id()           {

        if (cache_project_id == null) {
            Model_Project project = Model_Project.find.query().where().eq("hardware_groups.id", id).select("id").findOne();
            if (project == null) return null;
            cache_project_id = project.id;
        }

        return cache_project_id;
    }

    // TODO teoreticky cachovat?
    @JsonIgnore
    public List<UUID> getHardwareIds() {
        return Model_HardwareRegistration.find.query().where().eq("groups.id", id).findIds();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        super.save();

        project.cache_hardware_group_ids.add(id);

        //  if create something under project
        //  if (project != null ) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo(Model_Project.class, project_id(), project_id()))).start();
    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete: Delete object Id: {} ", this.id);

        // Case 1.1 :: We delete the object

        // Case 1.2 :: After Update - we send notification to frontend (Only if it is desirable)
        // new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( _Model_ExampleModelName.class, "project.id", "model.id"))).start();

        // Case 2.1 :: We delete the object with change of ORM parameter  @JsonIgnore  public boolean deleted;
        this.hardware.clear();

        this.deleted = true;
        this.update();

        if (project_id() != null) {
            Model_Project project = Model_Project.getById(project_id());
            if (project != null) project.cache_hardware_group_ids.remove(id);
        }

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
    @JsonIgnore   public boolean create_permission()  {  return  project != null && project.edit_permission(); }
    @JsonIgnore   public boolean read_permission()    {  return  project != null && project.read_permission(); }
    @JsonProperty public boolean update_permission()  {  return  project != null && project.update_permission(); }
    @JsonProperty public boolean edit_permission()    {  return  project != null && project.update_permission(); }
    @JsonProperty public boolean delete_permission()  {  return  project != null && project.update_permission(); }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_HardwareGroup.class)
    public static Cache<UUID, Model_HardwareGroup> cache;

    public static Model_HardwareGroup getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_HardwareGroup getById(UUID id) {

        Model_HardwareGroup group = cache.get(id);
        if (group == null) {

            group = find.byId(id);
            if (group == null) return null;

            cache.put(id, group);
        }

        return group;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_HardwareGroup> find = new Finder<>(Model_HardwareGroup.class);
}
