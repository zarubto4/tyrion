package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.Approval;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.VersionModel;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "BlockVersion", description = "Model of BlockVersion")
@Table(name="BlockVersion")
public class Model_BlockVersion extends VersionModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_BlockVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(columnDefinition = "TEXT") public String design_json;
    @Column(columnDefinition = "TEXT") public String logic_json;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Block block;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_block_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/


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


        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Block.class, get_block().get_project_id(), get_block().id));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

        block.cache_version_ids.add(0, id);

    }

    @JsonIgnore @Override
    public void update() {

        super.update();

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Block.class, get_block().get_project_id(), get_block().id));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

    }

    @JsonIgnore @Override
    public boolean delete() {


        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Block.class, get_block().get_project_id(), get_block().id));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();


        if (get_block() != null) {
            get_block().cache_version_ids.remove(id);
        }

        return super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSIONS ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public void check_create_permission() throws _Base_Result_Exception { block.check_update_permission();} // You have to access block directly, because get_block() finds the block by id of the version which is not yet created
    @JsonIgnore public void check_read_permission()   throws _Base_Result_Exception { get_block().check_read_permission();}
    @JsonIgnore public void check_update_permission() throws _Base_Result_Exception { get_block().check_update_permission();}
    @JsonIgnore public void check_delete_permission() throws _Base_Result_Exception { get_block().check_update_permission();}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_BlockVersion.class)
    public static Cache<UUID, Model_BlockVersion> cache;

    public static Model_BlockVersion getById(UUID id) throws _Base_Result_Exception {

        Model_BlockVersion version = cache.get(id);
        if (version == null) {

            version = find.query().where().idEq(id).eq("deleted", false).findOne();
            if (version == null) throw new Result_Error_NotFound(Model_Widget.class);

            cache.put(id, version);
        }
        // Check Permission
        if(version.its_person_operation()) {
            version.check_read_permission();
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