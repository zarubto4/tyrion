package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.FileRecord;
import models.compiler.Version_Object;
import models.project.b_program.instnace.Homer_Instance;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.global.Project;
import utilities.swagger.outboundClass.Swagger_B_Program_State;
import utilities.swagger.outboundClass.Swagger_B_Program_Version;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Entity
public class B_Program extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String name;
                        @Column(columnDefinition = "TEXT")   public String description;
    @JsonIgnore @OneToOne(cascade = CascadeType.ALL)         public Homer_Instance instance; // TODO - do budoucna více instnací!!!!

    @ApiModelProperty(required = true,
                     dataType = "integer", readOnly = true,
                     value = "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970",
                     example = "1466163478925")              public Date last_update;
    @ApiModelProperty(required = true,
            dataType = "integer", readOnly = true,
            value = "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970",
            example = "1466163478925")                       public Date date_of_create;
                                    @JsonIgnore @ManyToOne   public Project project;

    @JsonIgnore   @OneToMany(mappedBy="b_program", cascade=CascadeType.ALL, fetch = FetchType.LAZY) public List<Version_Object> version_objects = new ArrayList<>();
                                                                    @JsonProperty @Transient     public String   project_id() {  return project.id; }


/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient public List<Swagger_B_Program_Version> program_versions() {

        List<Swagger_B_Program_Version> versions = new ArrayList<>();

        for(Version_Object v : getVersion_objects()){

            Swagger_B_Program_Version b_program_version = new Swagger_B_Program_Version();
            b_program_version.version_object = v;
            b_program_version.hardware_group = v.b_program_hw_groups;
            b_program_version.m_project_program_snapshots = v.b_program_version_snapshots;

            FileRecord fileRecord = FileRecord.find.where().eq("version_object.id", v.id).eq("file_name", "program.js").findUnique();
            if(fileRecord != null) b_program_version.program             = fileRecord.get_fileRecord_from_Azure_inString();

            versions.add(b_program_version);
        }

        return versions;
    }

    @JsonProperty @Transient public Swagger_B_Program_State instance_details(){

        Swagger_B_Program_State state = new Swagger_B_Program_State();

        if(instance.actual_instance == null) {
            state.uploaded = false;
            return  state;
        }

        // Je nahrán
        state.uploaded = true;          // Jestli je aktuální - nebo plánovaný
        state.instance_online = instance.instance_online();

        // Jaká verze Blocko Programu?
        state.version_id = instance.actual_instance.version_object.id;
        state.version_name = instance.actual_instance.version_object.version_name;

        // Instnace ID
        state.instance_id = instance.blocko_instance_name;

        // Informace o Serveru
        state.server_id = instance.cloud_homer_server.id;
        state.server_name = instance.cloud_homer_server.server_name;
        state.server_online = instance.cloud_homer_server.server_is_online();

        return state;
    }




/* Private Documentation Class -----------------------------------------------------------------------------------------*/


    // Objekt určený k vracení verze
    @JsonIgnore @Transient
    public Swagger_B_Program_Version program_version(Version_Object version_object){

        Swagger_B_Program_Version b_program_version = new Swagger_B_Program_Version();

        b_program_version.version_object                = version_object;
        b_program_version.hardware_group                = version_object.b_program_hw_groups;
        b_program_version.m_project_program_snapshots   = version_object.b_program_version_snapshots;

        FileRecord fileRecord = FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "program.js").findUnique();
        if(fileRecord != null) b_program_version.program             = fileRecord.get_fileRecord_from_Azure_inString();

        return b_program_version;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public List<Version_Object> getVersion_objects() {
        return Version_Object.find.where().eq("b_program.id", id).eq("removed_by_user", false).order().asc("date_of_create").findList();
    }



/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore            private String azure_b_program_link;


    @JsonIgnore @Override public void save() {
        while(true){ // I need Unique Value
            this.azure_b_program_link = project.get_path() + "/b-programs/"  + UUID.randomUUID().toString();
            if (B_Program.find.where().eq("azure_b_program_link", azure_b_program_link ).findUnique() == null) break;
        }


        if(instance == null){
            Cloud_Homer_Server destination_server = Cloud_Homer_Server.find.where().eq("server_name", "Alfa").findUnique();

            Homer_Instance instance = new Homer_Instance();
            instance.cloud_homer_server = destination_server;
            instance.save();
            this.instance = instance;

        }
        super.save();
    }

    @JsonIgnore @Override public void delete() {

       instance.delete();

    }


    @JsonIgnore @Transient
    public String get_path(){
        return azure_b_program_link;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission()  {  return  ( project.read_permission() ) || SecurityController.getPerson().has_permission("B_Program_create");  }
    @JsonProperty @Transient public boolean update_permission()  {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_update");  }
    @JsonIgnore   @Transient public boolean read_permission()    {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_read");   }
    @JsonProperty @Transient public boolean edit_permission()    {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_edit");    }
    @JsonProperty @Transient public boolean delete_permission()  {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_delete");  }

    public enum permissions{ B_Program_create, B_Program_update, B_Program_read, B_Program_edit , B_Program_delete}



/* FINDER --------------------------------------------------------------------------------------------------------------*/
     public static Model.Finder<String,B_Program> find = new Finder<>(B_Program.class);
}

