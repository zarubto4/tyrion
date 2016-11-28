package models.compiler;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.notification.Notification;
import models.person.Person;
import models.project.b_program.B_Pair;
import models.project.b_program.instnace.Homer_Instance;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.b_program.servers.Private_Homer_Server;
import models.project.c_program.actualization.C_Program_Update_Plan;
import models.project.global.Project;
import play.data.Form;
import play.libs.Json;
import utilities.enums.Firmware_type;
import utilities.enums.Notification_importance;
import utilities.enums.Notification_level;
import utilities.hardware_updater.Master_Updater;
import utilities.hardware_updater.States.C_ProgramUpdater_State;
import utilities.swagger.outboundClass.Swagger_Board_status;
import utilities.webSocket.WS_BlockoServer;
import utilities.webSocket.messageObjects.WS_DeviceConnected;
import utilities.webSocket.messageObjects.WS_YodaConnected;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
public class Board extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id                                @ApiModelProperty(required = true)   public String id; // Vlastní id je přidělováno

                                                                            public String ethernet_mac_address;
                                                                            public String wifi_mac_address;

    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)   public String personal_description;
                                       @JsonIgnore  @ManyToOne              public TypeOfBoard type_of_board;
                                       @ApiModelProperty(required = true)   public boolean is_active;
                                       @ApiModelProperty(required = true)   public boolean backup_mode;
                                                              @JsonIgnore   public Date date_of_create;

                      @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE)   public Project project;

                                                   @JsonIgnore @ManyToOne   public Version_Object actual_c_program_version;
                                                   @JsonIgnore              public String alternative_program_name;
                                                   @JsonIgnore @ManyToOne   public BootLoader actual_boot_loader;

                                                   @JsonIgnore @ManyToOne   public Cloud_Homer_Server latest_know_server;  // Pouze pokud je připojen přímo na blocko cloud_blocko_server!
                                                   @JsonIgnore @ManyToOne   public Private_Homer_Server private_homer_servers;



    @JsonIgnore  @OneToMany(mappedBy="board", cascade=CascadeType.ALL, fetch = FetchType.LAZY)     public List<B_Pair> b_pair = new ArrayList<>();
    @JsonIgnore  @OneToMany(mappedBy="board", cascade=CascadeType.ALL, fetch = FetchType.LAZY)     public List<C_Program_Update_Plan> c_program_update_plans;



    @JsonIgnore @ManyToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY) public Homer_Instance virtual_instance_under_project; // Propojení pokud HW není připojen do intnace - ale potřebuji na něj referenci - je ve vrituální instanci

/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String type_of_board_id()   { return type_of_board.id; }
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String type_of_board_name() { return type_of_board.name; }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public boolean main_board()        { return type_of_board.connectible_to_internet; }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String project_id()         { return       project == null ? null : project.id; }
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String project_name()       { return       project == null ? null : project.name; }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public Swagger_Board_status status()       {

        // Složený SQL dotaz pro nalezení funkční běžící instance (B_Pair)
        Homer_Instance instance =  Homer_Instance.find.where().disjunction()
                .add( Expr.eq("actual_instance.version_object.b_program_hw_groups.main_board_pair.board.id", id) )
                .add( Expr.eq("actual_instance.version_object.b_program_hw_groups.device_board_pairs.board.id", id) )
                .findUnique();


        Swagger_Board_status board_status = new Swagger_Board_status();


        if(instance == null){

            board_status.where = "nowhere";

        }else  {

            if (instance.cloud_homer_server != null) {
                board_status.where = "cloud";
            }

            if (instance.private_server  != null) {
                board_status.where = "local";
            }

            board_status.b_program_id = instance.b_program.id;
            board_status.b_program_name = instance.b_program.name;

            board_status.b_program_version_id = instance.actual_instance != null ? instance.actual_instance.version_object.id : null;
            board_status.b_program_version_name =instance.actual_instance != null ? instance.actual_instance.version_object.version_name : null;
        }

        if(alternative_program_name != null ) board_status.actual_program = alternative_program_name;

        if(actual_c_program_version != null){
                    board_status.actual_c_program_id = actual_c_program_version.c_program.id;
                    board_status.actual_c_program_name = actual_c_program_version.c_program.name;
                    board_status.actual_c_program_version_id = actual_c_program_version.id;
                    board_status.actual_c_program_version_name = actual_c_program_version.version_name;
        }

        if(!c_program_update_plans.isEmpty()){

            C_Program_Update_Plan plan = C_Program_Update_Plan.find.where().eq("board.id", id).order().asc("actualization_procedure.date_of_create").setMaxRows(1).findUnique();

            board_status.required_c_program_id = plan.c_program_version_for_update.c_program.id;
            board_status.required_c_program_name = plan.c_program_version_for_update.c_program.name;

            board_status.required_c_program_version_id = plan.c_program_version_for_update.id;
            board_status.required_c_program_version_name = plan.c_program_version_for_update.version_name;
         }

        return board_status;

    }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public boolean up_to_date_firmware()        { return  (c_program_update_plans == null);    }
    @JsonProperty  @Transient @ApiModelProperty(required = true) public boolean update_boot_loader_required(){ return  (type_of_board.main_boot_loader == null || actual_boot_loader == null) ? true : !this.type_of_board.main_boot_loader.id.equals(this.actual_boot_loader.id);}


/* BOARD WEBSOCKET CONTROLLING UNDER INSTANCE --------------------------------------------------------------------------*/

    @JsonIgnore @Transient public boolean is_online(){ // Velmi opatrně s touto proměnou - je časově velmi náročná!!!!!!!!
        try {

            Homer_Instance homer_instance;

            // Buď zkoumám virutální instnaci
            if (virtual_instance_under_project != null) {

                homer_instance = virtual_instance_under_project;

                // Nebo reálnou instnaci
            } else {

                homer_instance = Homer_Instance.find.where().disjunction()
                        .eq("actual_instance.version_object.b_program_hw_groups.device_board_pairs.board.id", this.id)
                        .eq("actual_instance.version_object.b_program_hw_groups.device_board_pairs.board.id", this.id)
                        .endJunction().findUnique();
            }

            if (homer_instance == null || !homer_instance.instance_online()) return false;


            ObjectNode request = Json.newObject();
            request.put("messageType", "device_online_state");
            request.put("messageChannel", Homer_Instance.CHANNEL);
            request.put("instanceId", homer_instance.blocko_instance_name);
            request.put("devicesId", this.id);

            JsonNode result = homer_instance.sendToInstance().write_with_confirmation(request, 1000 * 3, 0, 4);

            return !result.get("status").asText().equals("error") && result.get("device_state").asBoolean();


        }catch (Exception e){
            logger.error("Board:: is_online:: Error:: ", e);
            return false;
        }
    }

    @JsonIgnore @Transient  public static void master_device_Connected(WS_BlockoServer server, ObjectNode json){
        try {

            // Zpracování Json
            final Form<WS_YodaConnected> form = Form.form(WS_YodaConnected.class).bind(json);
            if(form.hasErrors()){logger.error("Incoming Json for Yoda has not right Form");return;}

            WS_YodaConnected help = form.get();
            Board master_device = Board.find.byId(help.deviceId);

            if(master_device == null){
                logger.error("Board:: master_device_Connected:: Unregistered Hardware connected to Blocko cloud_blocko_server:: ", server.identifikator);
                logger.error("Board:: master_device_Connected:: Unregistered Hardware:: ",  help.deviceId);
                return;
            }

            logger.debug("Board:: master_device_Connected:: Board connected to Blocko cloud_blocko_server:: ", help.deviceId);

            // Pokud se Yoda přihlásil poprvé - a nikdy neměl intanci a asi nemá ani klienta!
            if(master_device.latest_know_server == null){

                logger.debug("Board:: master_device_Connected:: ", help.deviceId, " The Board is not yet matched the Server");
                Cloud_Homer_Server cloud_server = Cloud_Homer_Server.find.where().eq("server_name", server.identifikator).findUnique();
                if(cloud_server == null) {
                    logger.error("Board:: master_device_Connected:: ", help.deviceId, " Cloud_Homer_Server not exist!!!!");
                    return;
                }

                cloud_server.boards.add(master_device);
                cloud_server.refresh();
                cloud_server.update();

                master_device.refresh();
                master_device.latest_know_server = cloud_server;
                master_device.is_active = true;
                master_device.update();
            }

            // Požádám o kontrolu zda nečeká nějaká nová aktualizační procedura - pro Yodu nebo jeho device
            Board.hardware_connected(master_device, help);

        }catch (Exception e){
            logger.error("Board:: master_device_Connected:: ERROR::", e);
        }
    }

    @JsonIgnore @Transient  public static void master_device_Disconnected(ObjectNode json){
        try {

        }catch (Exception e){
            logger.error("Board:: master_device_Disconnected:: ERROR:: ", e);
        }
    }

    @JsonIgnore @Transient  public static void device_Connected(WS_BlockoServer server, ObjectNode json){
        try {

            // Zpracování Json
            final Form<WS_DeviceConnected> form = Form.form(WS_DeviceConnected.class).bind(json);
            if(form.hasErrors()){ logger.error("Incoming Json from Device has not right Form"); return; }

            WS_DeviceConnected help = form.get();

            Board device = Board.find.byId(help.deviceId);

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
            Board.hardware_connected(device, help);

        }catch (Exception e){
            logger.error("Board:: device_Connected:: ERROR:: ", e);
        }
    }

    @JsonIgnore @Transient  public static void device_Disconnected(ObjectNode json){
        try {

        }catch (Exception e){
            logger.error("Board:: device_Disconnected:: ERROR:: ", e);
        }
    }


    @JsonIgnore @Transient public static void hardware_connected(Board board, WS_YodaConnected report){

        logger.debug("Tyrion Checking summary information of connected master board: ", board.id);


        // Kontrola nastavení Backup modu
        logger.trace("Checking autobackup");
        if(board.backup_mode != report.autobackup){
            // TODO

        }

        // Pokusím se najít Aktualizační proceduru jestli existuje s následujícími stavy
        logger.debug("Tyrion Checking actualization state of connected board: ", board.id);
        List<C_Program_Update_Plan> plans = C_Program_Update_Plan.find.where().eq("board.id", board.id).disjunction()
                    .add(   Expr.eq("state", C_ProgramUpdater_State.in_progress)         )
                    .add(   Expr.eq("state", C_ProgramUpdater_State.waiting_for_device)         )
                    .add(   Expr.eq("state", C_ProgramUpdater_State.instance_inaccessible)      )
                    .add(   Expr.eq("state", C_ProgramUpdater_State.critical_error)      )
                    .add(   Expr.eq("state", C_ProgramUpdater_State.homer_server_is_offline)    )
                .endJunction().order().asc("id").findList();


        if(plans.size() > 1){
            logger.error("Hardware Yoda: ", board.id, " connected into system, but we have mote than 2 update-plan!!!");
            logger.error("Earlier plans are terminate! Last one - by ID is used now!");

            for(int i = 1; i < plans.size(); i++ ){
                plans.get(i).state = C_ProgramUpdater_State.overwritten;
                plans.get(i).update();
                plans.remove(i);
            }
        }

        if(plans.size() == 1){

            logger.debug("Found one actualization procedure on ", board.id);

            C_Program_Update_Plan plan = plans.get(0);


            if(plan.firmware_type == Firmware_type.FIRMWARE){

                logger.debug("Checking Firmware");

                // Mám shodu oproti očekávánemů
                if(plan.c_program_version_for_update.c_compilation.firmware_build_id .equals( report.firmware_build_id )){

                    plan.state = C_ProgramUpdater_State.complete;
                    plan.update();

                }else {

                    plan.state = C_ProgramUpdater_State.in_progress;
                    plan.update();

                    Master_Updater.add_new_Procedure(plan.actualization_procedure);

                }

            }else if(plan.firmware_type == Firmware_type.BOOTLOADER){

                logger.debug("Checking Firmware");

                // Mám shodu oproti očekávánemů
                if(plan.binary_file.boot_loader.version_identificator.equals( report.bootloader_build_id )){

                    plan.state = C_ProgramUpdater_State.complete;
                    plan.update();

                }else {

                    plan.state = C_ProgramUpdater_State.in_progress;
                    plan.update();

                    Master_Updater.add_new_Procedure(plan.actualization_procedure);
                }

            }else if(plan.firmware_type == Firmware_type.BACKUP){

                logger.debug("Checking Backup");

                plan.state = C_ProgramUpdater_State.complete;
                plan.update();
            }
        }else {
            logger.debug("No actualization plan found for Master Device: " + board.id);
        }


        for(WS_DeviceConnected device_report : report.devices_summary){

            Board device = Board.find.byId(device_report.deviceId);

            // Smazat device z instance a tím i z yody
            if(device == null){
                logger.error("Unauthorized device connected to Yoda!" + board.id);
                //TODO

            }else {
                Board.hardware_connected(device, device_report);
            }

        }

    }

    @JsonIgnore @Transient public static void hardware_connected(Board board, WS_DeviceConnected report) {
        logger.debug("Tyrion Checking summary information of connected padavan board: ", board.id);

        // Pokusím se najít Aktualizační proceduru jestli existuje s následujícími stavy
        logger.debug("Tyrion Checking actualization state of connected board: ", board.id);
        List<C_Program_Update_Plan> plans = C_Program_Update_Plan.find.where().eq("board.id", board.id).disjunction()
                .add(   Expr.eq("state", C_ProgramUpdater_State.in_progress)         )
                .add(   Expr.eq("state", C_ProgramUpdater_State.waiting_for_device)         )
                .add(   Expr.eq("state", C_ProgramUpdater_State.instance_inaccessible)      )
                .add(   Expr.eq("state", C_ProgramUpdater_State.critical_error)      )
                .add(   Expr.eq("state", C_ProgramUpdater_State.homer_server_is_offline)    ).order().asc("id").findList();


        if(plans.size() > 1){
            logger.error("Hardware Board: ", board.id, " connected into system, but we have mote than 2 update-plan!!!");
            logger.error("Earlier plans are terminate! Last one - by ID is used now!");

            for(int i = 1; i < plans.size(); i++ ){
                plans.get(i).state = C_ProgramUpdater_State.overwritten;
                plans.get(i).update();
                plans.remove(i);
            }
        }

        if(plans.size() == 1){

            logger.debug("Found one actualization procedure on ", board.id);

            C_Program_Update_Plan plan = plans.get(0);

            if(plan.firmware_type == Firmware_type.FIRMWARE){

                logger.debug("Checking Firmware");

                // Mám shodu oproti očekávánemů
                if(plan.c_program_version_for_update.c_compilation.firmware_build_id .equals( report.firmware_build_id )){

                    plan.state = C_ProgramUpdater_State.complete;
                    plan.update();

                }else {

                    plan.state = C_ProgramUpdater_State.in_progress;
                    plan.update();

                    Master_Updater.add_new_Procedure(plan.actualization_procedure);

                }

            }else if(plan.firmware_type == Firmware_type.BOOTLOADER) {

                logger.debug("Checking Firmware");

                // Mám shodu oproti očekávánemů
                if (plan.binary_file.boot_loader.version_identificator.equals(report.bootloader_build_id)) {

                    plan.state = C_ProgramUpdater_State.complete;
                    plan.update();

                } else {

                    plan.state = C_ProgramUpdater_State.in_progress;
                    plan.update();

                    Master_Updater.add_new_Procedure(plan.actualization_procedure);
                }
            }

        }else {
            logger.debug("No actualization plan found for Master Device: " + board.id);
        }


    }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String connection_permission_docs    = "read: If user want connect Project with board, he needs two Permission! Project.update_permission == true and also Board.first_connect_permission == true. " +
                                                                                      "- Or user need combination of static/dynamic permission key and Board.first_connect_permission == true";
    @JsonIgnore @Transient public static final String disconnection_permission_docs = "read: If user want remove Board from Project, he needs one single permission Project.update_permission, where hardware is registered. - Or user need static/dynamic permission key";

                                       @JsonIgnore   @Transient public boolean create_permission(){  return   SecurityController.getPerson().has_permission("Board_Create"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return  (project != null && project.update_permission())|| SecurityController.getPerson().has_permission("Board_edit")  ;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean read_permission()  {  return  (project != null && project.read_permission()  )|| SecurityController.getPerson().has_permission("Board_read")  ;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return  (project != null && project.update_permission())|| SecurityController.getPerson().has_permission("Board_delete");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission(){  return  (project != null && project.update_permission())|| SecurityController.getPerson().has_permission("Board_update");}


    public enum permissions {Board_read, Board_Create, Board_edit, Board_delete, Board_update}


/* ZVLÁŠTNÍ POMOCNÉ METODY ---------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public boolean first_connect_permission(){  return  project != null ? false : true;}

    @Override
    public void update(){
        super.update();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Board> find = new Finder<>(Board.class);

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    public void board_connect(){

        for(Person person : this.project.ownersOfProject) {

            Notification notification = new Notification(Notification_importance.low, Notification_level.info, person)
                    .setText("One of your Board " + (this.personal_description != null ? this.personal_description : null))
                    .setObject(Board.class, this.id, this.id, this.project_id())
                    .setText("is connected.");
        }

    }

}
