package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.Approval;
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

    @Column(columnDefinition = "TEXT") public String design_json; // TODO https://youtrack.byzance.cz/youtrack/issue/TYRION-639
    @Column(columnDefinition = "TEXT") public String logic_json;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Block block;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/


    @JsonIgnore
    public UUID get_block_id() {

        if (idCache().get(Model_Block.class) == null) {
            idCache().add(Model_Block.class, (UUID) Model_Block.find.query().where().eq("versions.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_Block.class);
    }

    @JsonIgnore
    public Model_Block get_block() {
        try {
            return Model_Block.find.byId(get_block_id());
        } catch (Exception e) {
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/
    
    @JsonIgnore @Override
    public void save() {
        
        super.save();

        // Add to Cache
        if(get_block() != null) {
            System.out.println("Add To Blocko by get_block()");
            get_block().getVersionsId();
            get_block().idCache().add(this.getClass(), id);
        }

        new Thread(() -> {
            try {
                EchoHandler.addToQueue(new WSM_Echo(Model_Block.class, get_block().get_project_id(), get_block().id));
            } catch (_Base_Result_Exception e) {
                // Nothing
            }
        }).start();

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
            get_block().idCache().remove(this.getClass(), id);
        }

        return super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSIONS ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public void check_create_permission() throws _Base_Result_Exception { block.check_update_permission();} // You have to access block directly, because get_block() finds the block by id of the version which is not yet created
    @JsonIgnore public void check_read_permission()   throws _Base_Result_Exception {
        get_block().check_read_permission();
    }
    @JsonIgnore public void check_update_permission() throws _Base_Result_Exception { get_block().check_update_permission();}
    @JsonIgnore public void check_delete_permission() throws _Base_Result_Exception { get_block().check_update_permission();}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_BlockVersion get_scheme() {
        return find.query().where().eq("name", "version_scheme").findOne();
    }

    @JsonIgnore
    public static List<Model_BlockVersion> get_pending() {
        return find.query().where().eq("approval_state", Approval.PENDING).findList();
    }

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_BlockVersion.class)
    public static CacheFinder<Model_BlockVersion> find = new CacheFinder<>(Model_BlockVersion.class);
}