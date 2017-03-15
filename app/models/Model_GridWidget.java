package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Approval_state;
import utilities.swagger.outboundClass.Swagger_GridWidgetVersion_Short_Detail;
import utilities.swagger.outboundClass.Swagger_GridWidget_Short_Detail;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of GridWidget",
        value = "GridWidget")
public class Model_GridWidget extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true)   public String id;
                                                            @ApiModelProperty(required = true)   public String name;
    @Column(columnDefinition = "TEXT")                      @ApiModelProperty(required = true)   public String description;

                                                                        @JsonIgnore @ManyToOne   public Model_Person author;
                                                                        @JsonIgnore @ManyToOne   public Model_TypeOfWidget type_of_widget;
                                                                        @JsonIgnore @ManyToOne   public Model_Producer producer;

    @JsonIgnore @OneToMany(mappedBy="grid_widget", cascade = CascadeType.ALL) @OrderBy("date_of_create desc") public List<Model_GridWidgetVersion> grid_widget_versions = new ArrayList<>();


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = false, readOnly = true, value = "can be hidden, if GridWidget is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty
    public String    author_id()         { return author != null ? author.id : null;}

    @ApiModelProperty(required = false, readOnly = true, value = "can be hidden, if GridWidget is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty public String    author_nick_name()  { return  author != null ? author.nick_name : null;}


    @Transient  @JsonProperty @ApiModelProperty(required = true, readOnly = true)  public String  type_of_widget_id()             { return type_of_widget.id; }
    @Transient  @JsonProperty @ApiModelProperty(required = true, readOnly = true)  public String  type_of_widget_name()           { return type_of_widget.name; }

    @Transient  @JsonProperty @ApiModelProperty(required = true) public  List<Swagger_GridWidgetVersion_Short_Detail> versions(){

        List<Swagger_GridWidgetVersion_Short_Detail> list = new ArrayList<>();

        for( Model_GridWidgetVersion v : grid_widget_versions){

            if((v.approval_state == Approval_state.approved)||(v.approval_state == Approval_state.edited)||((this.author != null)&&(this.author.id.equals(Controller_Security.getPerson().id)))) {

                list.add(v.get_short_gridwidget_version());
            }
        }

        return list;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_GridWidget_Short_Detail get_grid_widget_short_detail(){
        Swagger_GridWidget_Short_Detail help = new Swagger_GridWidget_Short_Detail();
        help.id = id;
        help.name = name;
        help.description = description;

        help.edit_permission = edit_permission();
        help.delete_permission = delete_permission();
        help.update_permission = update_permission();
        return help;
    }

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (get_byId(this.id) == null) break;
        }
        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user can read TypeOfWidget, than can read all GridWidgets from list of TypeOfWidget ( You get ids of list of GridWidgets in object \"GridWidgets\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have TypeOfWidget.update_permission = true, you can create new GridWidgets on this TypeOfWidget - Or you need static/dynamic permission key if user want create GridWidget in public TypeOfWidget";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore  @Transient                                     public boolean create_permission() {return  type_of_widget.update_permission();}
    @JsonIgnore  @Transient                                     public boolean read_permission()   {return  type_of_widget.read_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()   {return  type_of_widget.update_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission() {return  type_of_widget.update_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission() {return  type_of_widget.delete_permission();}


    public enum permissions{GridWidget_create, GridWidget_read, GridWidget_edit, GridWidget_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    private static Model.Finder<String,Model_GridWidget> find = new Finder<>(Model_GridWidget.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_GridWidget get_byId(String id) {
        return find.byId(id);
    }

    @JsonIgnore
    public static Model_GridWidget get_publicByName(String name) {
        return find.where().isNull("type_of_widget.project").eq("name", name).findUnique();
    }
}
