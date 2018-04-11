package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.Server;
import utilities.cache.CacheField;
import utilities.enums.GridAccess;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.swagger.input.Swagger_GridWidgetVersion_GridApp_source;
import utilities.swagger.output.Swagger_Short_Reference;

import javax.persistence.*;
import javax.validation.Valid;
import java.util.*;

@Entity
@ApiModel(value = "BProgramVersionSnapGridProjectProgram", description = "")
@Table(name="BPVersionSnapGridProgram")
public class Model_BProgramVersionSnapGridProjectProgram extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_BProgramVersionSnapGridProjectProgram.class);

/* DATABASE VALUE  ----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne public Model_BProgramVersionSnapGridProject grid_project_program_snapshot;
    @JsonIgnore @ManyToOne public Model_GridProgramVersion grid_program_version;

/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public Swagger_Short_Reference grid_program() {
        try {
            return new Swagger_Short_Reference(grid_program_version.get_grid_program().id, grid_program_version.get_grid_program().name, grid_program_version.get_grid_program().description);
        } catch (_Base_Result_Exception e) {
            // nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public Swagger_Short_Reference grid_program_version() {
        try {
            return new Swagger_Short_Reference(grid_program_version.id, grid_program_version.name, grid_program_version.description);

        }catch (_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {

            logger.internalServerError(e);
            return null;
        }
    }


/* JSON IGNORE  ---------------------------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
        super.save();
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* Helper Class --------------------------------------------------------------------------------------------------------*/


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override @Transient public void check_create_permission() throws _Base_Result_Exception  {
        if(_BaseController.person().has_permission(Permission.BProgramVersionSnapGridProjectProgram_create.name())) return;
        grid_project_program_snapshot.check_update_permission();
    }

    @JsonIgnore @Override  @Transient public void check_read_permission() throws _Base_Result_Exception  {
        if(_BaseController.person().has_permission(Permission.BProgramVersionSnapGridProjectProgram_read.name())) return;
        grid_project_program_snapshot.check_update_permission();

    }

    @JsonIgnore @Override  @Transient public void check_update_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.BProgramVersionSnapGridProjectProgram_update.name())) return;
        grid_project_program_snapshot.check_update_permission();
    }

    @JsonIgnore @Override  @Transient public void check_delete_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.BProgramVersionSnapGridProjectProgram_delete.name())) return;
        grid_project_program_snapshot.check_update_permission();
    }

    public enum Permission { BProgramVersionSnapGridProjectProgram_create, BProgramVersionSnapGridProjectProgram_update, BProgramVersionSnapGridProjectProgram_read, BProgramVersionSnapGridProjectProgram_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_BProgramVersionSnapGridProjectProgram.class)
    public static Cache<UUID, Model_BProgramVersionSnapGridProjectProgram> cache;

    public static Model_BProgramVersionSnapGridProjectProgram getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_BProgramVersionSnapGridProjectProgram getById(UUID id) throws _Base_Result_Exception {
        Model_BProgramVersionSnapGridProjectProgram instanceParameter = cache.get(id);
        if (instanceParameter == null) {

            instanceParameter = find.query().where().idEq(id).eq("deleted", false).findOne();
            if (instanceParameter == null) throw new Result_Error_NotFound(Model_BProgramVersionSnapGridProjectProgram.class);

            cache.put(id, instanceParameter);
        }
        // Check Permission
        if(instanceParameter.its_person_operation()) {
            instanceParameter.check_read_permission();
        }

        return instanceParameter;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_BProgramVersionSnapGridProjectProgram> find = new Finder<>(Model_BProgramVersionSnapGridProjectProgram.class);
}
