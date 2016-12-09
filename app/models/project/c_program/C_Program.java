package models.project.c_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.SecurityController;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.FileRecord;
import models.compiler.TypeOfBoard;
import models.compiler.Version_Object;
import models.project.global.Project;
import play.libs.Json;
import utilities.enums.Compile_Status;
import utilities.swagger.documentationClass.Swagger_C_Program_Version_New;
import utilities.swagger.outboundClass.Swagger_C_Program_Version;
import utilities.swagger.outboundClass.Swagger_C_Program_Version_Short_Detail;
import utilities.swagger.outboundClass.Swagger_C_program_Short_Detail;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Entity
@ApiModel(value="C_Program", description="Object represented C_Program in database")
public class C_Program extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;


    @ApiModelProperty(required = true, value = "minimal length is 8 characters")                        public String name;
    @ApiModelProperty(required = false, value = "can be empty")  @Column(columnDefinition = "TEXT")     public String description;
                                              @JsonIgnore @ManyToOne(cascade = CascadeType.PERSIST)     public Project project;

                                @JsonIgnore  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)            public TypeOfBoard type_of_board;  // Typ desky


    @ApiModelProperty(required = true, dataType = "integer", readOnly = true,
            value = "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970",
            example = "1466163478925")                                                       public Date date_of_create;

    @JsonIgnore @OneToMany(mappedBy="c_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)public List<Version_Object> version_objects = new ArrayList<>();
                                                                                     @JsonIgnore @ManyToOne(fetch = FetchType.EAGER)   public Version_Object first_default_version_object;

    @JsonIgnore @OneToOne()                                   public TypeOfBoard default_program_type_of_board;   // Pro defaultní program na devicu a první verzi C_Programu při vytvoření  (Určeno výhradně pro Byzance)
    @JsonIgnore @OneToOne(mappedBy="default_version_program") public Version_Object default_main_version;        // Defaultní verze programu, konkrétního typu desky  (Určeno výhradně pro Byzance)

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty  @Transient public String project_id()           { return project != null ? project.id : null; }
    @JsonProperty  @Transient public String project_name()         { return project != null ? project.name : null;}
    @JsonProperty  @Transient public String type_of_board_id()     { return type_of_board == null ? null : type_of_board.id;}
    @JsonProperty  @Transient public String type_of_board_name()   { return type_of_board == null ? null : type_of_board.name;}


    @JsonProperty @Transient public List<Swagger_C_Program_Version_Short_Detail> program_versions() {

        List<Swagger_C_Program_Version_Short_Detail> versions = new ArrayList<>();

        for(Version_Object version : getVersion_objects()){
            versions.add(version.get_short_c_program_version());
        }

        //if(first_default_version_object != null) versions.add(first_default_version_object.get_short_c_program_version());

        return versions;
    }

/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_C_program_Short_Detail get_c_program_short_detail(){

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
    }


/* Private Documentation Class -------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public List<Version_Object> getVersion_objects() {
        return Version_Object.find.where().eq("c_program.id", id).eq("removed_by_user", false).order().desc("date_of_create").findList();
    }

    @JsonIgnore @Transient public List<Version_Object> getVersion_objects_all_For_Admin() {
        return Version_Object.find.where().eq("c_program.id", id).order().desc("date_of_create").findList();
    }

    // Objekt určený k vracení verze
    @JsonIgnore @Transient
    public Swagger_C_Program_Version program_version(Version_Object version_object){
        try {

            Swagger_C_Program_Version c_program_versions = new Swagger_C_Program_Version();

            c_program_versions.status = version_object.c_compilation != null ? version_object.c_compilation.status : Compile_Status.undefined;
            c_program_versions.version_object = version_object;
            c_program_versions.remove_permission = delete_permission();
            c_program_versions.edit_permission   = edit_permission();

            FileRecord fileRecord = FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "code.json").findUnique();

            if (fileRecord != null) {

                JsonNode json = Json.parse(fileRecord.get_fileRecord_from_Azure_inString());

                Swagger_C_Program_Version_New version_new = Json.fromJson(json, Swagger_C_Program_Version_New.class);

                c_program_versions.main = version_new.main;
                c_program_versions.user_files = version_new.user_files;
                c_program_versions.external_libraries = version_new.external_libraries;

            }

            if (version_object.c_compilation != null) {
                c_program_versions.virtual_input_output = version_object.c_compilation.virtual_input_output;
            }


            return c_program_versions;

        }catch (Exception e){
          e.printStackTrace();
          return null;
        }
    }



/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String azure_c_program_link; // Link, který je náhodně generovaný pro Azure - a který se připojuje do cesty souborům

    @JsonIgnore @Override public void save() {


        if(project != null){   // C_Program je vázaný na Projekt
            while(true){ // I need Unique Value
                this.azure_c_program_link = project.get_path()  + "/c-programs/"  + UUID.randomUUID().toString();
                if (C_Program.find.where().eq("azure_c_program_link", azure_c_program_link ).findUnique() == null) break;
            }
        }


        else{      // C_Program je veřejný
            while(true){ // I need Unique Value
                this.azure_c_program_link = "public-c-programs/"  + UUID.randomUUID().toString();
                if (C_Program.find.where().eq("azure_c_program_link", azure_c_program_link ).findUnique() == null) break;
            }
        }

        super.save();
    }

    @JsonIgnore @Transient
    public String get_path(){

       if(azure_c_program_link == null){  // Tato vyjímka je tu pro demo data která nevyvolají metodu save();
            while(true){ // I need Unique Value
                this.azure_c_program_link = "public-c-programs/"  + UUID.randomUUID().toString();
                if (C_Program.find.where().eq("azure_c_program_link", azure_c_program_link ).findUnique() == null) break;
            }
            update();
        }


        return  azure_c_program_link;
    }



/* Object Override ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void delete() {

        // Slouží k odpojení defaultních prvních verzí programu pro divnostav
        // Lexa

        if (this.first_default_version_object != null) {
            this.first_default_version_object.first_version_of_c_programs.remove(this);
            this.first_default_version_object.update();
        }

        if (this.default_main_version != null) this.default_main_version.delete();

        super.delete();
    }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.read_permission = true, you can read C_program on this Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create C_program on this Project - Or you need static/dynamic permission key";

    @JsonIgnore   @Transient  @ApiModelProperty(required = true) public boolean create_permission(){  return project != null ? ( project.update_permission() ) : SecurityController.getPerson().has_permission("C_program_create");      }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean update_permission(){  return ( C_Program.find.where().eq("project.participants.person.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("C_program_update"); }
    @JsonIgnore   @Transient  @ApiModelProperty(required = true) public boolean read_permission()  {
        if(project == null) return true;
        return ( C_Program.find.where().eq("project.participants.person.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("C_program_read");
    }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean edit_permission()  {  return ( C_Program.find.where().eq("project.participants.person.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("C_program_edit"); }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean delete_permission(){  return ( C_Program.find.where().eq("project.participants.person.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("C_program_delete"); }

    public enum permissions{  C_program_create,  C_program_update, C_program_read ,  C_program_edit, C_program_delete; }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,C_Program> find = new Finder<>(C_Program.class);
}

