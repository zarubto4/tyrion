package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.compiler.Version_Object;
import models.project.global.Project;
import play.libs.Json;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Entity
public class B_Program extends Model {


    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String programId;
                                                             public String programName;
                        @Column(columnDefinition = "TEXT")   public String programDescription;
                                                             public Date lastUpdate;
                                                             public Date dateOfCreate;
                                    @JsonIgnore @ManyToOne   public Project project;
                                                @JsonIgnore  public String azurePackageLink;
                                                @JsonIgnore  public String azureStorageLink;

    @JsonIgnore @OneToOne   @JoinColumn(name="bcloud_id")    public B_Program_Cloud b_program_cloud;

    @OneToMany(mappedBy="b_program", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC") public List<Version_Object> versionObjects = new ArrayList<>();

    @JsonProperty public JsonNode program_state()               {

        ObjectNode result = Json.newObject();

        List<ObjectNode>  homer_list = new ArrayList<>();

        // Každý program může mít N verzí a každá verze může být nahrána na m Homerech
        // Ale každý homer mám jen jeden program - tedy jednu verzi programu!
        if(!versionObjects.isEmpty()){
            for(Version_Object versionObject : versionObjects) {
                for(B_Program_Homer b_program_homer : versionObject.b_program_homers){

                    // Uložím do jména ID homera
                    ObjectNode json = Json.newObject();
                    json.put("homer",   b_program_homer.homer.homerId );
                    json.put("version", versionObject.azureLinkVersion );
                    json.put("state",   b_program_homer.state());
                    homer_list.add(json);
                }
            }

            result.set("in_homer", Json.toJson(homer_list));

        } else  result.put("in_homer", "null" );


        if(b_program_cloud != null){
            ObjectNode json = Json.newObject();

            json.put("version", b_program_cloud.version_object.id);
            json.put("state", b_program_cloud.state());

            result.set("in_cloud", json);

        }
        else                        result.put("in_cloud", "not uploaded to cloud");




        return result;
    }
    @JsonProperty public String project()                       {  return "http://localhost:9000/project/project/" + this.project.projectId; }


    public static Finder<String,B_Program> find = new Finder<>(B_Program.class);

    public void setUniqueAzureStorageLink() {
        while(true){ // I need Unique Value
            this.azureStorageLink = UUID.randomUUID().toString();
            if (B_Program.find.where().eq("azureStorageLink", azureStorageLink ).findUnique() == null) break;
        }
    }
}

