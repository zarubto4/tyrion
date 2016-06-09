package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.blocko.Cloud_Blocko_Server;
import models.compiler.Version_Object;
import models.project.global.Project;
import models.project.m_program.M_Project;
import utilities.swagger.outboundClass.B_Program_State;

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
                        @Column(columnDefinition = "TEXT")   public String program_description;

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1461854312") public Date lastUpdate;
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1461854312") public Date dateOfCreate;
                                    @JsonIgnore @ManyToOne   public Project project;
                                                @JsonIgnore  public String azurePackageLink;
                                                @JsonIgnore  public String azureStorageLink;

    @JsonIgnore   @OneToOne(mappedBy="b_program",cascade=CascadeType.ALL) public M_Project m_project;

    @JsonIgnore   @OneToMany(mappedBy="b_program", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC") public List<Version_Object> versionObjects = new ArrayList<>();
                                                                    @JsonProperty @Transient     public String   project_id() {  return project.id; }


/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient public List<Blocko_Versions> program_versions() {
        List<Blocko_Versions> versions = new ArrayList<>();

        for(Version_Object v : versionObjects){
            Blocko_Versions bl = new Blocko_Versions();
            bl.version_Object = v;
            bl.connected_boards = v.b_pairs_b_program;

            versions.add(bl);
        }

        return versions;
    }

    class Blocko_Versions{
        public Version_Object version_Object;
        public List<B_Pair> connected_boards = new ArrayList<>();
    }

    @JsonProperty @Transient public B_Program_State program_state(){

        B_Program_State state = new B_Program_State();
        state.m_project_id = m_project != null ? m_project.id : null;

        Version_Object version_object = where_program_run();

        if(version_object == null){
            state.uploaded = false;
            return state;
        }

        state.uploaded = true;
        state.version_id = version_object.id;

        if( version_object.b_program_cloud != null ) {
            state.where = "cloud";

            Cloud_Blocko_Server server = Cloud_Blocko_Server.find.where().eq("cloud_programs.id", version_object.b_program_cloud.id).findUnique();

            state.set_Cloud_State(version_object.b_program_cloud, server.server_name );
        }
        else {
            state.where = "homer";
            state.set_Local_State(version_object.b_program_homer, version_object.b_program_homer.homer);

        }

        return state;
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public void setUniqueAzureStorageLink() {
        while(true){ // I need Unique Value
            this.azureStorageLink = UUID.randomUUID().toString();
            if (B_Program.find.where().eq("azureStorageLink", azureStorageLink ).findUnique() == null) break;
        }
    }

    @JsonIgnore @Transient  public Version_Object where_program_run(){
        Version_Object version_object = Version_Object.find.where().eq("b_program.id", id).where().or(
                com.avaje.ebean.Expr.isNotNull("b_program_cloud"),
                com.avaje.ebean.Expr.isNotNull("b_program_homer")
        ).findUnique();

        return  version_object;
    }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public Boolean create_permission()  {  return  ( Project.find.where().where().eq("ownersOfProject.id", SecurityController.getPerson().id ).eq("id", project.id ).findUnique().create_permission() ) || SecurityController.getPerson().has_permission("B_Program_create");  }
    @JsonProperty @Transient public Boolean update_permission()  {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_update");  }
    @JsonIgnore   @Transient public Boolean read_permission()    {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_read");   }
    @JsonProperty @Transient public Boolean edit_permission()    {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_edit");    }
    @JsonProperty @Transient public Boolean delete_permission()  {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_delete");  }

    public enum permissions{ B_Program_create, B_Program_update, B_Program_read, B_Program_edit , B_Program_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
     public static Finder<String,B_Program> find = new Finder<>(B_Program.class);
}

