package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.enums.Enum_Approval_state;
import utilities.logger.Class_Logger;
import utilities.swagger.outboundClass.Swagger_GridWidgetVersion_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Person_Short_Detail;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "GridWidgetVersion", description = "Model of GridWidgetVersion")
public class Model_GridWidgetVersion extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_GridWidgetVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true)    public String id;
                                                            @ApiModelProperty(required = true)    public String version_name;
                                                            @ApiModelProperty(required = true)    public String version_description;
                                                            @ApiModelProperty(required = true)    public Enum_Approval_state approval_state;

                                                                        @JsonIgnore @ManyToOne    public Model_Person author;
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true,
            value = "UNIX time in ms", example = "1466163478925")                             public Date date_of_create;

    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)    public String design_json;
    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)    public String logic_json;
    @JsonIgnore @ManyToOne                                                   public Model_GridWidget grid_widget;

    @JsonIgnore  public boolean removed_by_user;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty
    public Swagger_Person_Short_Detail author(){
        return this.author.get_short_person();
    }
    
/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Swagger_GridWidgetVersion_Short_Detail get_short_gridwidget_version(){

        Swagger_GridWidgetVersion_Short_Detail help = new Swagger_GridWidgetVersion_Short_Detail();
        help.id = this.id;
        help.name = this.version_name;
        help.description = this.version_description;
        help.date_of_create = this.date_of_create;
        help.design_json = this.design_json;
        help.delete_permission = this.delete_permission();
        help.edit_permission = this.edit_permission();
        help.author = this.author.get_short_person();

        return help;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_GridWidgetVersion.get_byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);
        super.update();
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("delete :: Delete object Id: {}",  this.id);

        this.removed_by_user =true;
        super.update();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user can read GridWidget, than can read all Versions from list of GridWidgets ( You get ids of list of version in object \"GridWidgets\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have GridWidget.update_permission = true, you can create new version of GridWidgets on this GridWidget - Or you need static/dynamic permission key if user want create version of GridWidget in public GridWidget in public TypeOfWidget";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean create_permission()  {  return  grid_widget.update_permission() ||  Controller_Security.get_person().permissions_keys.containsKey("GridWidgetVersion_create"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean read_permission()    {  return  grid_widget.read_permission()   ||  Controller_Security.get_person().permissions_keys.containsKey("GridWidgetVersion_read");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()    {  return  grid_widget.update_permission() ||  Controller_Security.get_person().permissions_keys.containsKey("GridWidgetVersion_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission()  {  return  grid_widget.update_permission() ||  Controller_Security.get_person().permissions_keys.containsKey("GridWidgetVersion_delete"); }

    public enum permissions{GridWidgetVersion_create, GridWidgetVersion_read, GridWidgetVersion_edit, GridWidgetVersion_delete}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_GridWidgetVersion.class.getSimpleName();
    public static Cache<String, Model_GridWidgetVersion> cache = null;               // < ID, Model_GridWidgetVersion>

    @JsonIgnore
    public static Model_GridWidgetVersion get_byId(String id) {

        Model_GridWidgetVersion grid_widget_version = cache.get(id);
        if (grid_widget_version == null){

            grid_widget_version = Model_GridWidgetVersion.find.byId(id);
            if (grid_widget_version == null){
                terminal_logger.warn("get_byId :: This object id:: " + id + " wasn't found.");
            }

            cache.put(id, grid_widget_version);
        }

        return grid_widget_version;
    }

    @JsonIgnore
    public static Model_GridWidgetVersion get_scheme() {
        return find.where().eq("version_name", "version_scheme").findUnique();
    }

    @JsonIgnore
    public static List<Model_GridWidgetVersion> get_pending() {
        return find.where().eq("approval_state", Enum_Approval_state.pending).findList();
    }

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_GridWidgetVersion> find = new Finder<>(Model_GridWidgetVersion.class);


}
