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
import utilities.models_update_echo.Update_echo_handler;
import utilities.swagger.documentationClass.Swagger_C_Program_Version_New;
import utilities.swagger.documentationClass.Swagger_Library_File_Load;
import utilities.swagger.documentationClass.Swagger_Library_Library_Version_pair;
import utilities.swagger.documentationClass.Swagger_Library_Version;
import utilities.swagger.outboundClass.Swagger_C_Program_Version;
import utilities.swagger.outboundClass.Swagger_Library_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Library_Version_Short_Detail;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Library", description = "Model of Library")
public class Model_Library extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Library.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @ApiModelProperty(required = true) public String id;
    @ApiModelProperty(required = true)     public String name;
    @ApiModelProperty(required = true)     public String description;

                           @JsonIgnore     public boolean removed_by_user;

    @JsonIgnore @OneToMany(mappedBy = "library", cascade = CascadeType.ALL) @OrderBy("date_of_create DESC") public List<Model_VersionObject> versions   = new ArrayList<>();

    @ManyToMany public List<Model_TypeOfBoard>  type_of_boards  = new ArrayList<>();

    @JsonIgnore public String project_id; // Jednodušší vazba na Project bez příme ORM vazby


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty
    public List<Swagger_Library_Version_Short_Detail> versions(){

        List<Swagger_Library_Version_Short_Detail> versions = new ArrayList<>();
        for (Model_VersionObject version : this.versions){
            versions.add(version.get_short_library_version());
        }

        return versions;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Swagger_Library_Short_Detail get_short_library(){
        try{

            Swagger_Library_Short_Detail help = new Swagger_Library_Short_Detail();

            help.id = this.id;
            help.name = this.name;
            help.description = this.description;

            for (Model_TypeOfBoard typeOfBoard : this.type_of_boards) {
                help.type_of_board_names.add(typeOfBoard.name);
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

    @JsonIgnore
    public Swagger_Library_Version library_version(Model_VersionObject version_object){
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

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            this.azure_library_link = "libraries/"  + this.id;
            if (find.byId(this.id) == null) break;
        }
        super.save();
    }

    @Override
    public void update(){

        terminal_logger.debug("update :: Update object Id: " + this.id);

        //Cache Update
        //cache.put(this.id, this);

        if(project_id != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Library.class, project_id, this.id))).start();

        //Database Update
        super.update();
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("remove :: Update (hide) object Id: " + this.id);

        removed_by_user = true;

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
            if(project_id != null) return Model_Project.get_byId(project_id).update_permission(); return Controller_Security.get_person().has_permission("Library_create");
        }catch (NullPointerException exception){
            terminal_logger.warn("create_permission null pointer exception - project is not probably in cache");
            return false;
        }
    }
    @JsonIgnore   @Transient public boolean read_permission()  {  return true; }
    @JsonProperty @Transient public boolean edit_permission()  {
        try {
          
            if (project_id != null) return Model_Project.get_byId(project_id).update_permission();
            return Controller_Security.get_person().has_permission("Library_edit");

        }catch (NullPointerException exception){
            terminal_logger.warn("edit_permission null pointer exception - project is not probably in cache");
            return false;
        }
    }
    @JsonProperty @Transient public boolean delete_permission(){
        try {

             if(project_id != null) return Model_Project.get_byId(project_id).update_permission();
             return Controller_Security.get_person().has_permission("Library_delete");

        }catch (NullPointerException exception){
            terminal_logger.warn("delete_permission null pointer exception - project is not probably in cache");
            return false;
        }
    }
    @JsonProperty @Transient public boolean update_permission(){
        try {

            if(project_id != null) return Model_Project.get_byId(project_id).update_permission();
            return Controller_Security.get_person().has_permission("Library_update");

        }catch (NullPointerException exception){
            terminal_logger.warn("update_permission null pointer exception - project is not probably in cache");
            return false;
        }
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
            if (library == null){
                terminal_logger.warn("get get_version_byId_byId :: This object id:: " + id + " wasn't found.");
            }
            cache.put(id, library);
        }

        return library;
    }




/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_Library> find = new Model.Finder<>(Model_Library.class);

}
