package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.Server;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.enums.Enum_Homer_instance_type;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import utilities.swagger.outboundClass.*;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Entity
@ApiModel(value = "BProgram", description = "Model of BProgram")
@Table(name="BProgram")
public class Model_BProgram extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_BProgram.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                         @Id public String id;
                                                             public String name;
                        @Column(columnDefinition = "TEXT")   public String description;

    // TODO smazat @JsonIgnore @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY) public Model_HomerInstance instance; // TODO - do budoucna více instnací!!!! http://youtrack.byzance.cz/youtrack/issue/TYRION-502

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time in ms", example = "1466163478925") public Date last_update;
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time in ms", example = "1466163478925") public Date date_of_create;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)   public Model_Project project;
    @JsonIgnore                                      public boolean removed_by_user;  // Defaultně false - když true - tak se to nemá uživateli vracet!

    @JsonIgnore @OneToMany(mappedBy="b_program", cascade=CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_VersionObject> version_objects = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList public List<String> cache_list_version_objects_ids = new ArrayList<>();
    @JsonIgnore @Transient @TyrionCachedList private String cache_value_type_of_board_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_value_project_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_instance_id;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @Transient public String project_id() {

        if (cache_value_project_id == null) {
            Model_Project project = Model_Project.find.where().eq("b_programs.id", id).select("id").findUnique();
            cache_value_project_id = project.id;
        }

        return cache_value_project_id;
    }

    @JsonProperty @Transient public List<Swagger_B_Program_Version_Short_Detail> program_versions() {

        try {

            List<Swagger_B_Program_Version_Short_Detail> versions = new ArrayList<>();

            for (Model_VersionObject version : getVersion_objects().stream().sorted((element1, element2) -> element2.date_of_create.compareTo(element1.date_of_create)).collect(Collectors.toList())) {
                versions.add(version.get_short_b_program_version());
            }

            return versions;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonProperty @Transient public Swagger_B_Program_State instance_details() {
        try {

            Swagger_B_Program_State state = new Swagger_B_Program_State();

            state.online_state = Model_HomerInstance.get_byId(instance_id()).online_state();

            if (Server.server_mode == Enum_Tyrion_Server_mode.developer && instance().get_actual_instance() != null) {
                // /#token - frontend pouze nahradí substring - můžeme tedy do budoucna za adresu přidávat další parametry
                state.instance_remote_url = "ws://" + Model_HomerServer.get_byId(instance().server_id()).get_WebView_APP_URL() + instance_id() + "/#token";
            } else {
                state.instance_remote_url = "wss://" + Model_HomerServer.get_byId(instance().server_id()).get_WebView_APP_URL()  + instance_id() + "/#token";
            }

            if (instance().get_actual_instance() != null) {
                // Jaká verze Blocko Programu je aktuální?
                state.version_id = instance().get_actual_instance().get_b_program_version().id;
                state.version_name = instance().get_actual_instance().get_b_program_version().version_name;

                // Vracím naposledy použitou - Becki si to vyřeší sama
            } else if (!instance().instance_history.isEmpty()) {
                state.version_id = instance().instance_history.get(0).get_b_program_version().id;
                state.version_name = instance().instance_history.get(0).get_b_program_version().version_name;
            }

            // Instnace ID
            state.instance_id = instance_id();

            // Informace o Serveru
            state.server_id = instance().server_id();
            state.server_name = instance().server_name();
            state.server_online_state = instance().server_online_state();

            return state;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

/* GET Variable short type of objects ----------------------------------------------------------------------------------*/


    @Transient @JsonIgnore public Swagger_B_Program_Short_Detail get_b_program_short_detail() {
        try {

            Swagger_B_Program_Short_Detail help = new Swagger_B_Program_Short_Detail();
            help.id = id;
            help.name = name;
            help.description = description;

            help.edit_permission = edit_permission();
            help.delete_permission = delete_permission();
            help.update_permission = update_permission();

            return help;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }
    // Objekt určený k vracení verze
    @JsonIgnore @Transient public Swagger_B_Program_Version program_version(Model_VersionObject version_object) {

        Swagger_B_Program_Version b_program_version = new Swagger_B_Program_Version();

        b_program_version.version_object                = version_object;

        b_program_version.remove_permission = delete_permission();
        b_program_version.edit_permission   = edit_permission();

        b_program_version.hardware_group                = version_object.b_program_hw_groups;
        b_program_version.m_project_program_snapshots   = version_object.b_program_version_snapshots;

        Model_FileRecord fileRecord = Model_FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "program.js").findUnique();
        if (fileRecord != null) b_program_version.program             = fileRecord.get_fileRecord_from_Azure_inString();

        return b_program_version;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public List<Model_VersionObject> getVersion_objects() {
        try {

            if (cache_list_version_objects_ids.isEmpty()) {

                List<Model_VersionObject> versions =  Model_VersionObject.find.where().eq("b_program.id", id).eq("removed_by_user", false).order().desc("date_of_create").select("id").findList();

                // Získání seznamu
                for (Model_VersionObject version : versions) {
                    cache_list_version_objects_ids.add(version.id);
                }
            }

            List<Model_VersionObject> versions  = new ArrayList<>();

            for (String version_id : cache_list_version_objects_ids) {
                versions.add(Model_VersionObject.get_byId(version_id));
            }

            return versions;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new ArrayList<>();
        }

    }

    @JsonIgnore @Transient public String instance_id() {
        try {

            if (this.cache_instance_id == null) {

               Model_HomerInstance instance =  Model_HomerInstance.find.where().eq("b_program.id", id).select("id").findUnique();

               if (instance != null) {
                   cache_instance_id = instance.id;
               }
            }

            return cache_instance_id;


        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore @Transient public Model_HomerInstance instance() {
        try {

            if (this.instance_id() != null) {
                this.instance = Model_HomerInstance.get_byId(instance_id());
                return instance;
            }

            return null;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        terminal_logger.debug("save :: Creating new Object");

        this.id = UUID.randomUUID().toString();
        this.azure_b_program_link = project.get_path() + "/b-programs/"  + this.id;

        if (instance == null) {

            Model_HomerInstance instance = new Model_HomerInstance();
            instance.instance_type = Enum_Homer_instance_type.INDIVIDUAL;
            instance.cloud_homer_server = Model_HomerServer.get_destination_server();
            instance.project_id = project.id;
            instance.save();
            this.instance = instance;

        }

        project.cache_list_b_program_ids.add(id);

        super.save();

        cache.put(id, this);

        if (project_id() != null) {
            new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, project_id(), project_id()))).start();
        }
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);

        super.update();

        if (project_id() != null) {
            new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_BProgram.class, project_id(), id))).start();
        }
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object Id: {} ", this.id);

        try {

            instance().remove_from_cloud();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }

        this.removed_by_user = true;
        instance().update();


        if (project_id() != null) {
            Model_Project.get_byId(project_id()).cache_list_b_program_ids.remove(id);
        }

        cache.remove(id);

        super.update();

        if (project_id() != null) {
            new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_BProgram.class, project_id(), project_id()))).start();
        }

    }

/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String azure_b_program_link;

    @JsonIgnore @Transient
    public String get_path() {
        return azure_b_program_link;
    }


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_BProgram.class.getSimpleName();

    public static Cache<String, Model_BProgram> cache = null; // < ID, Model_BProgram>

    @JsonIgnore
    public static Model_BProgram get_byId(String id) {

        Model_BProgram b_program = cache.get(id);
        if (b_program == null) {

            b_program = Model_BProgram.find.byId(id);
            if (b_program == null) return null;

            cache.put(id, b_program);
        }

        return b_program;
    }

    public void cache_refresh() {
        this.refresh();
        if (cache.containsKey(this.id)) {
            cache.replace(this.id, this);
        } else {
            cache.put(this.id, this);
        }
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission()  {

        if (Controller_Security.get_person().has_permission("B_Program_create")) return true;
        return project != null && project.update_permission();

    }
    @JsonProperty @Transient public boolean update_permission()  {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (Controller_Security.get_person().has_permission("b_program_update_" + id)) return Controller_Security.get_person().has_permission("b_program_update_"+ id);
        if (Controller_Security.get_person().has_permission("B_Program_update")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_BProgram.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).where().eq("id", id).findRowCount() > 0) {
            Controller_Security.get_person().cache_permission("b_program_update_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().cache_permission("update_" + id, false);
        return false;

    }
    @JsonIgnore   @Transient public boolean read_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (Controller_Security.get_person().has_permission("b_program_read_" + id)) return Controller_Security.get_person().has_permission("b_program_read_"+ id);
        if (Controller_Security.get_person().has_permission("B_Program_read")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_BProgram.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).where().eq("id", id).findRowCount() > 0) {
            Controller_Security.get_person().cache_permission("b_program_read_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().cache_permission("read_" + id, false);
        return false;

    }
    @JsonProperty @Transient public boolean edit_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (Controller_Security.get_person().has_permission("b_program_edit_" + id)) return Controller_Security.get_person().has_permission("b_program_edit_"+ id);
        if (Controller_Security.get_person().has_permission("B_Program_edit")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_BProgram.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).where().eq("id", id).findRowCount() > 0) {
            Controller_Security.get_person().cache_permission("b_program_edit_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().cache_permission("edit_" + id, false);
        return false;

    }
    @JsonProperty @Transient public boolean delete_permission()  {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (Controller_Security.get_person().has_permission("b_program_delete_" + id)) return Controller_Security.get_person().has_permission("b_program_delete_"+ id);
        if (Controller_Security.get_person().has_permission("B_Program_delete")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_BProgram.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).where().eq("id", id).findRowCount() > 0) {
            Controller_Security.get_person().cache_permission("b_program_delete_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().cache_permission("b_program_delete_" + id, false);
        return false;

     }

     // Statické univerzální klíče
    public enum permissions{ B_Program_create, B_Program_update, B_Program_read, B_Program_edit , B_Program_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
     public static Model.Finder<String,Model_BProgram> find = new Finder<>(Model_BProgram.class);
}

