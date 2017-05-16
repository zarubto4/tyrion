package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import utilities.swagger.outboundClass.Swagger_Library_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Library_Version_Short_Detail;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Library", description = "Model of Library")
public class Model_Library extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Library.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @ApiModelProperty(required = true) public String id;
    @ApiModelProperty(required = true)     public String name;
    @ApiModelProperty(required = true)     public String description;
    @Column(columnDefinition = "TEXT")
    @ApiModelProperty(required = true)     public String markdown_description;

                           @JsonIgnore     public boolean removed;

    @JsonIgnore @OneToMany(mappedBy = "library", cascade = CascadeType.ALL) @OrderBy("date_of_create DESC") public List<Model_VersionObject> versions        = new ArrayList<>();

    @ManyToMany public List<Model_TypeOfBoard>  type_of_boards  = new ArrayList<>();

    @JsonIgnore public String project_id; // Jednodušší vazba na Project bez příme ORM vazby


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty
    public List<Swagger_Library_Version_Short_Detail> versions(){

        List<Swagger_Library_Version_Short_Detail> versions = new ArrayList<>();
        for (Model_VersionObject version : this.versions){
            versions.add(version.get_short_import_library_version());
        }

        return versions;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Swagger_Library_Short_Detail get_short_import_library(){
        Swagger_Library_Short_Detail help = new Swagger_Library_Short_Detail();

        help.id = this.id;
        help.name = this.name;
        help.description = this.description;

        help.last_version = this.last_version();

        return help;
    }

    @JsonIgnore
    public Swagger_Library_Version_Short_Detail last_version(){

        if (this.versions.isEmpty()) return null;

        return this.versions.get(0).get_short_import_library_version();
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            this.azure_library_link = "libraries/"  + this.id;
            if (Model_Library.find.byId(this.id) == null) break;
        }
        super.save();
    }

    @Override
    public void update(){

        terminal_logger.debug("update :: Update object Id: " + this.id);

        //Cache Update
        //cache.put(this.id, this);

        if(project_id != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Library.class, project_id, this.id))).start();

        //Database Update
        super.update();
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("remove :: Update (hide) object Id: " + this.id);

        removed = true;

        //Database Update
        super.update();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore     private String azure_library_link;
    @JsonIgnore     public String get_path(){
        return azure_library_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  if(project_id != null) return Model_Project.get_byId(project_id).update_permission(); return Controller_Security.get_person().has_permission("Library_create");}
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  if(project_id != null) return Model_Project.get_byId(project_id).update_permission(); return Controller_Security.get_person().has_permission("Library_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  if(project_id != null) return Model_Project.get_byId(project_id).update_permission(); return Controller_Security.get_person().has_permission("Library_delete"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission(){  if(project_id != null) return Model_Project.get_byId(project_id).update_permission(); return Controller_Security.get_person().has_permission("Library_update"); }

    public enum permissions{Library_create, Library_edit, Library_delete, Library_update}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_Library> find = new Model.Finder<>(Model_Library.class);

}
