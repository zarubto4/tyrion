package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.EntityType;
import utilities.enums.ProgramType;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.models_update_echo.EchoHandler;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.swagger.output.Swagger_Short_Reference;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "Block", description = "Model of Block")
@Table(name="Block")
public class Model_Block extends TaggedModel implements Permissible, UnderProject {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Block.class);

/* DATABASE VALUES -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore public boolean active; // U veřejných Skupin administrátor zveřejňuje skupinu - může připravit něco do budoucna

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Producer producer;

                                       @JsonIgnore public Integer order_position;
                                                   public ProgramType publish_type;

    @JsonIgnore @OneToMany(mappedBy="block", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Model_BlockVersion> versions = new ArrayList<>();

    @JsonIgnore public UUID original_id; // KDyž se vytvoří kopie nebo se publikuje program, zde se uloží původní ID pro pozdější párování

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(value = "Visible only if user has permission to know it", required = false)
    public Model_Person author() throws _Base_Result_Exception {
        try {
            return get_author();
        }catch(_Base_Result_Exception e){
            //nothing
            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty  @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by User not by Company", required = false)
    public Swagger_Short_Reference producer() {
        try {
            Model_Producer producer = get_producer();
            if (producer == null) return null;
            return new Swagger_Short_Reference(producer.id, producer.name, producer.description);
        }catch (_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }


    @JsonProperty @ApiModelProperty(required = true)
    public  List<Model_BlockVersion> versions() {
        try {
            return getVersions();

        }catch(_Base_Result_Exception e){
            //nothing
            return null;
        }catch(Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty(required = false) @ApiModelProperty(required = false, value = "Only for Community Administrator") @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean active() {
        try {
            return publish_type == ProgramType.PUBLIC || publish_type == ProgramType.DEFAULT_MAIN ? active : null;
        }catch(_Base_Result_Exception e){
            //nothing
            return null;
        }catch(Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_project_id() throws _Base_Result_Exception {

        if (idCache().get(Model_Project.class) == null) {
            idCache().add(Model_Project.class, Model_Project.find.query().where().eq("blocks.id", id).select("id").findSingleAttributeList());
        }

        return idCache().get(Model_Project.class);

    }

    @JsonIgnore
    public Model_Project getProject() throws _Base_Result_Exception {
        return this.project != null ? this.project : Model_Project.find.query().where().eq("blocks.id", id).findOne();
    }

    @JsonIgnore
    public List<UUID> getVersionsId() {
        if (idCache().gets(Model_BlockVersion.class) == null) {
            idCache().add(Model_BlockVersion.class, Model_BlockVersion.find.query().where().eq("block.id", id).eq("deleted", false).select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_BlockVersion.class) != null ?  idCache().gets(Model_BlockVersion.class) : new ArrayList<>();
    }
    @JsonIgnore
    public List<Model_BlockVersion> getVersions() {
        try {

            List<Model_BlockVersion> versions  = new ArrayList<>();

            for (UUID version_id : getVersionsId()) {
                versions.add(Model_BlockVersion.find.byId(version_id));
            }

            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }

    }

    @JsonIgnore
    public Model_Person get_author() {
        if(author_id != null) {
            return Model_Person.find.byId(author_id);
        } else return null;
    }

    @JsonIgnore
    public UUID get_producerId() {

        if (idCache().get(Model_Producer.class) == null) {
            idCache().add(Model_Producer.class, Model_Producer.find.query().where().eq("blocks.id", id).select("id").findSingleAttributeList());
        }

        return idCache().get(Model_Producer.class);

    }

    @JsonIgnore
    public Model_Producer get_producer() {

        try {
            return Model_Producer.find.byId(get_producerId());
        } catch (Exception e) {
            return null;
        }
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        super.save();

        if (project != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, project.id, project.id))).start();
    }

    @JsonIgnore @Override
    public void update() {

        super.update();

        // Call notification about model update
        if (publish_type == ProgramType.PRIVATE) {
            new Thread(() -> {
                try {
                    EchoHandler.addToQueue(new WSM_Echo(Model_Block.class, get_project_id(), this.id));
                } catch (_Base_Result_Exception e) {
                    // Nothing
                }
            }).start();
        }
    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete :: Delete object Id: {} ", this.id);
        super.delete();

        // Remove from Project Cache
        if (publish_type == ProgramType.PRIVATE) {

            try {

                getProject().idCache().remove(this.getClass(), id);

            } catch (Exception e) {
                // Nothing
            }

            new Thread(() -> {
                try {
                    EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, get_project_id(), get_project_id()));
                } catch (Exception e) {
                    // Nothing
                }
            }).start();
        }

        return false;
    }

/* ORDER ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void up() throws _Base_Result_Exception {
        check_update_permission();
/*
        logger.trace("up :: Change Order Position! Up");

        Model_Block up = find.query().where().eq("order_position", (order_position-1) ).eq("type_of_block.id", type_of_block.id).findOne();
        if (up == null) {
            logger.warn("up :: illegal operation (out of index)! ");
            return;
        }

        up.order_position += 1;
        up.update();

        this.order_position -= 1;
        this.update();*/
    }

    @JsonIgnore
    public void down() throws _Base_Result_Exception {
        check_update_permission();
/*
        logger.trace("down :: Change Order Position! DOWN ");

        Model_Block down = find.query().where().eq("order_position", (order_position+1) ).eq("type_of_block.id", type_of_block.id).findOne();
        if (down == null) {
            logger.warn("down :: illegal operation (out of index)! ");
            return;
        }

        down.order_position -= 1;
        down.update();

        this.order_position += 1;
        this.update();*/
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @Override
    public EntityType getEntityType() {
        return EntityType.BLOCK;
    }

    @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.PUBLISH);
    }

    @JsonProperty @ApiModelProperty("Visible only for Administrator with permission") @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean community_publishing_permission()  {
        /*try {
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
        }*/
        return true;
    }

    // public enum Permission { Block_create, Block_read, Block_edit, Block_update, Block_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Block getPublicByName(String name) {
        return find.query().where().isNull("project").eq("deleted", false).eq("name", name).findOne();
    }

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Block.class)
    public static CacheFinder<Model_Block> find = new CacheFinder<>(Model_Block.class);
}