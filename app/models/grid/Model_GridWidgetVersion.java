package models.grid;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Approval_state;

import javax.persistence.*;
import java.util.Date;
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
    @ApiModelProperty(required = true,
            dataType = "integer", readOnly = true,
            value = "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970",
            example = "1466163478925")                                                            public Date date_of_create;

    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)    public String design_json;
    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)    public String logic_json;
    @JsonIgnore @ManyToOne                                                   public Model_GridWidget grid_widget;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_GridWidgetVersion.find.byId(this.id) == null) break;
        }
        super.save();
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
    public static Model.Finder<String,Model_GridWidgetVersion> find = new Finder<>(Model_GridWidgetVersion.class);

}
