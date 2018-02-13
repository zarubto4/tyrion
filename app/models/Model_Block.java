package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.ProgramType;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "Block", description = "Model of Block")
@Table(name="Block")
public class Model_Block extends TaggedModel {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Block.class);

/* DATABASE VALUES -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore public boolean active; // U veřejných Skupin administrátor zveřejňuje skupinu - může připravit něco do budoucna

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Person author;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Producer producer;

                                       @JsonIgnore public Integer order_position;
                                                   public ProgramType publish_type;

    @JsonIgnore @OneToMany(mappedBy="block", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Model_BlockVersion> versions = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_project_id;
    @JsonIgnore @Transient @Cached public List<UUID> cache_versions_id;
    @JsonIgnore @Transient @Cached private UUID cache_author_id;
    @JsonIgnore @Transient @Cached private UUID cache_producer_id;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public UUID author_id() {

        if (cache_author_id != null) return cache_author_id;

        Model_Person person = get_author();
        if (person == null) return null;

        return person.id;
    }

    @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public String author_nick_name() {

        Model_Person person = get_author();
        if (person == null) return null;

        return person.nick_name;
    }

    @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by User not by Company")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public UUID producer_id() {

        if (cache_producer_id != null) return cache_producer_id;

        Model_Producer producer = get_producer();
        if (producer == null) return null;

        return producer.id;
    }

    @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by User not by Company")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty
    public String producer_name() {

        Model_Producer producer = get_producer();
        if (producer == null) return null;

        return producer.name;
    }

    @JsonProperty @ApiModelProperty(required = true)
    public  List<Model_BlockVersion> versions() {
        return getVersions();
    }

    @JsonProperty(required = false) @ApiModelProperty(required = false, value = "Only for Community Administrator") @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean active() {
        return publish_type == ProgramType.PUBLIC ? true : null;
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_project_id() throws _Base_Result_Exception {

        if (cache_project_id == null) {
            Model_Project project = Model_Project.find.query().where().eq("blocks.id", id).select("id").findOne();
            if (project == null) return null;
            cache_project_id = project.id;
        }

        return cache_project_id;
    }

    @JsonIgnore
    public Model_Project get_project() throws _Base_Result_Exception {

        if (cache_project_id == null) {
            return Model_Project.getById(get_project_id());
        }else {
            return Model_Project.getById(cache_project_id);
        }
    }

    @JsonIgnore
    public List<Model_BlockVersion> getVersions() {
        try {

            if (cache_versions_id.isEmpty()) {

                List<Model_BlockVersion> blocko_versions =  Model_BlockVersion.find.query().where().eq("block.id", id).eq("deleted", false).order().desc("created").select("id").findList();

                // Získání seznamu
                for (Model_BlockVersion blocko_version : blocko_versions) {
                    cache_versions_id.add(blocko_version.id);
                }
            }

            List<Model_BlockVersion> blocko_versions  = new ArrayList<>();

            for (UUID version_id : cache_versions_id) {
                blocko_versions.add(Model_BlockVersion.getById(version_id));
            }

            return blocko_versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }

    }

    @JsonIgnore
    public Model_Person get_author() {

        if (cache_author_id == null) {
            Model_Person person = Model_Person.find.query().where().eq("blocksAuthor.id", id).select("id").findOne();
            if (person == null) return null;

            cache_author_id = person.id;
        }

        return Model_Person.getById(cache_author_id);
    }

    @JsonIgnore
    public Model_Producer get_producer() {

        if (cache_producer_id == null) {
            Model_Producer producer = Model_Producer.find.query().where().eq("blocks.id", id).select("id").findOne();
            if (producer == null) return null;

            cache_producer_id = producer.id;
        }

        return Model_Producer.getById(cache_producer_id);
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        if(project != null) project.check_update_permission();

        super.save();

        if (project != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, project.id, project.id))).start();
    }

    @JsonIgnore @Override
    public void update() {

        super.update();

        if (cache.containsKey(this.id)) {
            cache.replace(this.id, this);
        } else {
            cache.put(this.id, this);
        }

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Block.class, get_project_id(), id))).start();
    }

    @JsonIgnore @Override
    public boolean delete() {

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, get_project_id(), get_project_id()))).start();

        return super.delete();
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

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user can read TypeOfBlock, than can read all BlockoBlocks from list of TypeOfBlock ( You get ids of list of BlockoBlocks in object \"BlockoBlocks\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have TypeOfBlock.update_permission = true, you can create new BlockoBlocks on this TypeOfBlock - Or you need static/dynamic permission key if user want create BlockoBlock in public TypeOfBlock";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Block_create.name())) return;
        project.check_update_permission();
    }
    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception   {
        if(_BaseController.person().has_permission(Permission.Block_read.name())) return;
        if(publish_type == ProgramType.PUBLIC || publish_type == ProgramType.DEFAULT_MAIN ) return;
        get_project().check_read_permission();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Block_update.name())) return;
        get_project().check_update_permission();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception  {
        if(_BaseController.person().has_permission(Permission.Block_delete.name())) return;
        get_project().check_update_permission();
    }
    @JsonProperty @ApiModelProperty("Visible only for Administrator with permission") @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean community_publishing_permission()  {
        try {
            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if(_BaseController.person().has_permission(Model_CProgram.Permission.C_Program_community_publishing_permission.name())) return true;
            return null;
        }catch (_Base_Result_Exception exception){
            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    public enum Permission { Block_create, Block_read, Block_edit, Block_update, Block_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_Block.class)
    public static Cache<UUID, Model_Block> cache;

    public static Model_Block getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_Block getById(UUID id) throws _Base_Result_Exception {

        Model_Block block = cache.get(id);
        if (block == null) {

            block = find.query().where().idEq(id).eq("deleted", false).findOne();
            if (block == null) throw new Result_Error_NotFound(Model_Block.class);

            cache.put(id, block);
        }

        return block;
    }

    public static Model_Block getPublicByName(String name) {
        return find.query().where().isNull("type_of_block.project").eq("deleted", false).eq("name", name).findOne();
    }

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Block> find = new Finder<>(Model_Block.class);
}