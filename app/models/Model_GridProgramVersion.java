package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import play.libs.Json;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.logger.Logger;
import utilities.model.UnderProject;
import utilities.model.VersionModel;
import utilities.permission.Action;
import utilities.permission.Permissible;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "GridProgramVersion", description = "Model of GridProgramVersion")
@Table(name="GridProgramVersion")
public class Model_GridProgramVersion extends VersionModel implements Permissible, UnderProject {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_GridProgramVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) public Model_GridProgram grid_program;
    @JsonProperty @Column(columnDefinition = "TEXT")  public String m_program_virtual_input_output;

    @JsonIgnore @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "grid_program_version") public List<Model_BProgramVersionSnapGridProjectProgram> m_program_instance_parameters = new ArrayList<>();

    public boolean public_access;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

/* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient public String link_to_download() {
        try {

            Model_Blob fileRecord = Model_Blob.find.query().nullable().where().eq("grid_program_version.id", id).eq("name", "grid_program.json").findOne();
            return fileRecord.link;


        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public Model_Project getProject() {
        return this.getGridProgram().getProject();
    }

    @JsonIgnore
    public UUID get_grid_program_id() {


        if (idCache().get(Model_GridProgram.class) == null) {
            idCache().add(Model_GridProgram.class, (UUID) Model_GridProgram.find.query().where().eq("versions.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_GridProgram.class);
    }

    @JsonIgnore
    public Model_GridProgram getGridProgram() {
        return isLoaded("grid_program") ? this.grid_program : Model_GridProgram.find.query().nullable().where().eq("versions.id", id).findOne();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        super.save();

        Model_GridProgram program = getGridProgram();
        program.idCache().add(this.getClass(), this.getId());
        program.sort_Model_Model_GridProgramVersion_ids();
    }

    @JsonIgnore @Override
    public boolean delete() {

        getGridProgram().idCache().remove(this.getClass(), this.getId());

        return super.delete();
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public String get_path() {
        if (getGridProgram() != null) {
            return getGridProgram().get_path() + "/version/" + this.getId();

        } else {
            return getGridProgram().get_path() + "/version/" + this.getId();
        }
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.GRID_PROGRAM_VERSION;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_GridProgramVersion.class)
    public static CacheFinder<Model_GridProgramVersion> find = new CacheFinder<>(Model_GridProgramVersion.class);
}
