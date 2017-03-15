package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Approval_state;
import utilities.swagger.outboundClass.Swagger_GridWidgetVersion_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Person_Short_Detail;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of GridWidgetVersion",
        value = "GridWidgetVersion")
public class Model_GridWidgetVersion extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true)    public String id;
                                                            @ApiModelProperty(required = true)    public String version_name;
                                                            @ApiModelProperty(required = true)    public String version_description;
                                                            @ApiModelProperty(required = true)    public Approval_state approval_state;

                                                                        @JsonIgnore @ManyToOne    public Model_Person author;
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true,
            value = "UNIX time in ms", example = "1466163478925")                             public Date date_of_create;

    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)    public String design_json;
    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)    public String logic_json;
    @JsonIgnore @ManyToOne                                                   public Model_GridWidget grid_widget;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty
    public Swagger_Person_Short_Detail author(){
        return this.author.get_short_person();
    }
    
/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_GridWidgetVersion.find.byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore
    public Swagger_GridWidgetVersion_Short_Detail get_short_gridwidget_version(){

        Swagger_GridWidgetVersion_Short_Detail help = new Swagger_GridWidgetVersion_Short_Detail();
        help.id = this.id;
        help.name = this.version_name;
        help.description = this.version_description;
        help.date_of_create = this.date_of_create;
        help.design_json = this.design_json;
        help.author = this.author.get_short_person();

        return help;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user can read GridWidget, than can read all Versions from list of GridWidgets ( You get ids of list of version in object \"GridWidgets\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have GridWidget.update_permission = true, you can create new version of GridWidgets on this GridWidget - Or you need static/dynamic permission key if user want create version of GridWidget in public GridWidget in public TypeOfWidget";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean create_permission()  {  return  grid_widget.update_permission() ||  Controller_Security.getPerson().has_permission("GridWidgetVersion_create"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean read_permission()    {  return  grid_widget.read_permission()   ||  Controller_Security.getPerson().has_permission("GridWidgetVersion_read");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()    {  return  grid_widget.update_permission() ||  Controller_Security.getPerson().has_permission("GridWidgetVersion_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission()  {  return  grid_widget.update_permission() ||  Controller_Security.getPerson().has_permission("GridWidgetVersion_delete"); }

    public enum permissions{GridWidgetVersion_create, GridWidgetVersion_read, GridWidgetVersion_edit, GridWidgetVersion_delete}

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    private static Model.Finder<String,Model_GridWidgetVersion> find = new Finder<>(Model_GridWidgetVersion.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_GridWidgetVersion get_byId(String id) {
        return find.byId(id);
    }

    @JsonIgnore
    public static Model_GridWidgetVersion get_scheme() {
        return find.where().eq("version_name", "version_scheme").findUnique();
    }

    @JsonIgnore
    public static List<Model_GridWidgetVersion> get_pending() {
        return find.where().eq("approval_state",Approval_state.pending).findList();
    }
}
