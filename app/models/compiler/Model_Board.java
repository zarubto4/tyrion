package models.compiler;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.notification.Model_Notification;
import models.person.Model_Person;
import models.project.b_program.Model_BPair;
import models.project.b_program.instnace.Model_HomerInstance;
import models.project.b_program.servers.Model_HomerServer;
import models.project.c_program.actualization.Model_CProgramUpdatePlan;
import models.project.global.Model_Project;
import models.project.global.Model_ProjectParticipant;
import play.data.Form;
import play.libs.Json;
import utilities.enums.*;
import utilities.hardware_updater.Master_Updater;
import utilities.swagger.outboundClass.Swagger_Board_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Board_Status;
import utilities.web_socket.WS_HomerServer;
import utilities.web_socket.message_objects.homer_instance.*;
import utilities.web_socket.message_objects.homer_tyrion.WS_Is_device_connected;
import utilities.web_socket.message_objects.homer_tyrion.WS_Unregistred_device_connected;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Entity
@ApiModel(description = "Model of Board",
        value = "Board")
public class Model_Board extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

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
                                                   @JsonIgnore              public String alternative_program_name;


    @JsonIgnore @ManyToOne   public Model_BootLoader actual_boot_loader;

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

    @JsonProperty  @Transient @ApiModelProperty(required = true) public Swagger_Board_Status status()       {

        logger.debug("Model_Board:: status:: Check Status" + this.id);

        // Složený SQL dotaz pro nalezení funkční běžící instance (B_Pair)
        Model_HomerInstance instance =  Model_HomerInstance.find.where().disjunction()
                .add( Expr.eq("actual_instance.version_object.b_program_hw_groups.main_board_pair.board.id", id) )
                .add( Expr.eq("actual_instance.version_object.b_program_hw_groups.device_board_pairs.board.id", id) )
                .findUnique();


        Swagger_Board_Status board_status = new Swagger_Board_Status();
        board_status.status = is_online() ? Board_Status.online : Board_Status.offline;
        if(project == null) board_status.where = Board_Type_of_connection.connected_to_server_unregistered;

        // Stavy Desky--------------------

        // 1) Není známo kam se deska připojila a nemá instanci
        if(get_instance() == null && get_connected_server() == null){

            board_status.status = Board_Status.not_yet_first_connected;

        // 2) Je známo kam se deska připojila a nemá instanci - Takže třeba když jí uživatel vyndal z krabičky nahrál na ní něco
        }else if(get_instance() == null && get_connected_server() != null){

            logger.debug("Model_Board:: status:: Check Status:: ");
            board_status.where = Board_Type_of_connection.connected_to_byzance;
            board_status.server_name = connected_server.personal_server_name;
            board_status.homer_server_id = connected_server.unique_identificator;

        // 3) Je ve Virtuální instanci
        } else if(instance != null) {

            board_status.where = Board_Type_of_connection.in_person_instance;
            board_status.instance_id = get_instance().blocko_instance_name;
            if( instance.getB_program() != null) board_status.b_program_id = instance.getB_program().id;
            if( instance.getB_program() != null) board_status.b_program_name = instance.getB_program().name;

            if(instance.actual_instance != null ) board_status.b_program_version_id = instance.actual_instance.version_object.id ;
            if(instance.actual_instance != null ) board_status.b_program_version_name = instance.actual_instance.version_object.version_name;


        // 4) Je ve virtuální instanci
        } else if( get_virtual_instance() != null ){
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

        if(!c_program_update_plans.isEmpty()){

            Model_CProgramUpdatePlan plan = Model_CProgramUpdatePlan.find.where().eq("board.id", id).order().asc("actualization_procedure.date_of_create").setMaxRows(1).findUnique();

            board_status.required_c_program_id = plan.c_program_version_for_update.c_program.id;
            board_status.required_c_program_name = plan.c_program_version_for_update.c_program.name;

            board_status.required_c_program_version_id = plan.c_program_version_for_update.id;
            board_status.required_c_program_version_name = plan.c_program_version_for_update.version_name;
         }



        return board_status;

    }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public boolean up_to_date_firmware()        { return  (c_program_update_plans == null);    }
    @JsonProperty  @Transient @ApiModelProperty(required = true) public boolean update_boot_loader_required(){ return  (type_of_board.main_boot_loader == null || actual_boot_loader == null) ? true : !this.type_of_board.main_boot_loader.id.equals(this.actual_boot_loader.id);}


/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_Board_Short_Detail get_short_board(){

        Swagger_Board_Short_Detail swagger_board_short_detail = new Swagger_Board_Short_Detail();
        swagger_board_short_detail.id = id;
        swagger_board_short_detail.personal_description = personal_description;
        swagger_board_short_detail.type_of_board_id = type_of_board_id();
        swagger_board_short_detail.type_of_board_name = type_of_board_name();

        swagger_board_short_detail.edit_permission = edit_permission();
        swagger_board_short_detail.delete_permission = delete_permission();
        swagger_board_short_detail.update_permission = update_permission();

        return swagger_board_short_detail;

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
            WS_Online_states_devices result = homer_instance.get_devices_online_state(list);


            if( result.status.equals("error")){
                return false;
            }

            if( result.status.equals("success") ){
                return result.device_state;
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
            Model_Board.hardware_firmware_state_check(master_device, help);

        }catch (Exception e){
            logger.error("Board:: master_device_Connected:: ERROR::", e);
        }
    }

    @JsonIgnore @Transient  public static void master_device_Disconnected(WS_Yoda_disconnected help){
        try {

        }catch (Exception e){
            logger.error("Board:: master_device_Disconnected:: ERROR:: ", e);
        }
    }

    @JsonIgnore @Transient  public static void device_Connected(WS_HomerServer server, WS_Device_connected help){
        try {

            Model_Board device = Model_Board.find.byId(help.deviceId);

            if(device == null){
                logger.warn("WARN! WARN! WARN! WARN!");
                logger.warn("Unregistered Hardware connected to Blocko cloud_blocko_server - " + server.identifikator);
                logger.warn("Unregistered Hardware: " +  help.deviceId);
                logger.warn("WARN! WARN! WARN! WARN!");
                return;
            }

            if(!device.is_active ){
                device.is_active = true;
                device.update();
            }

            // Požádám o kontrolu zda nečeká nějaká nová aktualizační procedura - pro Yodu nebo jeho device
           //  Model_Board.hardware_connected(device, help);

        }catch (Exception e){
            logger.error("Board:: device_Connected:: ERROR:: ", e);
        }
    }

    @JsonIgnore @Transient  public static void device_Disconnected(WS_Device_disconnected help){
        try {
            //TODO
        }catch (Exception e){
            logger.error("Board:: device_Disconnected:: ERROR:: ", e);
        }
    }



    // Kontrola up_to_date harwaru
    @JsonIgnore @Transient  public static void hardware_firmware_state_check(Model_Board board, WS_Yoda_connected report) {

        logger.debug("Model_Board:: hardware_firmware_state_check:: Summary information of connected master board: ", board.id);

        System.out.println("Kontrola Yody:: ");
        System.out.println("Kontrola Yody Id:: " + report.deviceId);
        System.out.println("Aktuální firmware_id:: " + report.firmware_build_id + " očekávaný dle tyriona" + board.actual_c_program_version != null ? board.actual_c_program_version.c_compilation.firmware_build_id : " zatím žádné");
        System.out.println("Aktuální bootlader_id:: " + report.bootloader_build_id + " očekávaný dle tyriona" + board.actual_boot_loader != null ? board.actual_boot_loader.version_identificator : " zatím žádné");


        // Pokusím se najít Aktualizační proceduru jestli existuje s následujícími stavy

        Integer plans = Model_CProgramUpdatePlan.find.where().eq("board.id", board.id).disjunction()
                .add(Expr.eq("state", C_ProgramUpdater_State.not_start_yet))
                .add(Expr.eq("state", C_ProgramUpdater_State.in_progress))
                .add(Expr.eq("state", C_ProgramUpdater_State.waiting_for_device))
                .add(Expr.eq("state", C_ProgramUpdater_State.instance_inaccessible))
                .add(Expr.eq("state", C_ProgramUpdater_State.homer_server_is_offline))
                .endJunction().findRowCount();


        System.out.println("Kolik mám aktualizačních procedur nažhavených pro dané zařízení:: " + plans);

        if (plans > 0) {

            System.out.println("Mám jich více než 0 ");

            Model_CProgramUpdatePlan plan = Model_CProgramUpdatePlan.find.where()
                    .eq("board.id", board.id)
                    .eq("actualization_procedure.homer_instance_record.id", board.get_instance().actual_instance.id)
                    .disjunction()
                    .add(Expr.eq("state", C_ProgramUpdater_State.not_start_yet))
                    .add(Expr.eq("state", C_ProgramUpdater_State.in_progress))
                    .add(Expr.eq("state", C_ProgramUpdater_State.waiting_for_device))
                    .add(Expr.eq("state", C_ProgramUpdater_State.instance_inaccessible))
                    .add(Expr.eq("state", C_ProgramUpdater_State.homer_server_is_offline))
                    .endJunction().findUnique();

            System.out.println("Bubu kontrolovat na co mám plán");

            if (plan.firmware_type == Firmware_type.FIRMWARE) {

                logger.debug("Homer_Instance_Record:: check_hardware:: Checking Firmware");

                // Mám shodu oproti očekávánemů
                if (plan.c_program_version_for_update.c_compilation.firmware_build_id.equals(report.firmware_build_id)) {

                    plan.state = C_ProgramUpdater_State.complete;
                    plan.update();

                } else {

                    plan.state = C_ProgramUpdater_State.in_progress;
                    plan.update();

                    Master_Updater.add_new_Procedure(plan.actualization_procedure);

                }

            } else if (plan.firmware_type == Firmware_type.BOOTLOADER) {

                logger.debug("Homer_Instance_Record:: check_hardware:: Checking Firmware");

                // Mám shodu oproti očekávánemů
                if (plan.binary_file.boot_loader.version_identificator.equals(report.bootloader_build_id)) {

                    plan.state = C_ProgramUpdater_State.complete;
                    plan.update();

                } else {

                    plan.state = C_ProgramUpdater_State.in_progress;
                    plan.update();

                    Master_Updater.add_new_Procedure(plan.actualization_procedure);
                }

            } else if (plan.firmware_type == Firmware_type.BACKUP) {

                logger.debug("Homer_Instance_Record:: check_hardware:: Checking Backup");

                plan.state = C_ProgramUpdater_State.complete;
                plan.update();
            }

            board.get_instance().check_hardware(board, report);
        } else {
            logger.debug("No actualization plan found for Master Device: " + board.id);
        }



        board.notification_board_connect();


    }

    @JsonIgnore @Transient public static WS_Update_device_firmware update_devices_firmware(Model_HomerInstance instance, String actualization_procedure_id, List<String> targetIds, Firmware_type firmware_type, Model_FileRecord record){

        try {

            logger.debug("Homer: " + instance.send_to_instance().identifikator + ", will update Yodas or Devices");



            JsonNode node = instance.send_to_instance().write_with_confirmation(new WS_Update_device_firmware().make_request(instance, actualization_procedure_id, firmware_type, targetIds, record), 1000 * 30, 0, 3);


            final Form<WS_Update_device_firmware> form = Form.form(WS_Update_device_firmware.class).bind(node);
            if(form.hasErrors()){logger.error("Model_Board:: WS_Update_device_firmware:: Incoming Json for Yoda has not right Form");return new WS_Update_device_firmware();}

            return form.get();

        }catch (Exception e){
            return new WS_Update_device_firmware();
        }
    }

    @JsonIgnore @Transient public static void unregistred_device_connected(WS_HomerServer homer_server, WS_Unregistred_device_connected report) {
        logger.debug("Model_Board:: unregistred_device_connected:: " + report.deviceId);

        Model_Board board = Model_Board.find.byId(report.deviceId);
        if(board == null){
            logger.warn("Unknown device tries to connect:: " + report.deviceId);
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
            if(!board.get_instance().cloud_homer_server.unique_identificator.equals(homer_server.server.unique_identificator)){
                board.device_change_server(board.get_instance().cloud_homer_server);
            }
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


/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_board_connect(){

        List<Model_Person> receivers = new ArrayList<>();
        for (Model_ProjectParticipant participant : this.project.participants)
            receivers.add(participant.person);

        new Model_Notification(Notification_importance.low, Notification_level.info)
                .setText("One of your Boards " + (this.personal_description != null ? this.personal_description : null ), "black", false, false, false)
                .setObject(Model_Board.class, this.id, this.id, this.project_id(), "black", false, true, false, false)
                .setText("is connected.", "black", false, false, false)
                .send(receivers);
    }

    @JsonIgnore @Transient
    public void notification_board_disconnect(){

        List<Model_Person> receivers = new ArrayList<>();
        for (Model_ProjectParticipant participant : this.project.participants)
            receivers.add(participant.person);

        new Model_Notification(Notification_importance.low, Notification_level.info)
                .setText("One of your Boards " + (this.personal_description != null ? this.personal_description : null ), "black", false, false, false)
                .setObject(Model_Board.class, this.id, this.id, this.project_id(), "black", false, true, false, false)
                .setText("is disconnected.", "black", false, false, false)
                .send(receivers);
    }

    @JsonIgnore @Transient
    public void notification_new_actualization_request_with_file(){

        new Model_Notification(Notification_importance.low, Notification_level.info)
                .setText("New actualization task was added to Task Queue on ")
                .setObject(Model_Board.class, this.id, "board", this.project_id())
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
            this.hash_for_adding = UUDID.substring(0, 4) + "-" + UUDID.substring(5, 8) + "-" + UUDID.substring(10, 14);

            if (Model_Board.find.where().eq("hash_for_adding", hash_for_adding).findUnique() == null) break;
        }

        super.save();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_Board> find = new Finder<>(Model_Board.class);

}
