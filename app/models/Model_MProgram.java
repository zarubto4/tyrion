package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.libs.Json;
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

@Entity
@ApiModel(description = "Model of M_Program",
        value = "M_Program")
public class Model_MProgram extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_MacAddressRegisterRecord.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                                                @Id public String id;
    @JsonInclude(JsonInclude.Include.NON_NULL)                                      public String name;         // Název programu
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Column(columnDefinition = "TEXT")  public String description;  // Uživatelský popis programu

    @JsonIgnore  public boolean removed_by_user;

    @ApiModelProperty(required = true,
            dataType = "integer", readOnly = true,
            value = "UNIX time stamp in millis", example = "1458315085338")         public Date date_of_create;

                                    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_MProject m_project;
    @JsonIgnore @OneToMany(mappedBy="m_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_VersionObject> version_objects = new ArrayList<>();

/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public  String m_project_id()             {  return m_project.id;}


    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_M_Program_Version_Short_Detail> program_versions() {
        List<Swagger_M_Program_Version_Short_Detail> versions = new ArrayList<>();
        for(Model_VersionObject v : getVersion_objects()) versions.add(v.get_short_m_program_version());
        return versions;
    }

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
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    // Objekt určený k vracení verze - Fatch lazy!!
    @JsonIgnore @Transient
    public List<Model_VersionObject> getVersion_objects() {
        return Model_VersionObject.find.where().eq("m_program.id", this.id).eq("removed_by_user", false).order().desc("date_of_create").findList();
    }


    @JsonIgnore @Transient
    public static Swagger_M_Program_Version program_version(Model_VersionObject version_object){
        try {

            Swagger_M_Program_Version m_program_versions = new Swagger_M_Program_Version();

            m_program_versions.version_object = version_object;
            m_program_versions.public_mode = version_object.public_version;
            //m_program_versions.qr_token = version_object.qr_token; TODO Tomáš
            //m_program_versions.qr_token = version_object.qr_token;
            m_program_versions.virtual_input_output = version_object.m_program_virtual_input_output;

            Model_FileRecord fileRecord = Model_FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "m_program.json").findUnique();

            if (fileRecord != null) {

                JsonNode json = Json.parse(fileRecord.get_fileRecord_from_Azure_inString());
                m_program_versions.m_code = json.get("m_code").asText();


            }

            return m_program_versions;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore @Transient public static String get_m_code(Model_VersionObject version_object) {
        try{

            Model_FileRecord fileRecord = Model_FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "m_program.json").findUnique();

            if (fileRecord != null) {
                JsonNode json = Json.parse(fileRecord.get_fileRecord_from_Azure_inString());
                return json.get("m_code").asText();
            }

            return null;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }


    @JsonIgnore @Transient public List<Swagger_M_Program_Version_Interface> program_versions_interface() {
        try {

            List<Swagger_M_Program_Version_Interface> versions = new ArrayList<>();

            for (Model_VersionObject v : getVersion_objects()) {
                Swagger_M_Program_Version_Interface help = new Swagger_M_Program_Version_Interface();
                help.version_object = v;
                help.virtual_input_output = v.m_program_virtual_input_output;
                versions.add(help);
            }
            return versions;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        terminal_logger.debug("save :: Creating new Object");

        while(true){ // I need Unique Value
            this.id = UUID.randomUUID().toString();
            this.azure_m_program_link = m_project.get_path()  + "/m-programs/"  + this.id;
            if (Model_MProgram.find.byId(this.id) == null) break;
        }

        if(m_project.project != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_MProject.class, m_project.project_id(), m_project.id))).start();

        super.save();
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

    @JsonIgnore   @Transient public boolean create_permission(){  return ( Model_Project.find.where().where().eq("participants.person.id", Controller_Security.get_person().id ).eq("m_projects.id", m_project.id).findUnique().create_permission() ) || Controller_Security.get_person().has_permission("M_Program_create");      }
    @JsonIgnore   @Transient public boolean read_permission()  {
        if(Controller_Security.get_person() == null){terminal_logger.warn("read_permission:: Person is null in read_permission");}
        return ( Model_MProgram.find.where().eq("m_project.project.participants.person.id", Controller_Security.get_person().id).where().eq("id", id).findRowCount() > 0) ||
                Controller_Security.get_person().has_permission("M_Program_read");
    }
    @JsonProperty @Transient public boolean read_qr_token_permission() { return  true; } // TODO pokud uživatel vyloženě nebude chtít zakázat public přístup
    @JsonProperty @Transient public boolean edit_permission() {return Controller_Security.get_person() != null && ((Model_MProgram.find.where().eq("m_project.project.participants.person.id", Controller_Security.get_person().id).where().eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("M_Program_edit"));}
    @JsonProperty @Transient public boolean delete_permission(){
       if (Controller_Security.get_person() == null) return false;
        return ( Model_MProgram.find.where().eq("m_project.project.participants.person.id", Controller_Security.get_person().id).eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("M_Program_delete");
    }

    public enum permissions{ M_Program_create, M_Program_read, M_Program_edit, M_Program_delete }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_MProgram> find = new Finder<>(Model_MProgram.class);
}
