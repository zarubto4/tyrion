package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.logger.Class_Logger;
import utilities.swagger.outboundClass.Swagger_C_Program_Version_Short_Detail;
import utilities.swagger.outboundClass.Swagger_GridWidget_Short_Detail;
import utilities.swagger.outboundClass.Swagger_TypeOfWidget_Short_Detail;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(  value = "TypeOfWidget", description = "Model of TypeOfWidget")
public class Model_TypeOfWidget extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_TypeOfWidget.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true)  public String id;
                                                            @ApiModelProperty(required = true)  public String name;
                    @Column(columnDefinition = "TEXT")      @ApiModelProperty(required = true)  public String description;

                                                @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_Project project;
                                                                                   @JsonIgnore  public Integer order_position;

    @JsonIgnore @OneToMany(mappedBy="type_of_widget", cascade = CascadeType.ALL, fetch = FetchType.LAZY) @ApiModelProperty(required = true) public List<Model_GridWidget> grid_widgets = new ArrayList<>();


    @JsonIgnore              public boolean removed_by_user;


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @ApiModelProperty(value = "This value will be in Json only if TypeOfWidget is private!", readOnly = true, required = false)
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @Transient public String project_id() {  return project == null ? null : this.project.id; }


    @JsonProperty @Transient public List<Swagger_GridWidget_Short_Detail> widgets() {

        try {

            List<Swagger_GridWidget_Short_Detail> short_detail_widgets = new ArrayList<>();

            for (Model_GridWidget widget :  Model_GridWidget.find.where().eq("type_of_widget.id", id).eq("removed_by_user", false).order().asc("order_position").findList()) {
                short_detail_widgets.add( widget.get_grid_widget_short_detail() ) ;
            }

            return short_detail_widgets;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }



/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_TypeOfWidget_Short_Detail get_typeOfWidget_short_detail(){
        Swagger_TypeOfWidget_Short_Detail help = new Swagger_TypeOfWidget_Short_Detail();
        help.id = id;
        help.name = name;
        help.description = description;
        help.grid_widgets = widgets();
        help.edit_permission = edit_permission();
        help.delete_permission = delete_permission();
        help.update_permission = update_permission();
        return help;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");
        removed_by_user  = false;

        if(project == null){
            order_position = Model_TypeOfWidget.find.where().isNull("project").findRowCount() + 1;
        }else {
            order_position = Model_TypeOfWidget.find.where().eq("project.id", project.id).findRowCount() + 1;
        }

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_TypeOfWidget.find.byId(this.id) == null) break;
        }

        super.save();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object value: {}",  this.id);
        super.update();

    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object Id: {} ", this.id);

        removed_by_user = true;
        super.update();

        for(Model_GridWidget gridWidget : grid_widgets){
            gridWidget.delete();
        }
    }


/* ORDER  -------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void up(){

        Model_TypeOfWidget up = Model_TypeOfWidget.find.where().eq("order_position", (order_position-1) ).isNull("project").findUnique();
        if(up == null)return;

        up.order_position += 1;
        up.update();

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down(){

        Model_TypeOfWidget down = Model_TypeOfWidget.find.where().eq("order_position", (order_position+1) ).isNull("project").findUnique();
        if(down == null)return;

        down.order_position -= 1;
        down.update();

        this.order_position += 1;
        this.update();

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    /* GET Variable short type of objects ----------------------------------------------------------------------------------*/



/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.read_permission = true, you can read TypeOfWidget on this Project ( You get ids of list of TypeOfWidget in object \"project\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create TypeOfWidget on this Project - Or you need static/dynamic permission key if user want create public TypeOfWidget";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient                                      public boolean create_permission()  {return                      (project != null && project.update_permission()) || Controller_Security.get_person().has_permission("TypeOfWidget_create");}
    @JsonIgnore @Transient                                      public boolean read_permission()    {return (project == null) || (project != null && project.read_permission())   || Controller_Security.get_person().has_permission("TypeOfWidget_read");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission()  {return                      (project != null && project.update_permission()) || Controller_Security.get_person().has_permission("TypeOfWidget_update");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()    {return                      (project != null && project.edit_permission())   || Controller_Security.get_person().has_permission("TypeOfWidget_edit");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission()  {return                      (project != null && project.update_permission()) || Controller_Security.get_person().has_permission("TypeOfWidget_delete");}

    public enum permissions{TypeOfWidget_create, TypeOfWidget_read, TypeOfWidget_edit , TypeOfWidget_delete, TypeOfWidget_update}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_TypeOfWidget> find = new Finder<>(Model_TypeOfWidget.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_TypeOfWidget get_byId(String id) {
        return find.byId(id);
    }

    @JsonIgnore
    public static List<Model_TypeOfWidget> get_all() {

        List<Model_TypeOfWidget> typeOfWidgets = find.where().isNull("project").findList();
        typeOfWidgets.addAll( find.where().eq("project.participants.person.id", Controller_Security.get_person().id ).findList() );

        return typeOfWidgets;
    }

    @JsonIgnore
    public static Model_TypeOfWidget get_publicByName(String name) {
        return find.where().isNull("project").eq("name",name).findUnique();
    }

    @JsonIgnore
    public static List<Model_TypeOfWidget> get_public() {
        return find.where().isNull("project").order().asc("order_position").findList();
    }
}
