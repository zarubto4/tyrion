package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.enums.ProgramType;
import utilities.logger.Logger;
import utilities.model.Publishable;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.permission.Action;
import utilities.permission.JsonPermission;
import utilities.permission.Permissible;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Library", description = "Model of Library")
@Table(name="Library")
public class Model_Library extends TaggedModel implements Permissible, UnderProject, Publishable {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Library.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) public Model_Project project;

    public ProgramType publish_type;


    @ManyToMany(fetch = FetchType.LAZY) public List<Model_HardwareType> hardware_types = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy = "library", cascade = CascadeType.ALL, fetch = FetchType.LAZY) @OrderBy("created DESC") public List<Model_LibraryVersion> versions = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


    @JsonProperty
    public List<Model_LibraryVersion> versions() {
        try {
            return this.getVersions();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID getProjectId() {

        if (idCache().get(Model_Project.class) == null) {
            idCache().add(Model_Project.class, (UUID) Model_Project.find.query().where().eq("libraries.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_Project.class);
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return isLoaded("project") ? project : Model_Project.find.query().nullable().where().eq("libraries.id", id).findOne();
    }

    @JsonIgnore
    public List<UUID> getVersionIds() {
        if (idCache().gets(Model_LibraryVersion.class) == null) {
            idCache().add(Model_LibraryVersion.class, Model_LibraryVersion.find.query().where().eq("library.id", id).eq("deleted", false).order().desc("created").select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_LibraryVersion.class) != null ?  idCache().gets(Model_LibraryVersion.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_LibraryVersion> getVersions() {
        try {

            List<Model_LibraryVersion> versions  = new ArrayList<>();

            for (UUID version_id : getVersionIds()) {
                versions.add(Model_LibraryVersion.find.byId(version_id));
            }

            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }


    @JsonIgnore
    public List<UUID> getHardwareTypesId() {
        if (idCache().gets(Model_HardwareType.class) == null) {
            idCache().add(Model_HardwareType.class, Model_HardwareType.find.query().where().eq("libraries.id", id).eq("deleted", false).orderBy("UPPER(name) ASC").select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_HardwareType.class) != null ?  idCache().gets(Model_HardwareType.class) : new ArrayList<>();
    }
    @JsonIgnore
    public List<Model_HardwareType> getHardwareTypes() {
        try {

            List<Model_HardwareType> hardwareTypes  = new ArrayList<>();

            for (UUID hardware_type_id : getHardwareTypesId()) {
                hardwareTypes.add(Model_HardwareType.find.byId(hardware_type_id));
            }

            return hardwareTypes;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore @Override
    public boolean isPublic() {
        return this.publish_type == ProgramType.PUBLIC || this.publish_type == ProgramType.DEFAULT_MAIN;
    }

    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        this.azure_library_link = "libraries/"  + UUID.randomUUID().toString();

        super.save();

        if (getProject() != null) {
            getProject().idCache().add(this.getClass(),id);
        }
    }

    @JsonIgnore @Override
    public boolean delete() {

        getProject().idCache().remove(this.getClass(),id);

        return super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String azure_library_link;
    @JsonIgnore public String get_path() { return azure_library_link; }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.LIBRARY;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

    @JsonPermission(Action.PUBLISH) @Transient
    public boolean community_publishing_permission;

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_Library.class)
    public static CacheFinder<Model_Library> find = new CacheFinder<>(Model_Library.class);
}