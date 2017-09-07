package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.data.Form;
import play.libs.Json;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.enums.Enum_Publishing_type;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import utilities.swagger.documentationClass.Swagger_Library_File_Load;
import utilities.swagger.documentationClass.Swagger_Library_Version;
import utilities.swagger.outboundClass.Swagger_Library_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Library_Version_Short_Detail;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Library", description = "Model of Library")
@Table(name="Library")
public class Model_Library extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Library.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

            @Id public String id;           // String ID is required, because on some place we create ID manually
                public String name;
                public String description;
    @JsonIgnore public boolean removed_by_user;
    @JsonIgnore @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) public Model_Project project;

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time in ms", example = "1466163478925") public Date date_of_create;

    public Enum_Publishing_type publish_type;

    @ManyToMany(fetch = FetchType.LAZY) public List<Model_TypeOfBoard>  type_of_boards  = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy = "library", cascade = CascadeType.ALL, fetch = FetchType.LAZY) @OrderBy("date_of_create DESC") public List<Model_VersionObject> versions = new ArrayList<>();


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList public List<String> cache_list_version_objects_ids = new ArrayList<>();
    @JsonIgnore @Transient @TyrionCachedList public List<String> cache_list_type_of_boards_ids = null;
    @JsonIgnore @Transient @TyrionCachedList private String cache_value_project_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_value_project_name;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty  @Transient public String project_id()           {

        if(cache_value_project_id == null){
            Model_Project project = Model_Project.find.where().eq("libraries.id", id).select("id").findUnique();
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
    @JsonProperty  @Transient public List<Swagger_Library_Version_Short_Detail> versions(){

        List<Swagger_Library_Version_Short_Detail> versions = new ArrayList<>();
        for (Model_VersionObject version : this.getVersion_objects()){
            versions.add(version.get_short_library_version());
        }

        return versions;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Transient @JsonIgnore @TyrionCachedList public List<Model_VersionObject> getVersion_objects(){

        try{

            if(cache_list_version_objects_ids.isEmpty()){

                List<Model_VersionObject> versions =  Model_VersionObject.find.where().eq("library.id", id).eq("removed_by_user", false).order().desc("date_of_create").select("id").findList();

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

    @Transient @JsonIgnore @TyrionCachedList public List<Model_TypeOfBoard> getType_of_Boards(){

        try{

            if(cache_list_type_of_boards_ids == null){

                cache_list_type_of_boards_ids = new ArrayList<>();

                List<Model_TypeOfBoard> boards =  Model_TypeOfBoard.find.where().eq("libraries.id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").select("id").findList();

                // Získání seznamu
                for (Model_TypeOfBoard board : boards) {
                    cache_list_type_of_boards_ids.add(board.id);
                }

            }

            List<Model_TypeOfBoard> boards  = new ArrayList<>();

            for(String board_id : cache_list_type_of_boards_ids){
                boards.add(Model_TypeOfBoard.get_byId(board_id));
            }

            return boards;

        }catch (Exception e){
            terminal_logger.internalServerError("getType_of_Boards", e);
            return new ArrayList<Model_TypeOfBoard>();
        }
    }

    @Transient @JsonIgnore @TyrionCachedList public Swagger_Library_Short_Detail get_short_library(){
        try{

            Swagger_Library_Short_Detail help = new Swagger_Library_Short_Detail();

            help.id = this.id;
            help.name = this.name;
            help.description = this.description;

            for (Model_TypeOfBoard typeOfBoard : getType_of_Boards()) {
                help.add_board_type(typeOfBoard);
            }

            help.edit_permission   = this.edit_permission();
            help.update_permission = this.update_permission();
            help.delete_permission = this.delete_permission();

            return help;
        }catch (Exception e){
            terminal_logger.internalServerError("get_short_library:", e);
            return null;
        }
    }

    @Transient @JsonIgnore @TyrionCachedList public Swagger_Library_Version library_version(Model_VersionObject version_object){
        try {

            Swagger_Library_Version help = new Swagger_Library_Version();

            help.version_id = version_object.id;
            help.version_name = version_object.version_name;
            help.version_description = version_object.version_description;
            help.delete_permission = delete_permission();
            help.update_permission = update_permission();
            help.author = version_object.author();

            for (Model_CProgram cProgram : version_object.examples) {
                help.examples.add(cProgram.get_example_short_detail());
            }

            for (Model_FileRecord file : version_object.files) {

                JsonNode json = Json.parse(file.get_fileRecord_from_Azure_inString());

                Form<Swagger_Library_File_Load> form = Form.form(Swagger_Library_File_Load.class).bind(json);
                if (form.hasErrors()) return null;
                Swagger_Library_File_Load lib_form = form.get();

                help.files.addAll(lib_form.files);
            }

            return help;

        }catch (Exception e){
            terminal_logger.internalServerError("library_version:", e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        if(this.id == null) this.id = UUID.randomUUID().toString();
        this.azure_library_link = "libraries/"  + this.id;

        super.save();

        if(project != null){
            Model_Project.get_byId(project.id).cache_list_library_ids.add(id);
        }

        cache.put(id, this);
    }

    @Override
    public void update(){

        terminal_logger.debug("update :: Update object Id: " + this.id);

        //Cache Update
        if (cache.get(this.id) != null) {
            cache.replace(this.id, this);
        } else cache.put(this.id, this);

        if(project_id() != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Library.class, project_id(), this.id))).start();

        //Database Update
        super.update();
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("remove :: Update (hide) object Id: " + this.id);

        removed_by_user = true;

        if(project_id() != null){
            Model_Project.get_byId(project_id()).cache_list_library_ids.remove(id);
        }

        cache.remove(id);

        //Database Update
        super.update();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore     private String azure_library_link;
    @JsonIgnore     public String get_path(){
        return azure_library_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission(){
        try {
            if(project != null) return Model_Project.get_byId(project.id).update_permission(); return Controller_Security.get_person().has_permission("Library_create");
        }catch (NullPointerException exception){
            terminal_logger.warn("create_permission null pointer exception - project is not probably in cache");
            return false;
        }
    }
    @JsonIgnore   @Transient public boolean read_permission()  {  return true; }
    @JsonProperty @Transient public boolean edit_permission()  {
        try {
          
            if (project_id() != null) return Model_Project.get_byId(project_id()).update_permission();
            return Controller_Security.get_person().has_permission("Library_edit");

        }catch (NullPointerException exception){
            terminal_logger.warn("edit_permission null pointer exception - project is not probably in cache");
            return false;
        }
    }
    @JsonProperty @Transient public boolean delete_permission(){
        try {

             if(project_id() != null) return Model_Project.get_byId(project_id()).update_permission();
             return Controller_Security.get_person().has_permission("Library_delete");

        }catch (NullPointerException exception){
            terminal_logger.warn("delete_permission null pointer exception - project is not probably in cache");
            return false;
        }
    }
    @JsonProperty @Transient public boolean update_permission(){
        try {

            if(project_id() != null) return Model_Project.get_byId(project_id()).update_permission();
            return Controller_Security.get_person().has_permission("Library_update");

        }catch (NullPointerException exception){
            terminal_logger.warn("update_permission null pointer exception - project is not probably in cache");
            return false;
        }
   }
    @JsonProperty @Transient  @ApiModelProperty(required = false, value = "Visible only for Administrator with Permission") @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean community_publishing_permission()  {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        return Controller_Security.get_person().has_permission(Model_CProgram.permissions.C_Program_community_publishing_permission.name());
    }

    public enum permissions{Library_create, Library_edit, Library_delete, Library_update}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_Library.class.getSimpleName();

    public static Cache<String, Model_Library> cache = null; // < ID, Model_Library>

    @JsonIgnore
    public static Model_Library get_byId(String id) {

        Model_Library library= cache.get(id);
        if (library == null){

            library = Model_Library.find.byId(id);
            if (library == null) return null;

            cache.put(id, library);
        }

        return library;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String, Model_Library> find = new Model.Finder<>(Model_Library.class);
}