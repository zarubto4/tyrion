package models.grid;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Project;
import utilities.swagger.outboundClass.Swagger_TypeOfWidget_Short_Detail;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TypeOfWidget extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true) public String id;
                                                            @ApiModelProperty(required = true) public String name;
                    @Column(columnDefinition = "TEXT")      @ApiModelProperty(required = true) public String description;


    @JsonIgnore @ManyToOne  public Project project;

    @OneToMany(mappedBy="type_of_widget", cascade = CascadeType.ALL) @ApiModelProperty(required = true)  public List<GridWidget> grid_widgets = new ArrayList<>();


    @ApiModelProperty(value = "This value will be in Json only if TypeOfWidget is private!", readOnly = true, required = false)
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @Transient public String project_id() {  return project == null ? null : this.project.id; }

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    /* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_TypeOfWidget_Short_Detail get_typeOfWidget_short_detail(){
        Swagger_TypeOfWidget_Short_Detail help = new Swagger_TypeOfWidget_Short_Detail();
        help.id = id;
        help.name = name;
        help.description = description;

        help.edit_permission = edit_permission();
        help.delete_permission = delete_permission();
        help.update_permission = update_permission();
        return help;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.read_permission = true, you can read TypeOfWidget on this Project ( You get ids of list of TypeOfWidget in object \"project\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create TypeOfWidget on this Project - Or you need static/dynamic permission key if user want create public TypeOfWidget";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient                                      public boolean create_permission()  {return                      (project != null && project.update_permission()) || SecurityController.getPerson().has_permission("TypeOfWidget_create");}
    @JsonIgnore @Transient                                      public boolean read_permission()    {return (project == null) || (project != null && project.read_permission())   || SecurityController.getPerson().has_permission("TypeOfWidget_read");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission()  {return                      (project != null && project.update_permission()) || SecurityController.getPerson().has_permission("TypeOfWidget_update");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()    {return                      (project != null && project.edit_permission())   || SecurityController.getPerson().has_permission("TypeOfWidget_edit");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission()  {return                      (project != null && project.update_permission()) || SecurityController.getPerson().has_permission("TypeOfWidget_delete");}

    public enum permissions{TypeOfWidget_create, TypeOfWidget_read, TypeOfWidget_edit , TypeOfWidget_delete, TypeOfWidget_update}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,TypeOfWidget> find = new Finder<>(TypeOfWidget.class);
}
