package models.project.c_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Board;
import models.compiler.TypeOfBoard;
import models.compiler.Version_Object;
import models.project.global.Project;

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
                                                                public String program_name;
                          @Column(columnDefinition = "TEXT")    public String program_description;
                                      @JsonIgnore @ManyToOne    public Project project;
                                   @Transient  @JsonProperty    public String  project_id(){ return project.id; }

                                               @JsonIgnore      public String azurePackageLink;
                                               @JsonIgnore      public String azureStorageLink;
                                   @JsonIgnore  @ManyToOne      public TypeOfBoard type_of_board;  // Typ desky


    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1461854312") public Date dateOfCreate;
    @JsonIgnore @OneToMany(mappedBy="c_program", cascade = CascadeType.ALL) @OrderBy("azureLinkVersion DESC") public List<Version_Object> version_objects = new ArrayList<>();



/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty  @Transient public String type_of_board_id()   { return type_of_board == null ? null : type_of_board.id;}

    @JsonProperty @Transient public List<C_Program_Versions> program_versions() {
        List<C_Program_Versions> versions = new ArrayList<>();

        for(Version_Object v : version_objects){

            C_Program_Versions c_program_versions= new C_Program_Versions();

            c_program_versions.version_object = v;
            c_program_versions.successfully_compiled = v.c_compilation != null;
            if(v.c_compilation != null ) c_program_versions.virtual_input_output = v.c_compilation.virtual_input_output;

            versions.add(c_program_versions);
        }

        return versions;
    }

/* Private Documentation Class -----------------------------------------------------------------------------------------*/

    // Určeno pro metodu program_versions tohoto objektu
    class C_Program_Versions{
        public Version_Object version_object;
        public boolean successfully_compiled;
        public String virtual_input_output;
        public boolean compilation_restored;
        public List<Board> runing_on_board;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void setUniqueAzureStorageLink() {
        while(true){ // I need Unique Value
            this.azureStorageLink = UUID.randomUUID().toString();
            if (C_Program.find.where().eq("azureStorageLink", azureStorageLink ).findUnique() == null) break;
        }
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.read_permission = true, you can read C_program on this Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create C_program on this Project - Or you need static/dynamic permission key";

    @JsonIgnore   @Transient public Boolean create_permission(){  return ( project.update_permission() ) || SecurityController.getPerson().has_permission("C_program_create");      }
    @JsonProperty @Transient public Boolean update_permission(){  return ( C_Program.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("C_program_update"); }
    @JsonIgnore   @Transient public Boolean read_permission()  {  return ( C_Program.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("C_program_read"); }
    @JsonProperty @Transient public Boolean edit_permission()  {  return ( C_Program.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("C_program_edit"); }
    @JsonProperty @Transient public Boolean delete_permission(){  return ( C_Program.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("C_program_delete"); }

    public enum permissions{  C_program_create,  C_program_update, C_program_read ,  C_program_edit, C_program_delete; }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,C_Program> find = new Finder<>(C_Program.class);
}
