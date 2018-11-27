package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.Server;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.*;
import exceptions.NotFoundException;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.models_update_echo.EchoHandler;
import utilities.network.JsonNetworkStatus;
import utilities.network.Networkable;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.swagger.output.Swagger_Short_Reference;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@ApiModel(description = "Model of Instance", value = "Instance")
@Table(name="Instance")
public class Model_Instance extends TaggedModel implements Permissible, UnderProject, Networkable {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    public static final Logger logger = new Logger(Model_Instance.class);
    
/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore public UUID current_snapshot_id;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_HomerServer server_main;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_HomerServer server_backup;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_BProgram b_program; // Only first reference!

    @JsonIgnore @OneToMany(mappedBy = "instance", fetch = FetchType.LAZY) public List<Model_InstanceSnapshot> snapshots = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonNetworkStatus @Transient
    public NetworkStatus online_state;

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Swagger_Short_Reference> snapshots() {
        try {

            List<Swagger_Short_Reference> references = new ArrayList<>();
            for(Model_InstanceSnapshot snapshot :  getSnapShots()) {
                references.add(snapshot.ref());
            }

            return references;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_Short_Reference b_program(){
        try {
            return get_BProgram().ref();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_HomerServer server(){
        try {
            return getServer();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_InstanceSnapshot current_snapshot() {
        try {
            if (this.current_snapshot_id != null) {
                return Model_InstanceSnapshot.find.byId(this.current_snapshot_id);
            }
        } catch (NotFoundException e){
            //nothing
        } catch (Exception e) {
            logger.internalServerError(e);
            this.current_snapshot_id = null;
            this.update();

        }
        return null;
    }

    /*public NetworkStatus online_state() {

        // Pokud Tyrion nezná server ID - to znamená deska se ještě nikdy nepřihlásila - chrání to proti stavu "během výroby"
        // i stavy při vývoji kdy se tvoří zběsile nové desky na dev serverech
        if (current_snapshot() == null) {

            if (getSnapShots().isEmpty()) {
                return NetworkStatus.NOT_YET_FIRST_CONNECTED;
            } else {
                return NetworkStatus.SHUT_DOWN;
            }
        }

        // Pokud je server offline - tyrion si nemuže být jistý stavem hardwaru - ten teoreticky muže být online
        // nebo také né - proto se vrací stav Enum_Online_status - na to reaguje parameter latest_online(),
        // který následně vrací latest know online
        try {

            if ((Model_HomerServer.find.byId(getServer_id()) != null) && Model_HomerServer.find.byId(getServer_id()).online_state() == NetworkStatus.ONLINE) {

                if (cache_status.containsKey(id)) {
                    return cache_status.get(id) ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE;
                }

                //else {
                // Začnu zjišťovat stav - v separátním vlákně! Po strátě spojení se serverem nebo po načítání se někde opomělo změnit stav na online proto
                // je podmínka zakomentovaná aby se to ověřovalo vždy
                // TODO ASAP ošetřit!!!!
                new Thread(() -> {
                    try {

                        WS_Message_Instance_status status = get_instance_status();

                        if (status.status.equals("success")) cache_status.put(id, status.get_status(id).status);
                        WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Instance.class, this.id, status.get_status(id).status, getProjectId());

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }).start();

                return NetworkStatus.SYNCHRONIZATION_IN_PROGRESS;
                //}

            } else {
                return NetworkStatus.UNKNOWN_LOST_CONNECTION_WITH_SERVER;
            }

        } catch (Exception e) {
            // Záměrný Exception - Občas se nedosynchronizuje Cach - ale system stejnak po zvalidování dorovná stav
           return NetworkStatus.UNKNOWN_LOST_CONNECTION_WITH_SERVER;
        }
    }*/

    @JsonProperty @ApiModelProperty(required = true)
    public String instance_remote_url() {
        try {

            if (current_snapshot() != null) {

                if (Server.mode == ServerMode.DEVELOPER) {
                    return "ws://" + Model_HomerServer.find.byId(getServer_id()).server_url + ":" + Model_HomerServer.find.byId(getServer_id()).web_view_port + "/" + id + "/#token";
                } else {
                    return "wss://" + Model_HomerServer.find.byId(getServer_id()).server_url + ":" + Model_HomerServer.find.byId(getServer_id()).web_view_port + "/" + id + "/#token";
                }
            }
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID getProjectId() {

        if (idCache().get(Model_Project.class) == null) {
            idCache().add(Model_Project.class, Model_Project.find.query().where().eq("instances.id", id).select("id").findSingleAttributeList());
        }

        return idCache().get(Model_Project.class);
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return isLoaded("project") ? project :  Model_Project.find.query().nullable().where().eq("instances.id", id).findOne();
    }

    @JsonIgnore
    public Model_BProgram get_BProgram() {
        return isLoaded("b_program") ? b_program : Model_BProgram.find.query().where().eq("instances.id", id).findOne();
    }

    @JsonIgnore
    public List<UUID> getHardwareIds() {

        return current_snapshot().getHardwareIds();
    }

    @JsonIgnore
    public UUID getServer_id() {

        if (idCache().get(Model_HomerServer.class) == null) {
            idCache().add(Model_HomerServer.class, Model_HomerServer.find.query().where().eq("instances.id", id).select("id").findSingleAttributeList());
        }

        return idCache().get(Model_HomerServer.class);

    }

    @JsonIgnore
    public Model_HomerServer getServer() {
        return isLoaded("server_main") ? server_main : Model_HomerServer.find.query().where().eq("instances.id", id).findOne();
    }


    @JsonIgnore
    public List<UUID> getSnapShotsIds() {

        if (idCache().gets(Model_InstanceSnapshot.class) == null) {
            idCache().add(Model_InstanceSnapshot.class,  Model_InstanceSnapshot.find.query().where().ne("deleted", true).eq("instance.id", id).order().desc("created").select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_InstanceSnapshot.class) != null ?  idCache().gets(Model_InstanceSnapshot.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_InstanceSnapshot> getSnapShots() {
        try {

            List<Model_InstanceSnapshot> list = new ArrayList<>();

            for (UUID id : getSnapShotsIds() ) {
                try {
                    list.add(Model_InstanceSnapshot.find.byId(id));
                } catch (Exception e){
                    logger.error("getSnapShots: ID {} nenalezeno", id);
                }
            }

            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public void sort_Model_InstanceSnapshot_ids() {

        List<Model_InstanceSnapshot> snapshots = getSnapShots();
        this.idCache().removeAll(Model_InstanceSnapshot.class);
        snapshots.stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())
                .forEach(o -> this.idCache().add(Model_InstanceSnapshot.class, o.id));

    }

/* JSON Override  Method -----------------------------------------------------------------------------------------*/

    @Override
    public void save() {

        super.save();

        if (project != null) {
            try {
                getProject().idCache().add(this.getClass(), id);
            }catch (Exception e) {
                // Nothing
            }
        }
    }

    @Override
    public void update() {

        logger.trace("update - updating in database, id: {}",  this.id);

        super.update();

        if (getProject() != null) {
            logger.warn("Sending Update for Instance ID: {}", this.id);
            new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Instance.class, getProject().id, this.id))).start();
        }
    }
    
    @Override
    public boolean delete() {

        logger.debug("delete - deleting from database, id: {} ", this.id);

        super.delete();

        try {
            getProject().idCache().remove(this.getClass(), id);
        } catch (Exception e) {
            // Nothing
        }

        // Its required to change all HW devices where this instance is registered in paramater connected_instance_id ( // Latest know Instance ID)
        List<UUID> hardware_for_change = Model_Hardware.find.query().where().eq("connected_instance_id", this.id).select("id").findSingleAttributeList();
        for(UUID hardware_id: hardware_for_change) {
            Model_Hardware hw = Model_Hardware.find.byId(hardware_id);
            hw.connected_instance_id = null;
            hw.update();
        }

        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* INSTANCE WEBSOCKET CONTROLLING ON HOMER SERVER-----------------------------------------------------------------------*/

    public static final String CHANNEL = "instance";

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public String get_path() {
        return getProject().getPath() + "/instances/" + this.id;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.INSTANCE;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.DEPLOY);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @InjectCache(value = Boolean.class, name = "Model_Instance_Status")
    public static Cache<UUID, Boolean> cache_status;

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_Instance.class)
    public static CacheFinder<Model_Instance> find = new CacheFinder<>(Model_Instance.class);
}