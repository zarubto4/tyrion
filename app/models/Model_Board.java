package models;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import utilities.enums.*;
import utilities.hardware_updater.Actualization_procedure;
import utilities.hardware_updater.Master_Updater;
import utilities.swagger.documentationClass.Swagger_Board_for_fast_upload_detail;
import utilities.swagger.outboundClass.Swagger_Board_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Board_Status;
import utilities.swagger.outboundClass.Swagger_C_Program_Update_plan_Short_Detail;
import utilities.web_socket.WS_HomerServer;
import utilities.web_socket.message_objects.common.abstract_class.WS_AbstractMessageBoard;
import utilities.web_socket.message_objects.homer_instance.*;
import utilities.web_socket.message_objects.homer_tyrion.WS_Is_device_connected;
import utilities.web_socket.message_objects.homer_tyrion.WS_Unregistred_device_connected;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;


@Entity
@ApiModel(description = "Model of Board",
        value = "Board")
public class Model_Board extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

/* CACHE  -------------------------------------------------------------------------------------------------------------*/
    /**
    public void set_cache(){
        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String text = "This is some text";

            md.update(text.getBytes("UTF-8")); // Change this to "UTF-16" if needed
            byte[] digest = md.digest();

            final String hashed = Hashing.sha256()
                    .hashString("your input", StandardCharsets.UTF_8)
                    .toString();

        }catch (NoSuchAlgorithmException e){

        }catch (UnsupportedEncodingException e){

        }

        CacheManagerBuilder<PersistentCacheManager> cacheManagerBuilderAutoCreate = CacheManagerBuilder.newCacheManagerBuilder()
                .with(ClusteringServiceConfigurationBuilder.cluster(URI.create("terracotta://localhost:9510/my-application"))
                        .autoCreate()
                        .resourcePool("resource-pool", 32, MemoryUnit.MB, "primary-server-resource"));

        final PersistentCacheManager cacheManager1 = cacheManagerBuilderAutoCreate.build(false);
        cacheManager1.init();

        CacheConfiguration<Long, String> cacheConfigDedicated = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                        .with(ClusteredResourcePoolBuilder.clusteredDedicated("primary-server-resource", 8, MemoryUnit.MB)))
                .add(ClusteredStoreConfigurationBuilder.withConsistency(Consistency.STRONG))
                .build();

        Cache<Long, String> cacheDedicated = cacheManager1.createCache("my-dedicated-cache", cacheConfigDedicated);

        CacheManagerBuilder<PersistentCacheManager> cacheManagerBuilderExpecting = CacheManagerBuilder.newCacheManagerBuilder()
                .with(ClusteringServiceConfigurationBuilder.cluster(URI.create("terracotta://localhost:9510/my-application"))
                        .expecting()
                        .resourcePool("resource-pool", 32, MemoryUnit.MB, "primary-server-resource"));

        final PersistentCacheManager cacheManager2 = cacheManagerBuilderExpecting.build(false);
        cacheManager2.init();

        CacheConfiguration<Long, String> cacheConfigUnspecified = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                        .with(ClusteredResourcePoolBuilder.clustered()))
                .add(ClusteredStoreConfigurationBuilder.withConsistency(Consistency.STRONG))
                .build();

        Cache<Long, String> cacheUnspecified = cacheManager2.createCache("my-dedicated-cache", cacheConfigUnspecified);
    }
    */

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                   @Id @ApiModelProperty(required = true)   public String id;                   // Full_Id procesoru přiřazené Garfieldem
                                       @ApiModelProperty(required = true)   public String hash_for_adding;      // Vygenerovaný Hash pro přidávání a párování s Platformou.

                                       @ApiModelProperty(required = true)   public String wifi_mac_address;     // Mac addressa wifi čipu
                                       @ApiModelProperty(required = true)   public String mac_address;          // Přiřazená MacAdresa z rozsahu Adres
                                       @ApiModelProperty(required = true)   public String generationDescription;  // Info  výrobní generaci


    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)   public String personal_description;
                                       @JsonIgnore  @ManyToOne              public Model_TypeOfBoard type_of_board;
                                       @JsonIgnore                          public boolean is_active;
                                       @ApiModelProperty(required = true)   public boolean backup_mode;
                                                                            public Date date_of_create;

                      @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE)   public Model_Project project;

                                                   @JsonIgnore @ManyToOne   public Model_VersionObject actual_c_program_version;
                                                   @JsonIgnore @ManyToOne   public Model_VersionObject actual_backup_c_program_version;
                                                   @JsonIgnore @ManyToOne   public Model_BootLoader    actual_boot_loader;

    @JsonIgnore @OneToMany(mappedBy="board", cascade=CascadeType.ALL, fetch=FetchType.LAZY) public List<Model_BPair> b_pair = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="board", cascade=CascadeType.ALL, fetch=FetchType.LAZY) public List<Model_CProgramUpdatePlan> c_program_update_plans;



    /**
     * Propojení pokud HW není připojen do intnace - ale potřebuji na něj referenci - je ve vrituální instanci
     */
    @JsonIgnore @ManyToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY)   public Model_HomerInstance virtual_instance_under_project;

    /**
     * Když device nemá majitele - ale připojí se do internetu - někam se připojí - zde je záznam kam naposledy se připojil.
     * Pokud je nutné desku přeregistrovat - podle tohohoto záznamu jí zle dohledat a požádat jí o přeregistrování!
     */
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_HomerServer connected_server;

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String type_of_board_id()   { return type_of_board.id; }
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String type_of_board_name() { return type_of_board.name; }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public boolean main_board()        { return type_of_board.connectible_to_internet; }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String project_id()         { return       project == null ? null : project.id; }
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String project_name()       { return       project == null ? null : project.name; }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_bootloader_version_name()     { return  actual_boot_loader == null ? null : actual_boot_loader.name; }
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String actual_bootloader_id()               { return  actual_boot_loader == null ? null : actual_boot_loader.id;}

    @JsonProperty  @Transient @ApiModelProperty(required = false) public String avaible_bootloader_version_name()   { if(type_of_board.main_boot_loader != null && type_of_board.main_boot_loader.id.equals(actual_bootloader_id())) return type_of_board.main_boot_loader.name; else return null;}
    @JsonProperty  @Transient @ApiModelProperty(required = false) public String avaible_bootloader_id()             { if(type_of_board.main_boot_loader != null && type_of_board.main_boot_loader.id.equals(actual_bootloader_id())) return type_of_board.main_boot_loader.id; else return null;}

    @JsonProperty  @Transient @ApiModelProperty(required = true) public boolean update_boot_loader_required(){

       if(type_of_board.main_boot_loader == null || actual_boot_loader == null) return true;
       return (!this.type_of_board.main_boot_loader.id.equals(this.actual_boot_loader.id));

    }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public List<Swagger_C_Program_Update_plan_Short_Detail> updates(){

        List<Swagger_C_Program_Update_plan_Short_Detail> plans = new ArrayList<>();

        for(Model_CProgramUpdatePlan plan : Model_CProgramUpdatePlan.find.where().eq("board.id", this.id).order().asc("date_of_create").findList())
        plans.add(plan.get_short_version_for_board());

        return plans;
    }


    @JsonProperty  @Transient @ApiModelProperty(required = true) public Swagger_Board_Status status()       {

        logger.debug("Model_Board:: status:: Check Status" + this.id);

        // Složený SQL dotaz pro nalezení funkční běžící instance (B_Pair)
        Model_HomerInstance instance =  get_instance();

        Swagger_Board_Status board_status = new Swagger_Board_Status();
        board_status.status = is_online() ? Board_Status.online : Board_Status.offline;
        if(project == null) board_status.where = Board_Type_of_connection.connected_to_server_unregistered;


        // Stavy Desky--------------------------------------------------------------------------------------------------


        // 3) Je ve Virtuální instanci
        if(instance != null) {

            board_status.where = Board_Type_of_connection.in_person_instance;
            board_status.instance_id = instance.blocko_instance_name;
            board_status.instance_online_status = instance.instance_online();
            if (instance.getB_program() != null) board_status.b_program_id = instance.getB_program().id;
            if (instance.getB_program() != null) board_status.b_program_name = instance.getB_program().name;

            if (instance.actual_instance != null)
                board_status.b_program_version_id = instance.actual_instance.version_object.id;
            if (instance.actual_instance != null)
                board_status.b_program_version_name = instance.actual_instance.version_object.version_name;

            board_status.server_name = instance.cloud_homer_server.personal_server_name;
            board_status.homer_server_id = instance.cloud_homer_server.unique_identificator;
            board_status.server_online_status = instance.cloud_homer_server.server_is_online();
        }else {

            // 1) Není známo kam se deska připojila a nemá instanci
            if(get_instance() == null && get_connected_server() == null) {

                board_status.status = Board_Status.not_yet_first_connected;
                // 2) Je známo kam se deska připojila a nemá instanci - Takže třeba když jí uživatel vyndal z krabičky nahrál na ní něco
            }

            if(get_instance()  == null && get_connected_server() != null) {

                logger.debug("Model_Board:: status:: Check Status:: ");
                board_status.where = Board_Type_of_connection.connected_to_byzance;
                board_status.server_name = connected_server.personal_server_name;
                board_status.homer_server_id = connected_server.unique_identificator;
                board_status.server_online_status = connected_server.server_is_online();

            }

        }

        // 4) Je ve virtuální instanci
        if( get_virtual_instance() != null ){
            board_status.where = Board_Type_of_connection.under_project_virtual_instance;
            board_status.server_name = get_virtual_instance().cloud_homer_server.personal_server_name;
            board_status.homer_server_id = get_virtual_instance().cloud_homer_server.unique_identificator;
        }

        if(actual_c_program_version != null){
            board_status.actual_c_program_id = actual_c_program_version.c_program.id;
            board_status.actual_c_program_name = actual_c_program_version.c_program.name;
            board_status.actual_c_program_version_id = actual_c_program_version.id;
            board_status.actual_c_program_version_name = actual_c_program_version.version_name;
        }

        if(actual_backup_c_program_version != null){
            board_status.actual_backup_c_program_id = actual_backup_c_program_version.c_program.id;
            board_status.actual_backup_c_program_name = actual_backup_c_program_version.c_program.name;
            board_status.actual_backup_c_program_version_id = actual_backup_c_program_version.id;
            board_status.actual_backup_c_program_version_name = actual_backup_c_program_version.version_name;
        }


        List<Model_CProgramUpdatePlan> c_program_plans = Model_CProgramUpdatePlan.find.where()
                .eq("firmware_type", Firmware_type.FIRMWARE)
                .disjunction()
                    .eq("state", Enum_CProgram_updater_state.in_progress)
                    .eq("state", Enum_CProgram_updater_state.waiting_for_device)
                    .eq("state", Enum_CProgram_updater_state.homer_server_is_offline)
                    .eq("state", Enum_CProgram_updater_state.instance_inaccessible)
                .endJunction()
                .eq("board.id", id).order().asc("actualization_procedure.date_of_create").findList();

        for(Model_CProgramUpdatePlan plan : c_program_plans) board_status.required_c_programs.add(plan.get_short_version_for_board());



        List<Model_CProgramUpdatePlan> c_backup_program_plans = Model_CProgramUpdatePlan.find.where()
                .eq("firmware_type", Firmware_type.BACKUP)
                .disjunction()
                .eq("state", Enum_CProgram_updater_state.in_progress)
                .eq("state", Enum_CProgram_updater_state.waiting_for_device)
                .eq("state", Enum_CProgram_updater_state.homer_server_is_offline)
                .eq("state", Enum_CProgram_updater_state.instance_inaccessible)
                .endJunction()
                .eq("board.id", id).order().asc("date_of_create").findList();

        for(Model_CProgramUpdatePlan plan : c_backup_program_plans) board_status.required_backup_c_programs.add(plan.get_short_version_for_board());



        return board_status;

    }


/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_Board_Short_Detail get_short_board(){

        Swagger_Board_Short_Detail swagger_board_short_detail = new Swagger_Board_Short_Detail();
        swagger_board_short_detail.id = id;
        swagger_board_short_detail.personal_description = personal_description;
        swagger_board_short_detail.type_of_board_id = type_of_board_id();
        swagger_board_short_detail.type_of_board_name = type_of_board_name();

        swagger_board_short_detail.edit_permission   = edit_permission();
        swagger_board_short_detail.delete_permission = delete_permission();
        swagger_board_short_detail.update_permission = update_permission();

        swagger_board_short_detail.update_boot_loader_required = update_boot_loader_required();
        swagger_board_short_detail.board_online_status = is_online();

        return swagger_board_short_detail;

    }

    @Transient @JsonIgnore public Swagger_Board_for_fast_upload_detail get_short_board_for_fast_upload(){

        Swagger_Board_for_fast_upload_detail board_for_fast_upload_detail = new Swagger_Board_for_fast_upload_detail();
        board_for_fast_upload_detail.id = id;
        board_for_fast_upload_detail.personal_description = personal_description;

        System.out.println("Board " + id );

        if(this.get_instance() == null){

            System.out.println("Board nemá instanci " );
            board_for_fast_upload_detail.collision = Board_update_collision.NO_COLLISION;

        }else {

            System.out.println("Aktuální instance == " + this.get_instance().blocko_instance_name);
            System.out.println("Aktuální instance typ " + this.get_instance().instance_type);


            if(this.get_instance().instance_type == Homer_Instance_Type.VIRTUAL) board_for_fast_upload_detail.collision = Board_update_collision.NO_COLLISION;
            else                                     board_for_fast_upload_detail.collision = Board_update_collision.ALREADY_IN_INSTANCE;

        }

        board_for_fast_upload_detail.type_of_board_id = type_of_board_id();
        board_for_fast_upload_detail.type_of_board_name = type_of_board_name();

        return board_for_fast_upload_detail;
    }

    @Transient @JsonIgnore public Model_HomerServer get_connected_server(){

        if(connected_server == null){
            connected_server = Model_HomerServer.find.where().eq("latest_know_connected_board.id", this.id).findUnique();
        }

        return connected_server;
    }

    @Transient @JsonIgnore public Model_HomerInstance get_virtual_instance(){

        if(virtual_instance_under_project == null){
            virtual_instance_under_project = Model_HomerInstance.find.where().eq("boards_in_virtual_instance.id", this.id).findUnique();
        }

        return virtual_instance_under_project;
    }


/* BOARD WEBSOCKET CONTROLLING UNDER INSTANCE --------------------------------------------------------------------------*/

    @JsonIgnore @Transient  public boolean is_online(){ // Velmi opatrně s touto proměnou - je časově velmi náročná!!!!!!!!
        try {

            Model_HomerInstance homer_instance = get_instance();

            if(homer_instance == null){
                logger.warn("Board::"+  id + " has not set instance!");
                return false;
            }

            List<String> list = new ArrayList<>();
            list.add(this.id);

            logger.warn("Board::"+  id + " Checking online state!");

            WS_Online_states_devices result = homer_instance.get_devices_online_state(list);

            logger.warn("Board::"+  id + " Přišla odpověď na state devicu status :: " + result.status);

            if( result.status.equals("error")){
                logger.warn("Board::"+  id + " Checking online state! status is Error:: ");
                return false;
            }

            if( result.status.equals("success") ){
                return result.is_device_online(id);
            }

            return false;

        }catch (NullPointerException e){
            return false;
        }catch (Exception e){
            logger.error("Board:: is_online:: Error:: ", e);
            return false;
        }
    }

    @JsonIgnore @Transient  Model_HomerInstance homer_instance = null; // SLouží pouze k uchovávání get_instance()!
    @JsonIgnore @Transient  public Model_HomerInstance get_instance(){

            // Buď zkoumám virutální instnaci
            if (virtual_instance_under_project != null) {

                homer_instance = virtual_instance_under_project;

                // Nebo reálnou instnaci
            } else {

                homer_instance = Model_HomerInstance.find.where().disjunction()
                            .eq("actual_instance.version_object.b_program_hw_groups.main_board_pair.board.id", this.id)
                            .eq("actual_instance.version_object.b_program_hw_groups.device_board_pairs.board.id", this.id)
                        .endJunction().findUnique();
            }

            if(homer_instance == null) return null;
            return homer_instance;
    }


    // Kontrola připojení
    @JsonIgnore @Transient  public static void master_device_Connected(WS_HomerServer server, WS_Yoda_connected help){
        try {

            Model_Board master_device = Model_Board.find.byId(help.deviceId);

            if(master_device == null){
                logger.error("Board:: master_device_Connected:: Unregistered Hardware connected to Blocko cloud_blocko_server:: ", server.identifikator);
                logger.error("Board:: master_device_Connected:: Unregistered Hardware:: ",  help.deviceId);
                return;
            }

            logger.debug("Board:: master_device_Connected:: Board connected to Blocko cloud_blocko_server:: ", help.deviceId);

            // Požádám o kontrolu zda nečeká nějaká nová aktualizační procedura - pro Yodu nebo jeho device
            Model_Board.hardware_firmware_state_check(server, master_device, help);

        }catch (Exception e){
            logger.error("Board:: master_device_Connected:: ERROR::", e);
        }
    }

    @JsonIgnore @Transient  public static void master_device_Disconnected(WS_Yoda_disconnected help){
        try {

            // TODO Chache

        }catch (Exception e){
            logger.error("Board:: master_device_Disconnected:: ERROR:: ", e);
        }
    }

    @JsonIgnore @Transient  public static void device_Connected(WS_HomerServer server, WS_Device_connected help){
        try {

            Model_Board device = Model_Board.find.byId(help.deviceId);

            if(device == null){
                logger.error("Board:: master_device_Connected:: Unregistered Hardware connected to Blocko cloud_blocko_server:: ", server.identifikator);
                logger.error("Board:: master_device_Connected:: Unregistered Hardware:: ",  help.deviceId);
                return;
            }

            Model_Board.hardware_firmware_state_check( server, device, help);
            // Požádám o kontrolu zda nečeká nějaká nová aktualizační procedura - pro Yodu nebo jeho device
           //  Model_Board.hardware_connected(device, help);

        }catch (Exception e){
            logger.error("Board:: device_Connected:: ERROR:: ", e);
        }
    }

    @JsonIgnore @Transient  public static void device_Disconnected(WS_Device_disconnected help){
        try {

            //TODO Chache

        }catch (Exception e){
            logger.error("Board:: device_Disconnected:: ERROR:: ", e);
        }
    }

    @JsonIgnore @Transient public static void unregistred_device_connected(WS_HomerServer homer_server, WS_Unregistred_device_connected report) {
        logger.debug("Model_Board:: unregistred_device_connected:: " + report.deviceId);

        Model_Board board = Model_Board.find.byId(report.deviceId);
        if(board == null){
            logger.warn("Unknown device tries to connect:: " + report.deviceId);
            return;
        }

        if(board.project == null){
            logger.debug("Model_Board:: unregistred_device_connected is registed under server:: " + homer_server.identifikator + " Server name:: " + homer_server.server.personal_server_name);
            board.connected_server =  homer_server.server;
            board.is_active = true;
            board.update();
            return;
        }


        if(board.project != null){
            // Kontrola zda virtuální instance Projektu má stejný server jako je deska teď - Kdyžtak desku přeregistruji jinam!
            if(board.get_instance() != null){
                logger.warn("Board without own instance! " + report.deviceId);
                return;
            }
            if(!board.get_instance().cloud_homer_server.unique_identificator.equals(homer_server.server.unique_identificator)){
                board.device_change_server(board.get_instance().cloud_homer_server);
            }
        }

    }

    @JsonIgnore @Transient public static void update_report_from_homer(WS_Update_device_firmware report){

        for(WS_Update_device_firmware.UpdateDeviceInformation updateDeviceInformation : report.procedure_list){


            for(WS_Update_device_firmware.UpdateDeviceInformation_Device updateDeviceInformation_device : updateDeviceInformation.device_state_list){

                try{

                    Hardware_update_state_from_Homer status = Hardware_update_state_from_Homer.getUpdate_state(updateDeviceInformation_device.update_state);
                    if(status == null) throw new NullPointerException("Hardware_update_state_from_Homer " + updateDeviceInformation_device.update_state + " is not recognize in Json!");

                    Model_Board board = Model_Board.find.byId(updateDeviceInformation_device.deviceId);
                    if(board == null) throw new NullPointerException("Device id" +updateDeviceInformation_device.deviceId + " not found!");

                    Firmware_type firmware_type = Firmware_type.getFirmwareType(updateDeviceInformation_device.firmwareType);
                    if(firmware_type == null) throw new NullPointerException("Firmware_type " +updateDeviceInformation_device.firmwareType + "is not recognize in Json!");


                    Model_CProgramUpdatePlan plan = Model_CProgramUpdatePlan.find.byId(updateDeviceInformation_device.c_program_update_plan_id);
                    if(plan == null) throw new NullPointerException("Plan id" +updateDeviceInformation_device.c_program_update_plan_id + " not found!");


                    if(status == Hardware_update_state_from_Homer.SUCCESSFULLY_UPDATE){
                        plan.state = Enum_CProgram_updater_state.complete;
                        plan.date_of_finish = new Date();
                        plan.update();

                        logger.debug("Model_Board:: Update_report_from_homer:: FirmwareType check");

                        if(firmware_type == Firmware_type.FIRMWARE){

                            logger.debug("Model_Board:: Update_report_from_homer:: Firmware");

                            board.actual_c_program_version = plan.c_program_version_for_update;
                            board.update();
                            continue;
                        }

                        if(firmware_type == Firmware_type.BACKUP){

                            logger.debug("Model_Board:: Update_report_from_homer:: BACKUP");

                            board.actual_backup_c_program_version = plan.c_program_version_for_update;
                            board.backup_mode = false;
                            board.update();
                            continue;
                        }

                        if(firmware_type == Firmware_type.BOOTLOADER){

                            logger.debug("Model_Board:: Update_report_from_homer:: Bootloader");
                            board.actual_boot_loader = plan.bootloader;
                            board.update();
                            continue;
                        }

                        logger.error("Model_Board:: Update_report_from_homer:: ERROR: Its not Firmware, BACKUP or Bootloader!!! ");

                    }

                    if(status == Hardware_update_state_from_Homer.DEVICE_WAS_OFFLINE || status == Hardware_update_state_from_Homer.YODA_WAS_OFFLINE){
                        plan.state = Enum_CProgram_updater_state.waiting_for_device;
                        plan.update();
                        continue;
                    }

                    if(status == Hardware_update_state_from_Homer.DEVICE_WAS_NOT_UPDATED_TO_RIGHT_VERSION){
                        plan.state = Enum_CProgram_updater_state.not_updated;
                        plan.date_of_finish = new Date();
                        plan.update();
                        continue;
                    }

                    // Na závěr vše ostatní je chyba

                    plan.state = Enum_CProgram_updater_state.critical_error;
                    plan.error = updateDeviceInformation_device.error;
                    plan.errorCode = updateDeviceInformation_device.errorCode;
                    plan.date_of_finish = new Date();
                    plan.update();

                }catch (Exception e){
                    logger.error("Model_Board:: update_report_from_homer:: Error:: ", e);
                }
            }
        }



    }

    // Kontrola up_to_date harwaru
    @JsonIgnore @Transient  public static void hardware_firmware_state_check(WS_HomerServer server, Model_Board board, WS_AbstractMessageBoard report) {
        try {

            logger.debug("Model_Board:: hardware_firmware_state_check:: Summary information of connected master board: ", board.id);

            System.out.println("Kontrola Board:: ");
            System.out.println("Kontrola Board Id:: " + board.id);
            System.out.println("Aktuální firmware_id dle HW:: " + report.firmware_build_id);

            if (board.actual_c_program_version == null) System.out.println("Aktuální firmware_id dle DB není znám - ještě není žádný vypálen :( ");
            else System.out.println("Aktuální firmware_id dle DB:: " + board.actual_c_program_version.id);

            if (board.actual_boot_loader == null) System.out.println("Aktuální bootlader_id dle DB není znám :: " + report.bootloader_build_id);
            else System.out.println("Aktuální bootlader_id dle DB:: " + board.actual_boot_loader.version_identificator);


            // Pokusím se najít Aktualizační proceduru jestli existuje s následujícími stavy

            Integer plans_count = Model_CProgramUpdatePlan.find.where().eq("board.id", board.id).disjunction()
                    .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                    .endJunction().findRowCount();


            System.out.println("Kolik mám aktualizačních procedur nažhavených pro dané zařízení:: " + plans_count);

            if (plans_count > 0) {

                System.out.println("Mám jich více než 0");

                List<Model_CProgramUpdatePlan> plans = Model_CProgramUpdatePlan.find.where()
                        .eq("board.id", board.id)
                        .disjunction()
                            .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                        .endJunction().order().desc("date_of_create").findList();

                if(plans.size() > 1){
                    for(int i = 1; i < plans.size(); i++) {
                        plans.get(i).state = Enum_CProgram_updater_state.overwritten;
                        plans.get(i).update();
                    }
                }

                System.out.println("Bubu kontrolovat na co mám plán");

                if (plans.get(0).firmware_type == Firmware_type.FIRMWARE) {

                    logger.debug("Homer_Instance_Record:: check_hardware:: Checking Firmware");


                    // Mám shodu oproti očekávánemů
                    if(plans.get(0).board.actual_c_program_version != null ){

                        // Verze se rovnají
                        if (plans.get(0).board.actual_c_program_version.c_compilation.firmware_build_id.equals(plans.get(0).c_program_version_for_update.c_compilation.firmware_build_id) ) {
                            plans.get(0).state = Enum_CProgram_updater_state.complete;
                            plans.get(0).update();
                        }else {

                            plans.get(0).state = Enum_CProgram_updater_state.in_progress;
                            plans.get(0).update();
                            Master_Updater.add_new_Procedure(plans.get(0).actualization_procedure);
                        }

                    }else {

                        logger.debug("Homer_Instance_Record:: check_hardware:: Checking Firmware - Hardware has Un-databased Value");
                        plans.get(0).state = Enum_CProgram_updater_state.in_progress;
                        plans.get(0).update();

                        Master_Updater.add_new_Procedure(plans.get(0).actualization_procedure);

                    }

                } else if (plans.get(0).firmware_type == Firmware_type.BOOTLOADER) {

                    logger.debug("Homer_Instance_Record:: check_hardware:: Checking Firmware");

                    // Mám shodu oproti očekávánemů
                    if (plans.get(0).bootloader.version_identificator.equals(report.bootloader_build_id)) {

                        plans.get(0).state = Enum_CProgram_updater_state.complete;
                        plans.get(0).update();

                    } else {

                        plans.get(0).state = Enum_CProgram_updater_state.in_progress;
                        plans.get(0).update();

                        Master_Updater.add_new_Procedure(plans.get(0).actualization_procedure);
                    }

                } else if (plans.get(0).firmware_type == Firmware_type.BACKUP) {

                    logger.debug("Homer_Instance_Record:: check_hardware:: Checking Backup");

                    plans.get(0).state = Enum_CProgram_updater_state.complete;
                    plans.get(0).update();
                }


            } else {
                logger.debug("No actualization plan found for Master Device: " + board.id);
            }

            board.notification_board_connect();

            if(report instanceof WS_Yoda_connected){

                WS_Yoda_connected ws_yoda_connected = (WS_Yoda_connected) report;
                for(WS_Device_connected ws_device_connected : ws_yoda_connected.deviceList){
                    device_Connected(server, ws_device_connected);
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @JsonIgnore @Transient public static WS_Update_device_firmware update_devices_firmware(Model_HomerInstance instance, List<Actualization_procedure> procedures){

        try {

            JsonNode node = instance.send_to_instance().write_with_confirmation(new WS_Update_device_firmware().make_request(instance, procedures), 1000 * 30, 0, 3);

            final Form<WS_Update_device_firmware> form = Form.form(WS_Update_device_firmware.class).bind(node);
            if(form.hasErrors()){logger.error("Model_Board:: WS_Update_device_firmware:: Incoming Json for Yoda has not right Form:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Update_device_firmware();}

            return form.get();

        }catch (Exception e){
            return new WS_Update_device_firmware();
        }
    }




    @JsonIgnore @Transient  public void device_change_server(Model_HomerServer homerServer){

        logger.debug("Model_Board:: device_change_server for Device " + this.id);

        ObjectNode request = Json.newObject();
        request.put("messageType", "changeServerDeviceCommand");
        request.put("messageChannel", Model_HomerServer.CHANNEL);
        request.put("mainServerUrl", homerServer.server_url);
        request.put("mqttPort", homerServer.mqtt_port);
        request.put("mqttPassword",homerServer.mqtt_password);
        request.put("mqttUser", homerServer.mqtt_username);


        // Nejdříve vyzkoušíme Server pod virtuální instancí
        if(get_virtual_instance() != null && !get_virtual_instance().cloud_homer_server.unique_identificator.equals(homerServer.unique_identificator)){
            logger.debug("Model_Board:: device_change_server:: Transfer will be from virtual server " + get_virtual_instance().cloud_homer_server.unique_identificator);

           if(!get_virtual_instance().cloud_homer_server.server_is_online()){
               logger.debug("Model_Board:: device_change_server:: Execution is postponed");
           }
        }

        // Poté server posledního záznamu
        if(get_connected_server() != null && !get_connected_server().unique_identificator.equals(homerServer.unique_identificator)){
            logger.debug("Model_Board:: device_change_server:: Transfer will be from last connected server " + get_connected_server().unique_identificator);

            if(!get_connected_server().server_is_online()){
                logger.debug("Model_Board:: device_change_server:: Execution is postponed");
                // TODO
            }

            // TODO
            logger.warn("Model_Board:: device_change_server:: TODO");
            return;
        }

        // Server pod instancí
        if(get_instance() != null && !get_instance().cloud_homer_server.unique_identificator.equals(homerServer.unique_identificator)){
            logger.debug("Model_Board:: device_change_server:: Transfer will be from last know instance" + get_instance().cloud_homer_server.unique_identificator);

            if(!get_instance().cloud_homer_server.server_is_online()){
                logger.debug("Model_Board:: device_change_server:: Execution is postponed");
                // TODO
            }


            // TODO
            logger.warn("Model_Board:: device_change_server:: TODO");
            return;

        }

        logger.debug("Model_Board:: device_change_server:: Server not found - All servers will be checked");
        // Po zé ze zoufalosti zkusím všechny servery popořadě zeptat se zda ho někdo neviděl (JE to záloha selhání nevalidního přepsání!)
        for( Model_HomerServer find_server : Model_HomerServer.find.all()){

            if(!find_server.server_is_online()) continue;

            WS_Is_device_connected result = find_server.is_device_connected(this.id);

            // TODO...

        }


        logger.error("Model_Board:: device_change_server:: Device not found for Transfer!");

    }


    @JsonIgnore @Transient public static void update_bootloader(Enum_Update_type_of_update type_of_update, List<Model_Board> board_for_update, Model_BootLoader boot_loader){
        // Attention!! Value  boot_loader can be null - in this case - system will used

        Model_ActualizationProcedure procedure = new Model_ActualizationProcedure();
        procedure.state = Enum_Update_group_procedure_state.not_start_yet;
        procedure.type_of_update = type_of_update;
        procedure.save();

        for(Model_Board board : board_for_update)
        {
            List<Model_CProgramUpdatePlan>  procedures_for_overriding = Model_CProgramUpdatePlan
                    .find
                    .where()
                    .eq("firmware_type", Firmware_type.BOOTLOADER)
                    .disjunction()
                    .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                    .endJunction()
                    .eq("board.id", board.id).findList();

            for(Model_CProgramUpdatePlan cProgramUpdatePlan: procedures_for_overriding) {
                cProgramUpdatePlan.state = Enum_CProgram_updater_state.overwritten;
                cProgramUpdatePlan.date_of_finish = new Date();
                cProgramUpdatePlan.update();
            }

            Model_BootLoader boot_loader_for_using = null;

            if(boot_loader != null){

                boot_loader_for_using = boot_loader;

            } else {

                boot_loader_for_using = Model_BootLoader.find.where().eq("main_type_of_board.boards.id", board.id).findUnique();

            }

            Model_CProgramUpdatePlan plan = new Model_CProgramUpdatePlan();
            plan.board = board;

            plan.firmware_type = Firmware_type.BOOTLOADER;
            plan.actualization_procedure = procedure;

            if(boot_loader_for_using == null){

                plan.state = Enum_CProgram_updater_state.bin_file_not_found;

            }else {

                plan.bootloader = boot_loader_for_using;
                plan.state = Enum_CProgram_updater_state.not_start_yet;

            }

            plan.save();
        }

        procedure.refresh();
        Master_Updater.add_new_Procedure(procedure);
    }

    @JsonIgnore @Transient public static void update_firmware(Enum_Update_type_of_update type_of_update, List<Model_BPair> board_for_update){

        Model_ActualizationProcedure procedure = new Model_ActualizationProcedure();
        procedure.state = Enum_Update_group_procedure_state.not_start_yet;
        procedure.type_of_update = type_of_update;
        procedure.save();

        for(Model_BPair b_pair : board_for_update){

            List<Model_CProgramUpdatePlan>  procedures_for_overriding = Model_CProgramUpdatePlan
                    .find
                    .where()
                    .eq("firmware_type", Firmware_type.FIRMWARE)
                    .disjunction()
                    .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                    .endJunction()
                    .eq("board.id", b_pair.board.id).findList();

            for(Model_CProgramUpdatePlan cProgramUpdatePlan: procedures_for_overriding) {
                cProgramUpdatePlan.state = Enum_CProgram_updater_state.overwritten;
                cProgramUpdatePlan.date_of_finish = new Date();
                cProgramUpdatePlan.update();
            }

            Model_CProgramUpdatePlan plan = new Model_CProgramUpdatePlan();
            plan.board =  b_pair.board;
            plan.firmware_type = Firmware_type.FIRMWARE;
            plan.actualization_procedure = procedure;


            if( b_pair.c_program_version == null){

                plan.state = Enum_CProgram_updater_state.bin_file_not_found;

            }else {

                plan.c_program_version_for_update = b_pair.c_program_version;
                plan.state = Enum_CProgram_updater_state.not_start_yet;

            }

            plan.save();
        }

        procedure.refresh();

        Master_Updater.add_new_Procedure(procedure);
    }

    @JsonIgnore @Transient  public static void update_backup(Enum_Update_type_of_update type_of_update, List<Model_BPair> board_for_update){

        Model_ActualizationProcedure procedure = new Model_ActualizationProcedure();
        procedure.state = Enum_Update_group_procedure_state.not_start_yet;
        procedure.type_of_update = type_of_update;
        procedure.save();

        if(board_for_update.isEmpty()){
            logger.error("Model_Board:: update_backup:: Array is empty::");
            procedure.state = Enum_Update_group_procedure_state.complete_with_error;
            procedure.update();
            return;
        }

        List<Model_CProgramUpdatePlan> plans = new ArrayList<>();

        for(Model_BPair b_pair : board_for_update) {


            List<Model_CProgramUpdatePlan>  procedures_for_overriding = Model_CProgramUpdatePlan
                    .find
                    .where()
                    .eq("firmware_type", Firmware_type.BACKUP)
                    .disjunction()
                    .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                    .endJunction()
                    .eq("board.id", b_pair.board.id).findList();

            for(Model_CProgramUpdatePlan cProgramUpdatePlan: procedures_for_overriding) {
                cProgramUpdatePlan.state = Enum_CProgram_updater_state.overwritten;
                cProgramUpdatePlan.date_of_finish = new Date();
                cProgramUpdatePlan.update();
            }

            Model_CProgramUpdatePlan plan = new Model_CProgramUpdatePlan();
            plan.board = b_pair.board;
            plan.firmware_type = Firmware_type.BACKUP;
            plan.actualization_procedure = procedure;

            if( b_pair.c_program_version == null){

                plan.state = Enum_CProgram_updater_state.bin_file_not_found;

            }else {

                plan.c_program_version_for_update = b_pair.c_program_version;
                plan.state = Enum_CProgram_updater_state.not_start_yet;

            }


            plan.save();
            plans.add(plan);
        }

        procedure.updates.addAll(plans);
        procedure.update();

        Master_Updater.add_new_Procedure(procedure);
    }

    @JsonIgnore @Transient  public static WS_Board_set_autobackup set_auto_backup(Model_Board board_for_update){
        try{

            Model_HomerInstance instance = board_for_update.get_instance();
            if(instance == null) {
                logger.error("Model_Board:: set_auto_backup:: on DeviceId:: " + board_for_update.id + " has not own instance");

                WS_Board_set_autobackup result = new WS_Board_set_autobackup();
                return result;
            }

            if(!instance.instance_online()){
                logger.error("Model_Board:: set_auto_backup:: instanceId:: " + instance.blocko_instance_name + " is offline");

                WS_Board_set_autobackup result = new WS_Board_set_autobackup();
                return result;
            }

            JsonNode node =  instance.send_to_instance().write_with_confirmation(new WS_Board_set_autobackup().make_request(instance, board_for_update), 1000*3, 0, 4);

            final Form<WS_Board_set_autobackup> form = Form.form(WS_Board_set_autobackup.class).bind(node);
            if(form.hasErrors()){logger.error("Model_HomerServer:: WS_Add_Device_to_instance:: Incoming Json from Homer server has not right Form:: "  + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return new WS_Board_set_autobackup();}

            return form.get();

        }catch (TimeoutException e){
            return new WS_Board_set_autobackup();
        }catch (Exception e){
            logger.error("Model_Board:: set_auto_backup:: Error:: ", e);
            return new WS_Board_set_autobackup();
        }

    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_board_connect(){

        if(project == null) return;

        List<Model_Person> receivers = new ArrayList<>();
        for (Model_ProjectParticipant participant : this.project.participants)
            receivers.add(participant.person);

        new Model_Notification(Notification_importance.low, Notification_level.info)
                .setText("One of your Boards " + (this.personal_description != null ? this.personal_description : null ), "black", false, false, false)
                .setObject(this)
                .setText("is connected.", "black", false, false, false)
                .send(receivers);
    }

    @JsonIgnore @Transient
    public void notification_board_disconnect(){

        List<Model_Person> receivers = new ArrayList<>();
        for (Model_ProjectParticipant participant : this.project.participants)
            receivers.add(participant.person);

        new Model_Notification(Notification_importance.low, Notification_level.info)
                .setText("One of your Boards " + (this.personal_description != null ? this.personal_description : "" ))
                .setObject(this)
                .setText("is disconnected.")
                .send(receivers);
    }

    @JsonIgnore @Transient
    public void notification_new_actualization_request_with_file(){

        new Model_Notification(Notification_importance.low, Notification_level.info)
                .setText("New actualization task was added to Task Queue on ")
                .setObject(this)
                .setText(" with user File ") // TODO ? asi dodělat soubor ?
                .send(Controller_Security.getPerson());
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String connection_permission_docs    = "read: If user want connect Project with board, he needs two Permission! Project.update_permission == true and also Board.first_connect_permission == true. " +
                                                                                      "- Or user need combination of static/dynamic permission key and Board.first_connect_permission == true";
    @JsonIgnore @Transient public static final String disconnection_permission_docs = "read: If user want remove Board from Project, he needs one single permission Project.update_permission, where hardware is registered. - Or user need static/dynamic permission key";

                                       @JsonIgnore   @Transient public boolean create_permission(){  return   Controller_Security.getPerson().has_permission("Board_Create"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return  (project != null && project.update_permission())|| Controller_Security.getPerson().has_permission("Board_edit")  ;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean read_permission()  {  return  (project != null && project.read_permission()  )|| Controller_Security.getPerson().has_permission("Board_read")  ;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return  (project != null && project.update_permission())|| Controller_Security.getPerson().has_permission("Board_delete");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission(){  return  (project != null && project.update_permission())|| Controller_Security.getPerson().has_permission("Board_update");}


    public enum permissions {Board_read, Board_Create, Board_edit, Board_delete, Board_update}


/* ZVLÁŠTNÍ POMOCNÉ METODY ---------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public boolean first_connect_permission(){  return  project != null ? false : true;}

    @Override
    public void update(){
        super.update();
    }

    @Override
    public void save(){

        while(true){ // I need Unique Value

            String UUDID = UUID.randomUUID().toString().substring(0,14);
            this.hash_for_adding = UUDID.substring(0, 4) + "-" + UUDID.substring(4, 8) + "-" + UUDID.substring(9, 13);

            if (Model_Board.find.where().eq("hash_for_adding", hash_for_adding).findUnique() == null) break;
        }

        super.save();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_Board> find = new Finder<>(Model_Board.class);

}
