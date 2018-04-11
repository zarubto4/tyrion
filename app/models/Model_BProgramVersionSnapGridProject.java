package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.swagger.output.Swagger_Short_Reference;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "BProgramVersionSnapGridProject",  description = "Model of Snapshot of versions of M_Project Snapshots")
@Table(name="BPVersionSnapGridProject")
public class Model_BProgramVersionSnapGridProject extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_BProgramVersionSnapGridProject.class);

/* DATABASE VALUE  ----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)      public Model_BProgramVersion b_program_version;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)      public Model_GridProject grid_project;
    @JsonProperty @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "grid_project_program_snapshot") public List<Model_BProgramVersionSnapGridProjectProgram> grid_programs = new ArrayList<>(); // Verze M_Programu

/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/


    @JsonProperty @ApiModelProperty(required = true)
    public Swagger_Short_Reference grid_project() {
        try {
            return new Swagger_Short_Reference(grid_project.id, grid_project.name, grid_project.description);
        } catch (_Base_Result_Exception e) {
            // nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override @Transient public void check_create_permission() throws _Base_Result_Exception  {
        if(_BaseController.person().has_permission(Permission.BProgramVersionSnapGridProject_create.name())) return;
        grid_project.check_update_permission();
    }

    @JsonIgnore @Override  @Transient public void check_read_permission() throws _Base_Result_Exception  {
        if(_BaseController.person().has_permission(Permission.BProgramVersionSnapGridProject_read.name())) return;
        grid_project.check_update_permission();
    }

    @JsonIgnore @Override  @Transient public void check_update_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.BProgramVersionSnapGridProject_update.name())) return;
        grid_project.check_update_permission();
    }

    @JsonIgnore @Override  @Transient public void check_delete_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.BProgramVersionSnapGridProject_delete.name())) return;
        grid_project.check_update_permission();
    }

    public enum Permission { BProgramVersionSnapGridProject_create, BProgramVersionSnapGridProject_update, BProgramVersionSnapGridProject_read, BProgramVersionSnapGridProject_delete }


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_BProgramVersionSnapGridProject.class, duration = CacheField.DayCacheConstant)
    public static Cache<UUID, Model_BProgramVersionSnapGridProject> cache;

    public static Model_BProgramVersionSnapGridProject getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_BProgramVersionSnapGridProject getById(UUID id) throws _Base_Result_Exception {

        Model_BProgramVersionSnapGridProject snapShot = cache.get(id);

        if (snapShot == null) {

            snapShot = Model_BProgramVersionSnapGridProject.find.byId(id);
            if (snapShot == null) throw new Result_Error_NotFound(Model_BProgramVersionSnapGridProject.class);

            cache.put(id, snapShot);
        }
        // Check Permission
        if(snapShot.its_person_operation()) {
            snapShot.check_read_permission();
        }
        return snapShot;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_BProgramVersionSnapGridProject> find = new Finder<>(Model_BProgramVersionSnapGridProject.class);
}
