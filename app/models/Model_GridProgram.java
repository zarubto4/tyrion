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
        try {
            return get_versions().stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList());

        } catch (_Base_Result_Exception e) {
            //nothing
            return null;
        } catch (Exception e) {
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
            cache().add(Model_GridProgramVersion.class,  Model_GridProgramVersion.find.query().where().ne("deleted", true).eq("grid_program.id", id).select("id").findSingleAttributeList());
        }

        return cache().gets(Model_GridProgramVersion.class) != null ?  cache().gets(Model_GridProgramVersion.class) : new ArrayList<>();
    }

    @JsonIgnore
    public void sort_Model_Model_GridProgramVersion_ids() {

        List<Model_GridProgramVersion> versions = get_versions();
        this.cache().removeAll(Model_GridProgramVersion.class);
        versions.stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())
                .forEach(o -> this.cache().add(Model_GridProgramVersion.class, o.id));

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

        super.save();



        Model_GridProject grid_project = get_grid_project();
        if (grid_project != null) {
            grid_project.getGrid_programs_ids();
            grid_project.cache().add(this.getClass(), id);
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, grid_project.get_project_id(), grid_project.id));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

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

    @JsonIgnore @Deprecated
    private String blob_link; // TODO smazat z Databáze

    @JsonIgnore
    public String get_path() {


        // FOR OLD already created objects is still using blob_link, but its @Deprecated
        if(blob_link != null) return blob_link;
        return get_grid_project().get_path() + "/grid-programs/" + this.id;

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
        try {

            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_update_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_update_" + id);
                return;
            }

            if (_BaseController.person().has_permission(Permission.GridProgram_delete.name())) return;

            get_grid_project().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    @JsonIgnore
    public void check_read_permission() throws _Base_Result_Exception {
        try {

            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_read_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_read_" + id);
                return;
            }

            if (_BaseController.person().has_permission(Permission.GridProgram_read.name())) return;

            get_grid_project().check_read_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    @JsonProperty
    public void check_delete_permission() throws _Base_Result_Exception  {
        try {

            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_delete_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_delete_" + id);
                return;
            }

            if (_BaseController.person().has_permission(Permission.GridProgram_delete.name())) return;

            get_grid_project().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
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