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

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList private String cache_value_project_id;
    @JsonIgnore @Transient @TyrionCachedList private List<String> grid_widgets_ids = new ArrayList<>();

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @ApiModelProperty(value = "This value will be in Json only if TypeOfWidget is private!", readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @Transient public String project_id() {

        if(cache_value_project_id == null){
            Model_Project project = Model_Project.find.where().eq("type_of_widgets.id", id).select("id").findUnique();
            cache_value_project_id = project.id;
        }

        return cache_value_project_id;
    }


    @JsonProperty @Transient public List<Swagger_GridWidget_Short_Detail> widgets() {

        try {

            List<Swagger_GridWidget_Short_Detail> short_detail_widgets = new ArrayList<>();

            for (Model_GridWidget widget : get_grid_widgets()) {
                short_detail_widgets.add( widget.get_grid_widget_short_detail() ) ;
            }

            return short_detail_widgets;

        }catch (Exception e){
            terminal_logger.internalServerError("widgets:", e);
            return null;
        }
    }



/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @TyrionCachedList
    public List<Model_GridWidget> get_grid_widgets(){
        try {

            // Cache
            if(grid_widgets_ids.isEmpty()) {

                List<Model_GridWidget> blockoBlocks = Model_GridWidget.find.where().eq("type_of_widget.id", id).eq("removed_by_user", false).order().asc("order_position").select("id").findList();

                // Získání seznamu
                for (Model_GridWidget blockoBlock : blockoBlocks) {
                    grid_widgets_ids.add(blockoBlock.id);
                }
            }

            List<Model_GridWidget> blockoBlock = new ArrayList<>();

            for(String blockoBlock_id : grid_widgets_ids){
                blockoBlock.add(Model_GridWidget.get_byId(blockoBlock_id));
            }

            return blockoBlock;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new ArrayList<Model_GridWidget>();
        }
    }

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

    @JsonIgnore @Transient   public boolean create_permission()  {return (project != null && project.update_permission()) || Controller_Security.get_person().has_permission("TypeOfWidget_create");}
    @JsonIgnore @Transient   public boolean read_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("type_of_widget_read_" + id)) return Controller_Security.get_person().permissions_keys.get("type_of_widget_read_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("TypeOfWidget_read")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if(project_id() != null && Model_Project.get_byId(project_id()).read_permission()){
            Controller_Security.get_person().permissions_keys.put("type_of_widget_read_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("type_of_widget_read_" + id, false);
        return false;
    }

    @JsonProperty @Transient public boolean update_permission()  {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("type_of_widget_update_" + id)) return Controller_Security.get_person().permissions_keys.get("type_of_widget_update_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("TypeOfWidget_update")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if(project_id() != null && Model_Project.get_byId(project_id()).edit_permission()){
            Controller_Security.get_person().permissions_keys.put("type_of_widget_update_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("type_of_widget_edit_" + id, false);
        return false;

    }
    @JsonProperty @Transient public boolean edit_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("type_of_widget_edit_" + id)) return Controller_Security.get_person().permissions_keys.get("type_of_widget_edit_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("TypeOfWidget_edit")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if(project_id() != null && Model_Project.get_byId(project_id()).edit_permission()){
            Controller_Security.get_person().permissions_keys.put("type_of_widget_edit_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("type_of_widget_edit_" + id, false);
        return false;
    }
    @JsonProperty @Transient public boolean delete_permission()  {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("type_of_widget_delete_" + id)) return Controller_Security.get_person().permissions_keys.get("type_of_widget_delete_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("TypeOfWidget_delete")) return true;



        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if(project_id() != null && Model_Project.get_byId(project_id()).edit_permission()){
            Controller_Security.get_person().permissions_keys.put("type_of_widget_delete_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("type_of_widget_delete_" + id, false);
        return false;
    }




    public enum permissions{TypeOfWidget_create, TypeOfWidget_read, TypeOfWidget_edit , TypeOfWidget_delete, TypeOfWidget_update}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_TypeOfWidget> find = new Finder<>(Model_TypeOfWidget.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_TypeOfWidget.class.getSimpleName();
    public static Cache<String, Model_TypeOfWidget> cache = null;               // < Model_CProgram_id, Model_TypeOfWidget>

    @JsonIgnore
    public static Model_TypeOfWidget get_byId(String id) {

        Model_TypeOfWidget type_of_widget = cache.get(id);
        if (type_of_widget == null){

            type_of_widget = Model_TypeOfWidget.find.byId(id);
            if (type_of_widget == null){
                terminal_logger.warn("get_byId :: This object id:: " + id + " wasn't found.");
            }

            cache.put(id, type_of_widget);
        }

        return type_of_widget;

    }

    @JsonIgnore
    public static List<Model_TypeOfWidget> get_all() {

        List<Model_TypeOfWidget> typeOfWidgets = find.where().isNull("project").findList();
        typeOfWidgets.addAll( find.where().eq("project.participants.person.id", Controller_Security.get_person().id ).eq("removed_by_user",false).order().asc("name").findList() );
        typeOfWidgets.addAll( find.where().isNull("project").eq("removed_by_user", false).order().asc("order_position").findList());

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
