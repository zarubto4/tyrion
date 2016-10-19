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
import models.grid.Screen_Size_Type;
import models.project.global.Project;
import play.libs.Json;
import utilities.Server;
import utilities.swagger.documentationClass.Swagger_M_Program_Version;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class M_Program extends Model{

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;


    //# Název a popis Programu
    @JsonInclude(JsonInclude.Include.NON_NULL)                                      public String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Column(columnDefinition = "TEXT")  public String description;

                                            //# NAstavení Programu
                                            public boolean height_lock;
                                            public boolean width_lock;


    @ApiModelProperty(required = true)      public String qr_token;

    //# Vazby Programu
    @JsonIgnore @ManyToOne      public M_Project m_project;
    @JsonIgnore @ManyToOne      public Screen_Size_Type screen_size_type;

    // Každá verze má datum vytvoření
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp in millis", example = "1458315085338") public Date date_of_create;



    @JsonIgnore @OneToMany(mappedBy="m_program", cascade = CascadeType.ALL, fetch = FetchType.EAGER) @OrderBy("date_of_create DESC") public List<Version_Object> version_objects = new ArrayList<>();




/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @Transient @JsonProperty @ApiModelProperty(required = true) public  String m_project_id()             {  return m_project.id;}
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String screen_size_type_id()      {  return screen_size_type.id;}



    @ApiModelProperty(required = false, value = "Its here only if its possible to connect to B_Program") @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty public String websocket_address(){
        return (m_project == null || m_project.b_program_version == null) ? null :  Server.tyrion_webSocketAddress + "/websocket/mobile/" + m_project.id + "/{terminal_id}";
    }


    @JsonProperty @Transient public List<Swagger_M_Program_Version> program_versions() {
        List<Swagger_M_Program_Version> versions;
        versions = new ArrayList<>();

        for(Version_Object v : version_objects) versions.add(program_version(v));
        return versions;
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    /* Private Documentation Class -------------------------------------------------------------------------------------*/

    // Objekt určený k vracení verze
    @JsonIgnore @Transient
    public Swagger_M_Program_Version program_version(Version_Object version_object){
        try {

            Swagger_M_Program_Version m_program_versions = new Swagger_M_Program_Version();

            m_program_versions.version_object = version_object;

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


    @Transient @JsonIgnore
    public void set_QR_Token() {
        while(true){ // I need Unique Value
            this.qr_token  = UUID.randomUUID().toString();
            if (M_Program.find.where().eq("qr_token", this.qr_token ).findUnique() == null) break;
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
    public static Finder<String,M_Program> find = new Finder<>(M_Program.class);
}
