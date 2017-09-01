package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import utilities.swagger.documentationClass.Swagger_M_Program_Version;
import utilities.swagger.documentationClass.Swagger_M_Program_Version_Interface;
import utilities.swagger.outboundClass.Swagger_M_Program_Short_Detail;
import utilities.swagger.outboundClass.Swagger_M_Program_Version_Short_Detail;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@ApiModel(description = "Model of M_Program",
        value = "M_Program")
@Table(name="MProgram")
public class Model_MProgram extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_MProgram.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                                                @Id public String id;
    @JsonInclude(JsonInclude.Include.NON_NULL)                                      public String name;         // Název programu
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Column(columnDefinition = "TEXT")  public String description;  // Uživatelský popis programu

    @JsonIgnore  public boolean removed_by_user;

    @ApiModelProperty(required = true,
            dataType = "integer", readOnly = true,
            value = "UNIX time stamp in millis", example = "1458315085338")         public Date date_of_create;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                                                  public Model_MProject m_project;
    @JsonIgnore @OneToMany(mappedBy="m_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_VersionObject> version_objects = new ArrayList<>();




/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public  String m_project_id()             {  return m_project.id;}

    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_M_Program_Version_Short_Detail> program_versions() {

        List<Swagger_M_Program_Version_Short_Detail> versions = new ArrayList<>();

        for(Model_VersionObject v : getVersion_objects_not_removed_by_person().stream().sorted((element1, element2) -> element2.date_of_create.compareTo(element1.date_of_create)).collect(Collectors.toList())){
            versions.add(v.get_short_m_program_version());
        }

        return versions;
    }

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList public List<String> cache_list_version_objects_ids = new ArrayList<>();

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    /* GET Variable short type of objects ------------------------------------------------------------------------------*/
    @Transient @JsonIgnore public Swagger_M_Program_Short_Detail get_m_program_short_detail(){
        try {

            Swagger_M_Program_Short_Detail help = new Swagger_M_Program_Short_Detail();
            help.id = id;
            help.name = name;
            help.description = description;

            help.delete_permission = delete_permission();
            help.edit_permission = edit_permission();

            return help;
        }catch (Exception e){
            terminal_logger.internalServerError("get_m_program_short_detail:", e);
            return null;
        }
    }


    @JsonIgnore @Transient @TyrionCachedList
    public List<Model_VersionObject> getVersion_objects_not_removed_by_person() {

        try{

            if(cache_list_version_objects_ids.isEmpty()){

                List<Model_VersionObject> versions =  Model_VersionObject.find.where().eq("m_program.id", this.id).eq("removed_by_user", false).order().desc("date_of_create").select("id").findList();

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


    @JsonIgnore @Transient
    public static Swagger_M_Program_Version program_version(Model_VersionObject version_object){
        try {

            Swagger_M_Program_Version m_program_versions = new Swagger_M_Program_Version();

            m_program_versions.version_object = version_object;
            m_program_versions.public_mode = version_object.public_version;

            m_program_versions.virtual_input_output = version_object.m_program_virtual_input_output;

            Model_FileRecord fileRecord = Model_FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "m_program.json").findUnique();

            if (fileRecord != null) {

                JsonNode json = Json.parse(fileRecord.get_fileRecord_from_Azure_inString());
                m_program_versions.m_code = json.get("m_code").asText();

            }

            return m_program_versions;

        }catch (Exception e){
            terminal_logger.internalServerError("program_version:", e);
            return null;
        }
    }

    @JsonIgnore @Transient public static JsonNode get_m_code(Model_VersionObject version_object) {
        try{

            Model_FileRecord fileRecord = Model_FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "m_program.json").findUnique();

            if (fileRecord != null) {
                JsonNode json = Json.parse(fileRecord.get_fileRecord_from_Azure_inString());
                return json.get("m_code");
            }

            return Json.newObject();

        }catch (Exception e){
            terminal_logger.internalServerError("get_m_code:", e);
            return Json.newObject();
        }
    }


    @JsonIgnore @Transient public List<Swagger_M_Program_Version_Interface> program_versions_interface() {
        try {

            List<Swagger_M_Program_Version_Interface> versions = new ArrayList<>();

            for (Model_VersionObject v : getVersion_objects_not_removed_by_person()) {
                Swagger_M_Program_Version_Interface help = new Swagger_M_Program_Version_Interface();
                help.version_object = v;
                help.virtual_input_output = v.m_program_virtual_input_output;
                versions.add(help);
            }
            return versions;

        }catch (Exception e){
            terminal_logger.internalServerError("program_versions_interface:", e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        terminal_logger.debug("save :: Creating new Object");

        while(true){ // I need Unique Value
            this.id = UUID.randomUUID().toString();
            this.azure_m_program_link = m_project.get_path()  + "/m-programs/"  + this.id;
            if (Model_MProgram.get_byId(this.id) == null) break;
        }

        if(m_project.project_id() != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_MProject.class, m_project.project_id(), m_project.id))).start();

        super.save();

        if(m_project != null){
            m_project.m_programs_ids.add(id);
        }

        cache.put(this.id, this);


    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);

        super.update();

        if(m_project.project != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_MProgram.class, m_project.project_id(), id))).start();

    }


    @JsonIgnore @Override public void delete() {
        terminal_logger.debug("update :: Delete object Id: {} ", this.id);

        removed_by_user = true;
        super.update();

        if(m_project_id() != null){
            Model_MProject.get_byId(m_project_id()).m_programs_ids.remove(id);
        }

        cache.remove(id);

        if(m_project.project != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_MProject.class, m_project.project_id(), m_project.id))).start();

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore            private String azure_m_program_link;

    @JsonIgnore @Transient
    public String get_path(){
        return  azure_m_program_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs              = "read: If user have M_Project.read_permission = true, you can create M_program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs            = "create: If user have M_Project.update_permission = true, you can create M_Program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String read_qr_token_permission_docs     = "read: Private settings for M_Program";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission(){

        if(Controller_Security.get_person().has_permission("M_Program_create")) return true;
        return m_project != null && m_project.update_permission();

    }

    @JsonProperty @Transient public boolean update_permission()  {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().has_permission("m_program_update_" + id)) return Controller_Security.get_person().has_permission("m_program_update_"+ id);
        if(Controller_Security.get_person().has_permission("M_Program_update")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if( Model_MProgram.find.where().eq("m_project.project.participants.person.id", Controller_Security.get_person().id).eq("id", id).findRowCount() > 0){
            Controller_Security.get_person().cache_permission("m_program_update_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().cache_permission("m_program_update_" + id, false);
        return false;

    }
    @JsonIgnore   @Transient public boolean read_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().has_permission("m_program_read_" + id)) return Controller_Security.get_person().has_permission("m_program_read_"+ id);
        if(Controller_Security.get_person().has_permission("M_Program_read")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if(Model_MProgram.find.where().eq("m_project.project.participants.person.id", Controller_Security.get_person().id).eq("id", id).findRowCount() > 0){
            Controller_Security.get_person().cache_permission("m_program_read_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().cache_permission("m_program_read_" + id, false);
        return false;

    }
    @JsonProperty @Transient public boolean edit_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().has_permission("m_program_edit_" + id)) return Controller_Security.get_person().has_permission("m_program_edit_"+ id);
        if(Controller_Security.get_person().has_permission("M_Program_edit")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if( Model_MProgram.find.where().eq("m_project.project.participants.person.id", Controller_Security.get_person().id).eq("id", id).findRowCount() > 0){
            Controller_Security.get_person().cache_permission("m_program_edit_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().cache_permission("edit_" + id, false);
        return false;

    }
    @JsonProperty @Transient public boolean delete_permission()  {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().has_permission("m_program_delete_" + id)) return Controller_Security.get_person().has_permission("m_program_delete_"+ id);
        if(Controller_Security.get_person().has_permission("M_Program_delete")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if( Model_MProgram.find.where().eq("m_project.project.participants.person.id", Controller_Security.get_person().id).eq("id", id).findRowCount() > 0){
            Controller_Security.get_person().cache_permission("m_program_delete_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().cache_permission("m_program_delete_" + id, false);
        return false;

    }

    public enum permissions{ M_Program_create, M_Program_read, M_Program_edit, M_Program_delete }


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_MProgram.class.getSimpleName();

    public static Cache<String, Model_MProgram> cache = null; // < ID, Model_BProgram>

    @JsonIgnore
    public static Model_MProgram get_byId(String id) {

        Model_MProgram m_program = cache.get(id);
        if (m_program == null){

            m_program = Model_MProgram.find.byId(id);
            if (m_program == null) return null;

            cache.put(id, m_program);
        }

        return m_program;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_MProgram> find = new Finder<>(Model_MProgram.class);
}