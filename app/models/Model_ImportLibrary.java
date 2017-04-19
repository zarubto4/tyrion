package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Library_state;
import utilities.enums.Enum_Library_tag;
import utilities.swagger.outboundClass.Swagger_ImportLibrary_Short_Detail;
import utilities.swagger.outboundClass.Swagger_ImportLibrary_Version_Short_Detail;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of ImportLibrary",
        value = "ImportLibrary")
public class Model_ImportLibrary extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @ApiModelProperty(required = true) public String id;
    @ApiModelProperty(required = true)     public String name;
    @ApiModelProperty(required = true)     public String description;
    @Column(columnDefinition = "TEXT")
    @ApiModelProperty(required = true)     public String long_description;

    @ApiModelProperty(required = true)     public Enum_Library_state state;

    @ApiModelProperty(required = true)     public boolean removed;

                              @JsonIgnore private String azure_library_link;
    @Enumerated(EnumType.STRING)
    @ApiModelProperty(required = true)     public Enum_Library_tag tag; // K čemu knihovna slouží (matematická, audio, atd...)

    @JsonIgnore @OneToMany(mappedBy = "library", cascade = CascadeType.ALL) @OrderBy("date_of_create DESC") public List<Model_VersionObject> versions        = new ArrayList<>();

                                                                                                 @ManyToMany public List<Model_TypeOfBoard>  type_of_boards  = new ArrayList<>();


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty
    public List<Swagger_ImportLibrary_Version_Short_Detail> versions(){

        List<Swagger_ImportLibrary_Version_Short_Detail> versions = new ArrayList<>();
        for (Model_VersionObject version : this.versions){
            versions.add(version.get_short_import_library_version());
        }

        return versions;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            String id = UUID.randomUUID().toString();
            this.id = id;
            this.azure_library_link = "libraries/"  + id;
            if (Model_ImportLibrary.find.byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore public Swagger_ImportLibrary_Short_Detail get_short_import_library(){
        Swagger_ImportLibrary_Short_Detail help = new Swagger_ImportLibrary_Short_Detail();

        help.id = this.id;
        help.name = this.name;
        help.description = this.description;

        help.last_version = this.last_version();
        help.tag = this.tag;

        return help;
    }

    @JsonIgnore
    public Swagger_ImportLibrary_Version_Short_Detail last_version(){

        if (this.versions.isEmpty()) return null;

        return this.versions.get(0).get_short_import_library_version();
    }

    @JsonIgnore
    public String get_path(){
        return  azure_library_link;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return Controller_Security.get_person().has_permission("ImportLibrary_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return Controller_Security.get_person().has_permission("ImportLibrary_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return Controller_Security.get_person().has_permission("ImportLibrary_delete"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission(){  return Controller_Security.get_person().has_permission("ImportLibrary_update"); }

    public enum permissions{ImportLibrary_create, ImportLibrary_edit, ImportLibrary_delete, ImportLibrary_update}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_ImportLibrary> find = new Model.Finder<>(Model_ImportLibrary.class);

}
