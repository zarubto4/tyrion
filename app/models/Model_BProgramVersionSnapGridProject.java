package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
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

    @JsonIgnore @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)      public Model_BProgramVersion b_program_version;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)      public Model_GridProject grid_project;
    @JsonIgnore @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "grid_project_program_snapshot") public List<Model_BProgramVersionSnapGridProjectProgram> grid_programs = new ArrayList<>(); // Verze M_Programu

/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_id() {
        return this.id;
    }

    @JsonProperty @ApiModelProperty(required = true)
    public Swagger_Short_Reference grid_project() {
        try {
            return new Swagger_Short_Reference(get_grid_project_id(), get_grid_project().name, get_grid_project().description);
        } catch (_Base_Result_Exception e) {
            // nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public List<Model_BProgramVersionSnapGridProjectProgram> grid_programs() {
        try {
            return get_grid_programs();

        } catch (_Base_Result_Exception e) {
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_grid_project_id() throws _Base_Result_Exception {

        if (idCache().get(Model_GridProject.class) == null) {
            idCache().add(Model_GridProject.class, (UUID) Model_GridProject.find.query().where().eq("snapshots.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_GridProject.class);

    }

    @JsonIgnore
    public Model_GridProject get_grid_project() throws _Base_Result_Exception {
        try {
            return Model_GridProject.find.byId(get_grid_project_id());
        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public List<UUID> get_grid_program_ids() {
        if (idCache().gets(Model_BProgramVersionSnapGridProjectProgram.class) == null) {
            idCache().add(Model_BProgramVersionSnapGridProjectProgram.class,  Model_BProgramVersionSnapGridProjectProgram.find.query().where().ne("deleted", true).eq("grid_project_program_snapshot.id", id).select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_BProgramVersionSnapGridProjectProgram.class) != null ?  idCache().gets(Model_BProgramVersionSnapGridProjectProgram.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_BProgramVersionSnapGridProjectProgram> get_grid_programs() {
        try {

            List<Model_BProgramVersionSnapGridProjectProgram> programs  = new ArrayList<>();

            for (UUID version_id : get_grid_program_ids()) {
                programs.add(Model_BProgramVersionSnapGridProjectProgram.find.byId(version_id));
            }

            return programs;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.trace("save :: Model_BProgramVersionSnapGridProject Creating new Object");
        super.save();

    }
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
        get_grid_project().check_update_permission();
    }

    @JsonIgnore @Override  @Transient public void check_update_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.BProgramVersionSnapGridProject_update.name())) return;
        get_grid_project().check_update_permission();
    }

    @JsonIgnore @Override  @Transient public void check_delete_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.BProgramVersionSnapGridProject_delete.name())) return;
        get_grid_project().check_update_permission();
    }

    public enum Permission { BProgramVersionSnapGridProject_create, BProgramVersionSnapGridProject_update, BProgramVersionSnapGridProject_read, BProgramVersionSnapGridProject_delete }


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_BProgramVersionSnapGridProject.class)
    public static CacheFinder<Model_BProgramVersionSnapGridProject> find = new CacheFinder<>(Model_BProgramVersionSnapGridProject.class);
}
