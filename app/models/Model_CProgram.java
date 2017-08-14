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
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.enums.Enum_Compile_status;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import utilities.swagger.documentationClass.Swagger_C_Program_Version_New;
import utilities.swagger.documentationClass.Swagger_C_Program_Version_Update;
import utilities.swagger.documentationClass.Swagger_Library_Library_Version_pair;
import utilities.swagger.outboundClass.*;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


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

    @JsonIgnore  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)                      public Model_TypeOfBoard type_of_board;


    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time in ms", example = "1466163478925") public Date date_of_create;

    @JsonIgnore  public boolean removed_by_user;

    @JsonIgnore @OneToMany(mappedBy="c_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  private List<Model_VersionObject> version_objects = new ArrayList<>();

    @JsonIgnore @OneToOne(fetch = FetchType.LAZY)                                  public Model_TypeOfBoard   type_of_board_default;
    @JsonIgnore @OneToOne(mappedBy = "default_program", cascade = CascadeType.ALL) public Model_VersionObject default_main_version;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                                 public Model_VersionObject example_library;          // Program je příklad pro použití knihovny


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList public List<String> cache_list_version_objects_ids = new ArrayList<>();
    @JsonIgnore @Transient @TyrionCachedList private String cache_value_type_of_board_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_value_type_of_board_name;
    @JsonIgnore @Transient @TyrionCachedList private String cache_value_project_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_value_project_name;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty  @Transient public String project_id()           {

        if(cache_value_project_id == null){
            Model_Project project = Model_Project.find.where().eq("c_programs.id", id).select("id").findUnique();
            if(project == null) return null;
            cache_value_project_id = project.id;
        }

        return cache_value_project_id;


    }
    @JsonProperty  @Transient public String project_name()         {
        try {

            if(cache_value_project_name == null){
                if(project_id() == null) return null;
                cache_value_project_name = Model_Project.get_byId(project_id()).name;
            }

            return cache_value_project_name;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }
    @JsonProperty  @Transient public String type_of_board_id()     {

        try {

            if(cache_value_type_of_board_id == null){
                Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.where().eq("c_programs.id", id).select("id").findUnique();
                cache_value_type_of_board_id = typeOfBoard.id;
            }

            return cache_value_type_of_board_id;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }
    @JsonProperty  @Transient public String type_of_board_name()   {

        try {

            if(cache_value_type_of_board_name == null){
                cache_value_type_of_board_name = Model_TypeOfBoard.get_byId(type_of_board_id()).name;
            }

            return cache_value_type_of_board_name;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }

    }

    @JsonProperty @Transient public List<Swagger_C_Program_Version_Short_Detail> program_versions() {

        try {

            List<Swagger_C_Program_Version_Short_Detail> versions = new ArrayList<>();

            for (Model_VersionObject version : getVersion_objects().stream().sorted((element1, element2) -> element2.date_of_create.compareTo(element1.date_of_create)).collect(Collectors.toList())) {
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

            if (this.getVersion_objects().size() > 0){
                for (Model_FileRecord file : this.getVersion_objects().get(0).files){

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

    @Transient @JsonIgnore @TyrionCachedList public List<Model_VersionObject> getVersion_objects(){

        try{

            if(cache_list_version_objects_ids.isEmpty()){

                List<Model_VersionObject> versions =  Model_VersionObject.find.where().eq("c_program.id", id).eq("removed_by_user", false).order().desc("date_of_create").select("id").findList();;

                // Získání seznamu
                for (Model_VersionObject version : versions) {
                    cache_list_version_objects_ids.add(version.id);
                }

            }

            List<Model_VersionObject> versions  = new ArrayList<>();

            for(String version_id : cache_list_version_objects_ids){
                versions.add(Model_VersionObject.get_byId(version_id));
            }

            return versions;

        }catch (Exception e){
            terminal_logger.internalServerError("getVersion_objects", e);
            return new ArrayList<Model_VersionObject>();
        }
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
                c_program_versions.files = version_new.files;

                for( String imported_library_version_id : version_new.imported_libraries){

                    Model_VersionObject library_version = Model_VersionObject.get_byId(imported_library_version_id);

                    if(library_version == null) continue;

                    Swagger_Library_Library_Version_pair pair = new Swagger_Library_Library_Version_pair();
                    pair.library_version_short_detail = library_version.get_short_library_version();
                    pair.library_short_detail         = library_version.library.get_short_library();

                    c_program_versions.imported_libraries.add(pair);
                }
            }

            if (version_object.c_compilation != null) {
                c_program_versions.virtual_input_output = version_object.c_compilation.virtual_input_output;
            }


            return c_program_versions;

        }catch (Exception e){
            terminal_logger.internalServerError("program_version", e);
          return null;
        }
    }

    @Transient @JsonIgnore @TyrionCachedList public Model_Project get_project()           {
            if(project_id() == null) return null;
            return Model_Project.get_byId(project_id());
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        terminal_logger.debug("save :: Creating new Object");

        this.id = UUID.randomUUID().toString();

        // C_Program is Private registred under Project
        if(project != null) {

            terminal_logger.debug("save :: is a private Program");
            this.azure_c_program_link = project.get_path() + "/c-programs/" + this.id;

        }else{    // C_Program is public C_Program for every users

            terminal_logger.debug("save :: is a public Program");
            this.azure_c_program_link = "public-c-programs/"  + this.id;

        }

        // Call notification about project update
        if(project != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, project_id(), project_id()))).start();

        super.save();

        if(project != null){
            project.cache_list_c_program_ids.add(id);
        }

        cache.put(id, this);
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);

        // Call notification about model update
        if(get_project() != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_CProgram.class, project_id(), this.id))).start();

        super.update();

        cache.put(id, this);
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object Id: {} ", this.id);

        this.removed_by_user = true;


        if(project_id() != null){
            Model_Project.get_byId( project_id() ).cache_list_c_program_ids.remove(id);
        }

        cache.remove(id);

        // Call notification about project update
        if(get_project() != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, project_id(), project_id()))).start();

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

    @JsonIgnore   @Transient  @ApiModelProperty(required = true) public boolean create_permission(){

        if(Controller_Security.get_person().permissions_keys.containsKey("c_program_create")) return true;

        return project != null && project.update_permission();

    }

    @JsonProperty @Transient public boolean update_permission()  {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("c_program_update_" + id)) return Controller_Security.get_person().permissions_keys.get("c_program_update_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("c_program_update")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if( Model_CProgram.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).where().eq("id", id).findRowCount() > 0){
            Controller_Security.get_person().permissions_keys.put("c_program_update_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("c_program_update_" + id, false);
        return false;

    }
    @JsonIgnore   @Transient public boolean read_permission(){

        if (project_id() == null) return true; // TODO TOM - nevím, jak to máš promyšlené u public programů

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("c_program_read_" + id)) return Controller_Security.get_person().permissions_keys.get("c_program_read_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("c_program_read")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if( Model_CProgram.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).eq("id", id).findRowCount() > 0){
            Controller_Security.get_person().permissions_keys.put("c_program_read_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("read_" + id, false);
        return false;

    }
    @JsonProperty @Transient public boolean edit_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("c_program_edit_" + id)) return Controller_Security.get_person().permissions_keys.get("c_program_edit_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("c_program_edit")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if( Model_CProgram.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).where().eq("id", id).findRowCount() > 0){
            Controller_Security.get_person().permissions_keys.put("c_program_edit_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("c_program_edit_" + id, false);
        return false;

    }
    @JsonProperty @Transient public boolean delete_permission()  {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("c_program_delete_" + id)) return Controller_Security.get_person().permissions_keys.get("c_program_delete_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("c_program_delete")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if( Model_CProgram.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).where().eq("id", id).findRowCount() > 0){
            Controller_Security.get_person().permissions_keys.put("c_program_delete_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("c_program_delete_" + id, false);
        return false;

    }

    public enum permissions{ c_program_create,  c_program_update, c_program_read , c_program_edit, c_program_delete; }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE         = Model_CProgram.class.getSimpleName();
    public static Cache<String, Model_CProgram> cache = null;               // < Model_CProgram_id, Model_CProgram>

    @JsonIgnore
    public static Model_CProgram get_byId(String id) {

        Model_CProgram c_program = cache.get(id);
        if (c_program == null){

            c_program = Model_CProgram.find.byId(id);
            if (c_program == null) return null;

            cache.put(id, c_program);
        }

        return c_program;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_CProgram> find = new Finder<>(Model_CProgram.class);
}

