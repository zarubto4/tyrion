package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.data.Form;
import play.libs.Json;
import utilities.enums.Enum_Compile_status;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.logger.Server_Logger_Developer;
import utilities.models_update_echo.Update_echo_handler;
import utilities.swagger.documentationClass.Swagger_C_Program_Version_New;
import utilities.swagger.documentationClass.Swagger_C_Program_Version_Update;
import utilities.swagger.outboundClass.*;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Entity
@ApiModel(value="C_Program", description="Object represented C_Program in database")
public class Model_CProgram extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_CProgram.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                                                                    @Id public String id;
    @ApiModelProperty(required = true, value = "minimal length is 8 characters")                        public String name;
    @ApiModelProperty(required = false, value = "can be empty") @Column(columnDefinition = "TEXT")      public String description;
    @JsonIgnore @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)                       public Model_Project project;

    @JsonIgnore  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)                     public Model_TypeOfBoard type_of_board;


    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time in ms", example = "1466163478925") public Date date_of_create;

    @JsonIgnore  public boolean removed_by_user;

    @JsonIgnore @OneToMany(mappedBy="c_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_VersionObject> version_objects = new ArrayList<>();

                                                                              @JsonIgnore @OneToOne     public Model_TypeOfBoard type_of_board_default;
                       @JsonIgnore @OneToOne(mappedBy = "default_program", cascade = CascadeType.ALL)   public Model_VersionObject default_main_version;

                                                                             @JsonIgnore @ManyToOne     public Model_VersionObject example_library; // Program je příklad pro použití knihovny

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty  @Transient public String project_id()           { return project != null ? project.id : null; }
    @JsonProperty  @Transient public String project_name()         { return project != null ? project.name : null;}
    @JsonProperty  @Transient public String type_of_board_id()     { return type_of_board == null ? null : type_of_board.id;}
    @JsonProperty  @Transient public String type_of_board_name()   { return type_of_board == null ? null : type_of_board.name;}

    @JsonProperty @Transient public List<Swagger_C_Program_Version_Short_Detail> program_versions() {

        try {

            List<Swagger_C_Program_Version_Short_Detail> versions = new ArrayList<>();

            for (Model_VersionObject version : getVersion_objects()) {
                versions.add(version.get_short_c_program_version());
            }

            return versions;

        }catch (Exception e){
            terminal_logger.internalServerError("Model_CProgram:: program_versions", e);
            List<Swagger_C_Program_Version_Short_Detail> versions = new ArrayList<>();
            return versions;
        }
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_C_program_Short_Detail get_c_program_short_detail(){

        try {
            Swagger_C_program_Short_Detail help = new Swagger_C_program_Short_Detail();

            help.id = id;
            help.name = name;
            help.description = description;
            help.type_of_board_id = type_of_board_id();
            help.type_of_board_name = type_of_board_name();

            help.edit_permission = edit_permission();
            help.delete_permission = delete_permission();
            help.update_permission = update_permission();

            return help;
        }catch (Exception e){
            terminal_logger.internalServerError("Model_CProgram:: get_c_program_short_detail", e);
            return null;

        }
    }

    @Transient @JsonIgnore public Swagger_Example_Short_Detail get_example_short_detail(){

        try {

            Swagger_Example_Short_Detail help = new Swagger_Example_Short_Detail();

            help.id = id;
            help.name = name;
            help.description = description;

            if (this.version_objects.size() > 0){
                for (Model_FileRecord file : this.version_objects.get(0).files){

                    JsonNode json = Json.parse(file.get_fileRecord_from_Azure_inString());

                    Form<Swagger_C_Program_Version_Update> form = Form.form(Swagger_C_Program_Version_Update.class).bind(json);
                    if(form.hasErrors()) return null;
                    Swagger_C_Program_Version_Update example_form = form.get();

                    help.main = example_form.main;
                }
            }

            help.edit_permission = edit_permission();
            help.delete_permission = delete_permission();
            help.update_permission = update_permission();

            return help;

        }catch (Exception e){
            terminal_logger.internalServerError("Model_CProgram:: get_c_program_short_detail", e);
            return null;
        }
    }

    @Transient @JsonIgnore public List<Model_VersionObject> getVersion_objects() {
        return Model_VersionObject.find.where().eq("c_program.id", id).eq("removed_by_user", false).order().desc("date_of_create").findList();
    }

    @Transient @JsonIgnore public List<Model_VersionObject> getVersion_objects_all_For_Admin() {
        return Model_VersionObject.find.where().eq("c_program.id", id).order().desc("date_of_create").findList();
    }

    @Transient @JsonIgnore public Swagger_C_Program_Version program_version(Model_VersionObject version_object){
        try {

            Swagger_C_Program_Version c_program_versions = new Swagger_C_Program_Version();

            c_program_versions.status = version_object.c_compilation != null ? version_object.c_compilation.status : Enum_Compile_status.undefined;
            c_program_versions.version_object = version_object;
            c_program_versions.remove_permission = delete_permission();
            c_program_versions.edit_permission   = edit_permission();

            Model_FileRecord fileRecord = Model_FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "code.json").findUnique();

            if (fileRecord != null) {

                JsonNode json = Json.parse(fileRecord.get_fileRecord_from_Azure_inString());

                Swagger_C_Program_Version_New version_new = Json.fromJson(json, Swagger_C_Program_Version_New.class);

                c_program_versions.main = version_new.main;
                c_program_versions.user_files = version_new.user_files;
                c_program_versions.library_files = version_new.library_files;
            }

            if (version_object.c_compilation != null) {
                c_program_versions.virtual_input_output = version_object.c_compilation.virtual_input_output;
            }


            return c_program_versions;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
          return null;
        }
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        terminal_logger.debug("save :: Creating new Object");

        // C_Program is Private registred under Project
        if(project != null){

            terminal_logger.debug("save :: is a private Program");

            while(true){ // I need Unique Value
                this.id = UUID.randomUUID().toString();
                this.azure_c_program_link = project.get_path()  + "/c-programs/"  + this.id;
                if (Model_CProgram.find.byId(this.id) == null) break;
            }
        }

        // C_Program is public C_Program for every users
        else{

            terminal_logger.debug("save :: is a public Program");

            while(true){ // I need Unique Value
                this.id = UUID.randomUUID().toString();
                this.azure_c_program_link = "public-c-programs/"  + this.id;
                if (Model_CProgram.find.byId(this.id) == null) break;
            }
        }

        // Call notification about project update
        if(project != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, project_id(), project_id()))).start();

        super.save();

        cache.put(id, this);
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);

        // Call notification about model update
        if(project != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_CProgram.class, project_id(), this.id))).start();

        super.update();

        cache.put(id, this);
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object Id: {} ", this.id);

        this.removed_by_user = true;

        // Call notification about project update
        if(project != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, project_id(), project_id()))).start();

        this.update();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BlOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String azure_c_program_link; // Link, který je náhodně generovaný pro Azure - a který se připojuje do cesty souborům

    @JsonIgnore @Transient
    public String get_path(){

        return  azure_c_program_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.read_permission = true, you can read C_program on this Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create C_program on this Project - Or you need static/dynamic permission key";

    @JsonIgnore   @Transient  @ApiModelProperty(required = true) public boolean create_permission(){  return project != null ? ( project.update_permission() ) : Controller_Security.get_person().has_permission("C_program_create");      }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean update_permission(){  return ( Model_CProgram.find.where().eq("project.participants.person.id", Controller_Security.get_person().id).eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("C_program_update"); }
    @JsonIgnore   @Transient  @ApiModelProperty(required = true) public boolean read_permission()  {
        if(project == null) return true;
        return ( Model_CProgram.find.where().eq("project.participants.person.id", Controller_Security.get_person().id).eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("C_program_read");
    }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean edit_permission()  {  return ( Model_CProgram.find.where().eq("project.participants.person.id", Controller_Security.get_person().id).eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("C_program_edit"); }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean delete_permission(){  return ( Model_CProgram.find.where().eq("project.participants.person.id", Controller_Security.get_person().id).eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("C_program_delete"); }

    public enum permissions{  C_program_create,  C_program_update, C_program_read ,  C_program_edit, C_program_delete; }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE         = Model_CProgram.class.getSimpleName();
    public static final String CACHE_VERSION = Model_CProgram.class.getSimpleName() + "_VERSION";

    public static Cache<String, Model_CProgram> cache = null;               // < Model_CProgram_id, Model_CProgram>
    public static Cache<String, Model_VersionObject> cache_versions = null; // < Model_VersionObject_id, Model_VersionObject>

    @JsonIgnore
    public static Model_CProgram get_byId(String id) {

        Model_CProgram c_program = cache.get(id);
        if (c_program == null){

            c_program = Model_CProgram.find.byId(id);
            if (c_program == null){
                Server_Logger.warn( Model_CProgram.class, "get_byId :: This object id:: " + id + " wasn't found.");
                return null;
            }

            cache.put(id, c_program);
        }

        return c_program;
    }

    @JsonIgnore
    public static Model_VersionObject get_version_byId(String id) {

        Model_VersionObject c_program_version = cache_versions.get(id);
        if (c_program_version == null){

            c_program_version = Model_VersionObject.find.byId(id);
            if (c_program_version == null){
                Server_Logger.warn( Model_CProgram.class, "get_version_byId :: This object id:: " + id + " wasn't found.");
                return null;
            }

            cache_versions.put(id, c_program_version);
        }

        return c_program_version;
    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_CProgram> find = new Finder<>(Model_CProgram.class);
}

