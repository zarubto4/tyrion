package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Approval_state;
import utilities.logger.Class_Logger;
import utilities.swagger.outboundClass.Swagger_GridWidgetVersion_Short_Detail;
import utilities.swagger.outboundClass.Swagger_GridWidget_Short_Detail;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "GridWidget", description = "Model of GridWidget")
public class Model_GridWidget extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_GridWidget.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true)   public String id;
                                                            @ApiModelProperty(required = true)   public String name;
    @Column(columnDefinition = "TEXT")                      @ApiModelProperty(required = true)   public String description;

                                                                        @JsonIgnore @ManyToOne   public Model_Person author;
                                                                        @JsonIgnore @ManyToOne   public Model_TypeOfWidget type_of_widget;
                                                                        @JsonIgnore @ManyToOne   public Model_Producer producer;

    @JsonIgnore @OneToMany(mappedBy="grid_widget", cascade = CascadeType.ALL) @OrderBy("date_of_create desc") public List<Model_GridWidgetVersion> grid_widget_versions = new ArrayList<>();

    @JsonIgnore  public Integer order_position;

    @JsonIgnore  public boolean removed_by_user;


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

            if((v.approval_state == Enum_Approval_state.approved)||(v.approval_state == Enum_Approval_state.edited)||((this.author != null)&&(this.author.id.equals(Controller_Security.get_person().id)))) {

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

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        order_position = Model_GridWidget.find.where().eq("type_of_widget.id", type_of_widget.id).findRowCount() + 1;

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (get_byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);
        super.update();

    }


    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("delete :: Delete object Id: {}",  this.id);

        this.removed_by_user = true;
        super.update();
    }

/* ORDER  -------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void up(){

        Model_GridWidget up = Model_GridWidget.find.where().eq("order_position", (order_position-1) ).eq("type_of_widget.id", type_of_widget.id).findUnique();
        if(up == null)return;

        up.order_position += 1;
        up.update();

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down(){

        Model_GridWidget down = Model_GridWidget.find.where().eq("order_position", (order_position+1) ).eq("type_of_widget.id", type_of_widget.id).findUnique();
        if(down == null)return;

        down.order_position -= 1;
        down.update();

        this.order_position += 1;
        this.update();

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
