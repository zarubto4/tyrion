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
            return get_grid_project().ref();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public List<Model_BProgramVersionSnapGridProjectProgram> grid_programs() {
        try {
            return get_grid_programs();

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_grid_project_id() {

        if (idCache().get(Model_GridProject.class) == null) {
            idCache().add(Model_GridProject.class, (UUID) Model_GridProject.find.query().where().eq("snapshots.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_GridProject.class);

    }

    @JsonIgnore
    public Model_GridProject get_grid_project() {
        return isLoaded("grid_project") ? grid_project : Model_GridProject.find.query().nullable().where().eq("snapshots.id", id).findOne();
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

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_BProgramVersionSnapGridProject.class)
    public static CacheFinder<Model_BProgramVersionSnapGridProject> find = new CacheFinder<>(Model_BProgramVersionSnapGridProject.class);
}
