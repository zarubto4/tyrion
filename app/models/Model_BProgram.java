package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import org.ehcache.Cache;
import utilities.cache.CacheField;
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
import java.util.stream.Collectors;

@Entity
@ApiModel(value = "BProgram", description = "Model of BProgram")
@Table(name="BProgram")
public class Model_BProgram extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_BProgram.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;
    @JsonIgnore @OneToMany(mappedBy="b_program", cascade=CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_BProgramVersion> versions = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="b_program", cascade=CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_Instance>   instances    = new ArrayList<>(); // Dont used that, its only short reference fo rnew Instance

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty
    public List<Model_BProgramVersion> program_versions() {
        try {

            return getVersions();

        } catch(_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

/* GET Variable short type of objects ----------------------------------------------------------------------------------*/


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID getProjectId() {

        if (cache().get(Model_Project.class) == null) {
            cache().add(Model_Project.class, Model_Project.find.query().where().eq("b_programs.id", id).select("id").findSingleAttributeList());
        }

        return cache().get(Model_Project.class);
    }

    @JsonIgnore
    public Model_Project getProject() {
        try {
            return Model_Project.getById(getProjectId());
        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public List<UUID> getVersionsIds() {

        if (cache().gets(Model_BProgramVersion.class) == null) {
            cache().add(Model_BProgramVersion.class,  Model_BProgramVersion.find.query().where().ne("deleted", true).eq("b_program.id", id).order().desc("created").select("id").findSingleAttributeList());
        }

        return cache().gets(Model_BProgramVersion.class) != null ?  cache().gets(Model_BProgramVersion.class) : new ArrayList<>();
    }

    @JsonIgnore
    public void sort_Model_Model_BProgramVersion_ids() {
        List<Model_BProgramVersion> versions = getVersions();
        this.cache().removeAll(Model_BProgramVersion.class);
        versions.stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())
                .forEach(o -> this.cache().add(Model_BProgramVersion.class, o.id));
    }


    @JsonIgnore
    public List<Model_BProgramVersion> getVersions() {
        try {

            List<Model_BProgramVersion> list = new ArrayList<>();

            for (UUID id : getVersionsIds() ) {
                list.add(Model_BProgramVersion.getById(id));
            }

            return list;
        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        super.save();

        Model_Project project = getProject();

        if(project != null) {
            project.cache().add(this.getClass(), this.id);
        }

        cache.put(this.id, this);

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, project.id, project.id))).start();
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update - Update object Id: {}",  this.id);

        super.update();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo( Model_BProgram.class, getProjectId(), id));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("update :: Delete object Id: {} ", this.id);
        super.delete();

        // Remove from Project Cache
        try {
            getProject().cache().remove(this.getClass(), id);
        } catch (_Base_Result_Exception e) {
            // Nothing
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, getProjectId(), getProjectId()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();


        return false;
    }

/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public String get_path() {
        return getProject().getPath() + "/b-programs/" + this.id;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override @Transient
    public void check_create_permission() throws _Base_Result_Exception {
        if (_BaseController.person().has_permission(Permission.BProgram_create.name())) return;
        project.check_update_permission();
    }

    @JsonIgnore @Override @Transient
    public void check_update_permission() throws _Base_Result_Exception {
        try {
            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_update_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_update_" + id);
                return;
            }

            if (_BaseController.person().has_permission(Permission.BProgram_update.name())) return;

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
            this.getProject().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    @JsonIgnore @Override @Transient
    public void check_read_permission() throws _Base_Result_Exception  {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_read_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_read_" + id);
                return;
            }

            if (_BaseController.person().has_permission(Permission.BProgram_read.name())) return;

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
            this.getProject().check_read_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, false);
            throw new Result_Error_PermissionDenied();
        }

    }

    @JsonIgnore @Override @Transient
    public void check_delete_permission() throws _Base_Result_Exception {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_delete_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_delete_" + id);
                return;
            }

            if (_BaseController.person().has_permission(Permission.BProgram_delete.name())) return;

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
            this.getProject().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
     }

     // Statické univerzální klíče
    public enum Permission { BProgram_create, BProgram_read, BProgram_update, BProgram_edit, BProgram_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_BProgram.class)
    public static Cache<UUID, Model_BProgram> cache;

    public static Model_BProgram getById(UUID id) throws _Base_Result_Exception {

        Model_BProgram b_program = cache.get(id);
        if (b_program == null) {

            b_program = find.byId(id);
            if (b_program == null) throw new Result_Error_NotFound(Model_BProgram.class);

            cache.put(id, b_program);
        }
        // Check Permission
        if(b_program.its_person_operation()) {
            b_program.check_read_permission();
        }

        return b_program;
    }


    /* FINDER --------------------------------------------------------------------------------------------------------------*/
     
    public static Finder<UUID, Model_BProgram> find = new Finder<>(Model_BProgram.class);

}

