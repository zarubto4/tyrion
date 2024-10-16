package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.swagger.output.Swagger_Short_Reference;

import javax.persistence.*;
import java.util.*;

@Entity
@ApiModel(value = "BProgramVersionSnapGridProjectProgram", description = "")
@Table(name="BPVersionSnapGridProgram")
public class Model_BProgramVersionSnapGridProjectProgram extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_BProgramVersionSnapGridProjectProgram.class);

/* DATABASE VALUE  ----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_BProgramVersionSnapGridProject grid_project_program_snapshot;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_GridProgramVersion grid_program_version;


    // Don't return ID of this object throw Swagger
    @JsonIgnore
    public UUID get_id() {
        return this.id;
    }

/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public Swagger_Short_Reference grid_program() {
        try {
            return get_grid_version_program().getGridProgram().ref();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public Swagger_Short_Reference grid_program_version() {
        try {
            return get_grid_version_program().ref();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_grid_program_version_id() {

        if (idCache().get(Model_GridProgramVersion.class) == null) {
            idCache().add(Model_GridProgramVersion.class, (UUID) Model_GridProgramVersion.find.query().where().eq("m_program_instance_parameters.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_GridProgramVersion.class);

    }

    @JsonIgnore
    public Model_GridProgramVersion get_grid_version_program() {
        return isLoaded("grid_program_version") ? grid_program_version : Model_GridProgramVersion.find.query().where().eq("m_program_instance_parameters.id", id).findOne();
    }

    @JsonIgnore
    public UUID get_b_program_grid_version_id() {

        if (idCache().get(Model_BProgramVersionSnapGridProject.class) == null) {
            idCache().add(Model_BProgramVersionSnapGridProject.class, (UUID) Model_BProgramVersionSnapGridProject.find.query().where().eq("grid_programs.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_BProgramVersionSnapGridProject.class);

    }

    @JsonIgnore
    public Model_BProgramVersionSnapGridProject get_b_program_grid_version() {
        try {
            return Model_BProgramVersionSnapGridProject.find.byId(get_b_program_grid_version_id());
        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* Helper Class --------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_BProgramVersionSnapGridProjectProgram.class)
    public static CacheFinder<Model_BProgramVersionSnapGridProjectProgram> find = new CacheFinder<>(Model_BProgramVersionSnapGridProjectProgram.class);
}
