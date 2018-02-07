package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.Approval;
import utilities.enums.ProgramType;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "BlockVersion", description = "Model of BlockVersion")
@Table(name="BlockVersion")
public class Model_BlockVersion extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_BlockVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(columnDefinition = "TEXT") public String design_json;
    @Column(columnDefinition = "TEXT") public String logic_json;

    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty("Only if user make request for publishing") @Enumerated(EnumType.STRING)
    public Approval approval_state;
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty("Only for main / default program - and access only for administrators") @Enumerated(EnumType.STRING)
    public ProgramType publish_type;
    
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Person author;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Block block;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_block_id;
    @JsonIgnore @Transient @Cached private UUID cache_author_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public Model_Person author() {
        try {

            if (author == null) return null;

            return get_author();

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Person get_author() {

        if (cache_author_id == null) {
            Model_Person person = Model_Person.find.query().where().eq("blockVersionsAuthor.id", id).select("id").findOne();
            cache_author_id = person.id;
        }

        return Model_Person.getById(cache_author_id);
    }


    @JsonIgnore
    public UUID get_block_id() {

        if (cache_block_id == null) {

            Model_Block block = Model_Block.find.query().where().eq("versions.id", id).select("id").findOne();
            if (block != null) {
                cache_block_id = block.id;
            }
        }

        return cache_block_id;
    }

    @JsonIgnore
    public Model_Block get_block() {

        if (get_block_id() != null) {
           return Model_Block.getById(cache_block_id);
        }

        return null;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/
    
    @JsonIgnore @Override
    public void save() {
        
        super.save();

        if (block.project != null ) {
            new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Block.class, get_block().getProjectId(), get_block().id))).start();
        }

        if (block != null) {
            block.cache_versions_id.add(0, id);
        }
    }

    @JsonIgnore @Override
    public void update() {

        super.update();

        if (get_block().getProjectId() != null) {
            new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_BlockVersion.class, get_block().getProjectId(), id))).start();
        }
    }

    @JsonIgnore @Override
    public boolean delete() {

        if (get_block() != null && get_block().getProjectId() != null) {
            new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Block.class, get_block().getProjectId(), get_block().id))).start();
        }

        if (get_block() != null) {
            get_block().cache_versions_id.remove(id);
        }

        return super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user can read BlockoBlock, than can read all Versions from list of BlockoBlock ( You get ids of list of version in object \"BlockoBlocks\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have BlockoBlock.update_permission = true, you can create new version of BlockoBlocks on this BlockoBlock - Or you need static/dynamic permission key if user want create version of BlockoBlock in public BlockoBlock in public TypeOfBlock";

/* PERMISSIONS ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) public boolean create_permission()  {  return  block.update_permission() || BaseController.person().has_permission("BlockVersion_create"); }
    @JsonProperty @ApiModelProperty(required = true) public boolean read_permission()    {  return  get_block().read_permission()   || BaseController.person().has_permission("BlockVersion_read");   }
    @JsonProperty @ApiModelProperty(required = true) public boolean edit_permission()    {  return  get_block().update_permission() || BaseController.person().has_permission("BlockVersion_update");   }
    @JsonProperty @ApiModelProperty(required = true) public boolean delete_permission()  {  return  get_block().update_permission() || BaseController.person().has_permission("BlockVersion_delete"); }
    @JsonProperty @ApiModelProperty(required = false, value = "Visible only for Administrator with Permission") @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean community_publishing_permission()  { return BaseController.person().has_permission(Model_CProgram.Permission.C_Program_community_publishing_permission.name());}

    public enum Permission { BlockVersion_create, BlockVersion_read, BlockVersion_update, BlockVersion_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_BlockVersion.class)
    public static Cache<UUID, Model_BlockVersion> cache;

    public static Model_BlockVersion getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_BlockVersion getById(UUID id) {

        Model_BlockVersion version = cache.get(id);
        if (version == null) {

            version = find.query().where().idEq(id).eq("deleted", false).findOne();
            if (version == null) return null;

            cache.put(id, version);
        }

        return version;
    }

    @JsonIgnore
    public static Model_BlockVersion get_scheme() {
        return find.query().where().eq("name", "version_scheme").findOne();
    }

    @JsonIgnore
    public static List<Model_BlockVersion> get_pending() {
        return find.query().where().eq("approval_state", Approval.PENDING).findList();
    }

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_BlockVersion> find = new Finder<>(Model_BlockVersion.class);
}