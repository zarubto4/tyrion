package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "GridProject", description = "Model of GridProject")
@Table(name="GridProject")
public class Model_GridProject extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_GridProject.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)
    public Model_Project project;

    @JsonIgnore @OneToMany(mappedBy = "grid_project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Model_MProjectProgramSnapShot> snapShots = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy = "grid_project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Model_GridProgram> grid_programs = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_project_id;
    @JsonIgnore @Transient @Cached public List<UUID> grid_programs_ids = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) public UUID project_id() {
        return project.id;
    }

    @JsonProperty @ApiModelProperty(required = true) public List<Model_GridProgram> m_programs() { return getGridPrograms();}

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* GET SQL PARAMETER - CACHE OBJECTS ------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<Model_GridProgram> getGridPrograms() {
        try {

            if (grid_programs_ids.isEmpty()) {

                List<Model_GridProgram> gridPrograms =  Model_GridProgram.find.query().where().eq("grid_project.id", id).orderBy("UPPER(name) ASC").select("id").findList();

                // Získání seznamu
                for (Model_GridProgram gridProgram : gridPrograms) {
                    grid_programs_ids.add(gridProgram.id);
                }
            }

            List<Model_GridProgram> gridPrograms  = new ArrayList<>();

            for (UUID version_id : grid_programs_ids) {
                gridPrograms.add(Model_GridProgram.getById(version_id));
            }

            return gridPrograms;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        this.blob_link = project.getPath()  + "/grid-projects/"  + UUID.randomUUID().toString();

        super.save();

        if (project != null) {
            project.cache_grid_project_ids.add(id);
        }

        cache.put(id, this);

        if (project != null ) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, project_id(), project_id()))).start();

    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);

        super.update();

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_GridProject.class, project_id(), id))).start();

    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("update :: Delete object Id: {} ", this.id);

        deleted = true;
        super.update();

        if (project_id() != null) {
            Model_Project.getById( project_id() ).cache_grid_project_ids.remove(id);
        }

        cache.remove(id);

        if (project_id() != null ) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, project_id(), project_id()))).start();

        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    private String blob_link;

    @JsonIgnore
    public String get_path() {
        return blob_link;
    }


/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.read_permission = true, you can read M_project on this Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create M_project on this Project - Or you need static/dynamic permission key";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public boolean create_permission() {
        if (BaseController.person().has_permission("GridProject_create")) return true;
        return (project.update_permission());
    }
    @JsonProperty
    public boolean update_permission() {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("grid_project_update_" + id)) return BaseController.person().has_permission("grid_project_update_"+ id);
        if (BaseController.person().has_permission("GridProject_update")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_GridProject.find.query().where().where().eq("project.participants.person.id", BaseController.personId()).where().eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("grid_project_update_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("grid_project_update_" + id, false);
        return false;

    }
    @JsonIgnore
    public boolean read_permission() {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("grid_project_read_" + id)) return BaseController.person().has_permission("grid_project_read_"+ id);
        if (BaseController.person().has_permission("GridProject_read")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_GridProject.find.query().where().where().eq("project.participants.person.id", BaseController.personId()).where().eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("grid_project_read_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("grid_project_read_" + id, false);
        return false;

    }
    @JsonProperty
    public boolean edit_permission() {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("grid_project_edit_" + id)) return BaseController.person().has_permission("grid_project_edit_"+ id);
        if (BaseController.person().has_permission("GridProject_edit")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_GridProject.find.query().where().where().eq("project.participants.person.id", BaseController.personId()).where().eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("grid_project_edit_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("grid_project_edit_" + id, false);
        return false;

    }
    @JsonProperty
    public boolean delete_permission() {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("grid_project_delete_" + id)) return BaseController.person().has_permission("grid_project_delete_"+ id);
        if (BaseController.person().has_permission("GridProject_delete")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_GridProject.find.query().where().where().eq("project.participants.person.id", BaseController.personId()).where().eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("grid_project_delete_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("grid_project_delete_" + id, false);
        return false;

    }
    public enum Permission { GridProject_create, GridProject_read, GridProject_update, GridProject_edit, GridProject_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_GridProject.class)
    public static Cache<UUID, Model_GridProject> cache;

    public static Model_GridProject getById(String id) {
        return getById(UUID.fromString(id));
    }
    
    public static Model_GridProject getById(UUID id) {

        Model_GridProject grid_project= cache.get(id);
        if (grid_project == null) {

            grid_project = Model_GridProject.find.byId(id);
            if (grid_project == null) return null;

            cache.put(id, grid_project);
        }

        return grid_project;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    
    public static Finder<UUID, Model_GridProject> find = new Finder<>(Model_GridProject.class);
}

