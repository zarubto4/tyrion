package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.models_update_echo.EchoHandler;
import utilities.swagger.output.Swagger_M_Program_Version_Interface;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@ApiModel(value = "GridProgram", description = "Model of GridProgram")
@Table(name = "GridProgram")
public class Model_GridProgram extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_GridProgram.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                                                      public Model_GridProject grid_project;
    @JsonIgnore @OneToMany(mappedBy="grid_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_GridProgramVersion> versions = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) public List<Model_GridProgramVersion> program_versions() {
        try{
            return get_versions().stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList());

        } catch (_Base_Result_Exception e){
            //nothing
            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public UUID get_grid_project_id() throws _Base_Result_Exception {

        if (cache().get(Model_GridProject.class) == null) {
            cache().add(Model_GridProject.class, (UUID) Model_GridProject.find.query().where().eq("grid_programs.id", id).select("id").findSingleAttribute());
        }

        return cache().get(Model_GridProject.class);
    }

    @JsonIgnore @Transient public Model_GridProject get_grid_project() throws _Base_Result_Exception  {
        return  Model_GridProject.getById(get_grid_project_id());
    }

    @JsonIgnore @Transient public List<UUID> get_versionsId() {
        if (cache().gets(Model_GridProgramVersion.class) == null) {
            cache().add(Model_GridProgramVersion.class,  Model_GridProgramVersion.find.query().where().eq("grid_program.id", id).select("id").findSingleAttributeList());
        }

        return cache().gets(Model_GridProgramVersion.class);
    }

    @JsonIgnore @Transient public List<Model_GridProgramVersion> get_versions() {
        try {

            List<Model_GridProgramVersion> versions  = new ArrayList<>();

            for (UUID version_id : get_versionsId()) {
                versions.add(Model_GridProgramVersion.getById(version_id));
            }

            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore @Transient public List<Swagger_M_Program_Version_Interface> program_versions_interface() {
        try {

            List<Swagger_M_Program_Version_Interface> versions = new ArrayList<>();

            for (Model_GridProgramVersion v : get_versions()) {
                Swagger_M_Program_Version_Interface help = new Swagger_M_Program_Version_Interface();
                help.version = v;
                help.virtual_input_output = v.m_program_virtual_input_output;
                versions.add(help);
            }
            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        this.blob_link = grid_project.get_path()  + "/grid-programs/"  + UUID.randomUUID();

        super.save();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, grid_project.get_project_id(), grid_project.id));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

        if (grid_project != null) {
            grid_project.cache().add(this.getClass(), id);
        } else {
            get_grid_project().cache().add(this.getClass(), id);
        }

        cache.put(this.id, this);
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);

        super.update();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo( Model_GridProgram.class, get_grid_project().get_project_id(), id));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();
    }

    @JsonIgnore @Override
    public boolean delete() {
        logger.debug("update :: Delete object Id: {} ", this.id);

        super.delete();

        try {
            get_grid_project().cache().remove(this.getClass(), id);
        }catch (_Base_Result_Exception e){
            // Nothing
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo( Model_GridProject.class, get_grid_project().get_project_id(), get_grid_project_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    private String blob_link;

    @JsonIgnore
    public String get_path() {
        return blob_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void  check_create_permission() throws _Base_Result_Exception {
        if (_BaseController.person().has_permission(Permission.GridProgram_create.name())) return;
        grid_project.check_update_permission();
    }

    @JsonProperty
    public void check_update_permission() throws _Base_Result_Exception  {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (_BaseController.person().has_permission("grid_program_update_" + id)) _BaseController.person().valid_permission("grid_program_update_" + id);
        if (_BaseController.person().has_permission(Permission.GridProgram_update.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_GridProgram.find.query().where().eq("grid_project.project.participants.person.id", _BaseController.person().id).eq("id", id).findCount() > 0) {
            _BaseController.person().cache_permission("grid_program_update_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission("grid_program_update_" + id, false);
        throw new Result_Error_PermissionDenied();
    }

    @JsonIgnore
    public void check_read_permission() throws _Base_Result_Exception {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (_BaseController.person().has_permission("grid_program_read_" + id)) _BaseController.person().valid_permission("grid_program_read_" + id);
        if (_BaseController.person().has_permission(Permission.GridProgram_read.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if (Model_GridProgram.find.query().where().eq("grid_project.project.participants.person.id", _BaseController.person().id).eq("id", id).findCount() > 0) {
            _BaseController.person().cache_permission("grid_program_read_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission("grid_program_read_" + id, false);
        throw new Result_Error_PermissionDenied();
    }

    @JsonProperty
    public void check_delete_permission() throws _Base_Result_Exception  {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (_BaseController.person().has_permission("grid_program_delete_" + id)) _BaseController.person().valid_permission("grid_program_delete_" + id);
        if (_BaseController.person().has_permission(Permission.GridProgram_delete.name())) return;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_GridProgram.find.query().where().eq("grid_project.project.participants.person.id", _BaseController.person().id).eq("id", id).findCount() > 0) {
            _BaseController.person().cache_permission("grid_program_delete_" + id, true);
            return;
        }

        // Přidávám do listu false a vracím false
        _BaseController.person().cache_permission("grid_program_delete_" + id, false);
        throw new Result_Error_PermissionDenied();
    }

    public enum Permission {GridProgram_create, GridProgram_update, GridProgram_read, GridProgram_edit, GridProgram_delete}


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_GridProgram.class)
    public static Cache<UUID, Model_GridProgram> cache;
    
    public static Model_GridProgram getById(UUID id) throws _Base_Result_Exception {

        Model_GridProgram m_program = cache.get(id);
        if (m_program == null) {

            m_program = Model_GridProgram.find.byId(id);
            if (m_program == null) throw new Result_Error_NotFound(Model_Widget.class);

            cache.put(id, m_program);
        }

        // Check Permission
        if(m_program.its_person_operation()) {
            m_program.check_read_permission();
        }

        return m_program;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_GridProgram> find = new Finder<>(Model_GridProgram.class);
}