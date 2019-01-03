package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.permission.Action;
import utilities.permission.Permissible;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@ApiModel(value = "BProgram", description = "Model of BProgram")
@Table(name="BProgram")
public class Model_BProgram extends TaggedModel implements Permissible, UnderProject {

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
        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID getProjectId() {

        if (idCache().get(Model_Project.class) == null) {
            idCache().add(Model_Project.class, Model_Project.find.query().where().eq("b_programs.id", id).select("id").findSingleAttributeList());
        }

        return idCache().get(Model_Project.class);
    }

    @JsonIgnore
    public Model_Project getProject() {
        return isLoaded("project") ? this.project : Model_Project.find.query().nullable().where().eq("b_programs.id", id).findOne();
    }

    @JsonIgnore
    public List<UUID> getVersionsIds() {

        if (idCache().gets(Model_BProgramVersion.class) == null) {
            idCache().add(Model_BProgramVersion.class,  Model_BProgramVersion.find.query().where().ne("deleted", true).eq("b_program.id", id).order().desc("created").select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_BProgramVersion.class) != null ?  idCache().gets(Model_BProgramVersion.class) : new ArrayList<>();
    }

    @JsonIgnore
    public void sort_Model_Model_BProgramVersion_ids() {
        List<Model_BProgramVersion> versions = getVersions();
        this.idCache().removeAll(Model_BProgramVersion.class);
        versions.stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())
                .forEach(o -> this.idCache().add(Model_BProgramVersion.class, o.id));
    }


    @JsonIgnore
    public List<Model_BProgramVersion> getVersions() {
        try {

            List<Model_BProgramVersion> list = new ArrayList<>();

            for (UUID id : getVersionsIds() ) {
                list.add(Model_BProgramVersion.find.byId(id));
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

        if (getProject() != null) {
            project.idCache().add(this.getClass(), this.id);
        }
    }

    @JsonIgnore @Override
    public boolean delete() {

        if (getProject() != null) {
            getProject().idCache().remove(this.getClass(), id);
        }

        return super.delete();
    }

/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public String get_path() {
        return getProject().getPath() + "/b-programs/" + this.id;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.BLOCKO_PROGRAM;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_BProgram.class)
    public static CacheFinder<Model_BProgram> find = new CacheFinder<>(Model_BProgram.class);
}
