package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.ProgramType;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.models_update_echo.EchoHandler;
import utilities.swagger.output.Swagger_Short_Reference;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@ApiModel( value = "Widget", description = "Model of Widget")
@Table(name="Widget")
public class Model_Widget extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Widget.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                       @JsonIgnore public Integer order_position;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Producer producer;

    public ProgramType publish_type;

    @JsonIgnore @OneToMany(mappedBy="widget", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  @OrderBy("created desc") public List<Model_WidgetVersion> versions = new ArrayList<>();

    @JsonIgnore public boolean active; // U veřejných Skupin administrátor zveřejňuje skupinu - může připravit něco do budoucna

    @JsonIgnore public UUID original_id; // KDyž se vytvoří kopie nebo se publikuje program, zde se uloží původní ID pro pozdější párování

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(value = "Visible only if user has permission to know it", required = false)
    public Model_Person author() throws _Base_Result_Exception {
        try {
            if (author_id != null) {
                return Model_Person.find.byId(author_id);
            }

            return null;
        }catch (_Base_Result_Exception e){
            //nothing
            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by User not by Company", required = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_Short_Reference producer(){
        try {
            Model_Producer product = get_producer();
            if(product != null) {
                return new Swagger_Short_Reference(product.id, product.name, product.description);
            } else {
                return null;
            }
        }catch (_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }


    @JsonProperty @ApiModelProperty(required = true)
    public  List<Model_WidgetVersion> versions() {
        try{
        return get_versions();
    }catch (_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }


    @JsonProperty @ApiModelProperty(required = false, value = "Only for Community Administrator") @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean active() {
        try{
        return publish_type == ProgramType.PUBLIC || publish_type == ProgramType.DEFAULT_MAIN ? active : null;
    }catch (_Base_Result_Exception e){
            //nothing
            return false;
        } catch (Exception e) {
            logger.internalServerError(e);
            return false;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID getProjectId() throws _Base_Result_Exception {

        if (idCache().get(Model_Project.class) == null) {
            idCache().add(Model_Project.class, (UUID) Model_Project.find.query().where().eq("widgets.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_Project.class);
    }

    @JsonIgnore
    public Model_Project getProject() throws _Base_Result_Exception {

        try {
            return Model_Project.find.byId(getProjectId());
        }catch (Exception e) {
            return null;
        }
    }

    @JsonIgnore
    public List<UUID> get_versionsId() {
        if (idCache().gets(Model_WidgetVersion.class) == null) {
            idCache().add(Model_WidgetVersion.class, Model_WidgetVersion.find.query().where().eq("widget.id", id).ne("deleted", true).select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_WidgetVersion.class) != null ?  idCache().gets(Model_WidgetVersion.class) : new ArrayList<>();
    }

    @JsonIgnore
    public void sort_Model_Model_GridProgramVersion_ids() {

        List<Model_WidgetVersion> versions = get_versions();
        this.idCache().removeAll(Model_WidgetVersion.class);
        versions.stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())
                .forEach(o -> this.idCache().add(Model_WidgetVersion.class, o.id));

    }
    @JsonIgnore
    public List<Model_WidgetVersion> get_versions() {
        try {

            List<Model_WidgetVersion> grid_versions  = new ArrayList<>();

            for (UUID version_id : get_versionsId()) {
                grid_versions.add(Model_WidgetVersion.find.byId(version_id));
            }

            return grid_versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }

    }

    @JsonIgnore
    public Model_Person get_author() {
        return Model_Person.find.byId(author_id);
    }

    @JsonIgnore
    public UUID get_producerId() {
        if (idCache().get(Model_Producer.class) == null) {
            idCache().add(Model_Producer.class, (UUID) Model_Producer.find.query().where().eq("widgets.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_Producer.class);
    }

    @JsonIgnore
    public Model_Producer get_producer() {
        try {
            return Model_Producer.find.byId(get_producerId());
        }catch (Exception e) {
            return null;
        }
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("save::Creating new Object");

        // Save Object
        super.save();

        Model_Project project = getProject();

        // Add to Cache
        if (project != null) {
            new Thread(() -> { EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, project.id, project.id)); }).start();
            project.idCache().add(this.getClass(), id);
        }
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update::Update object Id: {}",  this.id);

        // Update Object
        super.update();

        if(publish_type == ProgramType.PRIVATE) {
            new Thread(() -> {
                try {
                    EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, getProjectId(), getProjectId()));
                } catch (_Base_Result_Exception e) {
                    // Nothing
                }
            }).start();
        }

    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete::Delete object Id: {}",  this.id);

        // Delete
        super.delete();

        if(publish_type == ProgramType.PRIVATE) {

            try {
                getProject().idCache().remove(this.getClass(), id);
            } catch (Exception e) {
                // Nothing
            }

            new Thread(() -> {
                try {
                    EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, getProjectId(), getProjectId()));
                } catch (Exception e) {
                    // Nothing
                }
            }).start();
        }

        return false;
    }

/* ORDER  -------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void up() throws _Base_Result_Exception {

        check_update_permission();

        /*
        Model_Widget up = Model_Widget.find.query().where().eq("order_position", (order_position-1) ).eq("type_of_widget.id", type_of_widget_id()).findOne();
        if (up == null) return;

        up.order_position += 1;
        up.update();

        this.order_position -= 1;
        this.update();
        */
    }

    @JsonIgnore @Transient
    public void down() throws _Base_Result_Exception {

        check_update_permission();
        /*
        Model_Widget down = Model_Widget.find.query().where().eq("order_position", (order_position+1) ).eq("type_of_widget.id", type_of_widget_id()).findOne();
        if (down == null) return;

        down.order_position -= 1;
        down.update();

        this.order_position += 1;
        this.update();
        */
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Widget_create.name())) return;
        if(this.project == null) throw new Result_Error_PermissionDenied();
        this.project.check_update_permission();
    }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        try {

            if(publish_type == ProgramType.PUBLIC || publish_type == ProgramType.DEFAULT_MAIN ) return;

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_read_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_read_" + id);
                return;
            }
            if (_BaseController.person().has_permission(Permission.Widget_read.name())) return;

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
            this.getProject().check_read_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_update_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_update_" + id);
                return;
            }
            if (_BaseController.person().has_permission(Permission.Widget_update.name())) return;

            if(publish_type == ProgramType.PUBLIC) {
                throw new Result_Error_PermissionDenied();
            }

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
            this.getProject().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_delete_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_delete_" + id);
                return;
            }

            if (_BaseController.person().has_permission(Permission.Widget_delete.name())) return;

            if(publish_type == ProgramType.PUBLIC) {
                throw new Result_Error_PermissionDenied();
            }

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
            this.getProject().check_read_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    @JsonProperty @ApiModelProperty("Visible only for Administrator with Special Permission") @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean community_publishing_permission()  {
        try {
            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(Model_CProgram.Permission.C_Program_community_publishing_permission.name())) {
                return true;
            }
            return null;
        }catch (_Base_Result_Exception e){
            return null;
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    public enum Permission { Widget_create, Widget_read, Widget_update, Widget_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Widget getPublicByName(String name) {
        return find.query().where().isNull("project").eq("name", name).findOne();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Widget.class)
    public static CacheFinder<Model_Widget> find = new CacheFinder<>(Model_Widget.class);
}