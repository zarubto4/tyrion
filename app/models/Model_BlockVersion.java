package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.Approval;
import utilities.enums.EntityType;
import utilities.logger.Logger;
import utilities.model.UnderProject;
import utilities.model.VersionModel;
import utilities.models_update_echo.EchoHandler;
import utilities.permission.Action;
import utilities.permission.Permissible;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "BlockVersion", description = "Model of BlockVersion")
@Table(name="BlockVersion")
public class Model_BlockVersion extends VersionModel implements Permissible, UnderProject {

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
    public Model_Block getBlock() {
        return isLoaded("block") ? block : Model_Block.find.query().nullable().where().eq("versions.id", id).findOne();
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return this.getBlock().getProject();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/
    
    @JsonIgnore @Override
    public void save() {
        
        super.save();

        // Add to Cache
        if(getBlock() != null) {
            getBlock().getVersionsId();
            getBlock().idCache().add(this.getClass(), id);
        }

        new Thread(() -> {
            EchoHandler.addToQueue(new WSM_Echo(Model_Block.class, getBlock().get_project_id(), getBlock().id));
        }).start();

    }

    @JsonIgnore @Override
    public void update() {

        super.update();

        new Thread(() -> {
            EchoHandler.addToQueue(new WSM_Echo(Model_Block.class, getBlock().get_project_id(), getBlock().id));
        }).start();

    }

    @JsonIgnore @Override
    public boolean delete() {


        new Thread(() -> {
            EchoHandler.addToQueue(new WSM_Echo(Model_Block.class, getBlock().get_project_id(), getBlock().id));
        }).start();


        if (getBlock() != null) {
            getBlock().idCache().remove(this.getClass(), id);
        }

        return super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSIONS ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.BLOCK_VERSION;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.PUBLISH);
    }

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

    @InjectCache(Model_BlockVersion.class)
    public static CacheFinder<Model_BlockVersion> find = new CacheFinder<>(Model_BlockVersion.class);
}