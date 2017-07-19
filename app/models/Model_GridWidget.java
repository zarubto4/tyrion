package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.helps_objects.TyrionCachedList;
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

                                                @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)   public Model_Person author;
                                                @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)   public Model_TypeOfWidget type_of_widget;
                                                @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)   public Model_Producer producer;

    @JsonIgnore @OneToMany(mappedBy="grid_widget", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  @OrderBy("date_of_create desc")  public List<Model_GridWidgetVersion> grid_widget_versions = new ArrayList<>();

    @JsonIgnore  public Integer order_position;
    @JsonIgnore  public boolean removed_by_user;


 /* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList private String cache_value_type_of_widget_id;
    @JsonIgnore @Transient @TyrionCachedList private List<String> cache_value_grid_versions_id = new ArrayList<>();
    @JsonIgnore @Transient @TyrionCachedList private String cache_value_author_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_value_producer_id;


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


    @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty  public String    author_id()         { return cache_value_author_id != null ? cache_value_author_id : get_author().id;}

    @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty  public String    author_nick_name()  { return get_author().nick_name; }



    @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by User not by Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty  public String    producer_id()       { return cache_value_producer_id != null ? cache_value_producer_id : get_producer().id;}

    @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by User not by Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty  public String    producer_name()     { return get_producer().name;}



    @Transient  @JsonProperty @ApiModelProperty(required = true, readOnly = true)  public String  type_of_widget_id()             { return cache_value_type_of_widget_id != null ? cache_value_type_of_widget_id : get_type_of_widget().id; }
    @Transient  @JsonProperty @ApiModelProperty(required = true, readOnly = true)  public String  type_of_widget_name()           { return get_type_of_widget().name; }

    @Transient  @JsonProperty @ApiModelProperty(required = true) public  List<Swagger_GridWidgetVersion_Short_Detail> versions(){

        List<Swagger_GridWidgetVersion_Short_Detail> list = new ArrayList<>();

        for( Model_GridWidgetVersion v : get_grid_widget_versions()){

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
        help.versions    = versions();
        help.edit_permission = edit_permission();
        help.delete_permission = delete_permission();
        help.update_permission = update_permission();
        return help;
    }

    @Transient @JsonIgnore @TyrionCachedList
    public Model_TypeOfWidget get_type_of_widget() {
        if(cache_value_type_of_widget_id == null){
            Model_TypeOfWidget type_of_block = Model_TypeOfWidget.find.where().eq("grid_widgets.id", id).select("id").findUnique();
            cache_value_type_of_widget_id = type_of_block.id;
        }

        return Model_TypeOfWidget.get_byId(cache_value_type_of_widget_id);
    }


    @Transient @JsonIgnore @TyrionCachedList
    public List<Model_GridWidgetVersion> get_grid_widget_versions(){
        try{

            if(cache_value_grid_versions_id.isEmpty()){

                List<Model_GridWidgetVersion> grid_versions =   Model_GridWidgetVersion.find.where().eq("grid_widget.id", id).eq("removed_by_user", false).order().desc("date_of_create").select("id").findList();

                // Získání seznamu
                for (Model_GridWidgetVersion grid_version : grid_versions) {
                    cache_value_grid_versions_id.add(grid_version.id);
                }

            }

            List<Model_GridWidgetVersion> grid_versions  = new ArrayList<>();

            for(String version_id : cache_value_grid_versions_id){
                grid_versions.add(Model_GridWidgetVersion.get_byId(version_id));
            }

            return grid_versions;

        }catch (Exception e){
            terminal_logger.internalServerError("getVersion_objects", e);
            return new ArrayList<Model_GridWidgetVersion>();
        }

    }

    @JsonIgnore @TyrionCachedList
    public Model_Person get_author(){

        if(cache_value_author_id == null){
            Model_Person person = Model_Person.find.where().eq("blocksAuthor.id", id).select("id").findUnique();
            cache_value_author_id = person.id;
        }

        return Model_Person.get_byId(cache_value_author_id);
    }

    @JsonIgnore @TyrionCachedList
    public Model_Producer get_producer(){

        if(cache_value_producer_id == null){
            Model_Producer producer = Model_Producer.find.where().eq("blocko_blocks.id", id).select("id").findUnique();
            cache_value_producer_id = producer.id;
        }

        return Model_Producer.get_byId(cache_value_producer_id);
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        order_position = Model_GridWidget.find.where().eq("type_of_widget.id", type_of_widget_id()).findRowCount() + 1;

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

        Model_GridWidget up = Model_GridWidget.find.where().eq("order_position", (order_position-1) ).eq("type_of_widget.id", type_of_widget_id()).findUnique();
        if(up == null)return;

        up.order_position += 1;
        up.update();

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down(){

        Model_GridWidget down = Model_GridWidget.find.where().eq("order_position", (order_position+1) ).eq("type_of_widget.id", type_of_widget_id()).findUnique();
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
    @JsonIgnore  @Transient                                     public boolean read_permission()   {return  get_type_of_widget().read_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()   {return  get_type_of_widget().update_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission() {return  get_type_of_widget().update_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission() {return  get_type_of_widget().delete_permission();}


    public enum permissions{GridWidget_create, GridWidget_read, GridWidget_edit, GridWidget_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_GridWidget> find = new Finder<>(Model_GridWidget.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_GridWidget.class.getSimpleName();
    public static Cache<String, Model_GridWidget> cache = null;               // < ID, Model_GridWidget>

    @JsonIgnore
    public static Model_GridWidget get_byId(String id) {

        Model_GridWidget grid_widget = cache.get(id);
        if (grid_widget == null){

            grid_widget = Model_GridWidget.find.byId(id);
            if (grid_widget == null) return null;

            cache.put(id, grid_widget);
        }

        return grid_widget;
    }

    @JsonIgnore
    public static Model_GridWidget get_publicByName(String name) {
        return find.where().isNull("type_of_widget.project").eq("name", name).findUnique();
    }
}