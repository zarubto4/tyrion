package models.project.c_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
                                    @JsonIgnore @ManyToOne      public Project project;
                                              @JsonProperty     public String  project_id(){ return project.id; }

                                               @JsonIgnore      public String azurePackageLink;
                                               @JsonIgnore      public String azureStorageLink;


    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1458315085338") public Date dateOfCreate;
    @OneToMany(mappedBy="c_program", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC")                                     public List<Version_Object> version_objects = new ArrayList<>();





/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void setUniqueAzureStorageLink() {
        while(true){ // I need Unique Value
            this.azureStorageLink = UUID.randomUUID().toString();
            if (C_Program.find.where().eq("azureStorageLink", azureStorageLink ).findUnique() == null) break;
        }
    }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty public Boolean read_permission()  {  return ( C_Program.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("C_Program.read"); }
    @JsonProperty public Boolean edit_permission()  {  return ( C_Program.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("C_Program.edit"); }
    @JsonProperty public Boolean delete_permission(){  return ( C_Program.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("C_Program.delete"); }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,C_Program> find = new Finder<>(C_Program.class);
}
