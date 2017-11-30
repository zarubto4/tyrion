package models;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClientException;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import org.mindrot.jbcrypt.BCrypt;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import utilities.Server;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.document_db.document_objects.DM_Board_BackupIncident;
import utilities.document_db.document_objects.DM_Board_Bootloader_DefaultConfig;
import utilities.document_db.document_objects.DM_Board_Connect;
import utilities.document_db.document_objects.DM_Board_Disconnected;
import utilities.enums.*;
import utilities.errors.ErrorCode;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.documentationClass.Swagger_Board_Developer_parameters;
import utilities.swagger.outboundClass.Swagger_Board_for_fast_upload_detail;
import utilities.swagger.outboundClass.*;
import web_socket.message_objects.homer_hardware_with_tyrion.*;
import web_socket.message_objects.homer_hardware_with_tyrion.helps_objects.WS_Help_Hardware_Pair;
import web_socket.message_objects.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Progress;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Online_Change_status;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;
import web_socket.services.WS_HomerServer;
import web_socket.services.helps_class.ParallelTask;

import javax.persistence.*;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;


@Entity
@ApiModel(value = "Board", description = "Model of Board")
@Table(name="Board")
public class Model_Board extends Model {

/* DOCUMENTATION -------------------------------------------------------------------------------------------------------*/

    /*

        Hardware je zastupující entita, která by měla být natolik univerzální, že dokáže pokrýt veškerý supportovaný hardware
        který je Byzance schopna obsluhovat. Rozdílem je Typ desky - který může měnit chování některých metod nebo executiv
        procedur.

        Batch je z Type OfBoards výrobní kolekce, nebo šarže tak aby se dalo trackovat kdo co vyrobil, kdy osadil atd..
     */

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Board.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                        @Id public String id;                  // Full_Id procesoru přiřazené garfieldem Garfieldem
    @JsonIgnore public String hash_for_adding;                                      // Vygenerovaný Hash pro přidávání a párování s Platformou. // Je na QR kodu na hardwaru

                                       public String wifi_mac_address;              // Mac addressa wifi čipu
                                       public String mac_address;                   // Přiřazená MacAdresa z rozsahu Adres

                                       public String name;                          // Jméno, které si uživatel pro hardware nasatvil
    @Column(columnDefinition = "TEXT") public String description;                   // Popisek, který si uživatel na hardwaru nastavil
    @Column(columnDefinition = "TEXT") @JsonIgnore public String json_bootloader_core_configuration; // DM_Board_Configuration.java Počáteční konfigurace - kde je uložený JSON mapovaný pomocí Objektů konkrétního typu hardwaru  //

    @JsonIgnore @OneToOne(fetch = FetchType.LAZY)  public Model_FileRecord picture;
                                       public Date date_of_user_registration;      // Datum, kdy si uživatel desku zaregistroval

    // Parametry při výrobě a osazení a registraci
    @JsonIgnore public Date date_of_create;                 // Datum vytvoření objektu (vypálení dat do procesoru)
    @JsonIgnore public String batch_id;                     // Výrobní šarže
    @JsonIgnore public boolean is_active;                   // Příznak, že deska byla oživena a je použitelná v platformě
    @JsonIgnore public String mqtt_password;                // V BCryptu uložený UUID
    @JsonIgnore public String mqtt_username;                // V BCryptu uložený UUID

    // Parametry konfigurovate uživatelem z frontendu
    // Pozor v Controller_Board jsou parametry v metodě board_update_parameters používány
                 public boolean developer_kit;
    @JsonIgnore  public boolean backup_mode;                 // True znamená automatické zálohování po 5* minutách po nahrátí - verze se označuje mezi hardwarářema jako stabilní  - Opakej je statický backup - vázaný na objekty
                                                             // Pozor tato hodna se také propisuje do json_bootloader_core_configuration!!!!
                 public boolean database_synchronize;        // Defautlní hodnota je True. Je možnost vypnout synchronizaci a to pro případy, že uživatel vypaluje firmwaru lokálně pomocí svého programaátoru a IIDE
                 // public boolean web_view;                    // Podpora webového rozhraní informací o hardwaru ze strany byzance - neuživatelský webserver
                 // public Integer web_port;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)   public Model_TypeOfBoard type_of_board;     // Typ desky - (Cachováno)
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)   public Model_Project project;         // Projekt, pod který Hardware spadá

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)   public Model_VersionObject actual_c_program_version;               // OVěřená verze firmwaru, která na hardwaru běží (Cachováno)
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)   public Model_VersionObject actual_backup_c_program_version;        // Ověřený statický backup - nebo aktuálně zazálohovaný firmware (Cachováno)
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)   public Model_BootLoader    actual_boot_loader;                     // Aktuální bootloader (Cachováno)

    @JsonIgnore @OneToMany(mappedBy="board", cascade=CascadeType.ALL, fetch=FetchType.LAZY) public List<Model_BPair> b_pair = new ArrayList<>();    // Vazba firmwaru a Hardwaru - používáno pro snapshoty v Blocku
    @JsonIgnore @OneToMany(mappedBy="board", cascade=CascadeType.ALL, fetch=FetchType.LAZY) public List<Model_CProgramUpdatePlan> c_program_update_plans = new ArrayList<>();   // Seznam update procedur s tímto hardware.

    @JsonIgnore public String connected_server_id;      // Latest know Server ID
    @JsonIgnore public String connected_instance_id;    // Latest know Instance ID


    // Hardware Groups
    @JsonIgnore @ManyToMany(mappedBy = "boards", fetch = FetchType.LAZY)  public List<Model_BoardGroup> board_groups = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    // For Faster reload
    @Transient @JsonIgnore @TyrionCachedList public Long   cache_value_latest_online;

    @Transient @JsonIgnore @TyrionCachedList public String cache_value_type_of_board_id;                        // Type of Board Id
    @Transient @JsonIgnore @TyrionCachedList public String cache_value_type_of_board_name;

    @Transient @JsonIgnore @TyrionCachedList public String cache_value_picture_id;

    @Transient @JsonIgnore @TyrionCachedList public String cache_value_producer_id;                             // Producer ID

    @Transient @JsonIgnore @TyrionCachedList public String cache_value_project_id;                              // Project

    @Transient @JsonIgnore @TyrionCachedList public String cache_value_actual_boot_loader_id;                   // Bootloader

    @Transient @JsonIgnore @TyrionCachedList public String cache_value_actual_c_program_id;                     // C Program
    @Transient @JsonIgnore @TyrionCachedList public String cache_value_actual_c_program_version_id;

    @Transient @JsonIgnore @TyrionCachedList public String cache_value_actual_c_program_backup_id;              // Backup
    @Transient @JsonIgnore @TyrionCachedList public String cache_value_actual_c_program_backup_version_id;
    @Transient @JsonIgnore @TyrionCachedList public String cache_latest_know_ip_address;
    @Transient @JsonIgnore @TyrionCachedList public List<String> cache_hardware_groups_id;

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty  @Transient  public Enum_Board_BackUpMode backup_mode() { return backup_mode ? Enum_Board_BackUpMode.AUTO_BACKUP : Enum_Board_BackUpMode.STATIC_BACKUP;}

    @JsonProperty  @Transient  public String type_of_board_id()                     { try { return cache_value_type_of_board_id   != null ? cache_value_type_of_board_id: get_type_of_board().id;} catch (NullPointerException e) {return  null;}}
    @JsonProperty  @Transient  public String type_of_board_name()                   { try { return cache_value_type_of_board_name != null ? cache_value_type_of_board_name: get_type_of_board().name;} catch (NullPointerException e) {return  null;}}
    @JsonProperty  @Transient  public String producer_id()                          { try { return cache_value_producer_id   != null ? cache_value_producer_id: get_producer().id.toString();} catch (NullPointerException e) {return  null;}}
    @JsonProperty  @Transient  public String producer_name()                        { try { return get_producer().name; } catch (NullPointerException e) {return  null;}}
    @JsonProperty  @Transient  public String project_id()                           { try { return cache_value_project_id         != null ? cache_value_project_id : get_project().id; } catch (NullPointerException e) {return  null;}}
    @JsonProperty  @Transient  public String actual_bootloader_version_name()       { try { return get_actual_bootloader().name; } catch (NullPointerException e) {return  null;}}
    @JsonProperty  @Transient  public String actual_bootloader_id()                 { try { return cache_value_actual_boot_loader_id != null ? cache_value_actual_boot_loader_id : get_actual_bootloader().id.toString(); } catch (NullPointerException e) {return  null;}}
    @JsonProperty  @Transient  public String bootloader_update_in_progress_bootloader_id()       {
        try {
            // TODO Tohle by šlo ukládat do kešky [TZ]
           Model_CProgramUpdatePlan plan_not_cached = Model_CProgramUpdatePlan.find.where().eq("board.id", this.id).eq("firmware_type", Enum_Firmware_type.BOOTLOADER.name())
                     .disjunction()
                         .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                         .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                         .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                         .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                         .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                         .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_never_connected))
                     .endJunction()
                     .eq("firmware_type", Enum_Firmware_type.BOOTLOADER)
                     .le("actualization_procedure.date_of_planing", new Date())
                     .order().desc("actualization_procedure.date_of_planing")
                     .select("id")
                     .setMaxRows(1)
                     .findUnique();

           if (plan_not_cached != null) {
               Model_CProgramUpdatePlan plan = Model_CProgramUpdatePlan.get_byId(plan_not_cached.id.toString());

               if (plan != null) {
                   return plan.get_bootloader().id.toString();
               }
           }

           return null;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null; // Raději true než false aby to uživatel neodpálil další update
        }
    }

    @JsonProperty  @Transient  public String picture_link() {
        try {

            if ( cache_value_picture_id == null) {

                Model_FileRecord fileRecord = Model_FileRecord.find.where().eq("board.id",id).select("id").findUnique();
                if (fileRecord != null) {
                    cache_value_picture_id =  fileRecord.id;
                }
            }

            if (cache_value_picture_id != null) {
                Model_FileRecord record = Model_FileRecord.get_byId(cache_value_picture_id);
                if (record != null) {
                    return record.get_file_path_for_direct_download_public_link(300);
                }
            }

            return null;
        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }
    @JsonProperty  @Transient  public String available_bootloader_version_name()    { return get_type_of_board().main_boot_loader()  == null ? null :  get_type_of_board().main_boot_loader().name;}
    @JsonProperty  @Transient  public String available_bootloader_id()              { return get_type_of_board().main_boot_loader()  == null ? null :  get_type_of_board().main_boot_loader().id.toString();}
    @JsonProperty  @Transient  public String ip_address() {
        try {

            if (cache_latest_know_ip_address != null) {
                return cache_latest_know_ip_address;
            } else {
                return null;
                // TODO - zjistit!!!! A předat frontendu!
            }

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }
    @JsonProperty  @Transient  public List<Enum_Board_Alert> alert_list() {
        try {

            List<Enum_Board_Alert> list = new ArrayList<>();

            if ( ( available_bootloader_id() != null &&  actual_bootloader_id() == null ) || ( available_bootloader_id() != null  && !actual_bootloader_id().equals(available_bootloader_id()) )) list.add(Enum_Board_Alert.BOOTLOADER_REQUIRED);

            return list;
        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonProperty  @Transient public DM_Board_Bootloader_DefaultConfig bootloader_core_configuration() {
        try {

            if (json_bootloader_core_configuration == null || json_bootloader_core_configuration.equals("{}") || json_bootloader_core_configuration.equals("null") || json_bootloader_core_configuration.length() == 0) {
                json_bootloader_core_configuration = Json.toJson(DM_Board_Bootloader_DefaultConfig.generateConfig()).toString();
                this.update();
            }

            JsonNode node = Json.parse(json_bootloader_core_configuration);

            ObjectNode json = (ObjectNode) new ObjectMapper().readTree(json_bootloader_core_configuration);
            final Form<DM_Board_Bootloader_DefaultConfig> form = Form.form(DM_Board_Bootloader_DefaultConfig.class).bind(node);
            if (form.hasErrors()) {
                throw new Exception("bootloader_core_configuration: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());
            }

            DM_Board_Bootloader_DefaultConfig config = form.get();

            // Manuálně doplněné hodnoty - ty které nejsou ve statickém Json
            config.autobackup = backup_mode;

            return config;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    // TODO updates a required_updates předělat do filtru - Updaty nad HW (jeden, projekt, více atd..)
    @JsonProperty  @Transient  public List<Swagger_C_Program_Update_plan_Short_Detail> updates() {

        try {

            List<Swagger_C_Program_Update_plan_Short_Detail> plans = new ArrayList<>();

            for (Model_CProgramUpdatePlan plan : Model_CProgramUpdatePlan.find.where().eq("board.id", this.id).order().desc("actualization_procedure.date_of_create").findList()) {
                try {
                    plans.add(plan.get_short_version_for_board());

                } catch (Exception e) {
                    terminal_logger.internalServerError(e);
                }
            }

            return plans;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty  @Transient  public List<Swagger_C_Program_Update_plan_Short_Detail> required_updates() {

        try {
            List<Model_CProgramUpdatePlan> c_program_plans = Model_CProgramUpdatePlan.find.where()
                    .disjunction()
                    .eq("state", Enum_CProgram_updater_state.in_progress)
                    .eq("state", Enum_CProgram_updater_state.waiting_for_device)
                    .eq("state", Enum_CProgram_updater_state.homer_server_is_offline)
                    .eq("state", Enum_CProgram_updater_state.instance_inaccessible)
                    .endJunction()
                    .eq("board.id", id).order().asc("actualization_procedure.date_of_create").findList();

            List<Swagger_C_Program_Update_plan_Short_Detail> required_c_programs = new ArrayList<>();

            for (Model_CProgramUpdatePlan plan : c_program_plans) required_c_programs.add(plan.get_short_version_for_board());

            return required_c_programs;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonProperty  @Transient  public Swagger_HomerServer_public_Detail server() { try { if (connected_server_id == null) return null; return Model_HomerServer.get_byId(connected_server_id).get_public_info(); } catch (Exception e) {terminal_logger.internalServerError(e); return null;}}
    @JsonProperty  @Transient @ApiModelProperty(value = "Can be null, if device is not in Instance") public Swagger_Instance_Short_Detail actual_instance() {

        Model_HomerInstance instance = get_instance();
        return instance != null  ? instance.get_instance_short_detail() : null;
    }

    @JsonProperty  @Transient  public String actual_c_program_id() { return cache_value_actual_c_program_id != null ? cache_value_actual_c_program_id : (get_actual_c_program() == null ? null : get_actual_c_program().id);}
    @JsonProperty  @Transient  public String actual_c_program_name() { return get_actual_c_program() == null ? null : get_actual_c_program().name;}
    @JsonProperty  @Transient  public String actual_c_program_description() { return get_actual_c_program() == null ? null :  get_actual_c_program().description;}

    @JsonProperty  @Transient  public String actual_c_program_version_id() { return cache_value_actual_c_program_version_id != null ? cache_value_actual_c_program_version_id : (get_actual_c_program_version() == null ? null : get_actual_c_program_version().id);}
    @JsonProperty  @Transient  public String actual_c_program_version_name() { return get_actual_c_program_version() == null ? null : get_actual_c_program_version().version_name;}
    @JsonProperty  @Transient  public String actual_c_program_version_description() {  return get_actual_c_program_version() == null ? null : get_actual_c_program_version().version_description;}

    @JsonProperty  @Transient  public String actual_c_program_backup_id() {  return cache_value_actual_c_program_backup_id != null ? cache_value_actual_c_program_backup_id : (get_backup_c_program() == null ? null : get_backup_c_program().id); }
    @JsonProperty  @Transient  public String actual_c_program_backup_name() { return get_backup_c_program() == null ? null : get_backup_c_program().name;}
    @JsonProperty  @Transient  public String actual_c_program_backup_description() { return get_backup_c_program() == null ? null : get_backup_c_program().description;}

    @JsonProperty  @Transient  public String actual_c_program_backup_version_id() { return cache_value_actual_c_program_backup_version_id != null ? cache_value_actual_c_program_backup_version_id : (get_backup_c_program_version() == null ? null :  get_backup_c_program_version().id) ;}
    @JsonProperty  @Transient  public String actual_c_program_backup_version_name() { return get_backup_c_program_version() == null ? null :  get_backup_c_program_version().version_name; }
    @JsonProperty  @Transient  public String actual_c_program_backup_version_description() {return get_backup_c_program_version() == null ? null :  get_backup_c_program_version().version_description;}

    @JsonProperty  @Transient @ApiModelProperty(value = "Value is null, if device status is online.") public Long latest_online() {
        if (online_state() == Enum_Online_status.online) return null;
        try {

            if (cache_value_latest_online != null) {
                return cache_value_latest_online;
            }

            List<Document> documents = Server.documentClient.queryDocuments(Server.online_status_collection.getSelfLink(),"SELECT * FROM root r  WHERE r.hardware_id='" + this.id + "' AND r.document_type_sub_type='DEVICE_DISCONNECT'", null).getQueryIterable().toList();

            terminal_logger.debug("last_online: number of retrieved documents = {}", documents.size());

            if (documents.size() > 0) {

                DM_Board_Disconnected record;

                if (documents.size() > 1) {

                    terminal_logger.debug("last_online: more than 1 record, finding latest record");
                    record = documents.stream().max(Comparator.comparingLong(document -> document.toObject(DM_Board_Disconnected.class).time)).get().toObject(DM_Board_Disconnected.class);

                } else {

                    terminal_logger.debug("last_online: result = {}", documents.get(0).toJson());

                    record = documents.get(0).toObject(DM_Board_Disconnected.class);
                }

                terminal_logger.debug("last_online: hardware_id: {}", record.device_id);

                cache_value_latest_online = new Date(record.time).getTime();
                return cache_value_latest_online;
            }

            return Long.MAX_VALUE;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty  @Transient  public Enum_Online_status online_state() {

        try {
            // Pokud Tyrion nezná server ID - to znamená deska se ještě nikdy nepřihlásila - chrání to proti stavu "během výroby"
            // i stavy při vývoji kdy se tvoří zběsile nové desky na dev serverech
            if (connected_server_id == null) {
                return Enum_Online_status.not_yet_first_connected;
            }

            // Pokud je server offline - tyrion si nemuže být jistý stavem hardwaru - ten teoreticky muže být online
            // nebo také né - proto se vrací stav Enum_Online_status - na to reaguje parameter latest_online(),
            // který následně vrací latest know online

            if (Model_HomerServer.get_byId(connected_server_id).online_state() == Enum_Online_status.online) {

                if (cache_status.containsKey(id)) {
                    return cache_status.get(id) ? Enum_Online_status.online : Enum_Online_status.offline;
                } else {
                    // Začnu zjišťovat stav - v separátním vlákně!
                    new Thread(() -> {
                        try {

                            write_without_confirmation(new WS_Message_Hardware_online_status().make_request(new ArrayList<String>() {{
                                add(id);
                            }}));

                        } catch (Exception e) {
                            terminal_logger.internalServerError(e);
                        }
                    }).start();

                    return Enum_Online_status.synchronization_in_progress;
                }
            } else {
                return Enum_Online_status.unknown_lost_connection_with_server;
            }

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return Enum_Online_status.offline;
        }
    }

    @JsonProperty @ApiModelProperty(required = true)
    public List<Swagger_HardwareGroup_Short_Detail> hardware_groups() {
        List<Swagger_HardwareGroup_Short_Detail> l = new ArrayList<>();
        for (Model_BoardGroup m : get_hardware_groups())
            l.add(m.get_group_short_detail());
        return l;
    }

/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_Board_Short_Detail get_short_board() {
        try {

            Swagger_Board_Short_Detail swagger_board_short_detail = new Swagger_Board_Short_Detail();
            swagger_board_short_detail.id = id;
            swagger_board_short_detail.name = name;
            swagger_board_short_detail.description = description;
            swagger_board_short_detail.type_of_board_id = type_of_board_id();
            swagger_board_short_detail.type_of_board_name = type_of_board_name();

            if (project_id() == null && read_permission()) {
                swagger_board_short_detail.hash_for_adding = hash_for_adding;
            }

            swagger_board_short_detail.hardware_groups = hardware_groups();


            swagger_board_short_detail.edit_permission = edit_permission();
            swagger_board_short_detail.delete_permission = delete_permission();
            swagger_board_short_detail.update_permission = update_permission();

            swagger_board_short_detail.alert_list.addAll(alert_list());

            swagger_board_short_detail.online_state = online_state();

            if ( swagger_board_short_detail.online_state != Enum_Online_status.online) swagger_board_short_detail.last_online = latest_online();

            return swagger_board_short_detail;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @Transient @JsonIgnore public Swagger_Board_for_fast_upload_detail get_short_board_for_fast_upload() {
        try {

            Swagger_Board_for_fast_upload_detail board_for_fast_upload_detail = new Swagger_Board_for_fast_upload_detail();
            board_for_fast_upload_detail.id = id;
            board_for_fast_upload_detail.name = name;
            board_for_fast_upload_detail.description = description;

            if (this.get_instance() == null) {
                 board_for_fast_upload_detail.collision = Enum_Board_update_collision.NO_COLLISION;
            } else {
                 board_for_fast_upload_detail.collision = Enum_Board_update_collision.ALREADY_IN_INSTANCE;
            }

            board_for_fast_upload_detail.type_of_board_id = type_of_board_id();
            board_for_fast_upload_detail.type_of_board_name = type_of_board_name();

            return board_for_fast_upload_detail;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @Transient @JsonIgnore public Model_HomerServer get_connected_server() { return Model_HomerServer.get_byId(this.connected_server_id);}

    @JsonIgnore @Transient @TyrionCachedList public Model_Producer get_producer() {

        if (cache_value_producer_id == null) {

            Model_Producer producer = Model_Producer.find.where().eq("type_of_boards.boards.id", id).select("id").findUnique();
            if (producer == null) return null;
            cache_value_producer_id = producer.id.toString();
        }

        return Model_Producer.get_byId(cache_value_producer_id);
    }

    @JsonIgnore @Transient @TyrionCachedList public Model_HomerInstance get_instance() {
        if (connected_instance_id == null) return null;
        return Model_HomerInstance.get_byId(connected_instance_id);
    }

    @JsonIgnore @Transient @TyrionCachedList public Model_CProgram get_actual_c_program() {

        if (cache_value_actual_c_program_id == null) {
            Model_CProgram program = Model_CProgram.find.where().eq("version_objects.c_program_version_boards.id", id).select("id").findUnique();
            if (program == null) return null;
            cache_value_actual_c_program_id = program.id;
        }

        return Model_CProgram.get_byId(cache_value_actual_c_program_id);
    }

    @JsonIgnore @Transient @TyrionCachedList public Model_VersionObject get_actual_c_program_version() {

        if (cache_value_actual_c_program_version_id == null) {
            Model_VersionObject version = Model_VersionObject.find.where().eq("c_program_version_boards.id", id).select("id").findUnique();
            if (version == null) return null;
            cache_value_actual_c_program_version_id = version.id;
        }

        return Model_VersionObject.get_byId(cache_value_actual_c_program_version_id);
    }

    @JsonIgnore @Transient @TyrionCachedList public Model_BootLoader get_actual_bootloader() {

        if (cache_value_actual_boot_loader_id == null) {
            Model_BootLoader bootLoader = Model_BootLoader.find.where().eq("boards.id", id).select("id").findUnique();
            if (bootLoader == null) return null;
            cache_value_actual_boot_loader_id = bootLoader.id.toString();
        }

        return Model_BootLoader.get_byId(cache_value_actual_boot_loader_id);
    }

    @JsonIgnore @Transient @TyrionCachedList public Model_CProgram get_backup_c_program() {

        if (cache_value_actual_c_program_backup_id == null) {
            Model_CProgram program = Model_CProgram.find.where().eq("version_objects.c_program_version_backup_boards.id", id).select("id").findUnique();
            if (program == null) return null;
            cache_value_actual_c_program_backup_id = program.id;
        }

        return Model_CProgram.get_byId(cache_value_actual_c_program_backup_id);
    }

    @JsonIgnore @Transient @TyrionCachedList public Model_VersionObject get_backup_c_program_version() {

        if (cache_value_actual_c_program_backup_version_id == null) {
            Model_VersionObject version = Model_VersionObject.find.where().eq("c_program_version_backup_boards.id", id).select("id").findUnique();
            if (version == null) return null;
            cache_value_actual_c_program_backup_version_id = version.id;
        }

        return Model_VersionObject.get_byId(cache_value_actual_c_program_backup_version_id);
    }

    @JsonIgnore @Transient @TyrionCachedList public Model_TypeOfBoard get_type_of_board() {

        if (cache_value_type_of_board_id == null) {
            Model_TypeOfBoard type_of_board = Model_TypeOfBoard.find.where().eq("boards.id", id).select("id").select("name").findUnique();
            cache_value_type_of_board_id = type_of_board.id;
            cache_value_type_of_board_name = type_of_board.name;
        }

        return Model_TypeOfBoard.get_byId(cache_value_type_of_board_id);
    }

    @JsonIgnore @Transient @TyrionCachedList public Model_Project get_project() {

        if (cache_value_project_id == null) {
            Model_Project project = Model_Project.find.where().eq("boards.id", id).select("id").findUnique();
            if (project == null) return null;
            cache_value_project_id = project.id;
        }

        return Model_Project.get_byId(cache_value_project_id);
    }

    @JsonIgnore @Transient @TyrionCachedList public List<Model_BoardGroup> get_hardware_groups() {

        if (cache_hardware_groups_id == null) {
            List<Model_BoardGroup> groups = Model_BoardGroup.find.where().eq("boards.id", id).select("id").findList();

            if (groups == null) {
                cache_hardware_groups_id = new ArrayList<>();
                return new ArrayList<>();
            } else {
                cache_hardware_groups_id = new ArrayList<>();
                for (Model_BoardGroup group : groups) {
                    cache_hardware_groups_id.add(group.id.toString());
                }
            }
        }

        List<Model_BoardGroup> groups = new ArrayList<>();

        for(String group_id : cache_hardware_groups_id) {
            groups.add(Model_BoardGroup.get_byId(group_id));
        }

        return groups;
    }

    @JsonIgnore @Transient @TyrionCachedList public List<String> get_hardware_groups_ids() {
        if (cache_hardware_groups_id == null) {
            get_hardware_groups();
            return cache_hardware_groups_id;
        } else {
           return cache_hardware_groups_id;
        }
    }

    @JsonIgnore @Transient @TyrionCachedList public boolean update_boot_loader_required() {
        if (get_type_of_board().main_boot_loader == null || get_actual_bootloader() == null) return true;
        return (!this.get_type_of_board().main_boot_loader.id.equals(get_actual_bootloader().id));
    }

/* JSON IGNORE  --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public void update_bootloader_configuration(DM_Board_Bootloader_DefaultConfig configuration) {
        this.json_bootloader_core_configuration = Json.toJson(configuration).toString();
        this.update();
    }

/* SERVER WEBSOCKET ----------------------------------------------------------------------------------------------------*/

    public static final String CHANNEL = "hardware";

    // Messenger
    @JsonIgnore @Transient
    public static void Messages(WS_HomerServer homer, ObjectNode json) {
        new Thread(() -> {
            try {

                switch (json.get("message_type").asText()) {

                    case WS_Message_Hardware_connected.message_type: {

                        final Form<WS_Message_Hardware_connected> form = Form.form(WS_Message_Hardware_connected.class).bind(json);
                        if (form.hasErrors()) {throw new Exception("WS_Message_Hardware_connected: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());}
                        Model_Board.device_Connected(form.get());
                        return;
                    }

                    case WS_Message_Hardware_disconnected.message_type: {

                        final Form<WS_Message_Hardware_disconnected> form = Form.form(WS_Message_Hardware_disconnected.class).bind(json);
                        if (form.hasErrors()) throw new Exception("WS_Message_Hardware_disconnected: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        Model_Board.device_Disconnected(form.get());
                        return;
                    }

                    case WS_Message_Hardware_online_status.message_type: {

                        final Form<WS_Message_Hardware_online_status> form = Form.form(WS_Message_Hardware_online_status.class).bind(json);
                        if (form.hasErrors()) throw new Exception("WS_Message_Hardware_online_status: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        Model_Board.device_online_synchronization(form.get());
                        return;
                    }

                    case WS_Message_Hardware_autobackup_made.message_type: {

                        final Form<WS_Message_Hardware_autobackup_made> form = Form.form(WS_Message_Hardware_autobackup_made.class).bind(json);
                        if (form.hasErrors()) throw new Exception("WS_Message_AutoBackUp_progress: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        Model_Board.device_auto_backup_done_echo(form.get());
                        return;
                    }

                    case WS_Message_Hardware_autobackup_making.message_type: {

                        final Form<WS_Message_Hardware_autobackup_making> form = Form.form(WS_Message_Hardware_autobackup_making.class).bind(json);
                        if (form.hasErrors()) throw new Exception("WS_Message_AutoBackUp_progress: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        Model_Board.device_auto_backup_start_echo(form.get());
                        return;
                    }

                    case WS_Message_Hardware_UpdateProcedure_Progress.message_type: {

                        final Form<WS_Message_Hardware_UpdateProcedure_Progress> form = Form.form(WS_Message_Hardware_UpdateProcedure_Progress.class).bind(json);
                        if (form.hasErrors()) throw new Exception("WS_Message_Hardware_UpdateProcedure_Progress: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        Model_CProgramUpdatePlan.update_procedure_progress(form.get());
                        return;
                    }

                    case WS_Message_Hardware_validation_request.message_type: {

                        final Form<WS_Message_Hardware_validation_request> form = Form.form(WS_Message_Hardware_validation_request.class).bind(json);
                        if (form.hasErrors()) throw new Exception("WS_Message_Hardware_UpdateProcedure_Progress: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                        Model_Board.check_hardware_validation(homer, form.get());
                        return;
                    }

                    // Ignor messages - Jde pravděpodobně o zprávy - které přišly s velkým zpožděním - Tyrion je má ignorovat
                    case WS_Message_Hardware_set_settings.message_type     : {terminal_logger.warn("WS_Message_Hardware_set_settings: A message with a very high delay has arrived.");return;}
                    case WS_Message_Hardware_command_execute.message_type  : {terminal_logger.warn("WS_Message_Hardware_Restart: A message with a very high delay has arrived.");return;}
                    case WS_Message_Hardware_overview.message_type         : {terminal_logger.warn("WS_Message_Hardware_overview: A message with a very high delay has arrived.");return;}
                    case WS_Message_Hardware_change_server.message_type    : {terminal_logger.warn("WS_Message_Hardware_change_server: A message with a very high delay has arrived.");return;}

                    default: {

                        terminal_logger.warn("Incoming Message not recognized::" + json.toString());

                        // Zarážka proti nevadliní odpovědi a zacyklení
                        if (json.has("status") && json.get("status").asText().equals("error")) {
                            return;
                        }

                        homer.write_without_confirmation(json.put("error_message", "message_type not recognized").put("error_code", 400));
                    }
                }

            } catch (Exception e) {
                if (!json.has("message_type")) {
                    homer.write_without_confirmation(json.put("error_message", "Your message not contains message_type").put("error_code", 400));
                    return;
                } else {
                    terminal_logger.internalServerError(e);
                }
            }
        }).start();
    }

    @JsonIgnore @Transient public String get_id() {return id;}

    // Kontrola připojení - Echo o připojení
    @JsonIgnore @Transient public static void device_Connected(WS_Message_Hardware_connected help) {
        try {

            terminal_logger.debug("master_device_Connected:: Updating device ID:: {} is online ", help.hardware_id);

            Model_Board device = Model_Board.get_byId(help.hardware_id);

            if (device == null) {
                terminal_logger.warn("Hardware not found. Message from Homer server: ID = " + help.websocket_identificator + ". Unregistered Hardware Id: " + help.hardware_id);
                return;
            }

            // Aktualizuji cache status online HW
            cache_status.put(help.hardware_id, Boolean.TRUE);

            if (device.project_id() == null) {
                terminal_logger.warn("device_Connected - hardware {} is not in project", device.id);
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
                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Board.class, device.id, true, device.project_id());
            }

            // Nastavím server_id - pokud nekoresponduje s tím, který má HW v databázi uložený
            if (help.websocket_identificator != null && ( device.connected_server_id == null || !device.connected_server_id.equals(help.websocket_identificator))) {
                terminal_logger.debug("master_device_Connected:: Changing server id property to {} ", help.websocket_identificator);
                device.connected_server_id = help.websocket_identificator;
                device.update();
            }

            device.make_log_connect();

            // ZDe do budoucna udělat synchronizaci jen když to bude opravdu potřeba - ale momentálně je nad lidské síly udělat argoritmus,
            // který by vyřešil zbytečné dotazování
            device.hardware_firmware_state_check();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    // Echo o odpojení
    @JsonIgnore @Transient public static void device_Disconnected(WS_Message_Hardware_disconnected help) {
        try {

            terminal_logger.debug("master_device_Disconnected:: Updating device status " +  help.hardware_id + " on offline ");

            // CHACHE OFFLINE
            cache_status.put(help.hardware_id, Boolean.FALSE);

            Model_Board device =  Model_Board.get_byId(help.hardware_id);

            if (device == null) {
                terminal_logger.warn("device_Disconnected:: Hardware not recognized: ID = {} ", help.hardware_id);
                return;
            }

            // Uprava Cache Paměti
            device.cache_value_latest_online = new Date().getTime();

            // Standartní synchronizace
            if (device.project_id() != null) {
                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Board.class, device.id, false, device.project_id());
            }

            // Notifikace
            device.notification_board_disconnect();

            // Záznam do DM databáze
            device.make_log_disconnect();

            Model_Board.cache_status.put(help.hardware_id, false);

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    // Device dělá autobackup
    @JsonIgnore @Transient public static void device_auto_backup_start_echo(WS_Message_Hardware_autobackup_making help) {
        try {

            terminal_logger.debug("device_auto_backup_echo:: Device send Echo about making backup on device ID:: {} ", help.hardware_id);

            Model_Board device = Model_Board.get_byId(help.hardware_id);

            if (device == null) {
                terminal_logger.warn("device_Disconnected:: Hardware not recognized: ID = {} ", help.hardware_id);
                return;
            }

            if (device.developer_kit) {
                // TODO notification
            }
        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    // Device udělal autobackup
    @JsonIgnore @Transient public static void device_auto_backup_done_echo(WS_Message_Hardware_autobackup_made help) {
        try {

            terminal_logger.debug("device_auto_backup_done_echo:: Device send Echo about backup done on device ID:: {} ", help.hardware_id);

            Model_Board device = Model_Board.get_byId(help.hardware_id);

            if (device == null) {
                terminal_logger.warn("device_Disconnected:: Hardware not recognized: ID = {} ", help.hardware_id);
                return;
            }
            Model_VersionObject c_program_version = Model_VersionObject.find.where().eq("c_compilation.firmware_build_id", help.build_id).select("id").findUnique();
            if (c_program_version == null) throw new Exception("Firmware with build ID = " + help.build_id + " was not found in the database!");

            device.actual_backup_c_program_version = c_program_version;
            device.update();

            if (device.developer_kit) {
                // TODO notification
            }
        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    // žádost void o synchronizaci online stavu
    @JsonIgnore @Transient public static void device_online_synchronization(WS_Message_Hardware_online_status report) {
        try {

            for(WS_Message_Hardware_online_status.DeviceStatus status : report.hardware_list) {

                cache_status.put(status.hardware_id, status.online_status);

                // Odešlu echo pomocí websocketu do becki
                Model_Board device = get_byId(status.hardware_id);

                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Board.class, device.id, status.online_status, device.project_id());
            }

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }


    @JsonIgnore @Transient public static void check_hardware_validation(WS_HomerServer homer, WS_Message_Hardware_validation_request request){
        try {

            Model_Board board = request.get_hardware();

            if (BCrypt.checkpw(request.password, board.mqtt_password) && BCrypt.checkpw(request.user_name, board.mqtt_username)){
                homer.write_without_confirmation(request.get_result(true));
            } else {
                homer.write_without_confirmation(request.get_result(false));
            }

        } catch (Exception e){
            terminal_logger.internalServerError(e);
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
    @JsonIgnore @Transient private static List<JsonNode> server_parallel(HashMap<String, ObjectNode> get_result_from_servers, Integer time, Integer delay, Integer number_of_retries) {
        try {

            List<Callable<ObjectNode>> callables = new ArrayList<Callable<ObjectNode>>();

            for (String server_id : get_result_from_servers.keySet()) {

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

    @JsonIgnore @Transient private static HashMap<String, List<String>> server_Separate(List<Model_Board> devices) {

        HashMap<String, List<String>> serverHashMap = new HashMap<>(); // < Model_HomerServer.id, List<Model_Board.id>

        // Separate Board Acording servers
        for(Model_Board device : devices) {

            // Skip never unconnected device
            if (device.connected_server_id == null) continue;

            // If not collection exist -> create that
            if (!serverHashMap.containsKey(device.connected_server_id)) {serverHashMap.put(device.connected_server_id, new ArrayList<String>());}

            // Add to collection
            serverHashMap.get(device.connected_server_id).add(device.id);
        }

        return serverHashMap;
    }

    // Odesílání zprávy harwaru jde skrze serve, zde je metoda, která pokud to nejde odeslat naplní objekt a vrácí ho
    @JsonIgnore @Transient private ObjectNode write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries) {

        // Response with Error Message
        if (this.connected_server_id == null) {

            ObjectNode request = Json.newObject();
            request.put("message_type", json.get("message_type").asText());
            request.put("message_channel", Model_Board.CHANNEL);
            request.put("error_code", ErrorCode.HOMER_SERVER_NOT_SET_FOR_HARDWARE.error_code());
            request.put("error_message", ErrorCode.HOMER_SERVER_NOT_SET_FOR_HARDWARE.error_message());
            request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
            request.put("websocket_identificator", "unknown");

            return request;
        }

        Model_HomerServer server = Model_HomerServer.get_byId(this.connected_server_id);
        if (server == null) {

            terminal_logger.internalServerError(new Exception("write_with_confirmation:: Hardware " + id + " has not exist server id " + this.connected_server_id + " and it wll be removed!"));

            this.connected_server_id = null;
            this.update();

            ObjectNode request = Json.newObject();
            request.put("message_type", json.get("message_type").asText());
            request.put("message_channel", Model_Board.CHANNEL);
            request.put("error_code", ErrorCode.HOMER_NOT_EXIST.error_code());
            request.put("error_message", ErrorCode.HOMER_NOT_EXIST.error_message());
            request.put("message_id", json.has("message_id") ? json.get("message_id").asText() : "unknown");
            request.put("websocket_identificator", "unknown");

            return request;
        }
        return server.write_with_confirmation(json, time, delay, number_of_retries);
    }

    // Metoda překontroluje odeslání a pak předává objektu - zpráva plave skrze program
    @JsonIgnore @Transient private void write_without_confirmation(ObjectNode json) {

        if (this.connected_server_id == null) {
            return;
        }

        Model_HomerServer server = Model_HomerServer.get_byId(this.connected_server_id);

        if (server == null) {

            terminal_logger.internalServerError(new Exception("write_without_confirmation:: Hardware " + id + " has not exist server id " + this.connected_server_id + " and it wll be removed!"));

            this.connected_server_id = null;
            this.update();

            return;
        }
        server.write_without_confirmation(json);
    }

    // Metoda překontroluje odeslání a pak předává objektu - zpráva plave skrze program
    @JsonIgnore @Transient public void write_without_confirmation(String message_id, ObjectNode json) {

        if (this.connected_server_id == null) {
            return;
        }

        Model_HomerServer server = Model_HomerServer.get_byId(this.connected_server_id);

        if (server == null) {

            terminal_logger.internalServerError(new Exception("write_without_confirmation::message_id " + message_id + " Hardware " + id + " has not exist server id " + this.connected_server_id + " and it wll be removed!"));

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

    @JsonIgnore @Transient public static WS_Message_Hardware_online_status get_devices_online_state(List<Model_Board> devices) {

        // Sepparate Device by servers
        HashMap<String, List<String>> serverHashMap = server_Separate(devices);

        // Create Server Parralell command
        HashMap<String, ObjectNode> request_collection = new HashMap<>();
        for(String key : serverHashMap.keySet()) {
            request_collection.put(key, new WS_Message_Hardware_online_status().make_request(serverHashMap.get(key)));
        }

        // Make Parallel Operation
        List<JsonNode> results = server_parallel(request_collection,1000 * 10, 0, 1 );

        // Common Result for fill
        WS_Message_Hardware_online_status result = new WS_Message_Hardware_online_status();

        for(JsonNode json_result : results ) {
            try {

                final Form<WS_Message_Hardware_online_status> form = Form.form(WS_Message_Hardware_online_status.class).bind(json_result);
                if (form.hasErrors()) throw new Exception("WS_Message_Hardware_online_status: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

                result.hardware_list.addAll(form.get().hardware_list);

            } catch (Exception e) {
                terminal_logger.internalServerError(e);
                return new WS_Message_Hardware_online_status();
            }
        }
        return result;
    }

    //-- Over View Hardware  --//
    @JsonIgnore @Transient public WS_Message_Hardware_overview_Board get_devices_overview() {

        WS_Message_Hardware_overview overview = get_devices_overview(Collections.singletonList(this));
        if (overview.get_device_from_list(id) != null) {
            return overview.get_device_from_list(id);
        }

        return new WS_Message_Hardware_overview_Board();
    }

    @JsonIgnore @Transient public static WS_Message_Hardware_overview get_devices_overview(List<Model_Board> devices) {

        // Separate Device by servers
        HashMap<String, List<String>> serverHashMap = server_Separate(devices);

        // Create Server Parallel command
        HashMap<String, ObjectNode> request_collection = new HashMap<>();
        for(String key : serverHashMap.keySet()) {
            request_collection.put(key, new WS_Message_Hardware_overview().make_request(serverHashMap.get(key)) );
        }

        // Make Parallel Operation
        List<JsonNode> results = server_parallel(request_collection,1000 * 10, 0, 1 );

        // Common Result for fill
        WS_Message_Hardware_overview result = new WS_Message_Hardware_overview();
        result.status = "success";

        for (JsonNode json_result : results) {
            try {

                final Form<WS_Message_Hardware_overview> form = Form.form(WS_Message_Hardware_overview.class).bind(json_result);
                if (form.hasErrors()) throw new Exception("WS_Message_Hardware_overview: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());
                WS_Message_Hardware_overview overview = form.get();

                // TODO should the exception be really thrown here? [AT]
                if (!overview.status.equals("success") && overview.hardware_list.isEmpty()) {
                    throw new Exception("WS_Message_Hardware_overview: Status is not success or hw list is empty");
                }

                result.hardware_list.addAll(overview.hardware_list);

            } catch (Exception e) {
                terminal_logger.internalServerError(e);
                return new WS_Message_Hardware_overview();
            }
        }

        return result;
    }

    // Change Hardware Alias  --//
    @JsonIgnore @Transient public WS_Message_Hardware_set_settings set_alias(String alias) {
        try {

            if (!this.name.equals(alias)) {
                this.name = alias;
                this.update();
            }

            JsonNode node = write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this), "ALIAS", alias), 1000 * 5, 0, 2);

            final Form<WS_Message_Hardware_set_settings> form = Form.form(WS_Message_Hardware_set_settings.class).bind(node);
            if (form.hasErrors()) throw new Exception("WS_Message_Hardware_set_settings: Incoming Json from Homer server has not right Form: "  + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
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

            final Form<WS_Message_Hardware_set_settings> form = Form.form(WS_Message_Hardware_set_settings.class).bind(node);
            if (form.hasErrors()) throw new Exception("WS_Message_Hardware_set_settings: Incoming Json from Homer server has not right Form: "  + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new WS_Message_Hardware_set_settings();
        }
    }

    // Set Hardware groups in Hardware Instance on Homer --//
    @JsonIgnore @Transient public WS_Message_Hardware_set_hardware_groups set_hardware_groups_on_hardware(List<String> get_hardware_groups_ids, Enum_type_of_command command) {
        try {

            JsonNode node = write_with_confirmation(new WS_Message_Hardware_set_hardware_groups().make_request(Collections.singletonList(this), get_hardware_groups_ids, command), 1000 * 5, 0, 2);
            final Form<WS_Message_Hardware_set_hardware_groups> form = Form.form(WS_Message_Hardware_set_hardware_groups.class).bind(node);
            if (form.hasErrors()) throw new Exception("WS_Message_Hardware_set_settings: Incoming Json from Homer server has not right Form: "  + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new WS_Message_Hardware_set_hardware_groups();
        }
    }

    // Change Hardware web view port --//
    @JsonIgnore @Transient public WS_Message_Hardware_set_settings set_hardware_configuration_parameter(Swagger_Board_Developer_parameters help) throws IllegalArgumentException, Exception{

            DM_Board_Bootloader_DefaultConfig configuration = this.bootloader_core_configuration();

            for(Field field : configuration.getClass().getFields()) {

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
                        final Form<WS_Message_Hardware_set_settings> form = Form.form(WS_Message_Hardware_set_settings.class).bind(node);
                        if (form.hasErrors()) throw new Exception("WS_Message_Hardware_set_settings: Incoming Json from Homer server has not right Form: "  + form.errorsAsJson(Lang.forCode("en-US")).toString());
                        return form.get();
                    }

                    if (field.getType().getSimpleName().toLowerCase().equals(String.class.getSimpleName().toLowerCase())) {

                        field.set(configuration, help.string_value);
                        this.update_bootloader_configuration(configuration);

                        JsonNode node = write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this), field.getName(), help.string_value), 1000 * 5, 0, 2);
                        final Form<WS_Message_Hardware_set_settings> form = Form.form(WS_Message_Hardware_set_settings.class).bind(node);
                        if (form.hasErrors()) throw new Exception("WS_Message_Hardware_set_settings: Incoming Json from Homer server has not right Form: "  + form.errorsAsJson(Lang.forCode("en-US")).toString());
                        return form.get();
                    }

                    if (field.getType().getSimpleName().toLowerCase().equals(Integer.class.getSimpleName().toLowerCase())) {

                        try {
                            System.out.println("Jaká je integer value:: " + help.integer_value);
                            field.set(configuration, help.integer_value);
                            this.update_bootloader_configuration(configuration);

                            JsonNode node = write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this), field.getName(), help.integer_value), 1000 * 5, 0, 2);
                            final Form<WS_Message_Hardware_set_settings> form = Form.form(WS_Message_Hardware_set_settings.class).bind(node);
                            if (form.hasErrors())
                                throw new Exception("WS_Message_Hardware_set_settings: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());
                            return form.get();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            throw  new IllegalArgumentException("Incoming Value " + help.parameter_type.toLowerCase() + " not recognized");
    }

    //-- ADD or Remove // Change Server --//
    @JsonIgnore @Transient public void device_relocate_server(Model_HomerServer future_server) {
        List<Model_Board> devices = new ArrayList<>();
        devices.add(this);
        device_relocate_server(devices, future_server);
    }

    @JsonIgnore @Transient public void device_relocate_server(List<Model_Board> devices, Model_HomerServer future_server) {
        try {

            // Sepparate Device by servers
            HashMap<String, List<String>> serverHashMap = server_Separate(devices);

            // Create Server Parralell command
            HashMap<String, ObjectNode> request_collection = new HashMap<>();
            for(String key : serverHashMap.keySet()) {
                request_collection.put(key, new WS_Message_Hardware_change_server().make_request(future_server, serverHashMap.get(key)) );
            }

            // Make Parallel Operation
            List<JsonNode> results = server_parallel(request_collection,1000 * 10, 0, 1);

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    //-- Set AutoBackup  --//
    @JsonIgnore @Transient public WS_Message_Hardware_set_settings set_auto_backup() {
        try {

            // 1) změna registru v configur
            DM_Board_Bootloader_DefaultConfig configuration = this.bootloader_core_configuration();
            configuration.autobackup = true;
            this.update_bootloader_configuration(configuration);
            // V databázi
            this.backup_mode = true;
            this.update();

            //Zabít všechny procedury kde je nastaven backup a ještě nebyly provedeny - ty co jsou teprve v plánu budou provedeny standartně
            List<Model_CProgramUpdatePlan> firmware_plans = Model_CProgramUpdatePlan.find.where().eq("board.id", this.id)
                    .disjunction()
                    .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_never_connected))
                    .endJunction()
                    .eq("firmware_type", Enum_Firmware_type.BACKUP.name())
                    .lt("actualization_procedure.date_of_planing", new Date())
                    .order().desc("actualization_procedure.date_of_planing")
                    .select("id")
                    .findList();

            // Zaloha kdyby byly stále platné aktualizace na backup
            for (int i = 0; i < firmware_plans.size(); i++) {
                firmware_plans.get(i).state = Enum_CProgram_updater_state.overwritten;
                firmware_plans.get(i).update();
            }

            JsonNode node = write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this), "AUTOBACKUP", true), 1000*5, 0, 2);

            final Form<WS_Message_Hardware_set_settings> form = Form.form(WS_Message_Hardware_set_settings.class).bind(node);
            if (form.hasErrors()) {
                throw new Exception("WS_Message_Hardware_set_autobackup: Incoming Json from Homer server has not right Form: "  + form.errorsAsJson(Lang.forCode("en-US")).toString());
            }

            return form.get();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new WS_Message_Hardware_set_settings();
        }
    }

    ///-- Send Update Procedure to All Hardware's  --//
    @JsonIgnore @Transient public static void execute_update_plan(Model_CProgramUpdatePlan plan) {
        terminal_logger.debug("execute_update_plan - start execution of plan: {}", plan.id );
        terminal_logger.debug("execute_update_plan - actual state: {} ", plan.state.name());

        if (plan.actualization_procedure.state == Enum_Update_group_procedure_state.complete || plan.actualization_procedure.state == Enum_Update_group_procedure_state.successful_complete ) {
            terminal_logger.debug("execute_update_plan - procedure: {} is done (successful_complete or complete) -> return", plan.actualization_procedure.id);
            return;
        }

        if (plan.actualization_procedure.updates.isEmpty()) {
            plan.actualization_procedure.state = Enum_Update_group_procedure_state.complete_with_error;
            plan.actualization_procedure.update();
            terminal_logger.debug("execute_update_plan - procedure: {} is empty -> return" , plan.actualization_procedure.id);
            return;
        }

        plan.actualization_procedure.state = Enum_Update_group_procedure_state.in_progress;
        plan.actualization_procedure.update();

        try {

            if ( plan.count_of_tries > 5 ) {
                plan.state = Enum_CProgram_updater_state.critical_error;
                plan.error = ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_message();
                plan.error_code = ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_code();
                plan.update();
                terminal_logger.warn("execute_update_plan - Procedure id:: {} plan {} CProgramUpdatePlan:: Error:: {} Message:: {} Continue Cycle. " , plan.actualization_procedure.id , plan.id, ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_code() , ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_message());
                return;
            }

            if (plan.get_board().connected_server_id == null) {
                plan.state = Enum_CProgram_updater_state.homer_server_never_connected;
                plan.update();
                return;
            }

            if (Model_HomerServer.get_byId(plan.get_board().connected_server_id).online_state() != Enum_Online_status.online) {
                terminal_logger.warn("execute_update_procedure - Procedure id:: {}  plan {}  Server {} is offline. Putting off the task for later. -> Return. ", plan.actualization_procedure.id , plan.id, Model_HomerServer.get_byId(plan.get_board().connected_server_id).personal_server_name);
                plan.state = Enum_CProgram_updater_state.homer_server_is_offline;
                plan.update();
                return;
            }

            plan.state = Enum_CProgram_updater_state.in_progress;
            plan.update();

            Model_HomerServer.get_byId(plan.get_board().connected_server_id).update_devices_firmware(Collections.singletonList(plan.get_brief_for_update_homer_server()));

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            plan.state = Enum_CProgram_updater_state.critical_error;
            plan.update();
        }
    }

    @JsonIgnore @Transient public static void execute_update_procedure(Model_ActualizationProcedure procedure) {

        terminal_logger.debug("execute_update_procedure - start execution of procedure: {}", procedure.id );
        terminal_logger.debug("execute_update_procedure - actual state: {} ", procedure.state.name());

        if (procedure.state == Enum_Update_group_procedure_state.complete || procedure.state == Enum_Update_group_procedure_state.successful_complete ) {
            terminal_logger.debug("execute_update_procedure - procedure: {} is done (successful_complete or complete) -> return", procedure.id);
            return;
        }

        if (procedure.updates.isEmpty()) {
            procedure.state = Enum_Update_group_procedure_state.complete_with_error;
            procedure.update();
            terminal_logger.debug("execute_update_procedure - procedure: {} is empty -> return" , procedure.id);
            return;
        }

        procedure.state = Enum_Update_group_procedure_state.in_progress;
        procedure.update();

        List<Model_CProgramUpdatePlan> plans = Model_CProgramUpdatePlan.find.where().eq("actualization_procedure.id", procedure.id)
                .disjunction()
                .eq("state", Enum_CProgram_updater_state.not_start_yet)
                .eq("state", Enum_CProgram_updater_state.waiting_for_device)
                .eq("state", Enum_CProgram_updater_state.instance_inaccessible)
                .eq("state", Enum_CProgram_updater_state.homer_server_is_offline)
                .eq("state", Enum_CProgram_updater_state.homer_server_never_connected)
                .eq("state", Enum_CProgram_updater_state.bin_file_not_found)
                .endJunction()
                .findList();

        if (plans.isEmpty()) {
            terminal_logger.debug("execute_update_procedure - Procedure id:: {} all updates is done or in progress. -> Return.", procedure.id );
            return;
        }

        terminal_logger.debug("execute_update_procedure - Procedure id:: {} . Number of C_Procedures By database for execution:: {}" , procedure.id , plans.size());

        HashMap<String, List<Model_CProgramUpdatePlan> > server_device_sort = new HashMap<>();

        for (Model_CProgramUpdatePlan plan : plans) {
            try {

                terminal_logger.debug("execute_update_procedure - Procedure id:: {} plan {} CProgramUpdatePlan:: ID:: {} - New Cycle" , procedure.id , plan.id, plan.id);
                terminal_logger.debug("execute_update_procedure - Procedure id:: {} plan {} CProgramUpdatePlan:: Board ID:: {}" , procedure.id , plan.id,  plan.get_board().id);
                terminal_logger.debug("execute_update_procedure - Procedure id:: {} plan {} CProgramUpdatePlan:: Status:: {} ", procedure.id , plan.id,  plan.state);

                terminal_logger.debug("execute_update_procedure - Procedure id:: {} plan {} CProgramUpdatePlan:: Number of tries {} ", procedure.id , plan.id,  plan.count_of_tries);

                if (plan.count_of_tries == null) {
                    plan.count_of_tries = 0;
                }

                // Pokud HW nemá nastavený Backup Mode - nastaví se
                if (plan.firmware_type == Enum_Firmware_type.BACKUP) {
                    if (!plan.get_board().backup_mode) {
                        // Na Homera Nemusím posílat příkaz o změně registru, protože Homer při updatu Backupu vždy sám přepne register hardwaru pokud tak nahardwaru není
                        // JE to trochu vybočení ze standardu, ale byl problém s tím, že se registr nastavil a pak se mohlo posrat ještě milion věcí.
                        // Takto Homer register mění až ve chvíli, kdy přenese bynárku do bufru hardwaru a řekne udělej z toho backup (což je jen zpoždění jedné mqtt nstrukce)
                        // V Configu
                        DM_Board_Bootloader_DefaultConfig configuration = plan.get_board().bootloader_core_configuration();
                        configuration.autobackup = false;
                        plan.get_board().update_bootloader_configuration(configuration);
                        // V databázi
                        plan.get_board().backup_mode = false;
                        plan.get_board().update();
                    }
                }

                if (plan.count_of_tries > 5) {
                    plan.state = Enum_CProgram_updater_state.critical_error;
                    plan.error = ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_message();
                    plan.error_code = ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_code();
                    plan.update();
                    terminal_logger.warn("execute_update_procedure - Procedure id:: {} plan {} CProgramUpdatePlan:: Error:: {} Message:: {} Continue Cycle. " , procedure.id , plan.id, ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_code() , ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_message());
                    continue;
                }

                if (plan.get_board().connected_server_id == null) {
                    plan.state = Enum_CProgram_updater_state.homer_server_never_connected;
                    plan.update();
                    continue;
                }

                if (!server_device_sort.containsKey(plan.get_board().connected_server_id)) {
                    server_device_sort.put(plan.get_board().connected_server_id, new ArrayList<Model_CProgramUpdatePlan>());
                }

                if (Model_HomerServer.get_byId(plan.get_board().connected_server_id).online_state() != Enum_Online_status.online) {
                    terminal_logger.warn("execute_update_procedure - Procedure id:: {}  plan {}  Server {} is offline. Putting off the task for later. -> Return. ", procedure.id , plan.id, Model_HomerServer.get_byId(plan.board.connected_server_id).personal_server_name);
                    plan.state = Enum_CProgram_updater_state.homer_server_is_offline;
                    plan.update();
                    continue;
                }

                server_device_sort.get(plan.get_board().connected_server_id).add(plan);

                terminal_logger.debug("execute_update_procedure - Procedure id:: {}  plan {} of blocko program is online and connected with Tyrion", procedure.id, plan.id);

                plan.state = Enum_CProgram_updater_state.in_progress;
                plan.update();

            } catch (Exception e) {
                terminal_logger.internalServerError(e);
                plan.state = Enum_CProgram_updater_state.critical_error;
                plan.update();
                break;
            }
        }

        terminal_logger.debug("execute_update_procedure - Summary for actualizations");

        //  new Thread(procedure::notification_update_procedure_start).start();

        for(String server_id : server_device_sort.keySet()) {

            List<Swagger_UpdatePlan_brief_for_homer> tasks = new ArrayList<>();

            for(Model_CProgramUpdatePlan plan : server_device_sort.get(server_id)) {
                tasks.add(plan.get_brief_for_update_homer_server());
            }

            Model_HomerServer.get_byId(server_id).update_devices_firmware(tasks);
        }
    }

    //-- Restart Device Command --//
    @JsonIgnore @Transient public void execute_command(Enum_Board_Command command, boolean priority) {
        try {

            // Priority false - pokud má hardware v ukolníčk předchozí úkony, tento se zařadí do fronty
            // true - všechny přeskočí - muže se stát že pak některé stratí smysl a platnost a homer je zahodí

            JsonNode node = write_with_confirmation(new WS_Message_Hardware_command_execute().make_request(this.id, command, priority), 1000 * 5, 0, 2);

            // Execute Command
            final Form<WS_Message_Hardware_command_execute> form = Form.form(WS_Message_Hardware_command_execute.class).bind(node);
            if (form.hasErrors()) {
                throw new Exception("WS_Message_Hardware_set_autobackup: Incoming Json from Homer server has not right Form: "  + form.errorsAsJson(Lang.forCode("en-US")).toString());
            }
        } catch (Exception e) {
            terminal_logger.internalServerError(e);
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
    @JsonIgnore @Transient  public void hardware_firmware_state_check() {
        try {

            WS_Message_Hardware_overview_Board report = get_devices_overview();

            if (report.error_message != null) {
                terminal_logger.debug("hardware_firmware_state_check - Report Device ID: {} contains ErrorCode:: {} ErrorMessage:: {} " , this.id, report.error_code, report.error_message);

                if (report.error_code.equals(ErrorCode.HARDWARE_IS_OFFLINE.error_code())) {
                    terminal_logger.debug("hardware_firmware_state_check -: Report Device ID: {} is offline" , this.id);
                    return;
                }
            }

            if (!report.status.equals("success")) {
                terminal_logger.warn("hardware_firmware_state_check: WS_Help_Hardware_board_overview something is wrong on device {}" , this.id);
                return;
            }

            if (!report.online_state) {
                terminal_logger.debug("hardware_firmware_state_check - device is offline");
                return;
            }

            terminal_logger.debug("hardware_firmware_state_check - Summary information of connected master board: ID = {}", this.id);

            terminal_logger.debug("hardware_firmware_state_check - Settings check ",this.id);
            if (!check_settings(report)) return;

            terminal_logger.debug("hardware_firmware_state_check - Firmware check ",this.id);
            check_firmware(report);

            terminal_logger.debug("hardware_firmware_state_check - Backup check ",this.id);
            check_backup(report);

            terminal_logger.debug("hardware_firmware_state_check - Bootloader check ",this.id);
            check_bootloader(report);

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
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
    @JsonIgnore @Transient private boolean check_settings(WS_Message_Hardware_overview_Board overview) {

        // Pokud uživatel nechce DB synchronizaci ingoruji
        if (!this.database_synchronize) {
            terminal_logger.trace("check_settings - database_synchronize is forbidden - change parameters not allowed!");
            return true;
        }

        // Kontrola zda je stejný
        if (this.backup_mode && !overview.autobackup) {
            terminal_logger.warn("check_settings - inconsistent state: set autobackup from static backup");
            set_auto_backup();
        }

        // Kontrola Aliasu
        if ((overview.alias == null || !overview.alias.equals(this.name)) && name != null) {
            terminal_logger.warn("check_settings - inconsistent state: alias");
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
        for(String hardware_group_id : get_hardware_groups_ids()) {
            // Pokud neobsahuje přidám - ale abych si ušetřil čas - nastavím rovnou celý seznam - Homer si s tím poradí
            if (!overview.hardware_group_ids.contains(hardware_group_id)) {
                set_hardware_groups_on_hardware(get_hardware_groups_ids(), Enum_type_of_command.SET);
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

        outCycle:
        for(Field config_field : configuration.getClass().getFields()) {

            try {
                Field incoming_report_right_filed = null;

                inCycle:
                for (Field incoming_filed : overview.getClass().getFields()) {
                    if (config_field.getName().equals(incoming_filed.getName())) {
                        incoming_report_right_filed = incoming_filed;
                        break inCycle;
                    }
                }

                if (incoming_report_right_filed == null) {
                    continue outCycle;
                }

                if (config_field.getType().getCanonicalName().equals(Boolean.class.getSimpleName().toLowerCase())) {

                    if (config_field.getBoolean(configuration) != incoming_report_right_filed.getBoolean(overview)) {
                        write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this), config_field.getName(), config_field.getBoolean(configuration)), 1000 * 5, 0, 2);
                        change_register = true;
                    }

                    continue outCycle;
                }

                if (config_field.getType().getCanonicalName().toLowerCase().equals(String.class.getSimpleName().toLowerCase())) {

                    if (!config_field.get(configuration).toString().equals(incoming_report_right_filed.get(overview).toString())) {
                        write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this), config_field.getName(), config_field.get(configuration).toString()), 1000 * 5, 0, 2);
                        change_register = true;
                    }

                    continue outCycle;
                }

                if (config_field.getType().getCanonicalName().toLowerCase().equals(Integer.class.getSimpleName().toLowerCase())) {

                    if (config_field.getInt(configuration) != incoming_report_right_filed.getInt(overview)) {
                        write_with_confirmation(new WS_Message_Hardware_set_settings().make_request(Collections.singletonList(this), config_field.getName(), config_field.getInt(configuration)), 1000 * 5, 0, 2);
                        change_register = true;
                    }

                    continue outCycle;
                }
            } catch (Exception e) {
                terminal_logger.internalServerError(e);
                return true;
            }
        }

        if (change_register) {
            // Priority false - protože v ukolníčku má Hardware předchozí úkony k updatu registrů - kdyby byl true,
            // tak všechny přeskočí
            this.execute_command(Enum_Board_Command.RESTART, false);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Pokud máme odchylku od databáze na hardwaru, to jest nesedí firmware_build_id na hW s tím co říká databáze
     * @param overview
     */
    @JsonIgnore @Transient private void check_firmware(WS_Message_Hardware_overview_Board overview) {

        // Pokud uživatel nechce DB synchronizaci ingoruji
        if (!this.database_synchronize) {
            terminal_logger.trace("check_firmware - database_synchronize is forbidden - change parameters not allowed!");
            return;
        }

        if (get_actual_c_program_version() == null) {
            terminal_logger.trace("check_firmware -: Actual firmware by DB not recognized :: ", overview.binaries.firmware.build_id);
        }

        if(overview.binaries.firmware == null) {
            terminal_logger.error("check_firmware -: overview.binaries.firmware is null!!");
            return;
        }

        if(overview.binaries.firmware.build_id == null || overview.binaries.firmware.build_id.equals("")) {
            terminal_logger.error("check_firmware -: overview.binaries.firmware.build_id is null");
            return;
        }

        // Vylistuji seznam úkolů k updatu
        List<Model_CProgramUpdatePlan> firmware_plans = Model_CProgramUpdatePlan.find.where().eq("board.id", this.id)
                .disjunction()
                    .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_never_connected))
                .endJunction()
                .eq("firmware_type", Enum_Firmware_type.FIRMWARE.name())
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
                    firmware_plans.get(i).state = Enum_CProgram_updater_state.overwritten;
                    firmware_plans.get(i).update();
            }
        }

        if (!firmware_plans.isEmpty()) {

            terminal_logger.debug("existují nedokončené procedury");

            Model_CProgramUpdatePlan plan = firmware_plans.get(0);

            terminal_logger.debug("Plan:: {} status: {} ", plan.id, plan.state);

            // Mám shodu firmwaru oproti očekávánemů
            if (get_actual_c_program_version() != null) {

                terminal_logger.debug("Firmware:: Co aktuálně je na HW podle Tyriona??:: {}", get_actual_c_program_version().c_compilation.firmware_build_id);
                terminal_logger.debug("Firmware:: Co aktuálně je na HW podle Homera??:: {}", overview.binaries.firmware.build_id);
                terminal_logger.debug("Firmware:: Co očekává nedokončená procedura??:: {}", plan.c_program_version_for_update.c_compilation.firmware_build_id);

                // Verze se rovnají
                if (overview.binaries.firmware.build_id.equals(plan.c_program_version_for_update.c_compilation.firmware_build_id)) {
                    terminal_logger.debug("check_firmware verze se shodují - tím pádem je procedura dokončená a uzavírám");

                    this.actual_c_program_version = plan.c_program_version_for_update;
                    this.cache_value_actual_c_program_version_id = plan.c_program_version_for_update.id;
                    this.cache_value_actual_c_program_id = plan.c_program_version_for_update.get_c_program().id;
                    this.update();

                    terminal_logger.debug("check_firmware - up to date, procedure is done");
                    plan.state = Enum_CProgram_updater_state.complete;
                    plan.date_of_finish = new Date();
                    plan.update();
                    return;

                } else {
                    terminal_logger.debug("check_firmware verze se neshodují");
                    terminal_logger.debug("check_firmware - need update, system starts a update again, number of tries {}", plan.count_of_tries);
                    plan.state = Enum_CProgram_updater_state.not_start_yet;
                    plan.count_of_tries++;
                    plan.update();
                    execute_update_plan(plan);
                }

            } else {

                terminal_logger.debug("check_firmware - no firmware, system starts a new update");
                plan.state = Enum_CProgram_updater_state.not_start_yet;
                plan.count_of_tries++;
                plan.update();

                execute_update_plan(plan);
            }

            return;
        }

        // Nemám Updaty - ale verze se neshodují
        if (get_actual_c_program_version() != null && firmware_plans.isEmpty()) {

            terminal_logger.debug("check_firmware - no update procedures found, but versions are not equal");
            terminal_logger.debug("check_firmware - current firmware according to Tyrion: {}", get_actual_c_program_version().c_compilation.firmware_build_id);
            terminal_logger.debug("check_firmware - current firmware according to Homer: {}", overview.binaries.firmware.build_id);

            if (!get_actual_c_program_version().c_compilation.firmware_build_id.equals(overview.binaries.firmware.build_id)) {
                // Na HW není to co by na něm mělo být.

                terminal_logger.debug("check_firmware - Different firmware versions versus database");

                if (get_backup_c_program_version() != null && get_backup_c_program_version().c_compilation.firmware_build_id.equals(overview.binaries.bootloader.build_id)) {

                    terminal_logger.warn("check_firmware - We have problem with firmware version. Backup is now running");

                    // Notifikace uživatelovi
                    this.notification_board_unstable_actual_firmware_version(get_actual_c_program_version());

                    // Označit firmare za nestabilní
                    get_actual_c_program_version().c_compilation.status = Enum_Compile_status.hardware_unstable;
                    get_actual_c_program_version().c_compilation.update();

                    // Přemapovat hardware
                    actual_c_program_version = get_backup_c_program_version();

                    this.cache_value_actual_c_program_id = actual_c_program_version.id;                     // C Program
                    this.cache_value_actual_c_program_version_id = actual_c_program_version.id;
                    this.cache_value_actual_c_program_backup_id = actual_c_program_version.id;              // Backup
                    this.cache_value_actual_c_program_backup_version_id  = actual_c_program_version.id;

                    this.update();

                    return;
                }

                // Backup to není
                else {

                    terminal_logger.warn("check_firmware - Wrong version on hardware - or null version on hardware");
                    terminal_logger.warn("check_firmware - Now System set Default Firmware or Firmware by Database!!!");

                    // Nastavuji nový systémový update
                    List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

                    WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                    b_pair.board = this;
                    b_pair.c_program_version = get_actual_c_program_version();

                    b_pairs.add(b_pair);

                    Model_ActualizationProcedure procedure = create_update_procedure(Enum_Firmware_type.FIRMWARE, Enum_Update_type_of_update.AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE, b_pairs);
                    procedure.execute_update_procedure();

                    return;
                }

            } else {
                // Na HW je to co by na něm podle databáze mělo být.
                terminal_logger.debug("check_firmware - hardware is up to date");
                return;
            }
        }

        // Set Defualt Program protože žádný teď nemáš
        if (get_actual_c_program_version() == null && firmware_plans.isEmpty()) {

            terminal_logger.debug("check_firmware - Actual firmware_id is not recognized by the DB - Device has not firmware from Tyrion");
            terminal_logger.debug("check_firmware - Tyrion will try to find Default C Program Main Version for this type of hardware. If is it set, Tyrion will update device to starting state");

            // Ověřím - jestli nemám nově nahraný firmware na Hardwaru (to je ten co je teď výcohí firmware pro aktuální typ hardwaru)
            if ( overview.binaries != null && overview.binaries.firmware != null
                    && overview.binaries.firmware.build_id != null
                    && get_type_of_board().get_main_c_program().default_main_version != null
                    && get_type_of_board().get_main_c_program().default_main_version.c_compilation.firmware_build_id != null
                    && overview.binaries.firmware.build_id.equals(get_type_of_board().get_main_c_program().default_main_version.c_compilation.firmware_build_id)
                    ) {

                terminal_logger.debug("check_firmware - board is brand new, but already has required firmware");

                this.actual_c_program_version = get_type_of_board().get_main_c_program().default_main_version;
                this.actual_backup_c_program_version = get_type_of_board().get_main_c_program().default_main_version; // Udělám rovnou zálohu, protože taková by tam měla být
                this.update();
                return;
            }

            // Nastavím default firmware podle schématu Tyriona!
            // Defaultní firmware je v v backandu určený výchozí program k typu desky.
            if (get_type_of_board().get_main_c_program() != null && get_type_of_board().get_main_c_program().default_main_version != null) {

                terminal_logger.debug("check_firmware - Yes, Default Version for Type Of Device {} is set", get_type_of_board().name);

                List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

                WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                b_pair.board = this;
                b_pair.c_program_version = get_type_of_board().get_main_c_program().default_main_version;

                b_pairs.add(b_pair);

                this.notification_board_not_databased_version();

                Model_ActualizationProcedure procedure = create_update_procedure(Enum_Firmware_type.FIRMWARE, Enum_Update_type_of_update.AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE, b_pairs);
                procedure.execute_update_procedure();

            } else {
                terminal_logger.error("Attention please! This is not a critical bug - Tyrion server is not just set for this type of device! Set main C_Program and version!");
                terminal_logger.error("Default main code version is not set for Type Of Board " + get_type_of_board().name + " please set that!");
            }

            return;
        }
    }

    @JsonIgnore @Transient private void check_backup(WS_Message_Hardware_overview_Board overview) {

        // Pokud uživatel nechce DB synchronizaci ingoruji
        if (!this.database_synchronize) {
            terminal_logger.trace("check_backup - database_synchronize is forbidden - change parameters not allowed!");
            return;
        }

        // když je autobackup tak sere pes - změna autobacku je rovnou z devicu
        if (backup_mode) {
            terminal_logger.trace("check_backup - autobacku is true change parameters not allowed!");
            return;
        }

        terminal_logger.debug("check_backup:: third check backup! - Backup is static!! ");

        // Backup je takový jaký očekává tyrion
        if (get_backup_c_program_version() != null && get_backup_c_program_version().c_compilation.firmware_build_id.equals(overview.binaries.backup.build_id)) {
            terminal_logger.debug("check_backup:: Backup is same as on hardware as on Tyrion");
            return;
        }

        // Backup není - to by se stát nemělo - ale šup sem sním
        if (get_backup_c_program_version() == null) {
            terminal_logger.warn("check_backup:: Static Backup is required - but tyrion not set any backup!");

            if (overview.binaries.backup != null && ( overview.binaries.backup.build_id == null || !overview.binaries.backup.build_id.equals(""))) {

                // Try to find it in User programs and
                Model_VersionObject version_not_cached = Model_VersionObject.find.where().eq("c_compilation.firmware_build_id", overview.binaries.backup.build_id).select("id").findUnique();

                if (version_not_cached != null) {
                    terminal_logger.debug("check_backup:: Ještě nebyla přiřazena žádná Backup verze k HW v Tyrionovi - ale program se podařilo najít");
                    Model_VersionObject cached_version = Model_VersionObject.get_byId(version_not_cached.id);
                    this.actual_backup_c_program_version = cached_version;
                    this.update();
                } else {
                    terminal_logger.warn("check_backup:: Nastal stav, kdy mám statický backup, Tyrion v databázi nic nemá a ani se mi nepodařilo najít program (build_id)"
                            + "který by byl kompatibilní. Což je trochu problém. Uvidíme co nabízí update procedury. \n" +
                            "Možnosti jsou 1.) Nahrát první určený plan na hardware a tato metoda začne v podmínce najdi fungovat \n" +
                            "nebo 2) přepnu do autobacku");
                }
            }
        }

        List<Model_CProgramUpdatePlan> firmware_plans = Model_CProgramUpdatePlan.find.where().eq("board.id", this.id)
                .disjunction()
                    .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_never_connected))
                .endJunction()
                .eq("firmware_type", Enum_Firmware_type.BACKUP.name())
                .lt("actualization_procedure.date_of_planing", new Date())
                .order().desc("actualization_procedure.date_of_planing")
                .findList();

         // Zaloha kdyby byly stále platné aktualizace na backup
        for (int i = 1; i < firmware_plans.size(); i++) {
            firmware_plans.get(i).state = Enum_CProgram_updater_state.overwritten;
            firmware_plans.get(i).update();
        }

        // Kontrola Firmwaru a přepsání starých
        // Je žádoucí přepsat všechny předhozí update plány - ale je nutné se podívat jestli nejsou rozdílné!
        // To jest pokud mám 2 updaty firmwaru pak ten starší zahodím
        // Ale jestli mám udpate firmwaru a backupu pak k tomu dojít nesmí!
        // Poměrně krkolomné řešení a HNUS kod - ale chyba je výjmečná a stává se jen sporadicky těsně před nebo po restartu serveru
        if (firmware_plans.size() > 1) {
            for (int i = 1; i < firmware_plans.size(); i++) {
                firmware_plans.get(i).state = Enum_CProgram_updater_state.overwritten;
                firmware_plans.get(i).update();
            }
        }

        // Mám updaty a tak kontroluji
        if (!firmware_plans.isEmpty()) {

            Model_CProgramUpdatePlan plan = firmware_plans.get(0);

            terminal_logger.debug("check_backup - Actual backup according to Tyrion: {}", get_backup_c_program_version().c_compilation.firmware_build_id);
            terminal_logger.debug("check_backup - Actual backup according to Homer: {}", overview.binaries.backup.build_id);
            terminal_logger.debug("check_backup - incomplete procedure expects: {}", plan.c_program_version_for_update.c_compilation.firmware_build_id);

            if (plan.get_board().get_backup_c_program_version() != null) {

                // Verze se rovnají
                if (get_backup_c_program_version().c_compilation.firmware_build_id.equals(plan.c_program_version_for_update.c_compilation.firmware_build_id)) {

                    terminal_logger.debug("check_backup - up to date, procedure is done");
                    plan.state = Enum_CProgram_updater_state.complete;
                    plan.date_of_finish = new Date();
                    plan.update();

                } else {

                    terminal_logger.debug("check_backup - need update, system starts a new update, number of tries {}", plan.count_of_tries);
                    plan.state = Enum_CProgram_updater_state.not_start_yet;
                    plan.count_of_tries++;
                    plan.update();
                    execute_update_procedure(plan.actualization_procedure);
                }

            } else {

                terminal_logger.debug("check_backup - no backup, system starts a new update");
                plan.state = Enum_CProgram_updater_state.not_start_yet;
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
    }

    @JsonIgnore @Transient private void check_bootloader(WS_Message_Hardware_overview_Board overview) {

        // Pokud uživatel nechce DB synchronizaci ingoruji
        if (!this.database_synchronize) return;

        if (get_actual_bootloader() == null) {
            terminal_logger.trace("check_bootloader -: Actual bootloader_id by DB not recognized :: ", overview.binaries.bootloader.build_id);
        }

        // Vylistuji seznam úkolů k updatu
        List<Model_CProgramUpdatePlan> firmware_plans = Model_CProgramUpdatePlan.find.where().eq("board.id", this.id)
                .disjunction()
                    .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_never_connected))
                .endJunction()
                .eq("firmware_type", Enum_Firmware_type.BOOTLOADER)
                .le("actualization_procedure.date_of_planing", new Date())
                .order().desc("actualization_procedure.date_of_planing")
                .findList();

        // Kontrola Bootloader a přepsání starých
        // Je žádoucí přepsat všechny předhozí update plány - ale je nutné se podívat jestli nejsou rozdílné!
        // To jest pokud mám 2 updaty firmwaru pak ten starší zahodím
        // Ale jestli mám udpate firmwaru a backupu pak k tomu dojít nesmí!
        // Poměrně krkolomné řešení a HNUS kod - ale chyba je výjmečná a stává se jen sporadicky těsně před nebo po restartu serveru
        if (firmware_plans.size() > 1) {
            terminal_logger.trace("check_bootloader:: firmware_plans.size() > 1 ");
            for (int i = 1; i < firmware_plans.size(); i++) {
                terminal_logger.trace("check_bootloader:: overwritten procedure ID {}", firmware_plans.get(i).id);
                firmware_plans.get(i).state = Enum_CProgram_updater_state.overwritten;
                firmware_plans.get(i).update();
            }
        }

        if (!firmware_plans.isEmpty()) {

            Model_CProgramUpdatePlan plan = firmware_plans.get(0);

            if (plan.get_board().get_actual_bootloader() != null) {

                // Verze se rovnají
                if (plan.get_board().get_actual_bootloader().version_identificator.equals(plan.get_bootloader().version_identificator)) {

                    terminal_logger.debug("check_bootloader - up to date, procedure is done");
                    plan.state = Enum_CProgram_updater_state.complete;
                    plan.date_of_finish = new Date();
                    plan.update();

                    this.actual_boot_loader = plan.get_bootloader();
                    this.cache_value_actual_boot_loader_id = plan.get_bootloader().id.toString();
                    update();

                } else {

                    terminal_logger.debug("check_bootloader - need update, system starts a new update, number of tries {}", plan.count_of_tries);
                    plan.state = Enum_CProgram_updater_state.not_start_yet;
                    plan.count_of_tries++;
                    plan.update();
                    execute_update_procedure(plan.actualization_procedure);
                }

            } else {

                terminal_logger.debug("check_bootloader:: no bootloader, system starts a update again");
                plan.state = Enum_CProgram_updater_state.not_start_yet;
                plan.count_of_tries++;
                plan.update();

                execute_update_procedure(plan.actualization_procedure);
            }
        }

        // Pokud na HW opravdu žádný bootloader není a není ani vytvořená update procedura
        if (get_actual_bootloader() == null && firmware_plans.isEmpty()) {

            terminal_logger.debug("check_bootloader:: noo default bootloader on hardware - required automatic update");

            if (get_type_of_board().main_boot_loader() == null) {
                terminal_logger.error("check_bootloader::Main Bootloader for Type Of Board {} is not set for update device {}", this.type_of_board_name(), this.id);
                return;
            }

            List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

            WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
            b_pair.board = this;

            if (this.get_actual_bootloader() == null) b_pair.bootLoader =  get_type_of_board().main_boot_loader();
            else b_pair.bootLoader = this.get_actual_bootloader();

            b_pairs.add(b_pair);

            terminal_logger.debug("check_bootloader:: - creating update procedure for bootloader");
            Model_ActualizationProcedure procedure = create_update_procedure(Enum_Firmware_type.BOOTLOADER, Enum_Update_type_of_update.AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE, b_pairs);
            procedure.execute_update_procedure();
        }
    }

/* UPDATE --------------------------------------------------------------------------------------------------------------*/

    public static Model_ActualizationProcedure create_update_procedure(Enum_Firmware_type firmware_type, Enum_Update_type_of_update type_of_update, List<WS_Help_Hardware_Pair> board_for_update) {

        if (board_for_update == null || board_for_update.isEmpty()) {
            throw new NullPointerException("List<WS_Help_Hardware_Pair> board_for_update) is empty");
        }

        Model_ActualizationProcedure procedure = new Model_ActualizationProcedure();
        procedure.project_id = board_for_update.get(0).board.project_id();
        procedure.state = Enum_Update_group_procedure_state.not_start_yet;
        procedure.type_of_update = type_of_update;

        procedure.save();

        for (WS_Help_Hardware_Pair b_pair : board_for_update) {

            List<Model_CProgramUpdatePlan> procedures_for_overriding = Model_CProgramUpdatePlan
                    .find
                    .where()
                    .eq("firmware_type", firmware_type)
                    .disjunction()
                    .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_never_connected))
                    .endJunction()
                    .eq("board.id", b_pair.board.id).findList();

            for (Model_CProgramUpdatePlan cProgramUpdatePlan : procedures_for_overriding) {
                cProgramUpdatePlan.state = Enum_CProgram_updater_state.overwritten;
                cProgramUpdatePlan.date_of_finish = new Date();
                cProgramUpdatePlan.update();
            }

            Model_CProgramUpdatePlan plan = new Model_CProgramUpdatePlan();
            plan.board = b_pair.board;
            plan.firmware_type = firmware_type;
            plan.actualization_procedure = procedure;

            // Firmware
            if (firmware_type == Enum_Firmware_type.FIRMWARE) {
                plan.c_program_version_for_update = b_pair.c_program_version;
                plan.state = Enum_CProgram_updater_state.not_start_yet;
            }

            // Backup
            else if (firmware_type == Enum_Firmware_type.BACKUP) {
                plan.c_program_version_for_update = b_pair.c_program_version;
                plan.state = Enum_CProgram_updater_state.not_start_yet;
            }

            // Bootloader
            else if (firmware_type == Enum_Firmware_type.BOOTLOADER) {
                plan.bootloader = b_pair.bootLoader;
                plan.state = Enum_CProgram_updater_state.not_start_yet;
            }

            plan.save();
        }

        procedure.refresh();

        return procedure;
    }

/* ONLINE STATUS SYNCHRONIZATION ---------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    // Online Offline Notification
    @JsonIgnore @Transient
    public void notification_board_connect() {
        new Thread( () -> {
            if (project_id() == null) return;

            try {
                new Model_Notification()
                        .setImportance(Enum_Notification_importance.low)
                        .setLevel(Enum_Notification_level.success)
                        .setText(new Notification_Text().setText("Device " + this.id))
                        .setObject(this)
                        .setText(new Notification_Text().setText(" has just connected"))
                        .send_under_project(project_id());

            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }
        }).start();
    }

    @JsonIgnore @Transient
    public void notification_board_disconnect() {
        new Thread( () -> {
            if (project_id() == null) return;
            try {

                new Model_Notification()
                    .setImportance( Enum_Notification_importance.low )
                    .setLevel( Enum_Notification_level.warning)
                    .setText(  new Notification_Text().setText("Device" + this.id ))
                    .setObject(this)
                    .setText( new Notification_Text().setText(" has disconnected."))
                    .send_under_project(project_id());

            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }
        }).start();
    }

    @JsonIgnore @Transient
    public void notification_board_unstable_actual_firmware_version(Model_VersionObject firmware_version) {
        new Thread( () -> {

            // Pokud to není yoda ale device tak neupozorňovat v notifikaci, že je deska offline - zbytečné zatížení
            if (project_id() == null) return;

            try {

                new Model_Notification()
                        .setImportance( Enum_Notification_importance.high)
                        .setLevel(Enum_Notification_level.error)
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
                terminal_logger.internalServerError(e);
            }
        }).start();
    }

    @JsonIgnore @Transient
    public void notification_board_not_databased_version() {
        new Thread( () -> {

            if (project_id() == null) return;

            try {

                new Model_Notification()
                        .setImportance( Enum_Notification_importance.normal)
                        .setLevel(Enum_Notification_level.info)
                        .setText(new Notification_Text().setText("Attention! Device "))
                        .setObject(this)
                        .setText(new Notification_Text().setText(" has logged in. Unfortunately, we do not have a synchronized knowledge of the " +
                                "device status of what device firmware is running on. Perhaps this is the factory setting. " +
                                "We are now updating to the default firmware on device."))
                        .setnewLine()
                        .setText(new Notification_Text().setText("You do not have to do anything. Have a nice day."))
                        .setnewLine()
                        .setText(new Notification_Text().setText("Byzance"))
                        .send_under_project(project_id());



            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }
        }).start();
    }

    // Backup Notification
    @JsonIgnore
    public static void notification_set_static_backup_procedure_first_information_single(Model_BPair board_for_update) {
        try {

            new Thread( () -> {

                new Model_Notification()
                        .setImportance(Enum_Notification_importance.low)
                        .setLevel(Enum_Notification_level.warning)
                        .setText(new Notification_Text().setText("You set Static Backup program: "))
                        .setObject(board_for_update.c_program_version.c_program)
                        .setText(new Notification_Text().setText(", in Version "))
                        .setObject(board_for_update.c_program_version)
                        .setText(new Notification_Text().setText(" for device "))
                        .setObject(board_for_update.board)
                        .setText(new Notification_Text().setText(". "))
                        .setText(new Notification_Text().setText("We show you in hardware overview only what's currently on the device. " +
                                "Each update is assigned to the queue of tasks and will be made as soon as possible or according to schedule. " +
                                "In the details of the instance or hardware overview, you can see the status of each procedure. " +
                                "If the update command was not time-specific (immediately) and the device is online, the data transfer may have already begun."))
                        .send_under_project(board_for_update.board.project_id());

            }).start();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public static void notification_set_static_backup_procedure_first_information_list( List<Model_BPair> board_for_update) {
        try {

            new Thread( () -> {

                if ( board_for_update.size() == 0 )  throw new IllegalArgumentException("notification_set_static_backup_procedure_first_information_list:: List is empty! ");
                if ( board_for_update.size() == 1 ) {
                    notification_set_static_backup_procedure_first_information_single(board_for_update.get(0));
                    return;
                }

                new Model_Notification()
                        .setImportance(Enum_Notification_importance.low)
                        .setLevel(Enum_Notification_level.warning)
                        .setText(new Notification_Text().setText("You set Static Backup program for Hardware Collection (" + board_for_update.size() + "). "))
                        .setText(new Notification_Text().setText("We show you in hardware overview only what's currently on the device. " +
                                "Each update is assigned to the queue of tasks and will be made as soon as possible or according to schedule. " +
                                "In the details of the instance or hardware overview, you can see the status of each procedure. " +
                                "If the update command was not time-specific (immediately) and the device is online, the data transfer may have already begun."))
                        .send_under_project(board_for_update.get(0).board.project_id());

            }).start();

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }


/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    public void make_log_connect() {
        new Thread( () -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_Board_Connect.make_request(this.id), null, true);
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError(e);
            }
        }).start();
    }

    public void make_log_disconnect() {
        new Thread( () -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_Board_Disconnected.make_request(this.id), null, true);
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError(e);
            }
        }).start();
    }

    public void make_log_backup_arrise_change() {
        new Thread( () -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_Board_BackupIncident.make_request_success_backup(this.id), null, true);
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError(e);
            }
        }).start();
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String connection_permission_docs    = "read: If user want connect Project with board, he needs two Permission! Project.update_permission == true and also Board.first_connect_permission == true. " +
                                                                                      " - Or user need combination of static/dynamic permission key and Board.first_connect_permission == true";
    @JsonIgnore @Transient public static final String disconnection_permission_docs = "read: If user want remove Board from Project, he needs one single permission Project.update_permission, where hardware is registered. - Or user need static/dynamic permission key";

    // TODO Cachování oprávnění - Dá se to tu zlepšít obdobně jako třeba v C_Program
    @JsonIgnore   @Transient public boolean create_permission() {  return  Controller_Security.has_token() && Controller_Security.get_person().has_permission("Board_Create"); }
    @JsonProperty @Transient public boolean edit_permission()  {  return  Controller_Security.has_token() && ((project_id() != null && get_project().update_permission()) || Controller_Security.get_person().has_permission("Board_edit")) ;}
    @JsonProperty @Transient public boolean read_permission()  {  return  Controller_Security.has_token() && ((project_id() != null && get_project().read_permission())   || Controller_Security.has_token() && Controller_Security.get_person().has_permission("Board_read"));}
    @JsonProperty @Transient public boolean delete_permission() {  return  Controller_Security.has_token() && ((project_id() != null && get_project().update_permission()) || Controller_Security.has_token() && Controller_Security.get_person().has_permission("Board_delete"));}
    @JsonProperty @Transient public boolean update_permission() {  return  Controller_Security.has_token() && ((project_id() != null && get_project().update_permission()) || Controller_Security.has_token() && Controller_Security.get_person().has_permission("Board_update"));}
    @JsonIgnore   @Transient public boolean first_connect_permission() { return project_id() == null;}

    public enum permissions {Board_read, Board_Create, Board_edit, Board_delete, Board_update}

    public static String generate_hash() {
        String hash = "HW" + UUID.randomUUID().toString().replaceAll("[-]","").substring(0, 24);
        if (Model_Board.find.where().eq("hash_for_adding", hash).findUnique() != null) {
            return generate_hash();
        } else {
            return hash;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @Override
    public void save() {

        terminal_logger.debug("save - inserting to database");

        this.date_of_create = new Date();
        if (hash_for_adding == null) this.hash_for_adding = generate_hash();
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

        terminal_logger.debug("update - updating database, id: {}", this.id);

        //Cache Update
        cache.replace(this.id, this);

        if (project_id() != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Board.class, project_id(), this.id))).start();

        //Database Update
        super.update();
    }

    @Override
    public void delete() {
        try {

            if (cache.containsKey(this.id))
                cache.remove((this.id));

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }

        super.delete();
    }

/* BlOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public String get_path() {
        return get_project().get_path() + "/hardware";
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE        = Model_Board.class.getSimpleName();
    public static final String CACHE_STATUS = Model_Board.class.getSimpleName() + "_STATUS";

    public static Cache<String, Model_Board> cache;         // Server_cache Override during server initialization
    public static Cache<String, Boolean> cache_status;      // Server_cache Override during server initialization

    public static Model_Board get_byId(String id) {

        Model_Board board = cache.get(id);
        if (board == null) {

            board = find.byId(id);
            if (board == null) return null;

            cache.put(id, board);
        }

        return board;
    }

    @JsonIgnore
    public boolean is_online_get_from_cache() {

        Boolean status = cache_status.get(id);

        if (status == null) {

            terminal_logger.debug("is_online - online status is not in cache: {}", id);
            try {

                WS_Message_Hardware_online_status result = get_devices_online_state();

                if (result.status.equals("error")) {

                    terminal_logger.debug("is_online - hardware_id: {}, device is offline", id);
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
                terminal_logger.internalServerError(e);
                return false;
            }
        } else {
            return status;
        }
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String, Model_Board> find = new Finder<>(Model_Board.class);
}