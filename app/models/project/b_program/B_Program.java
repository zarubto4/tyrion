package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.compiler.Version_Object;
import models.project.global.Project;
import play.libs.Json;
import utilities.Server;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Entity
public class B_Program extends Model {


    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String b_program_id;
                                                             public String name;
                        @Column(columnDefinition = "TEXT")   public String program_description;
                                                             public Date lastUpdate;
                                                             public Date dateOfCreate;
                                    @JsonIgnore @ManyToOne   public Project project;
                                                @JsonIgnore  public String azurePackageLink;
                                                @JsonIgnore  public String azureStorageLink;

    @OneToMany(mappedBy="b_program", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC") public List<Version_Object> versionObjects = new ArrayList<>();

    @JsonProperty public JsonNode program_state()               {

        ObjectNode result = Json.newObject();

            Version_Object version_object = Version_Object.find.where().eq("b_program.b_program_id", b_program_id).where().or(
                    com.avaje.ebean.Expr.isNotNull("b_program_cloud"),
                    com.avaje.ebean.Expr.isNotNull("b_program_homer")
            ).findUnique();


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
            result.put("homer_id", version_object.b_program_homer.homer.homer_id );
            result.put("homer_online", version_object.b_program_homer.homer.online() );
        }

        return result;
    }


    @JsonProperty public String   project()                     {  return Server.tyrion_serverAddress + "/project/project/" + this.project.id; }


    public static Finder<String,B_Program> find = new Finder<>(B_Program.class);

    public void setUniqueAzureStorageLink() {
        while(true){ // I need Unique Value
            this.azureStorageLink = UUID.randomUUID().toString();
            if (B_Program.find.where().eq("azureStorageLink", azureStorageLink ).findUnique() == null) break;
        }
    }
}

