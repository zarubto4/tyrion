package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClientException;
import controllers._BaseController;
import io.ebean.Expr;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import org.mindrot.jbcrypt.BCrypt;
import play.libs.Json;
import utilities.Server;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.document_db.document_objects.DM_Board_BackupIncident;
import utilities.document_db.document_objects.DM_Board_Bootloader_DefaultConfig;
import utilities.document_db.document_objects.DM_Board_Connect;
import utilities.document_db.document_objects.DM_Board_Disconnected;
import utilities.enums.*;
import utilities.errors.ErrorCode;
import utilities.errors.Exceptions.*;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.models_update_echo.EchoHandler;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.input.Swagger_Board_Developer_parameters;
import utilities.swagger.output.Swagger_Short_Reference;
import utilities.swagger.output.Swagger_UpdatePlan_brief_for_homer;
import websocket.ParallelTask;
import websocket.interfaces.WS_Homer;
import websocket.messages.homer_hardware_with_tyrion.*;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Help_Hardware_Pair;
import websocket.messages.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Progress;
import websocket.messages.tyrion_with_becki.WS_Message_Online_Change_status;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;

@Entity
@ApiModel(value = "Hardware", description = "Model of Hardware")
@Table(name="Hardware")
public class Model_Hardware extends TaggedModel {

/* DOCUMENTATION -------------------------------------------------------------------------------------------------------*/

    /*
        Hardware je zastupující entita, která by měla být natolik univerzální, že dokáže pokrýt veškerý supportovaný hardware
        který je Byzance schopna obsluhovat. Rozdílem je Typ desky - který může měnit chování některých metod nebo executiv
        procedur.

        Batch je z Tykpepe OfBoards výrobní kolekce, nebo šarže tak aby se dalo trackovat kdo co vyrobil, kdy osadil atd..
     */

    /*
        Hardware si uživatel registruje pomocí registration_hash. Hardware lze registrovat kolikrát uživatel chce, ale pokaždé do jiného
        projektu. Jedinou změnou je, že hardware je aktivní jen v jednom projektu. Takže v ostatních lze s ním dělat další operace jako
        je edit, drátování do blocka atd.. ale HW se nikdy nepřipojí online, tím se nikdy nezpustí žádné operace na jeho fyzickou alterantivu.

        Uživatel ho musí nejdříve deaktivoat (freeznout v jednom projektu) aby ho mohl aktivovat v jiném projektu.

     */

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Hardware.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    /**
     * Adresa (full_id) zařízení je dána výrobcem čipu. Je to komunikační ID s Homer Serverem.
     * Ale jelikož potřebujeme umožnit uživatelovi odstranit Hardware z projektu a zase ho tam vrátit, bylo nutné
     * mít více ID na tutéž full_id. Jde zejmena o to, abychom zachovali historii nad objektem a další návaznosti.
     *
     * Stejné Full_id může být i v několika objektech najednou!
    */
    public String full_id;

    /**
     * Kriticky důležitý objekt! Model_Hardware muže být totožných desítky, ale jen jeden se stejným full_id
     * muže být dominantní! JE nutné na to pamatovat! A možná vymyslet i  způsob jak mechanicky ošetřit,
     * aby nikdy nedošlo ke změně dominance a existovali dva objekty s dominancí.
     */
    public boolean dominant_entity;


    public String wifi_mac_address;
    public String mac_address;

    // @JsonIgnore public String registration_hash;   // Vygenerovaný Hash pro přidávání a párování s Platformou. // Je na QR kodu na hardwaru

    @JsonIgnore @Column(columnDefinition = "TEXT")
    public String json_bootloader_core_configuration; // DM_Board_Configuration.java Počáteční konfigurace - kde je uložený JSON mapovaný pomocí Objektů konkrétního typu hardwaru  //

    // Parametry při výrobě a osazení a registraci
    @JsonIgnore public String batch_id;         // Výrobní šarže
    @JsonIgnore public boolean is_active;       // Příznak, že deska byla oživena a je použitelná v platformě
    @JsonIgnore public String mqtt_password;    // V BCryptu uložený UUID
    @JsonIgnore public String mqtt_username;    // V BCryptu uložený UUID

    // Parametry konfigurovate uživatelem z frontendu
    // Pozor v Controller_Board jsou parametry v metodě board_update_parameters používány
                 public boolean developer_kit;
    @JsonIgnore  public boolean backup_mode;            // True znamená automatické zálohování po 5* minutách po nahrátí - verze se označuje mezi hardwarářema jako stabilní  - Opakej je statický backup - vázaný na objekty
                                                        // Pozor tato hodna se také propisuje do json_bootloader_core_configuration!!!!
                 public boolean database_synchronize;   // Defautlní hodnota je True. Je možnost vypnout synchronizaci a to pro případy, že uživatel vypaluje firmwaru lokálně pomocí svého programaátoru a IIDE

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_HardwareType hardware_type; // Typ desky - (Cachováno)

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_CProgramVersion actual_c_program_version;           // OVěřená verze firmwaru, která na hardwaru běží (Cachováno)
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_CProgramVersion actual_backup_c_program_version;    // Ověřený statický backup - nebo aktuálně zazálohovaný firmware (Cachováno)
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_BootLoader actual_boot_loader;              // Aktuální bootloader (Cachováno)

    // Hardware Groups
    @JsonIgnore @ManyToMany(fetch = FetchType.LAZY)  public List<Model_HardwareGroup> hardware_groups = new ArrayList<>();

    @JsonIgnore @OneToOne(fetch = FetchType.LAZY)  public Model_Blob picture;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;

    @JsonIgnore @OneToMany(mappedBy = "hardware", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_HardwareUpdate> updates = new ArrayList<>();



    @JsonIgnore public UUID connected_server_id;      // Latest know Server ID
    @JsonIgnore public UUID connected_instance_id;    // Latest know Instance ID



/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    // For Faster reload
    @JsonIgnore @Transient @Cached public Long cache_latest_online;
    @JsonIgnore @Transient @Cached public UUID cache_hardware_type_id;                        // Type of Board Id
    @JsonIgnore @Transient @Cached public String cache_hardware_type_name;
    @JsonIgnore @Transient @Cached public UUID cache_producer_id;                             // Producer ID
    @JsonIgnore @Transient @Cached public UUID cache_project_id;                              // Project
    @JsonIgnore @Transient @Cached public UUID cache_actual_boot_loader_id;                   // Bootloader
    @JsonIgnore @Transient @Cached public UUID cache_actual_c_program_id;                     // C Program
    @JsonIgnore @Transient @Cached public UUID cache_actual_c_program_version_id;             // C Program - version
    @JsonIgnore @Transient @Cached public UUID cache_actual_c_program_backup_id;              // Backup
    @JsonIgnore @Transient @Cached public UUID cache_actual_c_program_backup_version_id;      // Backup - version
    @JsonIgnore @Transient @Cached public UUID cache_harware_update_update_in_progress_bootloader_id;      // HardwareUpdate for bootloade
    @JsonIgnore @Transient @Cached public String cache_latest_know_ip_address;
    @Transient @JsonIgnore @Cached public List<UUID> cache_hardware_groups_ids;
    @JsonIgnore @Transient @Cached public UUID cache_picture_id;

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty public BackupMode backup_mode() { return backup_mode ? BackupMode.AUTO_BACKUP : BackupMode.STATIC_BACKUP;}
    @JsonProperty public UUID hardware_type_id()                     { try { return cache_hardware_type_id != null ? cache_hardware_type_id : getHardwareType().id;} catch (NullPointerException e) {return  null;}}
    @JsonProperty public String hardware_type_name()                 { try { return cache_hardware_type_name != null ? cache_hardware_type_name : getHardwareType().name;} catch (NullPointerException e) {return  null;}}
    @JsonProperty public UUID producer_id()                          { try { return cache_producer_id != null ? cache_producer_id : get_producer().id;} catch (NullPointerException e) {return  null;}}
    @JsonProperty public String producer_name()                      { try { return get_producer().name; } catch (NullPointerException e) {return  null;}}
    @JsonProperty public UUID project_id()                           { try { return cache_project_id != null ? cache_project_id : get_project().id; } catch (NullPointerException e) {return  null;}}
    @JsonProperty public Model_BootLoader actual_bootloader()        {
        try {
            return get_actual_bootloader();
        } catch (NullPointerException e) {
            return  null;
        }
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_BootLoader bootloader_update_in_progress() {
        try {

           if(cache_harware_update_update_in_progress_bootloader_id != null) {

               Model_HardwareUpdate plan_not_cached = Model_HardwareUpdate.find.query().where().eq("hardware.id", this.id).eq("firmware_type", FirmwareType.BOOTLOADER.name())
                       .disjunction()
                       .add(Expr.eq("state", HardwareUpdateState.NOT_YET_STARTED))
                       .add(Expr.eq("state", HardwareUpdateState.IN_PROGRESS))
                       .add(Expr.eq("state", HardwareUpdateState.WAITING_FOR_DEVICE))
                       .add(Expr.eq("state", HardwareUpdateState.INSTANCE_INACCESSIBLE))
                       .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE))
                       .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_NEVER_CONNECTED))
                       .endJunction()
                       .eq("firmware_type", FirmwareType.BOOTLOADER)
                       .le("actualization_procedure.date_of_planing", new Date())
                       .order().desc("actualization_procedure.date_of_planing")
                       .select("id")
                       .setMaxRows(1)
                       .findOne();


               if (plan_not_cached != null) {
                   cache_harware_update_update_in_progress_bootloader_id = plan_not_cached.getBootloader().id;
               }

           }

           if(cache_harware_update_update_in_progress_bootloader_id == null) return null;

           return Model_BootLoader.getById(cache_harware_update_update_in_progress_bootloader_id);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null; // Raději true než false aby to uživatel neodpálil další update
        }
    }

    @JsonProperty
    public Model_BootLoader available_latest_bootloader()  {
        try {
            return getHardwareType().main_boot_loader();
        }catch (Exception e) {
            logger.internalServerError(e);
            return null; // Raději true než false aby to uživatel neodpálil další update
        }
    }

    @JsonProperty
    public String ip_address() {
        try {

            if (cache_latest_know_ip_address != null) {
                return cache_latest_know_ip_address;
            } else {
                return null;
                // TODO - zjistit!!!! A předat frontendu!
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_Short_Reference dominant_project_active(){
        if(dominant_entity) return null;
        UUID uuid = Model_Project.find.query().where().eq("hardware.id", id).eq("dominant_entity", true).select("id").findSingleAttribute();
        if(uuid == null) return null;

        Model_Project project = Model_Project.getById(uuid);
        Swagger_Short_Reference reference = new Swagger_Short_Reference(project.id, project.name, project.description);
        return reference;
    }

    @JsonProperty
    @ApiModelProperty(required = false, readOnly = true)
    public List<BoardAlert> alert_list() {
        try {

            List<BoardAlert> list = new ArrayList<>();

            if (( available_latest_bootloader() != null && actual_bootloader() == null) || ( available_latest_bootloader() != null  && !actual_bootloader().id.equals(available_latest_bootloader().id) )) list.add(BoardAlert.BOOTLOADER_REQUIRED);

            return list;
        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonProperty
    @ApiModelProperty(required = false, readOnly = true, value = "Basic alerts for potential collisions when deploying or updating new programs")
    public BoardUpdateCollision collision(){
        if (connected_instance_id == null) {
            return BoardUpdateCollision.NO_COLLISION;
        } else {
            return BoardUpdateCollision.ALREADY_IN_INSTANCE;
        }
    }

    @JsonProperty
    public DM_Board_Bootloader_DefaultConfig bootloader_core_configuration() {
        try {

            if (json_bootloader_core_configuration == null || json_bootloader_core_configuration.equals("{}") || json_bootloader_core_configuration.equals("null") || json_bootloader_core_configuration.length() == 0) {
                json_bootloader_core_configuration = Json.toJson(DM_Board_Bootloader_DefaultConfig.generateConfig()).toString();
                this.update();
            }

            JsonNode node = Json.parse(json_bootloader_core_configuration);
            DM_Board_Bootloader_DefaultConfig config = baseFormFactory.formFromJsonWithValidation(DM_Board_Bootloader_DefaultConfig.class, node);

            // Manuálně doplněné hodnoty - ty které nejsou ve statickém Json
            config.autobackup = backup_mode;

            return config;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    public List<Model_HardwareUpdate> required_updates() {

        try {

            List<Model_HardwareUpdate> c_program_plans = Model_HardwareUpdate.find.query().where()
                    .disjunction()
                    .eq("state", HardwareUpdateState.IN_PROGRESS)
                    .eq("state", HardwareUpdateState.WAITING_FOR_DEVICE)
                    .eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE)
                    .eq("state", HardwareUpdateState.INSTANCE_INACCESSIBLE)
                    .endJunction()
                    .eq("hardware.id", id).order().asc("actualization_procedure.created").findList();

            return c_program_plans;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonProperty
    public Model_HomerServer server() { try { if (connected_server_id == null) return null; return Model_HomerServer.getById(connected_server_id); } catch (Exception e) {logger.internalServerError(e); return null;}}

    @JsonProperty @ApiModelProperty(value = "Can be null, if device is not in Instance")
    public Model_Instance actual_instance() {
        return get_instance();
    }

    @JsonProperty
    public UUID actual_c_program_id() { return cache_actual_c_program_id != null ? cache_actual_c_program_id : (get_actual_c_program() == null ? null : get_actual_c_program().id);}

    @JsonProperty
    public String actual_c_program_name() { return get_actual_c_program() == null ? null : get_actual_c_program().name;}

    @JsonProperty
    public String actual_c_program_description() { return get_actual_c_program() == null ? null :  get_actual_c_program().description;}

    @JsonProperty
    public UUID actual_c_program_version_id() { return cache_actual_c_program_version_id != null ? cache_actual_c_program_version_id : (get_actual_c_program_version() == null ? null : get_actual_c_program_version().id);}

    @JsonProperty
    public String actual_c_program_version_name() { return get_actual_c_program_version() == null ? null : get_actual_c_program_version().name;}

    @JsonProperty
    public String actual_c_program_version_description() {  return get_actual_c_program_version() == null ? null : get_actual_c_program_version().description;}

    @JsonProperty
    public UUID actual_c_program_backup_id() {  return cache_actual_c_program_backup_id != null ? cache_actual_c_program_backup_id : (get_backup_c_program() == null ? null : get_backup_c_program().id); }

    @JsonProperty
    public String actual_c_program_backup_name() { return get_backup_c_program() == null ? null : get_backup_c_program().name;}

    @JsonProperty
    public String actual_c_program_backup_description() { return get_backup_c_program() == null ? null : get_backup_c_program().description;}

    @JsonProperty
    public UUID actual_c_program_backup_version_id() { return cache_actual_c_program_backup_version_id != null ? cache_actual_c_program_backup_version_id : (get_backup_c_program_version() == null ? null :  get_backup_c_program_version().id) ;}

    @JsonProperty
    public String actual_c_program_backup_version_name() { return get_backup_c_program_version() == null ? null :  get_backup_c_program_version().name; }

    @JsonProperty
    public String actual_c_program_backup_version_description() {return get_backup_c_program_version() == null ? null :  get_backup_c_program_version().description;}

    @JsonProperty
    @ApiModelProperty(value = "Value is null, if device status is online")
    public Long latest_online() {
        if (online_state() == NetworkStatus.ONLINE) return null;
        try {

            if (cache_latest_online != null) {
                return cache_latest_online;
            }

            List<Document> documents = Server.documentClient.queryDocuments(Server.online_status_collection.getSelfLink(),"SELECT * FROM root r  WHERE r.hardware_id='" + this.id + "' AND r.document_type_sub_type='DEVICE_DISCONNECT'", null).getQueryIterable().toList();

            logger.debug("last_online: number of retrieved documents = {}", documents.size());

            if (documents.size() > 0) {

                DM_Board_Disconnected record;

                if (documents.size() > 1) {

                    logger.debug("last_online: more than 1 record, finding latest record");
                    record = documents.stream().max(Comparator.comparingLong(document -> document.toObject(DM_Board_Disconnected.class).time)).get().toObject(DM_Board_Disconnected.class);

                } else {

                    logger.debug("last_online: result = {}", documents.get(0).toJson());

                    record = documents.get(0).toObject(DM_Board_Disconnected.class);
                }

                logger.debug("last_online: hardware_id: {}", record.hardware_id);

                cache_latest_online = new Date(record.time).getTime();
                return cache_latest_online;
            }

            return Long.MAX_VALUE;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    @ApiModelProperty(value = "Value is cached with asynchronous refresh")
    public NetworkStatus online_state() {
        try {

            if(!dominant_entity) {
                return NetworkStatus.FREEZED;
                // Pokud FREEZED tak bych měl vrátit i kde je Hardware Aktivní pro Becki!
            }

            // Pokud Tyrion nezná server ID - to znamená deska se ještě nikdy nepřihlásila - chrání to proti stavu "během výroby"
            // i stavy při vývoji kdy se tvoří zběsile nové desky na dev serverech
            if (connected_server_id == null) {
                return NetworkStatus.NOT_YET_FIRST_CONNECTED;
            }

            // Pokud je server offline - tyrion si nemuže být jistý stavem hardwaru - ten teoreticky muže být online
            // nebo také né - proto se vrací stav Enum_Online_status - na to reaguje parameter latest_online(),
            // který následně vrací latest know online

            if (Model_HomerServer.getById(connected_server_id).online_state() == NetworkStatus.ONLINE) {

                if (cache_status.containsKey(id)) {
                    return cache_status.get(id) ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE;
                } else {
                    // Začnu zjišťovat stav - v separátním vlákně!
                    new Thread(() -> {
                        try {
                            write_without_confirmation(new WS_Message_Hardware_online_status().make_request(Collections.singletonList(full_id)));
                        } catch (Exception e) {
                            logger.internalServerError(e);
                        }
                    }).start();

                    return NetworkStatus.SYNCHRONIZATION_IN_PROGRESS;
                }
            } else {
                return NetworkStatus.UNKNOWN_LOST_CONNECTION_WITH_SERVER;
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            return NetworkStatus.OFFLINE;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public List<Model_HardwareGroup> hardware_groups() {
        try {
            List<Model_HardwareGroup> l = new ArrayList<>();
            for (Model_HardwareGroup m : get_hardware_groups())
                l.add(m);
            return l;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public String picture_link() {
        try {

            if ( cache_picture_id == null) {

                Model_Blob fileRecord = Model_Blob.find.query().where().eq("hardware.id",id).select("id").findOne();
                if (fileRecord != null) {
                    cache_picture_id =  fileRecord.id;
                }
            }

            if (cache_picture_id != null) {
                Model_Blob record = Model_Blob.getById(cache_picture_id);
                if (record != null) {
                    return record.getPublicDownloadLink(300);
                }
            }

            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    /* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_HomerServer get_connected_server() { return Model_HomerServer.getById(this.connected_server_id);}

    @JsonIgnore
    public Model_Producer get_producer() {

        if (cache_producer_id == null) {

            Model_Producer producer = Model_Producer.find.query().where().eq("hardware_types.hardware.id", id).select("id").findOne();
            if (producer == null) return null;
            cache_producer_id = producer.id;
        }

        return Model_Producer.getById(cache_producer_id);
    }

    @JsonIgnore
    public Model_Instance get_instance() {
        try {
            if (connected_instance_id == null) return null;
            return Model_Instance.getById(connected_instance_id);
        } catch (Exception e) {
            return null;
        }
    }

    @JsonIgnore
    public Model_CProgram get_actual_c_program(){
        try {
            if (cache_actual_c_program_id == null) {
                Model_CProgram program = Model_CProgram.find.query().where().eq("versions.c_program_version_boards.id", id).select("id").findOne();
                if (program == null) return null;
                cache_actual_c_program_id = program.id;
            }
            return Model_CProgram.getById(cache_actual_c_program_id);
        }catch (Exception e){
            return null;
        }
    }

    @JsonIgnore
    public Model_CProgramVersion get_actual_c_program_version(){
        try {
            if (cache_actual_c_program_version_id == null) {
                Model_CProgramVersion version = Model_CProgramVersion.find.query().where().eq("c_program_version_boards.id", id).select("id").findOne();
                if (version == null) throw new Result_Error_NotFound(Model_CProgramVersion.class);
                cache_actual_c_program_version_id = version.id;
            }
            return Model_CProgramVersion.getById(cache_actual_c_program_version_id);
        }catch (Exception e){
            return null;
        }
    }

    @JsonIgnore
    public Model_BootLoader get_actual_bootloader() {
        try {

            if (cache_actual_boot_loader_id == null) {
                Model_BootLoader bootLoader = Model_BootLoader.find.query().where().eq("hardware.id", id).select("id").findOne();
                if (bootLoader == null) return null;
                cache_actual_boot_loader_id = bootLoader.id;
            }

            return Model_BootLoader.getById(cache_actual_boot_loader_id);

        }catch (Exception e){
            return null;
        }
    }

    @JsonIgnore
    public Model_CProgram get_backup_c_program() {
        try {
            if (cache_actual_c_program_backup_id == null) {
                Model_CProgram program = Model_CProgram.find.query().where().eq("versions.c_program_version_backup_boards.id", id).select("id").findOne();
                if (program == null) return null;
                cache_actual_c_program_backup_id = program.id;
            }

            return Model_CProgram.getById(cache_actual_c_program_backup_id);
        } catch (Exception e){
            return null;
        }
    }

    @JsonIgnore
    public Model_CProgramVersion get_backup_c_program_version() {
        try {
            if (cache_actual_c_program_backup_version_id == null) {
                Model_CProgramVersion version = Model_CProgramVersion.find.query().where().eq("c_program_version_backup_boards.id", id).select("id").findOne();
                if (version == null) return null;
                cache_actual_c_program_backup_version_id = version.id;
            }

            return Model_CProgramVersion.getById(cache_actual_c_program_backup_version_id);

        }catch (Exception e){
            return null;
        }
    }

    @JsonIgnore
    public Model_HardwareType getHardwareType() {
        try {
            if (cache_hardware_type_id == null) {
                Model_HardwareType hardwareType = Model_HardwareType.find.query().where().eq("hardware.id", id).select("id").select("name").findOne();
                if (hardwareType == null) return null;
                cache_hardware_type_id = hardwareType.id;
                cache_hardware_type_name = hardwareType.name;
            }

            return Model_HardwareType.getById(cache_hardware_type_id);
        }catch (Exception e){
            return null;
        }
    }

    @JsonIgnore
    public Model_Project get_project() {
        try {
            if (cache_project_id == null) {
                return Model_Project.getById(get_project_id());
            }

            return Model_Project.getById(cache_project_id);
        }catch (Exception e){
            return  null;
        }
    }

    @JsonIgnore
    public UUID get_project_id() {
        try {

            if (cache_project_id == null) {
                Model_Project project = Model_Project.find.query().where().eq("hardware.id", id).select("id").findOne();
                if (project == null) throw new Result_Error_NotFound(Model_Project.class);
                cache_project_id = project.id;
            }

            return cache_project_id;
        }catch (Exception e) {
            return null;
        }
    }

    @JsonIgnore
    public List<Model_HardwareGroup> get_hardware_groups() {

        if (cache_hardware_groups_ids == null) {
            get_hardware_group_ids();
        }

        List<Model_HardwareGroup> groups = new ArrayList<>();

        for(UUID group_id : cache_hardware_groups_ids) {
            groups.add(Model_HardwareGroup.getById(group_id));
        }

        return groups;
    }

    @JsonIgnore
    public List<UUID> get_hardware_group_ids() {

        if (cache_hardware_groups_ids == null) {

            List<UUID> group_ids = Model_HardwareGroup.find.query().where().eq("hardware.id", id).select("id").findIds();
            this.cache_hardware_groups_ids = group_ids;
            return cache_hardware_groups_ids;
        } else {
            return cache_hardware_groups_ids;
        }
    }

    @JsonIgnore
    public boolean update_boot_loader_required() {
        if (getHardwareType().main_boot_loader == null || get_actual_bootloader() == null) return true;
        return (!this.getHardwareType().main_boot_loader.id.equals(get_actual_bootloader().id));
    }

/* JSON IGNORE  --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void update_bootloader_configuration(DM_Board_Bootloader_DefaultConfig configuration) {
        this.json_bootloader_core_configuration = Json.toJson(configuration).toString();
        this.update();
    }

/* SERVER WEBSOCKET ----------------------------------------------------------------------------------------------------*/

    public static final String CHANNEL = "hardware";

    // Messenger
    public static void Messages(WS_Homer homer, ObjectNode json) throws _Base_Result_Exception {
        new Thread(() -> {
            try {

                switch (json.get("message_type").asText()) {

                    case WS_Message_Hardware_connected.message_type: {

                        Model_Hardware.device_Connected(baseFormFactory.formFromJsonWithValidation(homer, WS_Message_Hardware_connected.class, json));
                        return;
                    }

                    case WS_Message_Hardware_disconnected.message_type: {

                        Model_Hardware.device_Disconnected(baseFormFactory.formFromJsonWithValidation(homer, WS_Message_Hardware_disconnected.class, json));
                        return;
                    }

                    case WS_Message_Hardware_online_status.message_type: {

                        Model_Hardware.device_online_synchronization(baseFormFactory.formFromJsonWithValidation(homer, WS_Message_Hardware_online_status.class, json));
                        return;
                    }

                    case WS_Message_Hardware_autobackup_made.message_type: {

                        Model_Hardware.device_auto_backup_done_echo(baseFormFactory.formFromJsonWithValidation(homer, WS_Message_Hardware_autobackup_made.class, json));
                        return;
                    }

                    case WS_Message_Hardware_autobackup_making.message_type: {

                        Model_Hardware.device_auto_backup_start_echo(baseFormFactory.formFromJsonWithValidation(homer, WS_Message_Hardware_autobackup_making.class, json));
                        return;
                    }

                    case WS_Message_Hardware_UpdateProcedure_Progress.message_type: {

                        Model_HardwareUpdate.update_procedure_progress(baseFormFactory.formFromJsonWithValidation(homer, WS_Message_Hardware_UpdateProcedure_Progress.class, json));
                        return;
                    }

                    case WS_Message_Hardware_validation_request.message_type: {

                        Model_Hardware.check_mqtt_hardware_connection_validation(homer, baseFormFactory.formFromJsonWithValidation(homer, WS_Message_Hardware_validation_request.class, json));
                        return;
                    }

                    case WS_Message_Hardware_terminal_logger_validation_request.message_type: {

                        Model_Hardware.check_hardware_logger_access_terminal_validation(homer, baseFormFactory.formFromJsonWithValidation(homer, WS_Message_Hardware_terminal_logger_validation_request.class, json));
                        return;
                    }

                    // Ignor messages - Jde pravděpodobně o zprávy - které přišly s velkým zpožděním - Tyrion je má ignorovat
                    case WS_Message_Hardware_set_settings.message_type     : {logger.warn("WS_Message_Hardware_set_settings: A message with a very high delay has arrived.");return;}
                    case WS_Message_Hardware_command_execute.message_type  : {logger.warn("WS_Message_Hardware_Restart: A message with a very high delay has arrived.");return;}
                    case WS_Message_Hardware_overview.message_type         : {logger.warn("WS_Message_Hardware_overview: A message with a very high delay has arrived.");return;}
                    case WS_Message_Hardware_change_server.message_type    : {logger.warn("WS_Message_Hardware_change_server: A message with a very high delay has arrived.");return;}

                    default: {

                        logger.error("Incoming Message not recognized::" + json.toString());

                        // Zarážka proti nevadliní odpovědi a zacyklení
                        if (json.has("status") && json.get("status").asText().equals("error")) {
                            return;
                        }

                        homer.send(json.put("error_message", "message_type not recognized").put("error_code", 400));
                    }
                }

            } catch (Exception e) {
                if (!json.has("message_type")) {
                    homer.send(json.put("error_message", "Your message not contains message_type").put("error_code", 400));
                    return;
                } else {
                    logger.internalServerError(e);
                }
            }
        }).start();
    }

    @JsonIgnore
    public String get_full_id() {return full_id;}

    // Kontrola připojení - Echo o připojení
    @JsonIgnore
    public static void device_Connected(WS_Message_Hardware_connected help) {
        try {

            logger.debug("master_device_Connected:: Updating device ID:: {} is online ", help.hardware_id);

            Model_Hardware device = Model_Hardware.getByFullId(help.hardware_id);

            if (device == null) {
                logger.warn("Hardware not found. Message from Homer server: ID = " + help.websocket_identificator + ". Unregistered Hardware Id: " + help.hardware_id);
                return;
            }

            // Aktualizuji cache status online HW
            cache_status.put(device.id, Boolean.TRUE);

            if (device.project_id() == null) {
                logger.warn("device_Connected - hardware {} is not in project", device.id);
            }

            // Notifikce
            if (device.developer_kit) {
                try {
                    device.notification_board_connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Standartní synchronizace s Becki - že je device online - pokud někdo na frotnendu (uživatel) poslouchá
            if (device.project_id() != null) {
                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Hardware.class, device.id, true, device.project_id());
            }

            // Nastavím server_id - pokud nekoresponduje s tím, který má HW v databázi uložený
            if (help.websocket_identificator != null && ( device.connected_server_id == null || !device.connected_server_id.equals(help.websocket_identificator))) {
                logger.debug("master_device_Connected:: Changing server id property to {} ", help.websocket_identificator);
                device.connected_server_id = help.websocket_identificator;
                device.update();
            }

            device.make_log_connect();

            // ZDe do budoucna udělat synchronizaci jen když to bude opravdu potřeba - ale momentálně je nad lidské síly udělat argoritmus,
            // který by vyřešil zbytečné dotazování
            device.hardware_firmware_state_check();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    // Echo o odpojení
    @JsonIgnore
    public static void device_Disconnected(WS_Message_Hardware_disconnected help) {
        try {


            Model_Hardware device =  Model_Hardware.getByFullId(help.full_id);

            if (device == null) {
                logger.warn("device_Disconnected:: Hardware not recognized: ID = {} ", help.full_id);
                return;
            }

            logger.debug("master_device_Disconnected:: Updating device status " +  help.full_id + " on offline ");

            // CHACHE OFFLINE
            cache_status.put(device.id, Boolean.FALSE);


            // Uprava Cache Paměti
            device.cache_latest_online = new Date().getTime();


            // Standartní synchronizace
            if (device.project_id() != null) {
                    WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Hardware.class, device.id, false, device.project_id());
            }

            if (device.developer_kit) {
                // Notifikace
                device.notification_board_disconnect();
             }

            // Záznam do DM databáze
            device.make_log_disconnect();

            Model_Hardware.cache_status.put(device.id, false);

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    // Device dělá autobackup
    @JsonIgnore
    public static void device_auto_backup_start_echo(WS_Message_Hardware_autobackup_making help) {
        try {

            logger.debug("device_auto_backup_echo:: Device send Echo about making backup on device ID:: {} ", help.full_id);

            Model_Hardware device = Model_Hardware.getByFullId(help.full_id);

            if (device == null) {
                logger.warn("device_Disconnected:: Hardware not recognized: ID = {} ", help.full_id);
                return;
            }

            if (device.developer_kit) {
                // TODO notification
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    // Device udělal autobackup
    @JsonIgnore
    public static void device_auto_backup_done_echo(WS_Message_Hardware_autobackup_made help) {
        try {

            logger.debug("device_auto_backup_done_echo:: Device send Echo about backup done on device ID:: {} ", help.full_id);

            Model_Hardware device = Model_Hardware.getByFullId(help.full_id);

            if (device == null) {
                logger.warn("device_Disconnected:: Hardware not recognized: ID = {} ", help.full_id);
                return;
            }
            Model_CProgramVersion c_program_version = Model_CProgramVersion.find.query().where().eq("compilation.firmware_build_id", help.build_id).select("id").findOne();
            if (c_program_version == null) throw new Exception("Firmware with build ID = " + help.build_id + " was not found in the database!");

            device.actual_backup_c_program_version = c_program_version;
            device.update();

            if (device.developer_kit) {
                // TODO notification
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    // žádost void o synchronizaci online stavu
    @JsonIgnore
    public static void device_online_synchronization(WS_Message_Hardware_online_status report) {
        try {

            for (WS_Message_Hardware_online_status.DeviceStatus status : report.hardware_list) {

                cache_status.put(status.hardware_id, status.online_status);

                // Odešlu echo pomocí websocketu do becki
                Model_Hardware device = getById(status.hardware_id);

                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Hardware.class, device.id, status.online_status, device.project_id());
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }


    @JsonIgnore
    public static void check_mqtt_hardware_connection_validation(WS_Homer homer, WS_Message_Hardware_validation_request request) {
        try {

            Model_Hardware board = request.get_hardware();

            if (BCrypt.checkpw(request.password, board.mqtt_password) && BCrypt.checkpw(request.user_name, board.mqtt_username)) {
                homer.send(request.get_result(true));
            } else {
                homer.send(request.get_result(false));
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public static void check_hardware_logger_access_terminal_validation(WS_Homer homer, WS_Message_Hardware_terminal_logger_validation_request request) {
        try {

            UUID project_id = null;

            for (String full_id : request.full_ids) {

                Model_Hardware board =  Model_Hardware.getByFullId(full_id);
                if (board == null) {
                    homer.send(request.get_result(false));
                    return;
                }

                if (project_id != null  && project_id.equals(board.project_id())) continue;
                if (project_id != null && !project_id.equals(board.project_id())) project_id = null;

                // P5edpokládá se že project_id bude u všech desek stejné - tak se podle toho bude taky kontrolovat
                if (project_id == null) {
                    Model_Person person = Model_Person.getByAuthToken(request.token);
                    if (person == null) {
                        homer.send(request.get_result(false));
                        return;
                    }

                    Model_Project project = Model_Project.find.query().where().eq("participants.person.id", person.id).eq("id", board.project_id()).findOne();

                    if (project == null) {
                        homer.send(request.get_result(false));
                        return;
                    }

                    project_id = project.id;
                }
            }

            homer.send(request.get_result(true));

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    /* Servers Parallel tasks  ----------------------------------------------------------------------------------------------*/

    /**
     * Jelikož je nutné vrstvou zakrýt dotazování na devices, které jsou na různých server
     * a jelikož dotazování má fungovat paralelně, slouží následující metoda k tomu, že jednoltivým serverům paralelně
     * rozešle příkaz a čeká na splnění všech maximální možnou dobu. Poté odpovědi sbalí a dá do pole, které vrátí.
     *
     * Každé vlákno se nakonci podívá, kolik je hotovo, když je hotovo vše - vrací vlákno které má shodné číslo kolekci.
     *
     * @param get_result_from_servers
     * @return JsonNode
     */
     private static List<JsonNode> server_parallel(HashMap<UUID, ObjectNode> get_result_from_servers, Integer time, Integer delay, Integer number_of_retries) {
        try {

            List<Callable<ObjectNode>> callables = new ArrayList<Callable<ObjectNode>>();

            for (UUID server_id : get_result_from_servers.keySet()) {

                 Callable<ObjectNode> callable = new ParallelTask(server_id, get_result_from_servers.get(server_id), time, delay, number_of_retries);
                 callables.add(callable);
            }

            ExecutorService executor = Executors.newWorkStealingPool();

            List<JsonNode> results = new ArrayList<>();

            executor.invokeAll(callables).forEach(future -> {
                try {
                    results.add(future.get());
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });

            return results;

        } catch (Exception e) {
            return null;
        }
    }

    private static HashMap<UUID, List<String>> server_Separate(List<Model_Hardware> devices) {

        HashMap<UUID, List<String>> serverHashMap = new HashMap<>(); // < Model_HomerServer.id, List<Model_Board.id>

        // Separate Board Acording servers
        for (Model_Hardware device : devices) {

            // Skip never unconnected device
            if (device.connected_server_id == null) continue;

            // If not collection exist -> create that
            if (!serverHashMap.containsKey(device.connected_server_id)) {serverHashMap.put(device.connected_server_id, new ArrayList<>());}

            // Add to collection
            serverHashMap.get(device.connected_server_id).add(device.full_id);
        }

        return serverHashMap;
    }

    // Odesílání zprávy harwaru jde skrze serve, zde je metoda, která pokud to nejde odeslat naplní objekt a vrácí ho
    @JsonIgnore
    public ObjectNode write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries) {

        // Response with Error Message
        if (this.connected_server_id == null) {

            ObjectNode request = Json.newObject();
            request.put("message_type", json.get("message_type").asText());
            request.put("message_channel", Model_Hardware.CHANNEL);
            request.put("error_code", ErrorCode.HOMER_SERVER_NOT_SET_FOR_HARDWARE.error_code());
            request.put("error_message", ErrorCode.HOMER_SERVER_NOT_SET_FOR_HARDWARE.error_message());
            request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
            request.put("websocket_identificator", "unknown");

            return request;
        }

        Model_HomerServer server = Model_HomerServer.getById(this.connected_server_id);
        if (server == null) {

            logger.internalServerError(new Exception("write_with_confirmation:: Hardware " + id + " has not exist server id " + this.connected_server_id + " and it wll be removed!"));

            this.connected_server_id = null;
            this.update();

            ObjectNode request = Json.newObject();
            request.put("message_type", json.get("message_type").asText());
            request.put("message_channel", Model_Hardware.CHANNEL);
            request.put("error_code", ErrorCode.HOMER_NOT_EXIST.error_code());
            request.put("error_message", ErrorCode.HOMER_NOT_EXIST.error_message());
            request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
            request.put("websocket_identificator", "unknown");

            return request;
        }
        return server.write_with_confirmation(json, time, delay, number_of_retries);
    }

    // Metoda překontroluje odeslání a pak předává objektu - zpráva plave skrze program
    @JsonIgnore
    public void write_without_confirmation(ObjectNode json) {

        if (this.connected_server_id == null) {
            return;
        }

        Model_HomerServer server = Model_HomerServer.getById(this.connected_server_id);

        if (server == null) {

            logger.internalServerError(new Exception("write_without_confirmation:: Hardware " + id + " has not exist server id " + this.connected_server_id + " and it wll be removed!"));

            this.connected_server_id = null;
            this.update();

            return;
        }
        server.write_without_confirmation(json);
    }

    // Metoda překontroluje odeslání a pak předává objektu - zpráva plave skrze program
    @JsonIgnore public void write_without_confirmation(String message_id, ObjectNode json) {

        if (this.connected_server_id == null) {
            return;
        }

        Model_HomerServer server = Model_HomerServer.getById(this.connected_server_id);

        if (server == null) {

            logger.internalServerError(new Exception("write_without_confirmation::message_id " + message_id + " Hardware " + id + " has not exist server id " + this.connected_server_id + " and it wll be removed!"));

            this.connected_server_id = null;
            this.update();

            return;
        }
        server.write_without_confirmation(message_id, json);
    }

    /* Commands ----------------------------------------------------------------------------------------------*/

    //-- Online State Hardware  --//
    @JsonIgnore @Transient public WS_Message_Hardware_online_status get_devices_online_state() {
        return get_devices_online_state(Collections.singletonList(this));
    }

    @JsonIgnore @Transient public static WS_Message_Hardware_online_status get_devices_online_state(List<Model_Hardware> devices) {

        // Sepparate Device by servers
        HashMap<UUID, List<String>> serverHashMap = server_Separate(devices);

        // Create Server Parralell command
        HashMap<UUID, ObjectNode> request_collection = new HashMap<>();
        for (UUID key : serverHashMap.keySet()) {
            request_collection.put(key, new WS_Message_Hardware_online_status().make_request(serverHashMap.get(key)));
        }

        // Make Parallel Operation
        List<JsonNode> results = server_parallel(request_collection,1000 * 10, 0, 1 );

        // Common Result for fill
        WS_Message_Hardware_online_status result = new WS_Message_Hardware_online_status();

        for (JsonNode json_result : results ) {
            try {
                result.hardware_list.addAll(baseFormFactory.formFromJsonWithValidation(WS_Message_Hardware_online_status.class, json_result).hardware_list);

            } catch (Exception e) {
                logger.internalServerError(e);
                return new WS_Message_Hardware_online_status();
            }
        }
        return result;
    }

    //-- Over View Hardware  --//
    @JsonIgnore
    public WS_Message_Hardware_overview_Board get_devices_overview() {

        WS_Message_Hardware_overview overview = get_devices_overview(Collections.singletonList(this));
        if (overview.get_device_from_list(full_id) != null) {
            return overview.get_device_from_list(full_id);
        }

        return new WS_Message_Hardware_overview_Board();
    }

    public static WS_Message_Hardware_overview get_devices_overview(List<Model_Hardware> devices) {

        // Separate Device by servers
        HashMap<UUID, List<String>> serverHashMap = server_Separate(devices);

        // Create Server Parallel command
        HashMap<UUID, ObjectNode> request_collection = new HashMap<>();
        for (UUID key : serverHashMap.keySet()) {
            request_collection.put(key, new WS_Message_Hardware_overview().make_request(serverHashMap.get(key)) );
        }

        // Make Parallel Operation
        List<JsonNode> results = server_parallel(request_collection,1000 * 10, 0, 1 );

        // Common Result for fill
        WS_Message_Hardware_overview result = new WS_Message_Hardware_overview();
        result.status = "success";

        for (JsonNode json_result : results) {
            try {

                WS_Message_Hardware_overview overview = baseFormFactory.formFromJsonWithValidation(WS_Message_Hardware_overview.class, json_result);

                // TODO should the exception be really thrown here? [AT]
                if (!overview.status.equals("success") && overview.hardware_list.isEmpty()) {
                    throw new Exception("WS_Message_Hardware_overview: Status is not success or hw list is empty");
                }

                result.hardware_list.addAll(overview.hardware_list);

            } catch (Exception e) {
                logger.internalServerError(e);
                return new WS_Message_Hardware_overview();
            }
        }

        return result;
    }


    // Change Hardware Alias  --//GRID Apps
    @JsonIgnore
    public WS_Message_Hardware_set_settings set_alias(String alias) {

        try {

            if (!this.name.equals(alias)) {
                this.name = alias;
                this.update();
            }

            JsonNode node = write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this), "alias", alias), 1000 * 5, 0, 2);
            return baseFormFactory.formFromJsonWithValidation(WS_Message_Hardware_set_settings.class, node);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Hardware_set_settings();
        }
    }

    // Change Hardware autosynchronize --//
    @JsonIgnore @Transient public WS_Message_Hardware_set_settings set_database_synchronize(boolean settings) {
        try {

            if (this.database_synchronize != settings) {
                this.database_synchronize = settings;
                this.update();
            }

            // Homer by měl přestat do odvolání posílat všechny sračky co se dějí na hardwaru - ale asi to ještě není implementováno // TODO??
            JsonNode node = write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this),"DATABASE_SYNCHRONIZE", settings), 1000 * 5, 0, 2);
            return baseFormFactory.formFromJsonWithValidation(WS_Message_Hardware_set_settings.class, node);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Hardware_set_settings();
        }
    }

    // Set Hardware groups in Hardware Instance on Homer --//
    @JsonIgnore
    public WS_Message_Hardware_set_hardware_groups set_hardware_groups_on_hardware(List<UUID> hardware_groups_ids, Enum_type_of_command command) {
        try {

            JsonNode node = write_with_confirmation(new WS_Message_Hardware_set_hardware_groups().make_request(Collections.singletonList(this), hardware_groups_ids, command), 1000 * 5, 0, 2);

            return baseFormFactory.formFromJsonWithValidation(WS_Message_Hardware_set_hardware_groups.class, node);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Hardware_set_hardware_groups();
        }
    }

    // Change Hardware web view port --//
    @JsonIgnore
    public WS_Message_Hardware_set_settings set_hardware_configuration_parameter(Swagger_Board_Developer_parameters help) throws IllegalArgumentException, Exception{

            DM_Board_Bootloader_DefaultConfig configuration = this.bootloader_core_configuration();

            for (Field field : configuration.getClass().getFields()) {

                if (help.parameter_type.toLowerCase().equals(field.getName().toLowerCase())) {

                    if (field.getType().getSimpleName().equals(Boolean.class.getSimpleName().toLowerCase())) {

                        // Jediná přístupná vyjímka je pro autoback - ten totiž je zároven v COnfig Json (DM_Board_Bootloader_DefaultConfig)
                        // Ale zároveň je také přímo přístupný v databázi Tyriona
                        if (help.parameter_type.equals("autobackup")) {
                            this.backup_mode = help.boolean_value;
                            // Update bude proveden v   this.update_bootloader_configuration
                        }

                        field.setBoolean(configuration, help.boolean_value); //setting field value to 10 in object
                        this.update_bootloader_configuration(configuration);

                        JsonNode node = write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this), field.getName(), help.boolean_value), 1000 * 5, 0, 2);
                        return baseFormFactory.formFromJsonWithValidation(WS_Message_Hardware_set_settings.class, node);
                    }

                    if (field.getType().getSimpleName().toLowerCase().equals(String.class.getSimpleName().toLowerCase())) {

                        field.set(configuration, help.string_value);
                        this.update_bootloader_configuration(configuration);

                        JsonNode node = write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this), field.getName(), help.string_value), 1000 * 5, 0, 2);
                        return baseFormFactory.formFromJsonWithValidation(WS_Message_Hardware_set_settings.class, node);
                    }

                    if (field.getType().getSimpleName().toLowerCase().equals(Integer.class.getSimpleName().toLowerCase())) {

                        try {
                            System.out.println("Jaká je integer value:: " + help.integer_value);
                            field.set(configuration, help.integer_value);
                            this.update_bootloader_configuration(configuration);

                            JsonNode node = write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this), field.getName(), help.integer_value), 1000 * 5, 0, 2);
                            return baseFormFactory.formFromJsonWithValidation(WS_Message_Hardware_set_settings.class, node);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            throw  new IllegalArgumentException("Incoming Value " + help.parameter_type.toLowerCase() + " not recognized");
    }

    //-- ADD or Remove // Change Server --//
    @JsonIgnore
    public void device_relocate_server(Model_HomerServer future_server) {
        List<Model_Hardware> devices = new ArrayList<>();
        devices.add(this);
        device_relocate_server(devices, future_server);
    }

    @JsonIgnore
    public WS_Message_Hardware_change_server device_relocate_server(String mqtt_host, String mqtt_port) {
        try {
            JsonNode node = write_with_confirmation( new WS_Message_Hardware_change_server().make_request(mqtt_host, mqtt_port, Collections.singletonList(this.full_id)), 1000 * 5, 0, 2);
            return baseFormFactory.formFromJsonWithValidation(WS_Message_Hardware_change_server.class, node);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Hardware_change_server();
        }
    }

    @JsonIgnore
    public void device_relocate_server(List<Model_Hardware> devices, Model_HomerServer future_server) {
        try {

            // Sepparate Device by servers
            HashMap<UUID, List<String>> serverHashMap = server_Separate(devices);

            // Create Server Parralell command
            HashMap<UUID, ObjectNode> request_collection = new HashMap<>();
            for (UUID key : serverHashMap.keySet()) {
                request_collection.put(key, new WS_Message_Hardware_change_server().make_request(future_server, serverHashMap.get(key)));
            }

            // Make Parallel Operation
            List<JsonNode> results = server_parallel(request_collection,1000 * 10, 0, 1);

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    //-- Set AutoBackup  --//
    @JsonIgnore
    public WS_Message_Hardware_set_settings set_auto_backup() {
        try {

            // 1) změna registru v configur
            DM_Board_Bootloader_DefaultConfig configuration = this.bootloader_core_configuration();
            configuration.autobackup = true;
            this.update_bootloader_configuration(configuration);
            // V databázi
            this.backup_mode = true;
            this.update();

            //Zabít všechny procedury kde je nastaven backup a ještě nebyly provedeny - ty co jsou teprve v plánu budou provedeny standartně
            List<Model_HardwareUpdate> firmware_plans = Model_HardwareUpdate.find.query().where().eq("hardware.id", this.id)
                    .disjunction()
                    .add(Expr.eq("state", HardwareUpdateState.NOT_YET_STARTED))
                    .add(Expr.eq("state", HardwareUpdateState.IN_PROGRESS))
                    .add(Expr.eq("state", HardwareUpdateState.WAITING_FOR_DEVICE))
                    .add(Expr.eq("state", HardwareUpdateState.INSTANCE_INACCESSIBLE))
                    .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE))
                    .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_NEVER_CONNECTED))
                    .endJunction()
                    .eq("firmware_type", FirmwareType.BACKUP.name())
                    .lt("actualization_procedure.date_of_planing", new Date())
                    .order().desc("actualization_procedure.date_of_planing")
                    .select("id")
                    .findList();

            // Zaloha kdyby byly stále platné aktualizace na backup
            for (int i = 0; i < firmware_plans.size(); i++) {
                firmware_plans.get(i).state = HardwareUpdateState.OBSOLETE;
                firmware_plans.get(i).update();
            }

            JsonNode node = write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this), "autobackup", true), 1000*5, 0, 2);

            return baseFormFactory.formFromJsonWithValidation(WS_Message_Hardware_set_settings.class, node);

        } catch (Exception e) {
            logger.internalServerError(e);
            return new WS_Message_Hardware_set_settings();
        }
    }

    ///-- Send Update Procedure to All Hardware's  --//
    @JsonIgnore
    public static void execute_update_plan(Model_HardwareUpdate plan) {
        logger.debug("execute_update_plan - start execution of plan: {}", plan.id );
        logger.debug("execute_update_plan - actual state: {} ", plan.state.name());

        if (plan.actualization_procedure.state == Enum_Update_group_procedure_state.complete || plan.actualization_procedure.state == Enum_Update_group_procedure_state.successful_complete ) {
            logger.debug("execute_update_plan - procedure: {} is done (successful_complete or complete) -> return", plan.actualization_procedure.id);
            return;
        }

        if (plan.actualization_procedure.updates.isEmpty()) {
            plan.actualization_procedure.state = Enum_Update_group_procedure_state.complete_with_error;
            plan.actualization_procedure.update();
            logger.debug("execute_update_plan - procedure: {} is empty -> return" , plan.actualization_procedure.id);
            return;
        }

        plan.actualization_procedure.state = Enum_Update_group_procedure_state.in_progress;
        plan.actualization_procedure.update();

        try {

            if ( plan.count_of_tries > 5 ) {
                plan.state = HardwareUpdateState.CRITICAL_ERROR;
                plan.error = ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_message();
                plan.error_code = ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_code();
                plan.update();
                logger.warn("execute_update_plan - Procedure id:: {} plan {} CProgramUpdatePlan:: Error:: {} Message:: {} Continue Cycle. " , plan.actualization_procedure.id , plan.id, ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_code() , ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_message());
                return;
            }

            if (plan.getHardware().connected_server_id == null) {
                plan.state = HardwareUpdateState.HOMER_SERVER_NEVER_CONNECTED;
                plan.update();
                return;
            }

            if (Model_HomerServer.getById(plan.getHardware().connected_server_id).online_state() != NetworkStatus.ONLINE) {
                logger.warn("execute_update_procedure - Procedure id:: {}  plan {}  Server {} is offline. Putting off the task for later. -> Return. ", plan.actualization_procedure.id , plan.id, Model_HomerServer.getById(plan.getHardware().connected_server_id).name);
                plan.state = HardwareUpdateState.HOMER_SERVER_IS_OFFLINE;
                plan.update();
                return;
            }

            plan.state = HardwareUpdateState.IN_PROGRESS;
            plan.update();

            Model_HomerServer.getById(plan.getHardware().connected_server_id).update_devices_firmware(Collections.singletonList(plan.get_brief_for_update_homer_server()));

        } catch (Exception e) {
            logger.internalServerError(e);
            plan.state = HardwareUpdateState.CRITICAL_ERROR;
            plan.error_code = ErrorCode.CRITICAL_TYRION_SERVER_SIDE_ERROR.error_code();
            plan.error = ErrorCode.CRITICAL_TYRION_SERVER_SIDE_ERROR.error_message();
            plan.update();
        }
    }

    @JsonIgnore
    public static void execute_update_procedure(Model_UpdateProcedure procedure) {

        logger.debug("execute_update_procedure - start execution of procedure: {}", procedure.id );
        logger.debug("execute_update_procedure - actual state: {} ", procedure.state.name());

        if (procedure.state == Enum_Update_group_procedure_state.complete || procedure.state == Enum_Update_group_procedure_state.successful_complete ) {
            logger.debug("execute_update_procedure - procedure: {} is done (successful_complete or complete) -> return", procedure.id);
            return;
        }

        if (procedure.updates.isEmpty()) {
            procedure.state = Enum_Update_group_procedure_state.complete_with_error;
            procedure.update();
            logger.debug("execute_update_procedure - procedure: {} is empty -> return" , procedure.id);
            return;
        }

        procedure.state = Enum_Update_group_procedure_state.in_progress;
        procedure.update();

        List<Model_HardwareUpdate> plans = Model_HardwareUpdate.find.query().where().eq("actualization_procedure.id", procedure.id)
                .disjunction()
                .eq("state", HardwareUpdateState.NOT_YET_STARTED)
                .eq("state", HardwareUpdateState.WAITING_FOR_DEVICE)
                .eq("state", HardwareUpdateState.INSTANCE_INACCESSIBLE)
                .eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE)
                .eq("state", HardwareUpdateState.HOMER_SERVER_NEVER_CONNECTED)
                .eq("state", HardwareUpdateState.BIN_FILE_MISSING)
                .endJunction()
                .findList();

        if (plans.isEmpty()) {
            logger.debug("execute_update_procedure - Procedure id:: {} all updates is done or in progress. -> Return.", procedure.id );
            return;
        }

        logger.debug("execute_update_procedure - Procedure id:: {} . Number of C_Procedures By database for execution:: {}" , procedure.id , plans.size());

        HashMap<UUID, List<Model_HardwareUpdate> > server_device_sort = new HashMap<>();

        for (Model_HardwareUpdate plan : plans) {
            try {

                logger.debug("execute_update_procedure - Procedure id:: {} plan {} CProgramUpdatePlan:: ID:: {} - New Cycle" , procedure.id , plan.id, plan.id);
                logger.debug("execute_update_procedure - Procedure id:: {} plan {} CProgramUpdatePlan:: Board ID:: {}" , procedure.id , plan.id,  plan.getHardware().id);
                logger.debug("execute_update_procedure - Procedure id:: {} plan {} CProgramUpdatePlan:: Status:: {} ", procedure.id , plan.id,  plan.state);

                logger.debug("execute_update_procedure - Procedure id:: {} plan {} CProgramUpdatePlan:: Number of tries {} ", procedure.id , plan.id,  plan.count_of_tries);

                if (plan.count_of_tries == null) {
                    plan.count_of_tries = 0;
                }

                // Pokud HW nemá nastavený Backup Mode - nastaví se
                if (plan.firmware_type == FirmwareType.BACKUP) {
                    if (!plan.getHardware().backup_mode) {
                        // Na Homera Nemusím posílat příkaz o změně registru, protože Homer při updatu Backupu vždy sám přepne register hardwaru pokud tak nahardwaru není
                        // JE to trochu vybočení ze standardu, ale byl problém s tím, že se registr nastavil a pak se mohlo posrat ještě milion věcí.
                        // Takto Homer register mění až ve chvíli, kdy přenese bynárku do bufru hardwaru a řekne udělej z toho backup (což je jen zpoždění jedné mqtt nstrukce)
                        // V Configu
                        DM_Board_Bootloader_DefaultConfig configuration = plan.getHardware().bootloader_core_configuration();
                        configuration.autobackup = false;
                        plan.getHardware().update_bootloader_configuration(configuration);
                        // V databázi
                        plan.getHardware().backup_mode = false;
                        plan.getHardware().update();
                    }
                }

                if (plan.count_of_tries > 5) {
                    plan.state = HardwareUpdateState.CRITICAL_ERROR;
                    plan.error = ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_message();
                    plan.error_code = ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_code();
                    plan.update();
                    logger.warn("execute_update_procedure - Procedure id:: {} plan {} CProgramUpdatePlan:: Error:: {} Message:: {} Continue Cycle. " , procedure.id , plan.id, ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_code() , ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_message());
                    continue;
                }

                if (plan.getHardware().connected_server_id == null) {
                    plan.state = HardwareUpdateState.HOMER_SERVER_NEVER_CONNECTED;
                    plan.update();
                    continue;
                }

                if (!server_device_sort.containsKey(plan.getHardware().connected_server_id)) {
                    server_device_sort.put(plan.getHardware().connected_server_id, new ArrayList<>());
                }

                if (Model_HomerServer.getById(plan.getHardware().connected_server_id).online_state() != NetworkStatus.ONLINE) {
                    logger.warn("execute_update_procedure - Procedure id:: {}  plan {}  Server {} is offline. Putting off the task for later. -> Return. ", procedure.id , plan.id, Model_HomerServer.getById(plan.hardware.connected_server_id).name);
                    plan.state = HardwareUpdateState.HOMER_SERVER_IS_OFFLINE;
                    plan.update();
                    continue;
                }

                server_device_sort.get(plan.getHardware().connected_server_id).add(plan);

                logger.debug("execute_update_procedure - Procedure id:: {}  plan {} of blocko program is online and connected with Tyrion", procedure.id, plan.id);

                plan.state = HardwareUpdateState.IN_PROGRESS;
                plan.update();

            } catch (Exception e) {
                logger.internalServerError(e);
                plan.state = HardwareUpdateState.CRITICAL_ERROR;
                plan.error_code = ErrorCode.CRITICAL_TYRION_SERVER_SIDE_ERROR.error_code();
                plan.error = ErrorCode.CRITICAL_TYRION_SERVER_SIDE_ERROR.error_message();
                plan.update();
                break;
            }
        }

        logger.debug("execute_update_procedure - Summary for actualizations");

        //  new Thread(procedure::notification_update_procedure_start).start();

        for (UUID server_id : server_device_sort.keySet()) {

            List<Swagger_UpdatePlan_brief_for_homer> tasks = new ArrayList<>();

            for (Model_HardwareUpdate plan : server_device_sort.get(server_id)) {
                tasks.add(plan.get_brief_for_update_homer_server());
            }

            Model_HomerServer.getById(server_id).update_devices_firmware(tasks);
        }
    }

    //-- Restart Device Command --//
    @JsonIgnore
    public void execute_command(BoardCommand command, boolean priority) {
        try {

            // Priority false - pokud má hardware v ukolníčk předchozí úkony, tento se zařadí do fronty
            // true - všechny přeskočí - muže se stát že pak některé stratí smysl a platnost a homer je zahodí

            JsonNode node = write_with_confirmation(new WS_Message_Hardware_command_execute().make_request(this.id, command, priority), 1000 * 5, 0, 2);

            // Execute Command
            baseFormFactory.formFromJsonWithValidation(WS_Message_Hardware_command_execute.class, node);

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    /**
     * Kontrola po připojení zařízení - každé připojení zařízení odstartuje kontrolní proceduru,
     * nejdříve korekntí nastavení s očekávaným podel DM_Board_Bootloader_DefaultConfig,
     * dále firmware, bootloader a backup.
     *
     *
      */
/* CHECK BOARD RIGHT FIRMWARE || BACKUP || BOOTLOADER STATUS -----------------------------------------------------------*/

    // Kontrola up_to_date harwaru
    @JsonIgnore
    public void hardware_firmware_state_check() {
        try {

            WS_Message_Hardware_overview_Board report = get_devices_overview();

            if (report.error_message != null) {
                logger.debug("hardware_firmware_state_check - Report Device ID: {} contains ErrorCode:: {} ErrorMessage:: {} " , this.id, report.error_code, report.error_message);

                if (report.error_code.equals(ErrorCode.HARDWARE_IS_OFFLINE.error_code())) {
                    logger.debug("hardware_firmware_state_check -: Report Device ID: {} is offline" , this.id);
                    return;
                }
            }

            if (!report.status.equals("success")) {
                logger.warn("hardware_firmware_state_check: WS_Help_Hardware_board_overview something is wrong on device {}" , this.id);
                return;
            }

            if (!report.online_state) {
                logger.debug("hardware_firmware_state_check - device is offline");
                return;
            }

            if (project_id() == null) {
                logger.debug("hardware_firmware_state_check device id:: {} - No project - synchronize is not allowed.", this.id);
                return;
            }

            logger.debug("hardware_firmware_state_check - Summary information of connected master hardware: ID = {}", this.id);

            logger.debug("hardware_firmware_state_check - Settings check ",this.id);
            if (!check_settings(report)) return;

            logger.debug("hardware_firmware_state_check - Firmware check ",this.id);
            check_firmware(report);

            logger.debug("hardware_firmware_state_check - Backup check ",this.id);
            check_backup(report);

            logger.debug("hardware_firmware_state_check - Bootloader check ",this.id);
            check_bootloader(report);

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    /**
     * Zde se kontroluje jestli je na HW to co na něm reálně být má.
     * Pokud některý parametr je jiný než se očekává, metoda ho změní (for cyklus), jenže je nutné zařízení restartovat,
     * protože může teoreticky dojít k tomu, že se register změní, ale na zařízení se to neprojeví.
     * Například změna portu www stránky pro vývojáře. Proto se vrací TRUE pokud vše sedí a připojovací procedura muže pokračovat nebo false, protože device byl restartován.
     *
     * @param overview
     */
    @JsonIgnore
    private boolean check_settings(WS_Message_Hardware_overview_Board overview) {

        // Pokud uživatel nechce DB synchronizaci ingoruji
        if (!this.database_synchronize) {
            logger.trace("check_settings - database_synchronize is forbidden - change parameters not allowed!");
            return true;
        }

        // Kontrola zda je stejný
        if (this.backup_mode && !overview.autobackup) {
            logger.warn("check_settings - inconsistent state: set autobackup from static backup");
            set_auto_backup();
        }

        // Kontrola Aliasu
        if ((overview.alias == null || !overview.alias.equals(this.name)) && name != null) {
            logger.warn("check_settings - inconsistent state: alias");
            set_alias(this.name);
        }

        // Uložení do Cache paměti // PORT je synchronizován v následujícím for cyklu
        if (cache_latest_know_ip_address == null || !cache_latest_know_ip_address.equals(overview.ip)) {
            cache_latest_know_ip_address = overview.ip;
        }

        // Synchronizace mac_adressy pokud k tomu ještě nedošlo
        if (mac_address == null) {
            if (overview.mac != null)
            mac_address = overview.mac;
            this.update();
        }

        // Kontrola Skupin Hardware Groups - To není synchronizace s HW ale s Instancí HW na Homerovi
        for(UUID hardware_group_id : get_hardware_group_ids()) {
            // Pokud neobsahuje přidám - ale abych si ušetřil čas - nastavím rovnou celý seznam - Homer si s tím poradí
            if (!overview.hardware_group_ids.contains(hardware_group_id)) {
                set_hardware_groups_on_hardware(get_hardware_group_ids(), Enum_type_of_command.SET);
                break;
            }
        }

        /*
            Tato smyčka prochází všechny položky v objektu DM_Board_Bootloader_DefaultConfig jeden po druhém a pak hledá
            ty samé config parametry v příchozím objektu - pokud je nazelzne následě porovná očekávanou ohnotu od té
            reálné a popřípadě upraví na hardwaru.

            Pokud se něco změnilo - nastaví se change register na true

         */
        DM_Board_Bootloader_DefaultConfig configuration = this.bootloader_core_configuration();
        boolean change_register = false; // Pokud došlo ke změně

        for (Field config_field : configuration.getClass().getFields()) {

            try {
                Field incoming_report_right_filed = null;

                for (Field incoming_filed : overview.getClass().getFields()) {
                    if (config_field.getName().equals(incoming_filed.getName())) {
                        incoming_report_right_filed = incoming_filed;
                        break;
                    }
                }

                if (incoming_report_right_filed == null) {
                    continue;
                }

                if (config_field.getType().getCanonicalName().equals(Boolean.class.getSimpleName().toLowerCase())) {

                    if (config_field.getBoolean(configuration) != incoming_report_right_filed.getBoolean(overview)) {
                        write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this), config_field.getName(), config_field.getBoolean(configuration)), 1000 * 5, 0, 2);
                        change_register = true;
                    }

                    continue;
                }

                if (config_field.getType().getCanonicalName().toLowerCase().equals(String.class.getSimpleName().toLowerCase())) {

                    if (!config_field.get(configuration).toString().equals(incoming_report_right_filed.get(overview).toString())) {
                        write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this), config_field.getName(), config_field.get(configuration).toString()), 1000 * 5, 0, 2);
                        change_register = true;
                    }

                    continue;
                }

                if (config_field.getType().getCanonicalName().toLowerCase().equals(Integer.class.getSimpleName().toLowerCase())) {

                    if (config_field.getInt(configuration) != incoming_report_right_filed.getInt(overview)) {
                        write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this), config_field.getName(), config_field.getInt(configuration)), 1000 * 5, 0, 2);
                        change_register = true;
                    }

                    continue;
                }
            } catch (Exception e) {
                logger.internalServerError(e);
                return true;
            }
        }

        if (change_register) {
            // Priority false - protože v ukolníčku má Hardware předchozí úkony k updatu registrů - kdyby byl true,
            // tak všechny přeskočí
            this.execute_command(BoardCommand.RESTART, false);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Pokud máme odchylku od databáze na hardwaru, to jest nesedí firmware_build_id na hW s tím co říká databáze
     * @param overview object of WS_Message_Hardware_overview_Board
     */
    @JsonIgnore
    private void check_firmware(WS_Message_Hardware_overview_Board overview) {

        try {
            // Pokud uživatel nechce DB synchronizaci ingoruji
            if (!this.database_synchronize) {
                logger.trace("check_firmware - database_synchronize is forbidden - change parameters not allowed!");
                return;
            }

            if (get_actual_c_program_version() == null) {
                logger.trace("check_firmware -: Actual firmware by DB not recognized :: ", overview.binaries.firmware.build_id);
            }

            if (overview.binaries.firmware == null) {
                logger.error("check_firmware -: overview.binaries.firmware is null!!");
                return;
            }

            if (overview.binaries.firmware.build_id == null || overview.binaries.firmware.build_id.equals("")) {
                logger.error("check_firmware -: overview.binaries.firmware.build_id is null");
                return;
            }

            // Vylistuji seznam úkolů k updatu
            List<Model_HardwareUpdate> firmware_plans = Model_HardwareUpdate.find.query().where().eq("hardware.id", this.id)
                    .disjunction()
                    .add(Expr.eq("state", HardwareUpdateState.NOT_YET_STARTED))
                    .add(Expr.eq("state", HardwareUpdateState.IN_PROGRESS))
                    .add(Expr.eq("state", HardwareUpdateState.WAITING_FOR_DEVICE))
                    .add(Expr.eq("state", HardwareUpdateState.INSTANCE_INACCESSIBLE))
                    .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE))
                    .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_NEVER_CONNECTED))
                    .endJunction()
                    .eq("firmware_type", FirmwareType.FIRMWARE.name())
                    .lt("actualization_procedure.date_of_planing", new Date())
                    .order().desc("actualization_procedure.date_of_planing")
                    .findList();

            // Kontrola Firmwaru a přepsání starých
            // Je žádoucí přepsat všechny předhozí update plány - ale je nutné se podívat jestli nejsou rozdílné!
            // To jest pokud mám 2 updaty firmwaru pak ten starší zahodím
            // Ale jestli mám udpate firmwaru a backupu pak k tomu dojít nesmí!
            // Poměrně krkolomné řešení a HNUS kod - ale chyba je výjmečná a stává se jen sporadicky těsně před nebo po restartu serveru
            if (firmware_plans.size() > 1) {
                for (int i = 1; i < firmware_plans.size(); i++) {
                    firmware_plans.get(i).state = HardwareUpdateState.OBSOLETE;
                    firmware_plans.get(i).update();
                }
            }

            if (!firmware_plans.isEmpty()) {

                logger.debug("existují nedokončené procedury");

                Model_HardwareUpdate plan = firmware_plans.get(0);

                logger.debug("Plan:: {} status: {} ", plan.id, plan.state);

                // Mám shodu firmwaru oproti očekávánemů
                if (get_actual_c_program_version() != null) {

                    logger.debug("Firmware:: Co aktuálně je na HW podle Tyriona??:: {}", get_actual_c_program_version().compilation.firmware_build_id);
                    logger.debug("Firmware:: Co aktuálně je na HW podle Homera??:: {}", overview.binaries.firmware.build_id);
                    logger.debug("Firmware:: Co očekává nedokončená procedura??:: {}", plan.c_program_version_for_update.compilation.firmware_build_id);

                    // Verze se rovnají
                    if (overview.binaries.firmware.build_id.equals(plan.c_program_version_for_update.compilation.firmware_build_id)) {
                        logger.debug("check_firmware verze se shodují - tím pádem je procedura dokončená a uzavírám");

                        this.actual_c_program_version = plan.c_program_version_for_update;
                        this.cache_actual_c_program_version_id = plan.c_program_version_for_update.id;
                        this.cache_actual_c_program_id = plan.c_program_version_for_update.get_c_program().id;
                        this.update();

                        logger.debug("check_firmware - up to date, procedure is done");
                        plan.state = HardwareUpdateState.COMPLETE;
                        plan.date_of_finish = new Date();
                        plan.update();
                        return;

                    } else {
                        logger.debug("check_firmware verze se neshodují");
                        logger.debug("check_firmware - need update, system starts a update again, number of tries {}", plan.count_of_tries);
                        plan.state = HardwareUpdateState.NOT_YET_STARTED;
                        plan.count_of_tries++;
                        plan.update();
                        execute_update_plan(plan);
                    }

                } else {

                    logger.debug("check_firmware - no firmware, system starts a new update");
                    plan.state = HardwareUpdateState.NOT_YET_STARTED;
                    plan.count_of_tries++;
                    plan.update();

                    execute_update_plan(plan);
                }

                return;
            }

            // Nemám Updaty - ale verze se neshodují
            if (get_actual_c_program_version() != null && firmware_plans.isEmpty()) {

                logger.debug("check_firmware - no update procedures found, but versions are not equal");
                logger.debug("check_firmware - current firmware according to Tyrion: {}", get_actual_c_program_version().compilation.firmware_build_id);
                logger.debug("check_firmware - current firmware according to Homer: {}", overview.binaries.firmware.build_id);

                if (!get_actual_c_program_version().compilation.firmware_build_id.equals(overview.binaries.firmware.build_id)) {
                    // Na HW není to co by na něm mělo být.

                    logger.debug("check_firmware - Different firmware versions versus database");

                    if (get_backup_c_program_version() != null && get_backup_c_program_version().compilation.firmware_build_id.equals(overview.binaries.bootloader.build_id)) {

                        logger.warn("check_firmware - We have problem with firmware version. Backup is now running");

                        // Notifikace uživatelovi
                        this.notification_board_unstable_actual_firmware_version(get_actual_c_program_version());

                        // Označit firmare za nestabilní
                        get_actual_c_program_version().compilation.status = CompilationStatus.UNSTABLE;
                        get_actual_c_program_version().compilation.update();

                        // Přemapovat hardware
                        actual_c_program_version = get_backup_c_program_version();

                        this.cache_actual_c_program_id = actual_c_program_version.id;                     // C Program
                        this.cache_actual_c_program_version_id = actual_c_program_version.id;
                        this.cache_actual_c_program_backup_id = actual_c_program_version.id;              // Backup
                        this.cache_actual_c_program_backup_version_id = actual_c_program_version.id;

                        this.update();

                        return;
                    }

                    // Backup to není
                    else {

                        logger.warn("check_firmware - Wrong version on hardware - or null version on hardware");
                        logger.warn("check_firmware - Now System set Default Firmware or Firmware by Database!!!");

                        // Nastavuji nový systémový update
                        List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

                        WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                        b_pair.hardware = this;
                        b_pair.c_program_version = get_actual_c_program_version();

                        b_pairs.add(b_pair);

                        Model_UpdateProcedure procedure = create_update_procedure(FirmwareType.FIRMWARE, UpdateType.AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE, b_pairs);
                        procedure.execute_update_procedure();

                        return;
                    }

                } else {
                    // Na HW je to co by na něm podle databáze mělo být.
                    logger.debug("check_firmware - hardware is up to date");
                    return;
                }
            }

            // Set Defualt Program protože žádný teď nemáš
            if (get_actual_c_program_version() == null && firmware_plans.isEmpty()) {

                logger.debug("check_firmware - Actual firmware_id is not recognized by the DB - Device has not firmware from Tyrion");
                logger.debug("check_firmware - Tyrion will try to find Default C Program Main Version for this type of hardware. If is it set, Tyrion will update device to starting state");

                // Ověřím - jestli nemám nově nahraný firmware na Hardwaru (to je ten co je teď výcohí firmware pro aktuální typ hardwaru)
                if (overview.binaries != null && overview.binaries.firmware != null
                        && overview.binaries.firmware.build_id != null
                        && getHardwareType().get_main_c_program().default_main_version != null
                        && getHardwareType().get_main_c_program().default_main_version.compilation.firmware_build_id != null
                        && overview.binaries.firmware.build_id.equals(getHardwareType().get_main_c_program().default_main_version.compilation.firmware_build_id)
                        ) {

                    logger.debug("check_firmware - hardware is brand new, but already has required firmware");

                    this.actual_c_program_version = getHardwareType().get_main_c_program().default_main_version;
                    this.cache_actual_c_program_id = getHardwareType().get_main_c_program().id;
                    this.cache_actual_c_program_version_id = getHardwareType().get_main_c_program().default_main_version.id;


                    this.actual_backup_c_program_version = getHardwareType().get_main_c_program().default_main_version; // Udělám rovnou zálohu, protože taková by tam měla být
                    this.cache_actual_c_program_backup_id = getHardwareType().get_main_c_program().id;
                    this.cache_actual_c_program_backup_version_id = getHardwareType().get_main_c_program().default_main_version.id;
                    this.update();
                    return;
                }

                // Nastavím default firmware podle schématu Tyriona!
                // Defaultní firmware je v v backandu určený výchozí program k typu desky.
                if (getHardwareType().get_main_c_program() != null && getHardwareType().get_main_c_program().default_main_version != null) {

                    logger.debug("check_firmware - Yes, Default Version for Type Of Device {} is set", getHardwareType().name);

                    List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

                    WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                    b_pair.hardware = this;
                    b_pair.c_program_version = getHardwareType().get_main_c_program().default_main_version;

                    b_pairs.add(b_pair);

                    this.notification_board_not_databased_version();

                    Model_UpdateProcedure procedure = create_update_procedure(FirmwareType.FIRMWARE, UpdateType.AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE, b_pairs);
                    procedure.execute_update_procedure();

                } else {
                    logger.error("Attention please! This is not a critical bug - Tyrion server is not just set for this type of device! Set main C_Program and version!");
                    logger.error("Default main code version is not set for Type Of Board " + getHardwareType().name + " please set that!");
                }
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    private void check_backup(WS_Message_Hardware_overview_Board overview) {
        try {
            // Pokud uživatel nechce DB synchronizaci ingoruji
            if (!this.database_synchronize) {
                logger.trace("check_backup - database_synchronize is forbidden - change parameters not allowed!");
                return;
            }

            // když je autobackup tak sere pes - změna autobacku je rovnou z devicu
            if (backup_mode) {
                logger.trace("check_backup - autobacku is true change parameters not allowed!");
                // Ale mohl bych udělat áznam o tom co tam je - kdyby to nebylo stejné s tím co si myslí tyrion že tam je:

                if (overview.binaries.backup != null && (overview.binaries.backup.build_id == null || !overview.binaries.backup.build_id.equals(""))) {
                    Model_CProgramVersion version_not_cached = Model_CProgramVersion.find.query().where().eq("compilation.firmware_build_id", overview.binaries.backup.build_id).select("id").findOne();
                    if (version_not_cached != null) {
                        logger.debug("check_backup:: Ještě nebyla přiřazena žádná Backup verze k HW v Tyrionovi - ale program se podařilo najít");
                        Model_CProgramVersion cached_version = Model_CProgramVersion.getById(version_not_cached.id);
                        this.actual_backup_c_program_version = cached_version;
                        this.cache_actual_c_program_backup_id = cached_version.get_c_program().id;
                        this.cache_actual_c_program_backup_version_id = cached_version.id;
                        this.update();
                    }
                }

                return;
            }

            logger.debug("check_backup:: third check backup! - Backup is static!! ");

            // Backup je takový jaký očekává tyrion
            if (get_backup_c_program_version() != null && get_backup_c_program_version().compilation.firmware_build_id.equals(overview.binaries.backup.build_id)) {
                logger.debug("check_backup:: Backup is same as on hardware as on Tyrion");
                return;
            }

            // Backup není - to by se stát nemělo - ale šup sem sním
            if (get_backup_c_program_version() == null) {
                logger.warn("check_backup:: Static Backup is required - but tyrion not set any backup!");

                if (overview.binaries.backup != null && (overview.binaries.backup.build_id == null || !overview.binaries.backup.build_id.equals(""))) {

                    // Try to find it in User programs and
                    Model_CProgramVersion version_not_cached = Model_CProgramVersion.find.query().where().eq("compilation.firmware_build_id", overview.binaries.backup.build_id).select("id").findOne();

                    if (version_not_cached != null) {
                        logger.debug("check_backup:: Ještě nebyla přiřazena žádná Backup verze k HW v Tyrionovi - ale program se podařilo najít");
                        Model_CProgramVersion cached_version = Model_CProgramVersion.getById(version_not_cached.id);
                        this.actual_backup_c_program_version = cached_version;
                        this.cache_actual_c_program_backup_id = cached_version.get_c_program().id;
                        this.cache_actual_c_program_backup_version_id = cached_version.id;
                        this.update();
                    } else {
                        logger.warn("check_backup:: Nastal stav, kdy mám statický backup, Tyrion v databázi nic nemá a ani se mi nepodařilo najít program (build_id)"
                                + "který by byl kompatibilní. Což je trochu problém. Uvidíme co nabízí update procedury. \n" +
                                "Možnosti jsou 1.) Nahrát první určený plan na hardware a tato metoda začne v podmínce najdi fungovat \n" +
                                "nebo 2) přepnu do autobacku");
                    }
                }
            }

            List<Model_HardwareUpdate> firmware_plans = Model_HardwareUpdate.find.query().where().eq("hardware.id", this.id)
                    .disjunction()
                    .add(Expr.eq("state", HardwareUpdateState.NOT_YET_STARTED))
                    .add(Expr.eq("state", HardwareUpdateState.IN_PROGRESS))
                    .add(Expr.eq("state", HardwareUpdateState.WAITING_FOR_DEVICE))
                    .add(Expr.eq("state", HardwareUpdateState.INSTANCE_INACCESSIBLE))
                    .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE))
                    .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_NEVER_CONNECTED))
                    .endJunction()
                    .eq("firmware_type", FirmwareType.BACKUP.name())
                    .lt("actualization_procedure.date_of_planing", new Date())
                    .order().desc("actualization_procedure.date_of_planing")
                    .findList();

            // Zaloha kdyby byly stále platné aktualizace na backup
            for (int i = 1; i < firmware_plans.size(); i++) {
                firmware_plans.get(i).state = HardwareUpdateState.OBSOLETE;
                firmware_plans.get(i).update();
            }

            // Kontrola Firmwaru a přepsání starých
            // Je žádoucí přepsat všechny předhozí update plány - ale je nutné se podívat jestli nejsou rozdílné!
            // To jest pokud mám 2 updaty firmwaru pak ten starší zahodím
            // Ale jestli mám udpate firmwaru a backupu pak k tomu dojít nesmí!
            // Poměrně krkolomné řešení a HNUS kod - ale chyba je výjmečná a stává se jen sporadicky těsně před nebo po restartu serveru
            if (firmware_plans.size() > 1) {
                for (int i = 1; i < firmware_plans.size(); i++) {
                    firmware_plans.get(i).state = HardwareUpdateState.OBSOLETE;
                    firmware_plans.get(i).update();
                }
            }

            // Mám updaty a tak kontroluji
            if (!firmware_plans.isEmpty()) {

                Model_HardwareUpdate plan = firmware_plans.get(0);

                logger.debug("check_backup - Actual backup according to Tyrion: {}", get_backup_c_program_version().compilation.firmware_build_id);
                logger.debug("check_backup - Actual backup according to Homer: {}", overview.binaries.backup.build_id);
                logger.debug("check_backup - incomplete procedure expects: {}", plan.c_program_version_for_update.compilation.firmware_build_id);

                if (plan.getHardware().get_backup_c_program_version() != null) {

                    // Verze se rovnají
                    if (get_backup_c_program_version().compilation.firmware_build_id.equals(plan.c_program_version_for_update.compilation.firmware_build_id)) {

                        logger.debug("check_backup - up to date, procedure is done");
                        plan.state = HardwareUpdateState.COMPLETE;
                        plan.date_of_finish = new Date();
                        plan.update();

                    } else {

                        logger.debug("check_backup - need update, system starts a new update, number of tries {}", plan.count_of_tries);
                        plan.state = HardwareUpdateState.NOT_YET_STARTED;
                        plan.count_of_tries++;
                        plan.update();
                        execute_update_procedure(plan.actualization_procedure);
                    }

                } else {

                    logger.debug("check_backup - no backup, system starts a new update");
                    plan.state = HardwareUpdateState.NOT_YET_STARTED;
                    plan.count_of_tries++;
                    plan.update();

                    execute_update_procedure(plan.actualization_procedure);
                }
                return;
            }

            // Nemám updaty ale verze se neshodují
            if (get_backup_c_program_version() == null && firmware_plans.isEmpty()) {
                set_auto_backup();
            }
        } catch (Exception e) {
           logger.internalServerError(e);
        }
    }

    @JsonIgnore
    private void check_bootloader(WS_Message_Hardware_overview_Board overview) {

        // Pokud uživatel nechce DB synchronizaci ingoruji
        if (!this.database_synchronize) {
            logger.trace("check_bootloader - database_synchronize is forbidden - change parameters not allowed!");
            return;
        }

        if (get_actual_bootloader() == null) {
            logger.trace("check_bootloader -: Actual bootloader_id by DB not recognized :: ", overview.binaries.bootloader.build_id);
        }

        // Vylistuji seznam úkolů k updatu
        List<Model_HardwareUpdate> firmware_plans = Model_HardwareUpdate.find.query().where().eq("hardware.id", this.id)
                .disjunction()
                    .add(Expr.eq("state", HardwareUpdateState.NOT_YET_STARTED))
                    .add(Expr.eq("state", HardwareUpdateState.IN_PROGRESS))
                    .add(Expr.eq("state", HardwareUpdateState.WAITING_FOR_DEVICE))
                    .add(Expr.eq("state", HardwareUpdateState.INSTANCE_INACCESSIBLE))
                    .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE))
                    .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_NEVER_CONNECTED))
                .endJunction()
                .eq("firmware_type", FirmwareType.BOOTLOADER)
                .le("actualization_procedure.date_of_planing", new Date())
                .order().desc("actualization_procedure.date_of_planing")
                .findList();

        // Kontrola Bootloader a přepsání starých
        // Je žádoucí přepsat všechny předhozí update plány - ale je nutné se podívat jestli nejsou rozdílné!
        // To jest pokud mám 2 updaty firmwaru pak ten starší zahodím
        // Ale jestli mám udpate firmwaru a backupu pak k tomu dojít nesmí!
        // Poměrně krkolomné řešení a HNUS kod - ale chyba je výjmečná a stává se jen sporadicky těsně před nebo po restartu serveru
        if (firmware_plans.size() > 1) {
            logger.trace("check_bootloader:: firmware_plans.size() > 1 ");
            for (int i = 1; i < firmware_plans.size(); i++) {
                logger.trace("check_bootloader:: OBSOLETE procedure ID {}", firmware_plans.get(i).id);
                firmware_plans.get(i).state = HardwareUpdateState.OBSOLETE;
                firmware_plans.get(i).update();
            }
        }

        if (!firmware_plans.isEmpty()) {

            Model_HardwareUpdate plan = firmware_plans.get(0);

            if (plan.getHardware().get_actual_bootloader() != null) {

                // Verze se rovnají
                if (plan.getHardware().get_actual_bootloader().version_identifier.equals(plan.getBootloader().version_identifier)) {

                    logger.debug("check_bootloader - up to date, procedure is done");
                    plan.state = HardwareUpdateState.COMPLETE;
                    plan.date_of_finish = new Date();
                    plan.update();

                    this.actual_boot_loader = plan.getBootloader();
                    this.cache_actual_boot_loader_id = plan.getBootloader().id;
                    update();

                } else {

                    logger.debug("check_bootloader - need update, system starts a new update, number of tries {}", plan.count_of_tries);
                    plan.state = HardwareUpdateState.NOT_YET_STARTED;
                    plan.count_of_tries++;
                    plan.update();
                    execute_update_procedure(plan.actualization_procedure);
                }

            } else {

                logger.debug("check_bootloader:: no bootloader, system starts a update again");
                plan.state = HardwareUpdateState.NOT_YET_STARTED;
                plan.count_of_tries++;
                plan.update();

                execute_update_procedure(plan.actualization_procedure);
            }

            return;
        }

        // Pokud na HW opravdu žádný bootloader není a není ani vytvořená update procedura
        if (get_actual_bootloader() == null && firmware_plans.isEmpty()) {

            logger.debug("check_bootloader:: noo default bootloader on hardware - required automatic update");

            // Zkontroluji jestli tam nějaká verze už je!
            Model_BootLoader bootloader = Model_BootLoader.find.query().where().eq("version_identifier", overview.binaries.bootloader.build_id).findOne();

            if (bootloader != null ) {
                logger.debug("check_bootloader:: Bootloader identificator {} recognized and found in database", overview.binaries.bootloader.build_id);
                actual_boot_loader = bootloader;
                update();
                return;
            }

            if (getHardwareType().main_boot_loader() == null) {
                logger.error("check_bootloader::Main Bootloader for Type Of Board {} is not set for update device {}", this.hardware_type_name(), this.id);
                return;
            }

            List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

            WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
            b_pair.hardware = this;

            if (this.get_actual_bootloader() == null) b_pair.bootLoader =  getHardwareType().main_boot_loader();
            else b_pair.bootLoader = this.get_actual_bootloader();

            b_pairs.add(b_pair);

            logger.debug("check_bootloader:: - creating update procedure for bootloader");
            Model_UpdateProcedure procedure = create_update_procedure(FirmwareType.BOOTLOADER, UpdateType.AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE, b_pairs);
            procedure.execute_update_procedure();
        }
    }

/* UPDATE --------------------------------------------------------------------------------------------------------------*/

    public static Model_UpdateProcedure create_update_procedure(FirmwareType firmware_type, UpdateType type_of_update, List<WS_Help_Hardware_Pair> board_for_update) {

        if (board_for_update == null || board_for_update.isEmpty()) {
            throw new NullPointerException("List<WS_Help_Hardware_Pair> board_for_update) is empty");
        }

        Model_UpdateProcedure procedure = new Model_UpdateProcedure();
        procedure.project_id = board_for_update.get(0).hardware.project_id();
        procedure.state = Enum_Update_group_procedure_state.not_start_yet;
        procedure.type_of_update = type_of_update;

        procedure.save();

        for (WS_Help_Hardware_Pair b_pair : board_for_update) {

            List<Model_HardwareUpdate> obsolete = Model_HardwareUpdate.find.query()
                    .where()
                    .eq("firmware_type", firmware_type)
                    .disjunction()
                    .add(Expr.eq("state", HardwareUpdateState.NOT_YET_STARTED))
                    .add(Expr.eq("state", HardwareUpdateState.IN_PROGRESS))
                    .add(Expr.eq("state", HardwareUpdateState.WAITING_FOR_DEVICE))
                    .add(Expr.eq("state", HardwareUpdateState.INSTANCE_INACCESSIBLE))
                    .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE))
                    .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_NEVER_CONNECTED))
                    .endJunction()
                    .eq("hardware.id", b_pair.hardware.id).findList();

            for (Model_HardwareUpdate update : obsolete) {
                update.state = HardwareUpdateState.OBSOLETE;
                update.date_of_finish = new Date();
                update.update();
            }

            Model_HardwareUpdate plan = new Model_HardwareUpdate();
            plan.hardware = b_pair.hardware;
            plan.firmware_type = firmware_type;
            plan.actualization_procedure = procedure;

            // Firmware
            if (firmware_type == FirmwareType.FIRMWARE) {
                plan.c_program_version_for_update = b_pair.c_program_version;
                plan.state = HardwareUpdateState.NOT_YET_STARTED;
            }

            // Backup
            else if (firmware_type == FirmwareType.BACKUP) {
                plan.c_program_version_for_update = b_pair.c_program_version;
                plan.state = HardwareUpdateState.NOT_YET_STARTED;
            }

            // Bootloader
            else if (firmware_type == FirmwareType.BOOTLOADER) {
                plan.bootloader = b_pair.bootLoader;
                plan.state = HardwareUpdateState.NOT_YET_STARTED;
            }

            plan.save();
        }

        procedure.refresh();

        return procedure;
    }

/* ONLINE STATUS SYNCHRONIZATION ---------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    // Online Offline Notification
    @JsonIgnore
    public void notification_board_connect() {
        new Thread(() -> {
            if (project_id() == null) return;

            try {
                new Model_Notification()
                        .setImportance(NotificationImportance.LOW)
                        .setLevel(NotificationLevel.SUCCESS)
                        .setText(new Notification_Text().setText("Device " + this.id))
                        .setObject(this)
                        .setText(new Notification_Text().setText(" has just connected"))
                        .send_under_project(project_id());

            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }).start();
    }

    @JsonIgnore
    public void notification_board_disconnect() {
        new Thread(() -> {
            if (project_id() == null) return;
            try {

                new Model_Notification()
                    .setImportance( NotificationImportance.LOW )
                    .setLevel( NotificationLevel.WARNING)
                    .setText(  new Notification_Text().setText("Device" + this.id ))
                    .setObject(this)
                    .setText( new Notification_Text().setText(" has disconnected."))
                    .send_under_project(project_id());

            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }).start();
    }

    @JsonIgnore
    public void notification_board_unstable_actual_firmware_version(Model_CProgramVersion firmware_version) {
        new Thread(() -> {

            // Pokud to není yoda ale device tak neupozorňovat v notifikaci, že je deska offline - zbytečné zatížení
            if (project_id() == null) return;

            try {

                new Model_Notification()
                        .setImportance( NotificationImportance.HIGH)
                        .setLevel(NotificationLevel.ERROR)
                        .setText(new Notification_Text().setText("Attention! We note the highest critical error on your device "))
                        .setObject(this)
                        .setText(new Notification_Text().setText(" There was a collapse of the running firmware "))
                        .setObject(firmware_version.get_c_program())
                        .setText(new Notification_Text().setText(" version "))
                        .setObject(firmware_version)
                        .setText(new Notification_Text().setText(". But stay calm. The hardware has successfully restarted and uploaded a backup version. " +
                                "This can cause a data collision in your Blocko Program, but you have the chance to fix the firmware. " +
                                "Incorrect version of Firmware has been flagged as unreliable."))
                        .send_under_project(project_id());

            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }).start();
    }

    @JsonIgnore
    public void notification_board_not_databased_version() {
        new Thread(() -> {

            if (project_id() == null) return;

            try {

                new Model_Notification()
                        .setImportance( NotificationImportance.NORMAL)
                        .setLevel(NotificationLevel.INFO)
                        .setText(new Notification_Text().setText("Attention! Device "))
                        .setObject(this)
                        .setText(new Notification_Text().setText(" has logged in. Unfortunately, we do not have a synchronized knowledge of the " +
                                "device status of what device firmware is running on. Perhaps this is the factory setting. " +
                                "We are now updating to the default firmware on device."))
                        .setNewLine()
                        .setText(new Notification_Text().setText("You do not have to do anything. Have a nice day."))
                        .setNewLine()
                        .setText(new Notification_Text().setText("Byzance"))
                        .send_under_project(project_id());



            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }).start();
    }
/*
    // Backup Notification
    @JsonIgnore
    public static void notification_set_static_backup_procedure_first_information_single(Model_BPair board_for_update) {
        try {

            new Thread(() -> {

                new Model_Notification()
                        .setImportance(NotificationImportance.LOW)
                        .setLevel(NotificationLevel.WARNING)
                        .setText(new Notification_Text().setText("You set Static Backup program: "))
                        .setObject(board_for_update.c_program_version.c_program)
                        .setText(new Notification_Text().setText(", in Version "))
                        .setObject(board_for_update.c_program_version)
                        .setText(new Notification_Text().setText(" for device "))
                        .setObject(board_for_update.hardware)
                        .setText(new Notification_Text().setText(". "))
                        .setText(new Notification_Text().setText("We show you in hardware overview only what's currently on the device. " +
                                "Each update is assigned to the queue of tasks and will be made as soon as possible or according to schedule. " +
                                "In the details of the instance or hardware overview, you can see the status of each procedure. " +
                                "If the update command was not time-specific (immediately) and the device is online, the data transfer may have already begun."))
                        .send_under_project(board_for_update.hardware.project_id());

            }).start();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public static void notification_set_static_backup_procedure_first_information_list( List<Model_BPair> board_for_update) {
        try {

            new Thread(() -> {

                if ( board_for_update.size() == 0 )  throw new IllegalArgumentException("notification_set_static_backup_procedure_first_information_list:: List is empty! ");
                if ( board_for_update.size() == 1 ) {
                    notification_set_static_backup_procedure_first_information_single(board_for_update.get(0));
                    return;
                }

                new Model_Notification()
                        .setImportance(NotificationImportance.LOW)
                        .setLevel(NotificationLevel.WARNING)
                        .setText(new Notification_Text().setText("You set Static Backup program for Hardware Collection (" + board_for_update.size() + "). "))
                        .setText(new Notification_Text().setText("We show you in hardware overview only what's currently on the device. " +
                                "Each update is assigned to the queue of tasks and will be made as soon as possible or according to schedule. " +
                                "In the details of the instance or hardware overview, you can see the status of each procedure. " +
                                "If the update command was not time-specific (immediately) and the device is online, the data transfer may have already begun."))
                        .send_under_project(board_for_update.get(0).hardware.project_id());

            }).start();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }*/


/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    public void make_log_connect() {
        new Thread(() -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_Board_Connect.make_request(this.id), null, true);
            } catch (DocumentClientException e) {
                logger.internalServerError(e);
            }
        }).start();
    }

    public void make_log_disconnect() {
        new Thread(() -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_Board_Disconnected.make_request(this.id), null, true);
            } catch (DocumentClientException e) {
                logger.internalServerError(e);
            }
        }).start();
    }

    public void make_log_backup_arrise_change() {
        new Thread(() -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_Board_BackupIncident.make_request_success_backup(this.id), null, true);
            } catch (DocumentClientException e) {
                logger.internalServerError(e);
            }
        }).start();
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override  public void check_create_permission() throws _Base_Result_Exception {
        // Only for Garfield Registration or Manual Registration
        if (_BaseController.person().has_permission(Permission.Hardware_create.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission("hardware_read_" + id)) _BaseController.person().valid_permission("hardware_read_" + id);
            if (_BaseController.person().has_permission(Permission.Hardware_read.name())) return;

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
            get_project().check_read_permission();
            _BaseController.person().cache_permission("hardware_read_" + id, true);

        } catch (_Base_Result_Exception e){
            _BaseController.person().cache_permission("hardware_read_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    /**
     * For this case - its not delete, but unregistration from Project
     * @throws _Base_Result_Exception
     */
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception  {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission("hardware_delete_" + id)) _BaseController.person().valid_permission("hardware_delete_" + id);
            if (_BaseController.person().has_permission(Permission.Hardware_delete.name())) return;

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
            get_project().check_update_permission();
            _BaseController.person().cache_permission("hardware_delete_" + id, true);

        } catch (_Base_Result_Exception e){
            _BaseController.person().cache_permission("hardware_delete_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception  {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission("hardware_update_" + id)) _BaseController.person().valid_permission("hardware_update_" + id);
            if (_BaseController.person().has_permission(Permission.Hardware_update.name())) return;

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
            get_project().check_update_permission();
            _BaseController.person().cache_permission("hardware_update_" + id, true);

        } catch (_Base_Result_Exception e){
            _BaseController.person().cache_permission("hardware_update_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    @JsonIgnore @Transient public void check_deactivate_permission() throws _Base_Result_Exception  {
        try {

            // Its not possible deactivate deactivated device!!!
            if(!dominant_entity) throw new Result_Error_PermissionDenied();

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission("hardware_update_" + id)) _BaseController.person().valid_permission("hardware_update_" + id);
            if (_BaseController.person().has_permission(Permission.Hardware_update.name())) return;

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
            get_project().check_update_permission();
            _BaseController.person().cache_permission("hardware_update_" + id, true);

        } catch (_Base_Result_Exception e){
            _BaseController.person().cache_permission("hardware_update_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    @JsonIgnore @Transient public void check_activate_permission() throws _Base_Result_Exception  {
        try {

            // Its not possible activate activated device!!!
            if(dominant_entity) {
                throw new Result_Error_Bad_request("Its not possible active device, which is already activated");
            }

            // Its not possibkle ative device, if another copy of same device is active - user have to deactivate that first!
            // For safety  - system will try to find all of them
            List<UUID> ids = Model_Hardware.find.query().where().eq("full_id", full_id).eq("dominant_entity", true).select("id").findIds();

            // Fix Shit situations where we have mote device's with dominance!
            if(ids.size()>1){
                for (int i = 1; i < ids.size(); i++) {
                    Model_Hardware hardware = Model_Hardware.find.byId(ids.get(i));
                    if(hardware != null) {
                        Model_Hardware.cache.remove(ids.get(i));
                        hardware.dominant_entity = false;
                        hardware.update();
                    }
                }
            }

            if(!ids.isEmpty()) {
                Model_Hardware hardware = getById(ids.get(0));
                throw new Result_Error_Bad_request("Its not possible active this device, because its already activated in Project  " + hardware.get_project().name + ". Please, deactivate hardware in project first." );
            }



            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission("hardware_update_" + id)) _BaseController.person().valid_permission("hardware_update_" + id);
            if (_BaseController.person().has_permission(Permission.Hardware_update.name())) return;

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
            get_project().check_update_permission();
            _BaseController.person().cache_permission("hardware_update_" + id, true);

        } catch (_Base_Result_Exception e){
            _BaseController.person().cache_permission("hardware_update_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }


    @JsonIgnore   public boolean first_connect_permission() {
        return project_id() == null;
    }

    public enum Permission {Hardware_create, Hardware_read, Hardware_update, Hardware_edit, Hardware_delete}



/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @Override
    public void save() {

        logger.debug("save - inserting to database");

        if (json_bootloader_core_configuration == null || json_bootloader_core_configuration.equals("{}") || json_bootloader_core_configuration.equals("null") || json_bootloader_core_configuration.length() == 0) {
            json_bootloader_core_configuration = Json.toJson(DM_Board_Bootloader_DefaultConfig.generateConfig()).toString();
        }

        //Default starting state
        this.database_synchronize = true;
        this.developer_kit = false;
        this.backup_mode = bootloader_core_configuration().autobackup;

        super.save();

        //Cache Update
        cache.put(this.id, this);
    }

    @Override
    public void update() {

        logger.debug("update - updating database, id: {}", this.id);

        //Cache Update
        cache.replace(this.id, this);

        if (project_id() != null) new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_Hardware.class, project_id(), this.id))).start();

        //Database Update
        super.update();
    }

    @Override
    public boolean delete() {
        try {

            if (cache.containsKey(this.id))
                cache.remove((this.id));

        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return super.delete();
    }

/* BlOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public String getPath() {
        return get_project().getPath() + "/hardware";
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_Hardware.class)
    public static Cache<UUID, Model_Hardware> cache;

    @CacheField(value = Boolean.class, duration = 300, name ="Model_Hardware_Status")
    public static Cache<UUID, Boolean> cache_status;

    public static Model_Hardware getById(UUID id) throws _Base_Result_Exception {

        Model_Hardware board = cache.get(id);
        if (board == null) {

            board = find.byId(id);
            if (board == null) throw new Result_Error_NotFound(Model_Widget.class);

            cache.put(id, board);
        }

        board.check_read_permission();
        return board;
    }

    /**
     * Specialní vyjímka - vždy vracíme Hardware podle full_id (číslo proceosru) kde
     * máme dominanci! Tuto metodu výlučn používá část systému obsluhující fyzický hardware.
     * @param fullId
     * @return
     */
    public static Model_Hardware getByFullId(String fullId) {
        UUID id = (UUID) find.query().where().eq("full_id", fullId).eq("dominant_entity", true).select("id").getId();
        return  getById(id);
    }

    @JsonIgnore
    public boolean is_online_get_from_cache() {

        Boolean status = cache_status.get(id);

        if (status == null) {

            logger.debug("is_online - online status is not in cache: {}", id);
            try {

                WS_Message_Hardware_online_status result = get_devices_online_state();

                if (result.status.equals("error")) {

                    logger.debug("is_online - hardware_id: {}, device is offline", id);
                    cache_status.put(id, false);
                    return false;

                } else if (result.status.equals("success")) {

                    cache_status.put(id, result.is_device_online(id));
                    return false;
                }

                cache_status.put(id, false );
                return false;


            } catch (NullPointerException e) {
                cache_status.put(id, false );
                return false;
            } catch (Exception e) {
                logger.internalServerError(e);
                return false;
            }
        } else {
            return status;
        }
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Hardware> find = new Finder<>(Model_Hardware.class);
}