package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.VersionModel;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "BProgramVersion", description = "Model of BProgram Version")
@Table(name="BProgramVersion")
public class Model_BProgramVersion extends VersionModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_BProgramVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne  public Model_Library library;
    @JsonIgnore @OneToMany(mappedBy = "example_library", cascade = CascadeType.ALL)  public List<Model_CProgram> examples = new ArrayList<>();

    @JsonIgnore @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)   public Model_BProgram b_program;

    @JsonIgnore public String additional_json_configuration;

    @JsonIgnore @OneToMany(mappedBy = "b_program_version", cascade = CascadeType.ALL) public List<Model_BProgramVersionSnapGridProject> grid_project_snapshots = new ArrayList<>();    // Vazba kvůli puštěným B_programům

    // B_Program - Instance
    @JsonIgnore @OneToMany(mappedBy="b_program_version", fetch = FetchType.LAZY) public List<Model_InstanceSnapshot> instances = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_b_program_id;

/* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/

    public String program() {
        // TODO Hodně náročné na stahování do Cahce - Nejlépe takový objekt na linky, že sám sebe zahodí po vypršení platnosti
        // Myslím, že jsem ho někde programoval!
        Model_Blob blob = Model_Blob.find.query().where().eq("b_program_version.id", id).eq("name", "blocko.json").findOne();
        if (blob != null) return blob.get_fileRecord_from_Azure_inString();
        return null;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_b_program_id() throws _Base_Result_Exception {

        if (cache_b_program_id == null) {

            Model_BProgram bProgram = Model_BProgram.find.query().where().eq("versions.id", id).select("id").findOne();
            if (bProgram != null) {
                cache_b_program_id = bProgram.id;
            } else {
                throw new Result_Error_NotFound(Model_BProgram.class);
            }
        }

        return cache_b_program_id;
    }

    @JsonIgnore
    public Model_BProgram get_b_program() throws _Base_Result_Exception {

        if (cache_b_program_id == null) {
           return Model_BProgram.getById(get_b_program_id());
        }
        return Model_BProgram.getById(cache_b_program_id);
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("save :: Creating new Object");

        super.save();

        new Thread(() -> {
            EchoHandler.addToQueue(new WSM_Echo(Model_Library.class, library.get_project_id(), library.id));
        }).start();

        // Add to Cache
        if (library != null) {
            library.cache_version_ids.add(0, id);
        }
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update::Update object Id: {}",  this.id);
        super.update();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Library.class, get_b_program().get_project_id(), get_b_program_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete::Delete object Id: {}",  this.id);

        this.deleted = true;
        super.update();

        // Remove from Cache
        try {
            get_b_program().cache_version_ids.remove(id);
        } catch (_Base_Result_Exception e) {
            // Nothing
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Library.class, get_b_program().get_project_id(), get_b_program_id()));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

        return false;
    }

/* Services --------------------------------------------------------------------------------------------------------*/


/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public String get_path() {
        if(b_program != null) {
            return b_program.get_path() + "/version/" + this.id;
        }else {
            return get_b_program().get_path() + "/version/" + this.id;
        }
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void check_create_permission() throws _Base_Result_Exception { get_b_program().check_update_permission();}
    @JsonIgnore @Override public void check_read_permission()   throws _Base_Result_Exception { get_b_program().check_read_permission();}
    @JsonIgnore @Override public void check_update_permission() throws _Base_Result_Exception { get_b_program().check_update_permission();}
    @JsonIgnore @Override public void check_delete_permission() throws _Base_Result_Exception { get_b_program().check_update_permission();}

    public enum Permission {} // Not Required here

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_BProgramVersion.class, duration = 600)
    public static Cache<UUID, Model_BProgramVersion> cache;

    public static Model_BProgramVersion getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_BProgramVersion getById(UUID id) throws _Base_Result_Exception {

        Model_BProgramVersion grid_widget_version = cache.get(id);

        if (grid_widget_version == null) {

            grid_widget_version = Model_BProgramVersion.find.byId(id);
            if (grid_widget_version == null) throw new Result_Error_NotFound(Model_BProgramVersion.class);

            cache.put(id, grid_widget_version);
        }


        return grid_widget_version;
    }

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_BProgramVersion> find = new Finder<>(Model_BProgramVersion.class);
}
