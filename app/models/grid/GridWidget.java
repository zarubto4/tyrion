package models.grid;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.person.Person;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class GridWidget extends Model{

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true)   public String id;
                                                            @ApiModelProperty(required = true)   public String name;
    @Column(columnDefinition = "TEXT")                      @ApiModelProperty(required = true)   public String general_description;

                                                                        @JsonIgnore @ManyToOne   public Person author;
                                                                        @JsonIgnore @ManyToOne   public TypeOfWidget type_of_widget;

    @JsonIgnore @OneToMany(mappedBy="grid_widget", cascade = CascadeType.ALL) @OrderBy("date_of_create desc") public List<GridWidgetVersion> grid_widget_versions = new ArrayList<>();


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = false, readOnly = true, value = "can be hidden, if GridWidget is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty
    public String    author_id()         { return author != null ? author.id : null;}

    @ApiModelProperty(required = false, readOnly = true, value = "can be hidden, if GridWidget is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty public String    author_nick_name()  { return  author != null ? author.nick_name : null;}


    @Transient  @JsonProperty @ApiModelProperty(required = true, readOnly = true)  public String  type_of_widget_id()             { return type_of_widget.id; }
    @Transient  @JsonProperty @ApiModelProperty(required = true, readOnly = true)  public String  type_of_widget_name()           { return type_of_widget.name; }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user can read TypeOfWidget, than can read all GridWidgets from list of TypeOfWidget ( You get ids of list of GridWidgets in object \"GridWidgets\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have TypeOfWidget.update_permission = true, you can create new GridWidgets on this TypeOfWidget - Or you need static/dynamic permission key if user want create GridWidget in public TypeOfWidget";

    @JsonIgnore  @Transient                                     public boolean create_permission() {return  type_of_widget.update_permission();}
    @JsonIgnore  @Transient                                     public boolean read_permission()   {return  type_of_widget.read_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()   {return  type_of_widget.update_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission() {return  type_of_widget.update_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission() {return  type_of_widget.delete_permission();}


    public enum permissions{GridWidget_create, GridWidget_read, GridWidget_edit, GridWidget_delete}

    /* FINDER -------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,GridWidget> find = new Finder<>(GridWidget.class);


}
