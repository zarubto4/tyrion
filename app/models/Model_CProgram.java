package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.enums.ProgramType;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.models_update_echo.EchoHandler;
import utilities.permission.Action;
import utilities.permission.JsonPermission;
import utilities.permission.Permissible;
import utilities.swagger.output.Swagger_Short_Reference;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@ApiModel(value="C_Program", description="Object represented C_Program in database")
@Table(name="CProgram")
public class Model_CProgram extends TaggedModel implements Permissible, UnderProject {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_CProgram.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) public Model_Project project;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST) public Model_HardwareType hardware_type;
                                                                                  public ProgramType publish_type;

    @JsonIgnore @OneToMany(mappedBy="c_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  private List<Model_CProgramVersion> versions = new ArrayList<>();

    @JsonIgnore @OneToOne(fetch = FetchType.LAZY) public Model_HardwareType hardware_type_default;  // Vazba pokud tento C_Program je výchozí program desky
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY) public Model_HardwareType hardware_type_test;     // Vazba pokud je tento C Program výchozím testovacím programem desky

    @JsonIgnore @OneToOne(mappedBy = "default_program", cascade = CascadeType.ALL) public Model_CProgramVersion default_main_version;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                                 public Model_LibraryVersion example_library;          // Program je příklad pro použití knihovny

    @JsonIgnore public UUID original_id; // KDyž se vytvoří kopie nebo se publikuje program, zde se uloží původní ID pro pozdější párování

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public Swagger_Short_Reference hardware_type(){
        try {
            return getHardwareType().ref();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    public List<Model_CProgramVersion> program_versions() {
        try {

            return getVersions();

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonProperty @ApiModelProperty(value = "Visible only for Administrators with permission - its default version of Main Program of each hardware type", required = false)
    public Model_CProgramVersion default_version() {
        try {

            if (getProjectId() != null) return null;

            return default_main_version;

        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID getProjectId() {

        if (publish_type == ProgramType.PRIVATE) {

            if (idCache().get(Model_Project.class) == null) {
                idCache().add(Model_Project.class, (UUID) Model_Project.find.query().where().eq("c_programs.id", id).select("id").findSingleAttribute());
            }

            return idCache().get(Model_Project.class);

        } else {
            return null;
        }
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return isLoaded("project") ? this.project : Model_Project.find.query().nullable().where().eq("c_programs.id", this.id).findOne();
    }

    @JsonIgnore
    public List<UUID> getVersionsId() {

        if (idCache().gets(Model_CProgramVersion.class) == null) {
            idCache().add(Model_CProgramVersion.class, Model_CProgramVersion.find.query().where().eq("c_program.id", id).ne("deleted", true).order().desc("created").select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_CProgramVersion.class) != null ?  idCache().gets(Model_CProgramVersion.class) : new ArrayList<>();
    }

    @JsonIgnore
    public void sort_Model_Model_CProgramVersion_ids() {

        List<Model_CProgramVersion> versions = getVersions();
        this.idCache().removeAll(Model_CProgramVersion.class);
        versions.stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())
                .forEach(o -> this.idCache().add(Model_CProgramVersion.class, o.id));
    }

    @JsonIgnore
    public List<Model_CProgramVersion> getVersions() {
        return this.getVersionsId().stream().map(Model_CProgramVersion.find::byId).collect(Collectors.toList());
    }

    @JsonIgnore
    public Model_HardwareType getHardwareType()     {
        return isLoaded("hardware_type") ? hardware_type : Model_HardwareType.find.query().where().eq("c_programs.id", id).findOne();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        super.save();

        Model_Project project = getProject();

        // Call notification about project update
        if (project != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, project.id, project.id))).start();
    }

    @JsonIgnore @Override
    public void update() {

        // Call notification about model update
        if(publish_type == ProgramType.PRIVATE) {
            new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_CProgram.class, getProjectId(), this.id))).start();
        }

        super.update();
    }

    @JsonIgnore @Override
    public boolean delete() {

        super.delete();

        // Remove from Project Cache
        if (publish_type == ProgramType.PRIVATE) {

            try {
                getProject().idCache().remove(this.getClass(), id);
            } catch (Exception e) {
                // Nothing
            }

            new Thread(() -> {
                try {
                    EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, getProjectId(), getProjectId()));
                } catch (Exception e) {
                    // Nothing
                }
            }).start();
        }
        
        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BlOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public String get_path() {

        // C_Program is Private registred under Project
        if (getProject() != null) {
            return getProject().getPath() + "/c-programs/" + this.id;
        } else {

            if(getProjectId() == null) {
                logger.debug("save :: is a public Program");
                return "public-c-programs/" + this.id;
            }else {
               return getProject().getPath() + "/c-programs/" + this.id;
            }
        }
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.FIRMWARE;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.PUBLISH);
    }

    @JsonPermission(Action.PUBLISH) @Transient
    public boolean community_publishing_permission;

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_CProgram.class)
    public static CacheFinder<Model_CProgram> find = new CacheFinder<>(Model_CProgram.class);
}

