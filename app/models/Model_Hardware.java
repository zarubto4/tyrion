package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import exceptions.NotFoundException;
import io.ebean.Expr;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import mongo.ModelMongo_Hardware_ActivationStatus;
import mongo.ModelMongo_Hardware_BackupIncident;
import play.libs.Json;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.cache.Cached;
import utilities.document_mongo_db.document_objects.*;
import utilities.enums.*;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.network.JsonLastOnline;
import utilities.network.JsonNetworkStatus;
import utilities.network.Networkable;
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Button;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.swagger.output.Swagger_Short_Reference;

import javax.persistence.*;
import javax.persistence.Transient;
import java.util.*;

@Entity
@ApiModel(value = "Hardware", description = "Model of Hardware")
@Table(name="Hardware")
public class Model_Hardware extends TaggedModel implements Permissible, UnderProject, Networkable {

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

/* STATIC CONFIG   -----------------------------------------------------------------------------------------------------*/

    public static final String CHANNEL = "hardware";

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

    @JsonIgnore public String wifi_mac_address;
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

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE) public Model_CProgramVersion actual_c_program_version;           // OVěřená verze firmwaru, která na hardwaru běží (Cachováno)
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
    @JsonIgnore @Transient @Cached public String cache_latest_know_ip_address;

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonNetworkStatus @Transient @ApiModelProperty(required = true, value = "Value is cached with asynchronous refresh")
    public NetworkStatus online_state;

    @JsonLastOnline @Transient @ApiModelProperty(required = true, value = "Value is cached with asynchronous refresh")
    public Long latest_online;

    @JsonProperty
    public BackupMode backup_mode() {
        return backup_mode ? BackupMode.AUTO_BACKUP : BackupMode.STATIC_BACKUP;
    }

    @JsonProperty public Swagger_Short_Reference hardware_type() {
        try {
            return this.getHardwareType().ref();
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty public Swagger_Short_Reference producer() {
        try {
            return this.getProducer().ref();
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty public Swagger_Short_Reference project() {
        try {
            return this.getProject().ref();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    public Swagger_Short_Reference cellular() {
        try {
            return this.getGSM().ref();
        } catch (Exception e) {
            // Can be null!
            return null;
        }
    }

    @JsonProperty public Model_BootLoader actual_bootloader() {
        try {
            return getCurrentBootloader();
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_BootLoader bootloader_update_in_progress() {
        try {


            if (idCache().get(Model_hardware_update_update_in_progress_bootloader.class) == null) {
                UUID update_id = (UUID) Model_HardwareUpdate.find.query().where().eq("hardware.id", this.id)
                        .disjunction()
                        .add(Expr.eq("state", HardwareUpdateState.PENDING))
                        .add(Expr.eq("state", HardwareUpdateState.RUNNING))
                        .endJunction()
                        .eq("firmware_type", FirmwareType.BOOTLOADER)
                        .select("id")
                        .setMaxRows(1)
                        .findSingleAttribute();
                if (update_id != null) {
                    System.out.println("Model_hardware_update_update_in_progress_bootloader Model_HardwareUpdate: state:: " + Model_HardwareUpdate.find.byId(update_id).state);
                    idCache().add(Model_hardware_update_update_in_progress_bootloader.class, Model_HardwareUpdate.find.byId(update_id).getBootloaderId());
                }
            }

            if (idCache().get(Model_hardware_update_update_in_progress_bootloader.class) == null) return null;

            return Model_BootLoader.find.byId(idCache().get(Model_hardware_update_update_in_progress_bootloader.class));

        } catch (Exception e) {
            logger.internalServerError(e);
            return null; // Raději true než false aby to uživatel neodpálil další update
        }
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_BootLoader available_latest_bootloader()  {
        try {
            return getHardwareType().main_boot_loader();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null; // Raději true než false aby to uživatel neodpálil další update
        }
    }

    @JsonProperty
    @ApiModelProperty(value = "Optional. Only if the address is cached", required = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String ip_address() {
        try {

            if (cache_latest_know_ip_address != null) {
                return cache_latest_know_ip_address;
            } else {

                // TODO custom serializer
                if(online_state == NetworkStatus.ONLINE){
                    new Thread(() -> {
                        try {

                            logger.warn("Need ip_address for device ID: {}", this.id);
                            // TODO WS_Message_Hardware_overview_Board overview_board = this.get_devices_overview();

                            /*

                            if (overview_board.status.equals("success") && overview_board.online_status) {
                                cache_latest_know_ip_address = overview_board.ip;
                                EchoHandler.addToQueue(new WSM_Echo(Model_Hardware.class, getProject().id, this.id));
                            } else {
                                this.cache_latest_know_ip_address = "";
                            }*/

                        } catch (Exception e) {
                            logger.internalServerError(e);
                        }
                    }).start();
                } else {
                    this.cache_latest_know_ip_address = "";
                }

                return null;
            }
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Swagger_Short_Reference dominant_project_active(){
        try {
            if (dominant_entity) return null;
            UUID uuid = Model_Project.find.query().where().eq("hardware.full_id", full_id).eq("dominant_entity", true).select("id").findSingleAttribute();
            if (uuid == null) return null;

            // Dont Cache IT!!!!!!!!!!!!!!
            Model_Project project = Model_Project.find.byId(uuid);
            return project.ref();

        } catch (NotFoundException e){
            // Uživatel na tento projekt nemá oprávnění - alew i tak by měl vědět že někde existuje
            return new Swagger_Short_Reference(null,"No Permission", "");
        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    @ApiModelProperty(value = "Optional. Only if we have Alert parameters", required = false, readOnly = true)
    public List<BoardAlert> alert_list() {
        try {

            List<BoardAlert> list = new ArrayList<>();

            Model_BootLoader available_latest_bootloader = available_latest_bootloader();

            Model_BootLoader actual_bootloader = actual_bootloader();

            if ((available_latest_bootloader != null && actual_bootloader == null) || (available_latest_bootloader != null && !actual_bootloader.id.equals(available_latest_bootloader.id)))
                list.add(BoardAlert.BOOTLOADER_REQUIRED);
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
                json_bootloader_core_configuration = DM_Board_Bootloader_DefaultConfig.generateConfig().json().toString();
                this.update();
            }

            JsonNode node = Json.parse(json_bootloader_core_configuration);
            DM_Board_Bootloader_DefaultConfig config = formFromJsonWithValidation(DM_Board_Bootloader_DefaultConfig.class, node);

            // Manuálně doplněné hodnoty - ty které nejsou ve statickém Json
            config.autobackup = backup_mode;

            return config;

        } catch (Exception e) {
            logger.internalServerError(e);

            // Set new Default config! The old one is broken!
            json_bootloader_core_configuration = DM_Board_Bootloader_DefaultConfig.generateConfig().json().toString();
            this.update();

            return bootloader_core_configuration();
        }
    }

    @JsonProperty
    public List<Model_HardwareUpdate> required_updates() {

        try {

            List<Model_HardwareUpdate> c_program_plans = Model_HardwareUpdate.find.query().where()
                    .disjunction()
                    .eq("state", HardwareUpdateState.PENDING)
                    .eq("state", HardwareUpdateState.RUNNING)
                    .endJunction()
                    .eq("hardware.id", id).order().asc("created").findList();

            return c_program_plans;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonProperty
    public Model_HomerServer server() {
        try{

            if (connected_server_id == null)
                return null;

            return Model_HomerServer.find.byId(connected_server_id);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;}
    }

    @JsonProperty @ApiModelProperty(value = "Can be null, if device is not in Instance")
    public Swagger_Short_Reference actual_instance() {
        try {
            Model_Instance i = getInstance();

            if (i != null){
                Swagger_Short_Reference instance = i.ref();
                // TODO instance.online_state = i.online_state();
                return instance;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    public Swagger_Short_Reference actual_c_program() {
        try {
            Model_CProgram type = this.getActualCProgram();
            if (type != null) {
                return type.ref();
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    public Swagger_Short_Reference actual_c_program_version() {
        try {
            Model_CProgramVersion version = this.getCurrentFirmware();
            if (version != null) {
                return version.ref();
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    public Swagger_Short_Reference actual_c_program_backup() {
        try{
            Model_CProgram program = this.getBackupCProgram();
            if (program != null) {
                return program.ref();
            } else {
                return null;
            }
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    public Swagger_Short_Reference actual_c_program_backup_version() {
        try {
            Model_CProgramVersion version = this.getCurrentBackup();
            if (version != null) {
                return version.ref();
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public List<Swagger_Short_Reference> hardware_groups() {
        try {
            List<Swagger_Short_Reference> l = new ArrayList<>();

            for (Model_HardwareGroup m : get_hardware_groups()) {
                l.add(m.ref());
            }

            return l;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public String picture_link() {
        try {

            if (this.idCache().get(Model_Blob.class) == null) {
                Model_Blob blob = Model_Blob.find.query().nullable().where().eq("hardware.id",id).findOne();
                if (blob != null) {
                    this.idCache().add(Model_Blob.class,  blob.id);
                }
            }

            if (this.idCache().get(Model_Blob.class) != null) {
                Model_Blob record = Model_Blob.find.byId(this.idCache().get(Model_Blob.class));
                return record.getPublicDownloadLink();
            }

            return null;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE VALUES --------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_HomerServer get_connected_server() {
        if (this.connected_server_id != null) {
            return Model_HomerServer.find.byId(this.connected_server_id);
        }
        return null;
    }

    @JsonIgnore
    public Model_Producer getProducer(){
        try {
            return Model_Producer.find.byId(getProducerId());
        } catch (Exception e) {
            return null;
        }
    }

    @JsonIgnore
    public UUID getProducerId() {
        if (idCache().get(Model_Producer.class) == null) {
            idCache().add(Model_Producer.class, (UUID) Model_Producer.find.query().where().eq("hardware_types.hardware.id", id).select("id").findSingleAttribute());
        }
        return idCache().get(Model_Producer.class);
    }

    @JsonIgnore
    public Model_Instance getInstance(){
        try {

            if(getInstanceId() != null) {
                return Model_Instance.find.byId(getInstanceId());
            }

            return null;
        }catch (Exception e) {
            return null;
        }
    }

    @JsonIgnore
    public UUID getInstanceId(){
        try {
           return connected_instance_id;
        }catch (Exception e) {
            return null;
        }
    }

    @JsonIgnore
    public UUID getActualCProgramId() {

        if (idCache().get(Model_CProgram.class) == null) {

            UUID uuid = Model_CProgram.find.query().where().eq("versions.c_program_version_boards.id", id).select("id").findSingleAttribute();
            if(uuid == null) return null;

            idCache().add(Model_CProgram.class, uuid);
        }

        return idCache().get(Model_CProgram.class);
    }

    @JsonIgnore
    public Model_CProgram getActualCProgram(){
        try {

            UUID id = getActualCProgramId();
            if(id == null) return null;
            return Model_CProgram.find.byId(id);

        }catch (Exception e) {
            return null;
        }

    }

    @JsonIgnore
    public UUID getActualCProgramVersionId(){

        if (idCache().get(Model_CProgramVersion.class) == null) {
            UUID uuid =  Model_CProgramVersion.find.query().where().eq("c_program_version_boards.id", id).select("id").findSingleAttribute();
            if(uuid == null) return null;
            idCache().add(Model_CProgramVersion.class, uuid);
        }

        return idCache().get(Model_CProgramVersion.class);
    }

    @JsonIgnore
    public Model_CProgramVersion getCurrentFirmware() {
        return isLoaded("actual_c_program_version") ? actual_c_program_version : Model_CProgramVersion.find.query().nullable().where().eq("c_program_version_boards.id", id).findOne();
    }

    @JsonIgnore
    public Model_CProgramVersion getCurrentBackup() {
        return isLoaded("actual_backup_c_program_version") ? actual_backup_c_program_version : Model_CProgramVersion.find.query().nullable().where().eq("c_program_version_backup_boards.id", id).findOne();
    }

    @JsonIgnore
    public Model_BootLoader getCurrentBootloader(){
        return isLoaded("actual_boot_loader") ? actual_boot_loader : Model_BootLoader.find.query().nullable().where().eq("hardware.id", id).findOne();
    }

    @JsonIgnore
    public Model_CProgram getBackupCProgram() {
        try {
            return Model_CProgram.find.query().where().eq("versions.c_program_version_backup_boards.id", id).select("id").findSingleAttribute();
        }catch (Exception e) {
            return null;
        }
    }

    @JsonIgnore
    public Model_HardwareType getHardwareType() {
        return isLoaded("hardware_type") ? hardware_type : Model_HardwareType.find.query().where().eq("hardware.id", id).findOne();
    }

    @JsonIgnore
    public UUID getProjectId() {

        if (idCache().get(Model_Project.class) == null) {
            idCache().add(Model_Project.class, (UUID) Model_Project.find.query().where().eq("hardware.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_Project.class);
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return isLoaded("project") ? project : Model_Project.find.query().nullable().where().eq("hardware.id", id).findOne();
    }

    @JsonIgnore
    public UUID getGSM_id() {
        try {
            if (idCache().get(Model_GSM.class) == null) {

                if (bootloader_core_configuration().iccid != null) {

                    Model_GSM gsm = Model_GSM.find.query().nullable().where().icontains("iccid", bootloader_core_configuration().iccid).findOne();
                    if (gsm != null) {
                        idCache().add(Model_CProgramVersion.class, gsm.id);
                    }
                }

            }

            return idCache().get(Model_GSM.class);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public Model_GSM getGSM() {
        try {
            return Model_GSM.find.byId(getGSM_id());
        } catch (Exception e) {
            return null;
        }
    }

    @JsonIgnore
    public List<UUID> get_hardware_group_ids() {

        if (idCache().gets(Model_HardwareGroup.class) == null) {
            idCache().add(Model_HardwareGroup.class,  Model_HardwareGroup.find.query().where().eq("hardware.id", id).select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_HardwareGroup.class) != null ?  idCache().gets(Model_HardwareGroup.class) : new ArrayList<>();
    }

    @JsonIgnore
    public List<Model_HardwareGroup> get_hardware_groups() {
        try {

            List<Model_HardwareGroup> groups  = new ArrayList<>();

            for (UUID group_id : get_hardware_group_ids()) {
                groups.add(Model_HardwareGroup.find.byId(group_id));
            }

            return groups;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public void update_bootloader_configuration(DM_Board_Bootloader_DefaultConfig configuration) {
        this.json_bootloader_core_configuration = Json.toJson(configuration).toString();
        this.update();
    }

    @JsonIgnore @Override
    public Swagger_Short_Reference ref(){
        return new Swagger_Short_Reference(id, name, description, this.tags(), this.online_state);
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    // Online Offline Notification

    /**
     * Send online notification only after long time of offline state
     * @return
     */
    @JsonIgnore
    public Model_Notification notificationOnline() {
        return new Model_Notification()
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.SUCCESS)
                .setText(new Notification_Text().setText("Hardware "))
                .setObject(this)
                .setText(new Notification_Text().setText(" is online."));
    }

    /**
     * Send online notification only after long time of online state
     * @return
     */
    @JsonIgnore
    public Model_Notification notificationOffline() {
        return new Model_Notification()
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.WARNING)
                .setText(new Notification_Text().setText("Hardware "))
                .setObject(this)
                .setText(new Notification_Text().setText(" is offline."));
    }

    @JsonIgnore
    public Model_Notification notificationFirmwareUnstable(Model_CProgramVersion firmware_version) {
        return new Model_Notification()
                .setImportance(NotificationImportance.HIGH)
                .setLevel(NotificationLevel.ERROR)
                .setText(new Notification_Text().setText("Attention! We note the highest critical error on your device "))
                .setObject(this)
                .setText(new Notification_Text().setText(" There was a collapse of the running firmware "))
                .setObject(firmware_version.get_c_program())
                .setText(new Notification_Text().setText(" version "))
                .setObject(firmware_version)
                .setText(new Notification_Text().setText(". But stay calm. The hardware has successfully restarted and uploaded a backup version. " +
                        "This can cause a data collision in your Blocko Program, but you have the chance to fix the firmware. " +
                        "Incorrect version of Firmware has been flagged as unreliable."));
    }

    /**
     * Send Notification, if Connected hardware has unrecognized Firmware on it.
     */
    @JsonIgnore
    public Model_Notification notificationUnknownFirmware () {
        return new Model_Notification()
                .setImportance(NotificationImportance.HIGH)
                .setLevel(NotificationLevel.WARNING)
                .setText(new Notification_Text().setText("Attention! Hardware "))
                .setObject(this)
                .setText(new Notification_Text().setText(" connected successfully, but we have no records about Firmware on this Hardware. " +
                        "Maybe you migrate Hardware from another project to this project "))
                .setObject(getProject())
                .setText(new Notification_Text().setText(" or this is a first time, when Hardware is connected under your account. "))
                .setNewLine()
                .setText(new Notification_Text().setText("Do you want to upload the default firmware for this hardware?"))
                .setButton(new Notification_Button().setAction(NotificationAction.ACCEPT_RESTORE_FIRMWARE).setPayload(getId().toString()).setColor(Becki_color.byzance_green).setText("Yes"))
                .setButton(new Notification_Button().setAction(NotificationAction.REJECT_RESTORE_FIRMWARE).setPayload(getId().toString()).setColor(Becki_color.byzance_red).setText("No"));
    }

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    public void make_log_deactivated() {
        new Thread(() -> {
            try {
                ModelMongo_Hardware_ActivationStatus.create_record(this, false);
            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }).start();
    }
    public void make_log_activated() {
        new Thread(() -> {
            try {
                ModelMongo_Hardware_ActivationStatus.create_record(this, true);
            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }).start();
    }

    public void make_log_backup_arrise_change() {
        new Thread(() -> {
            try {
                ModelMongo_Hardware_BackupIncident.create_record(this);
            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }).start();
    }

/* HELPER CLASS  ----------------------------------------------------------------------------------------------------------*/

    // Používáme protože nemáme rezervní klíč pro cachoání backup c program verze v lokální chache
    public abstract class Model_hardware_update_update_in_progress_bootloader {}

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.HARDWARE;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.ACTIVATE);
    }

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
        this.name = full_id;

        super.save();
    }

    @Override
    public boolean delete() {
        this.dominant_entity = false;
        return super.delete();
    }

/* BlOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public String getPath() {
        return getProject().getPath() + "/hardware";
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_Hardware.class)
    public static CacheFinder<Model_Hardware> find = new CacheFinder<>(Model_Hardware.class);
}