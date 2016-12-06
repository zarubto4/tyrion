package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.FileRecord;
import models.compiler.Version_Object;
import models.project.global.Project;
import play.libs.Json;
import utilities.swagger.documentationClass.Swagger_M_Program_Version;
import utilities.swagger.documentationClass.Swagger_M_Program_Version_Interface;
import utilities.swagger.outboundClass.Swagger_M_Program_Short_Detail;
import utilities.swagger.outboundClass.Swagger_M_Program_Version_Short_Detail;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class M_Program extends Model{

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)                         public String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)                                      public String name;         // Název programu
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Column(columnDefinition = "TEXT")  public String description;  // Uživatelský popis programu

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp in millis", example = "1458315085338") public Date date_of_create;



    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public M_Project m_project;
    @JsonIgnore @OneToMany(mappedBy="m_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY) @OrderBy("date_of_create DESC") public List<Version_Object> version_objects = new ArrayList<>();




/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public  String m_project_id()             {  return m_project.id;}


    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_M_Program_Version_Short_Detail> program_versions() {
        List<Swagger_M_Program_Version_Short_Detail> versions = new ArrayList<>();
        for(Version_Object v : getVersion_objects()) versions.add(v.get_short_m_program_version());
        return versions;
    }


/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_M_Program_Short_Detail get_m_program_short_detail(){
        Swagger_M_Program_Short_Detail help = new Swagger_M_Program_Short_Detail();
        help.id = id;
        help.name = name;
        help.description = description;

        help.delete_permission = delete_permission();
        help.edit_permission = edit_permission();

        return help;
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    // Objekt určený k vracení verze - Fatch lazy!!
    @JsonIgnore @Transient
    public List<Version_Object> getVersion_objects() {
        return Version_Object.find.where().eq("m_program.id", this.id).eq("removed_by_user", false).order().asc("date_of_create").findList();
    }


    @JsonIgnore @Transient
    public static Swagger_M_Program_Version program_version(Version_Object version_object){
        try {

            Swagger_M_Program_Version m_program_versions = new Swagger_M_Program_Version();

            m_program_versions.version_object = version_object;
            m_program_versions.public_mode = version_object.public_version;
            m_program_versions.qr_token = version_object.qr_token;

            FileRecord fileRecord = FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "m_program.json").findUnique();

            if (fileRecord != null) {

                JsonNode json = Json.parse(fileRecord.get_fileRecord_from_Azure_inString());
                m_program_versions.m_code = json.get("m_code").asText();
                m_program_versions.virtual_input_output = json.get("virtual_input_output").asText();

            }

            return m_program_versions;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @JsonIgnore @Transient public static String get_m_code(Version_Object version_object) {
        FileRecord fileRecord = FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "m_program.json").findUnique();

        if (fileRecord != null) {

            JsonNode json = Json.parse(fileRecord.get_fileRecord_from_Azure_inString());
            return json.get("m_code").asText();
        }

        return null;
    }


    @JsonIgnore @Transient public List<Swagger_M_Program_Version_Interface> program_versions_interface() {
        List<Swagger_M_Program_Version_Interface> versions = new ArrayList<>();

        for(Version_Object v : getVersion_objects()) versions.add(program_version_interface(v));
        return versions;
    }


    @JsonIgnore @Transient
    public Swagger_M_Program_Version_Interface program_version_interface(Version_Object version_object){
        try {

            Swagger_M_Program_Version_Interface m_program_versions = new Swagger_M_Program_Version_Interface();
            m_program_versions.version_object = version_object;

            FileRecord fileRecord = FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "m_program.json").findUnique();

            if (fileRecord != null) {

                JsonNode json = Json.parse(fileRecord.get_fileRecord_from_Azure_inString());
                m_program_versions.virtual_input_output = json.get("virtual_input_output").asText();

            }

            return m_program_versions;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/
    @JsonIgnore            private String azure_m_program_link;


    @JsonIgnore @Override public void save() {

         while(true){ // I need Unique Value
                this.azure_m_program_link = m_project.get_path()  + "/m-programs/"  + UUID.randomUUID().toString();
                if (M_Program.find.where().eq("azure_m_program_link", azure_m_program_link ).findUnique() == null) break;
         }

        super.save();
    }

    @JsonIgnore @Transient
    public String get_path(){
        return  azure_m_program_link;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs              = "read: If user have M_Project.read_permission = true, you can create M_program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs            = "create: If user have M_Project.update_permission = true, you can create M_Program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String read_qr_token_permission_docs     = "read: Private settings for M_Program";


    @JsonIgnore   @Transient public boolean create_permission(){  return ( Project.find.where().where().eq("ownersOfProject.id", SecurityController.getPerson().id ).eq("m_projects.id", m_project.id).findUnique().create_permission() ) || SecurityController.getPerson().has_permission("M_Program_create");      }
    @JsonIgnore   @Transient public boolean read_permission()  {  return ( M_Program.find.where().eq("m_project.project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Program_read"); }
    @JsonProperty @Transient public boolean read_qr_token_permission() { return  true; } // TODO pokud uživatel vyloženě nebude chtít zakázat public přístup
    @JsonProperty @Transient public boolean edit_permission() {return SecurityController.getPerson() != null && ((M_Program.find.where().eq("m_project.project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Program_edit"));}
    @JsonProperty @Transient public boolean delete_permission(){
       if (SecurityController.getPerson() == null) return false;
        return ( M_Program.find.where().eq("m_project.project.ownersOfProject.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Program_delete");
    }

    public enum permissions{ M_Program_create, M_Program_read, M_Program_edit, M_Program_delete }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,M_Program> find = new Finder<>(M_Program.class);
}
