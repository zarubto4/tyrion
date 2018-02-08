package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.ProgramType;
import utilities.enums.CompilationStatus;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.models_update_echo.EchoHandler;
import utilities.swagger.input.Swagger_C_Program_Version_New;
import utilities.swagger.input.Swagger_Library_Library_Version_pair;
import utilities.swagger.output.Swagger_C_Program_Version;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Entity
@ApiModel(value="C_Program", description="Object represented C_Program in database")
@Table(name="CProgram")
public class Model_CProgram extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_CProgram.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) public Model_Project project;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST) public Model_HardwareType hardware_type;
                                                                                  public ProgramType publish_type;

    @JsonIgnore @OneToMany(mappedBy="c_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  private List<Model_Version> versions = new ArrayList<>();

    @JsonIgnore @OneToOne(fetch = FetchType.LAZY) public Model_HardwareType hardware_type_default;  // Vazba pokud tento C_Program je výchozí program desky
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY) public Model_HardwareType hardware_type_test;     // Vaza pokud je tento C Program výchozím testovacím programem desky

    @JsonIgnore @OneToOne(mappedBy = "default_program", cascade = CascadeType.ALL) public Model_Version default_main_version;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                                 public Model_Version example_library;          // Program je příklad pro použití knihovny

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public List<UUID> cache_version_ids = new ArrayList<>();
    @JsonIgnore @Transient @Cached private UUID cache_hardware_type_id;
    @JsonIgnore @Transient @Cached private String cache_hardware_type_name;
    @JsonIgnore @Transient @Cached private UUID cache_project_id;
    @JsonIgnore @Transient @Cached private String cache_project_name;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) public UUID project_id()           {

        if (cache_project_id == null) {
            Model_Project project = Model_Project.find.query().where().eq("c_programs.id", id).select("id").findOne();
            if (project == null) return null;
            cache_project_id = project.id;
        }

        return cache_project_id;


    }
    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) public String project_name()         {
        try {

            if (cache_project_name == null) {
                if (project_id() == null) return null;
                cache_project_name = Model_Project.getById(project_id()).name;
            }

            return cache_project_name;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }
    @JsonProperty public UUID hardware_type_id()     {
        try {

            if (cache_hardware_type_id == null) {
                Model_HardwareType hardwareType = Model_HardwareType.find.query().where().eq("c_programs.id", id).select("id").findOne();
                cache_hardware_type_id = hardwareType.id;
            }

            return cache_hardware_type_id;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }
    @JsonProperty public String hardware_type_name()   {
        try {

            if (cache_hardware_type_name == null) {
                cache_hardware_type_name = Model_HardwareType.getById(hardware_type_id()).name;
            }

            return cache_hardware_type_name;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }

    }
    @JsonProperty public List<Swagger_C_Program_Version> program_versions() {
        try {

            List<Swagger_C_Program_Version> versions = new ArrayList<>();

            for (Model_Version version : getVersions().stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())) {
                versions.add(this.program_version(version));
            }

            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public List<Model_Version> getVersions() {

        try {

            if (cache_version_ids.isEmpty()) {

                List<Model_Version> versions =  Model_Version.find.query().where().eq("c_program.id", id).eq("deleted", false).order().desc("created").select("id").findList();

                // Získání seznamu
                for (Model_Version version : versions) {
                    cache_version_ids.add(version.id);
                }
            }

            List<Model_Version> versions  = new ArrayList<>();

            for (UUID version_id : cache_version_ids) {
                versions.add(Model_Version.getById(version_id));
            }

            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @Transient @JsonIgnore public List<Model_Version> getVersion_objects_all_For_Admin() {
        return Model_Version.find.query().where().eq("c_program.id", id).order().desc("created").findList();
    }

    @Transient @JsonIgnore public Swagger_C_Program_Version program_version(Model_Version version) {
        try {

            Swagger_C_Program_Version c_program_versions = new Swagger_C_Program_Version();

            c_program_versions.status = version.compilation != null ? version.compilation.status : CompilationStatus.UNDEFINED;
            c_program_versions.version = version;
            c_program_versions.remove_permission = delete_permission();
            c_program_versions.edit_permission   = edit_permission();

            Model_Blob fileRecord = Model_Blob.find.query().where().eq("version.id", version.id).eq("name", "code.json").findOne();

            if (fileRecord != null) {

                JsonNode json = Json.parse(fileRecord.get_fileRecord_from_Azure_inString());

                Swagger_C_Program_Version_New version_new = Json.fromJson(json, Swagger_C_Program_Version_New.class);

                c_program_versions.main = version_new.main;
                c_program_versions.files = version_new.files;

                for ( String imported_library_version_id : version_new.imported_libraries) {

                    Model_Version library_version = Model_Version.getById(imported_library_version_id);

                    if (library_version == null) continue;

                    Swagger_Library_Library_Version_pair pair = new Swagger_Library_Library_Version_pair();
                    pair.library_version_short_detail = library_version;
                    pair.library_short_detail         = library_version.library;

                    c_program_versions.imported_libraries.add(pair);
                }
            }

            if (version.compilation != null) {
                c_program_versions.virtual_input_output = version.compilation.virtual_input_output;
            }


            return c_program_versions;

        } catch (Exception e) {
            logger.internalServerError(e);
          return null;
        }
    }

    @JsonIgnore public Model_Project get_project() {
            if (project_id() == null) return null;
            return Model_Project.getById(project_id());
    }

    @JsonIgnore public Model_HardwareType getHardwareType() {
        if (hardware_type_id() == null) return null;
        return Model_HardwareType.getById(hardware_type_id());
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        logger.debug("save :: Creating new Object");

        // In some case we set id manualy (for example make copy for publishing)
        if (this.id == null) this.id = UUID.randomUUID(); // TODO maybe not necessary

        // C_Program is Private registred under Project
        if (project != null) {
            logger.debug("save :: is a private Program");
            this.azure_c_program_link = project.getPath() + "/c-programs/" + this.id;

        } else {    // C_Program is public C_Program for every users
            logger.debug("save :: is a public Program");
            this.azure_c_program_link = "public-c-programs/"  + this.id;
        }

        super.save();

        // Call notification about project update
        if (project != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, project_id(), project_id()))).start();


        if (project != null) {
            project.cache_c_program_ids.add(id);
        }

        cache.put(id, this);
    }

    @JsonIgnore @Override public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);

        // Call notification about model update
        if (get_project() != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_CProgram.class, project_id(), this.id))).start();

        super.update();

        cache.put(id, this);
    }

    @JsonIgnore @Override public boolean delete() {

        logger.debug("update :: Delete object Id: {} ", this.id);

        if (project_id() != null) {
            Model_Project.getById(project_id()).cache_c_program_ids.remove(id);
        }

        cache.remove(id);

        // Call notification about project update
        if (get_project() != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, project_id(), project_id()))).start();

        this.update();
        
        return false;
    }

    @JsonIgnore @Override public void refresh() {
        logger.debug("update :: Delete object Id: {} ", this.id);

        this.cache_version_ids.clear();
        cache.remove(id);
        super.refresh();

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BlOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String azure_c_program_link; // Link, který je náhodně generovaný pro Azure - a který se připojuje do cesty souborům

    @JsonIgnore @Transient
    public String get_path() {

        return  azure_c_program_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.read_permission = true, you can read C_program on this Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create C_program on this Project - Or you need static/dynamic permission key";

    @JsonIgnore   @Transient  @ApiModelProperty(required = true) public boolean create_permission() {

        if (BaseController.person().has_permission("c_program_create")) return true;

        return project != null && project.update_permission();

    }

    @JsonProperty @Transient public boolean update_permission()  {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("c_program_update_" + id)) return BaseController.person().has_permission("c_program_update_"+ id);
        if (BaseController.person().has_permission(Permission.CProgram_update.name())) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_CProgram.find.query().where().where().eq("project.participants.person.id", BaseController.person().id ).where().eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("c_program_update_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("c_program_update_" + id, false);
        return false;

    }
    @JsonIgnore   @Transient public boolean read_permission() {

        if (project_id() == null) return true; // TODO TOM - nevím, jak to máš promyšlené u public programů

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("c_program_read_" + id)) return BaseController.person().has_permission("c_program_read_"+ id);
        if (BaseController.person().has_permission(Permission.CProgram_read.name())) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_CProgram.find.query().where().where().eq("project.participants.person.id", BaseController.person().id ).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("c_program_read_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("read_" + id, false);
        return false;

    }
    @JsonProperty @Transient public boolean edit_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("c_program_edit_" + id)) return BaseController.person().has_permission("c_program_edit_"+ id);
        if (BaseController.person().has_permission(Permission.CProgram_edit.name())) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_CProgram.find.query().where().where().eq("project.participants.person.id", BaseController.person().id ).where().eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("c_program_edit_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("c_program_edit_" + id, false);
        return false;

    }
    @JsonProperty @Transient public boolean delete_permission()  {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("c_program_delete_" + id)) return BaseController.person().has_permission("c_program_delete_"+ id);
        if (BaseController.person().has_permission(Permission.CProgram_delete.name())) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_CProgram.find.query().where().where().eq("project.participants.person.id", BaseController.person().id ).where().eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("c_program_delete_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("c_program_delete_" + id, false);
        return false;

    }
    @JsonProperty @Transient  @ApiModelProperty(required = false, value = "Visible only for Administrator with Permission") @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean community_publishing_permission()  {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        return BaseController.person().has_permission(Permission.C_Program_community_publishing_permission.name());
    }

    public enum Permission { CProgram_create, CProgram_read, CProgram_edit, CProgram_update, CProgram_delete, C_Program_community_publishing_permission }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/
    
    @CacheField(Model_CProgram.class)
    public static Cache<UUID, Model_CProgram> cache;

    public static Model_CProgram getById(String id) {
        return getById(UUID.fromString(id));
    }
    
    public static Model_CProgram getById(UUID id) {

        Model_CProgram c_program = cache.get(id);
        if (c_program == null) {

            c_program = Model_CProgram.find.byId(id);
            if (c_program == null) return null;

            cache.put(id, c_program);
        }

        return c_program;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    
    public static Finder<UUID, Model_CProgram> find = new Finder<>(Model_CProgram.class);
}

