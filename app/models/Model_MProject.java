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
import utilities.model.NamedModel;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "M_Project", description = "Model of M_Project")
@Table(name="MProject")
public class Model_MProject extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_MProject.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)
    public Model_Project project;

    @JsonIgnore @OneToMany(mappedBy = "m_project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Model_MProjectProgramSnapShot> snapShots = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy = "m_project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Model_MProgram> m_programs = new ArrayList<>();

    @ManyToMany public List<Model_Tag> tags = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_project_id;
    @JsonIgnore @Transient @Cached public List<UUID> m_programs_ids = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) public UUID project_id() {
        return project.id;
    }

    @JsonProperty @ApiModelProperty(required = true) public List<Model_MProgram> m_programs() { return get_m_programs_not_deleted();}

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* GET SQL PARAMETER - CACHE OBJECTS ------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<Model_MProgram> get_m_programs_not_deleted() {
        try {

            if (m_programs_ids.isEmpty()) {

                List<Model_MProgram> m_programs =  Model_MProgram.find.query().where().eq("m_project.id", id).eq("deleted", false).orderBy("UPPER(name) ASC").select("id").findList();

                // Získání seznamu
                for (Model_MProgram m_program : m_programs) {
                    m_programs_ids.add(m_program.id);
                }

            }

            List<Model_MProgram> m_programs  = new ArrayList<>();

            for (UUID version_id : m_programs_ids) {
                m_programs.add(Model_MProgram.getById(version_id));
            }

            return m_programs;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        this.azure_m_project_link = project.get_path()  + "/m-projects/"  + UUID.randomUUID().toString();

        super.save();

        if (project != null) {
            project.cache_m_project_ids.add(id);
        }

        cache.put(id, this);

        if (project != null ) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, project_id(), project_id()))).start();

    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);

        super.update();

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_MProject.class, project_id(), id))).start();

    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("update :: Delete object Id: {} ", this.id);

        deleted = true;
        super.update();

        if (project_id() != null) {
            Model_Project.getById( project_id() ).cache_m_project_ids.remove(id);
        }

        cache.remove(id);

        if (project_id() != null ) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, project_id(), project_id()))).start();

        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    private String azure_m_project_link;

    @JsonIgnore
    public String get_path() {
        return  azure_m_project_link;
    }


/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.read_permission = true, you can read M_project on this Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create M_project on this Project - Or you need static/dynamic permission key";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public boolean create_permission() {
        if (BaseController.person().has_permission("MProject_create")) return true;
        return (project.update_permission());
    }
    @JsonProperty
    public boolean update_permission() {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("m_project_update_" + id)) return BaseController.person().has_permission("m_project_update_"+ id);
        if (BaseController.person().has_permission("MProject_update")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_MProject.find.query().where().where().eq("project.participants.person.id", BaseController.person().id ).where().eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("m_project_update_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("m_project_update_" + id, false);
        return false;

    }
    @JsonIgnore
    public boolean read_permission() {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("m_project_read_" + id)) return BaseController.person().has_permission("m_project_read_"+ id);
        if (BaseController.person().has_permission("MProject_read")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_MProject.find.query().where().where().eq("project.participants.person.id", BaseController.person().id ).where().eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("m_project_m_project_read_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("m_project_read_" + id, false);
        return false;

    }
    @JsonProperty
    public boolean edit_permission() {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("m_project_edit_" + id)) return BaseController.person().has_permission("m_project_edit_"+ id);
        if (BaseController.person().has_permission("MProject_edit")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_MProject.find.query().where().where().eq("project.participants.person.id", BaseController.person().id ).where().eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("m_project_edit_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("m_project_edit_" + id, false);
        return false;

    }
    @JsonProperty
    public boolean delete_permission() {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("m_project_delete_" + id)) return BaseController.person().has_permission("m_project_delete_"+ id);
        if (BaseController.person().has_permission("MProject_delete")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_MProject.find.query().where().where().eq("project.participants.person.id", BaseController.person().id ).where().eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("m_project_delete_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("m_project_delete_" + id, false);
        return false;

    }
    public enum Permission { MProject_create, MProject_read, MProject_update, MProject_edit, MProject_delete}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_MProject.class)
    public static Cache<UUID, Model_MProject> cache;

    public static Model_MProject getById(String id) {
        return getById(UUID.fromString(id));
    }
    
    public static Model_MProject getById(UUID id) {

        Model_MProject m_project= cache.get(id);
        if (m_project == null) {

            m_project = Model_MProject.find.byId(id);
            if (m_project == null) return null;

            cache.put(id, m_project);
        }

        return m_project;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    
    public static Finder<UUID, Model_MProject> find = new Finder<>(Model_MProject.class);
}

