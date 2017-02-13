package models.project.c_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Model_FileRecord;
import models.compiler.Model_TypeOfBoard;
import models.compiler.Model_VersionObject;
import models.project.global.Model_Project;
import play.data.Form;
import play.libs.Json;
import utilities.enums.Compile_Status;
import utilities.swagger.documentationClass.Swagger_C_Program_Version_New;
import utilities.swagger.documentationClass.Swagger_C_Program_Version_Update;
import utilities.swagger.outboundClass.Swagger_C_Program_Version;
import utilities.swagger.outboundClass.Swagger_C_Program_Version_Short_Detail;
import utilities.swagger.outboundClass.Swagger_C_program_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Example_Short_Detail;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Entity
@ApiModel(value="C_Program", description="Object represented C_Program in database")
public class Model_CProgram extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                                                                    @Id public String id;
    @ApiModelProperty(required = true, value = "minimal length is 8 characters")                        public String name;
    @ApiModelProperty(required = false, value = "can be empty")  @Column(columnDefinition = "TEXT")     public String description;
                                              @JsonIgnore @ManyToOne(cascade = CascadeType.PERSIST)     public Model_Project project;

                       @JsonIgnore  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)  public Model_TypeOfBoard type_of_board;  // Typ desky


    @ApiModelProperty(required = true, dataType = "integer", readOnly = true,
            value = "UNIX time in ms",
            example = "1466163478925")                                                                  public Date date_of_create;

    @JsonIgnore @OneToMany(mappedBy="c_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_VersionObject> version_objects = new ArrayList<>();

                                                                                @JsonIgnore @OneToOne   public Model_TypeOfBoard type_of_board_default;
                       @JsonIgnore @OneToOne(mappedBy = "default_program", cascade = CascadeType.ALL)   public Model_VersionObject default_main_version;

                                                                                @JsonIgnore @ManyToOne  public Model_VersionObject example_library; // Program je příklad pro použití knihovny

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty  @Transient public String project_id()           { return project != null ? project.id : null; }
    @JsonProperty  @Transient public String project_name()         { return project != null ? project.name : null;}
    @JsonProperty  @Transient public String type_of_board_id()     { return type_of_board == null ? null : type_of_board.id;}
    @JsonProperty  @Transient public String type_of_board_name()   { return type_of_board == null ? null : type_of_board.name;}


    @JsonProperty @Transient public List<Swagger_C_Program_Version_Short_Detail> program_versions() {

        List<Swagger_C_Program_Version_Short_Detail> versions = new ArrayList<>();

        for(Model_VersionObject version : getVersion_objects()){
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

    @JsonIgnore public Swagger_Example_Short_Detail get_example_short_detail(){

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
    }


/* Private Documentation Class -------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public List<Model_VersionObject> getVersion_objects() {
        return Model_VersionObject.find.where().eq("c_program.id", id).eq("removed_by_user", false).order().desc("date_of_create").findList();
    }

    @JsonIgnore @Transient public List<Model_VersionObject> getVersion_objects_all_For_Admin() {
        return Model_VersionObject.find.where().eq("c_program.id", id).order().desc("date_of_create").findList();
    }

    // Objekt určený k vracení verze
    @JsonIgnore @Transient
    public Swagger_C_Program_Version program_version(Model_VersionObject version_object){
        try {

            Swagger_C_Program_Version c_program_versions = new Swagger_C_Program_Version();

            c_program_versions.status = version_object.c_compilation != null ? version_object.c_compilation.status : Compile_Status.undefined;
            c_program_versions.version_object = version_object;
            c_program_versions.remove_permission = delete_permission();
            c_program_versions.edit_permission   = edit_permission();

            Model_FileRecord fileRecord = Model_FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "code.json").findUnique();

            if (fileRecord != null) {

                JsonNode json = Json.parse(fileRecord.get_fileRecord_from_Azure_inString());

                Swagger_C_Program_Version_New version_new = Json.fromJson(json, Swagger_C_Program_Version_New.class);

                c_program_versions.main = version_new.main;
                c_program_versions.user_files = version_new.user_files;

                for (String lib_id : version_new.library_files) {

                    Model_VersionObject v = Model_VersionObject.find.byId(lib_id);
                    if (v == null || v.library == null) break;
                    if (!v.files.isEmpty()) {
                        for (Model_FileRecord f : v.files) {

                            JsonNode j = Json.parse(f.get_fileRecord_from_Azure_inString());

                            Form<Swagger_C_Program_Version_New.Library_File> lib_form = Form.form(Swagger_C_Program_Version_New.Library_File.class).bind(j);
                            if (lib_form.hasErrors()) break;

                            Swagger_C_Program_Version_New.Library_File lib_file = lib_form.get();

                            for (Swagger_C_Program_Version_New.User_File user_file : c_program_versions.user_files) {

                                if (lib_file.file_name.equals(user_file.file_name)) break;
                                if (!c_program_versions.library_files.contains(lib_file)) c_program_versions.library_files.add(lib_file);

                            }
                        }
                    }
                }
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
                this.id = UUID.randomUUID().toString();
                this.azure_c_program_link = project.get_path()  + "/c-programs/"  + this.id;
                if (Model_CProgram.find.byId(this.id) == null) break;
            }
        }


        else{      // C_Program je veřejný
            while(true){ // I need Unique Value
                this.id = UUID.randomUUID().toString();
                this.azure_c_program_link = "public-c-programs/"  + this.id;
                if (Model_CProgram.find.byId(this.id) == null) break;
            }
        }

        super.save();
    }

    @JsonIgnore @Transient
    public String get_path(){

       if(azure_c_program_link == null){  // Tato vyjímka je tu pro demo data která nevyvolají metodu save();
            while(true){ // I need Unique Value
                this.azure_c_program_link = "public-c-programs/"  + UUID.randomUUID().toString();
                if (Model_CProgram.find.where().eq("azure_c_program_link", azure_c_program_link ).findUnique() == null) break;
            }
            update();
        }


        return  azure_c_program_link;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.read_permission = true, you can read C_program on this Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create C_program on this Project - Or you need static/dynamic permission key";

    @JsonIgnore   @Transient  @ApiModelProperty(required = true) public boolean create_permission(){  return project != null ? ( project.update_permission() ) : Controller_Security.getPerson().has_permission("C_program_create");      }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean update_permission(){  return ( Model_CProgram.find.where().eq("project.participants.person.id", Controller_Security.getPerson().id).eq("id", id).findRowCount() > 0) || Controller_Security.getPerson().has_permission("C_program_update"); }
    @JsonIgnore   @Transient  @ApiModelProperty(required = true) public boolean read_permission()  {
        if(project == null) return true;
        return ( Model_CProgram.find.where().eq("project.participants.person.id", Controller_Security.getPerson().id).eq("id", id).findRowCount() > 0) || Controller_Security.getPerson().has_permission("C_program_read");
    }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean edit_permission()  {  return ( Model_CProgram.find.where().eq("project.participants.person.id", Controller_Security.getPerson().id).eq("id", id).findRowCount() > 0) || Controller_Security.getPerson().has_permission("C_program_edit"); }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean delete_permission(){  return ( Model_CProgram.find.where().eq("project.participants.person.id", Controller_Security.getPerson().id).eq("id", id).findRowCount() > 0) || Controller_Security.getPerson().has_permission("C_program_delete"); }

    public enum permissions{  C_program_create,  C_program_update, C_program_read ,  C_program_edit, C_program_delete; }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_CProgram> find = new Finder<>(Model_CProgram.class);
}

