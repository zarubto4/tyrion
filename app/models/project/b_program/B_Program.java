package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;
import models.project.global.Project;
import models.project.m_program.M_Project;
import play.libs.Json;

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

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1458315085338") public Date lastUpdate;
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1458315085338") public Date dateOfCreate;
                                    @JsonIgnore @ManyToOne   public Project project;
                                                @JsonIgnore  public String azurePackageLink;
                                                @JsonIgnore  public String azureStorageLink;

    @JsonIgnore   @OneToOne(mappedBy="b_program",cascade=CascadeType.ALL) public M_Project m_program; // TODO asi časem předělat na MayToMany!

    @OneToMany(mappedBy="b_program", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC")     public List<Version_Object> versionObjects = new ArrayList<>();
                                                                                 @JsonProperty      public String   project_id() {  return project.id; }


/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty public JsonNode program_state(){

        ObjectNode result = Json.newObject();

        Version_Object version_object = where_program_run();


        if(version_object == null){
            result.put("version", "null" );
            return  result;
        }

        result.put("version_id", version_object.id);

        if( version_object.b_program_cloud != null ) {
            result.put("where", "cloud");
            result.put("b_program_cloud_id", version_object.b_program_cloud.id );
        }
        else {
            result.put("where", "homer");
            result.put("b_program_homer_id", version_object.b_program_homer.id );
            result.put("id", version_object.b_program_homer.homer.id);
            result.put("homer_online", version_object.b_program_homer.homer.online() );
        }

        return result;
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public void setUniqueAzureStorageLink() {
        while(true){ // I need Unique Value
            this.azureStorageLink = UUID.randomUUID().toString();
            if (B_Program.find.where().eq("azureStorageLink", azureStorageLink ).findUnique() == null) break;
        }
    }

    @JsonIgnore  public Version_Object where_program_run(){
        Version_Object version_object = Version_Object.find.where().eq("b_program.id", id).where().or(
                com.avaje.ebean.Expr.isNotNull("b_program_cloud"),
                com.avaje.ebean.Expr.isNotNull("b_program_homer")
        ).findUnique();

        return  version_object;
    }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   public Boolean create_permission()  {  return  ( Project.find.where().where().eq("ownersOfProject.id", SecurityController.getPerson().id ).eq("id", project.id ).findUnique().create_permission() ) || SecurityController.getPerson().has_permission("B_Program_create");  }
    @JsonProperty public Boolean update_permission()  {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_update");  }
    @JsonIgnore   public Boolean read_permission()    {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_read");   }
    @JsonProperty public Boolean edit_permission()    {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_edit");    }
    @JsonProperty public Boolean delete_permission()  {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_delete");  }

    public enum permissions{ B_Program_create, B_Program_update, B_Program_read, B_Program_edit , B_Program_delete}

    /* FINDER --------------------------------------------------------------------------------------------------------------*/
     public static Finder<String,B_Program> find = new Finder<>(B_Program.class);
}

