package models;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Update_group_procedure_state;
import utilities.enums.Enum_CProgram_updater_state;
import utilities.enums.Enum_Firmware_type;
import utilities.enums.Enum_Update_type_of_update;
import utilities.hardware_updater.Utilities_HW_Updater_Master_thread_updater;
import utilities.logger.Class_Logger;
import web_socket.message_objects.homer_instance.WS_Message_Get_summary_information;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "HomerInstanceRecord", description = "Model of HomerInstanceRecord")
public class Model_HomerInstanceRecord extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_HomerInstanceRecord.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                                @JsonIgnore @Id   public String id;

    @JsonIgnore @ManyToOne(cascade = CascadeType.ALL,  fetch = FetchType.LAZY)     public Model_HomerInstance main_instance_history;

    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date date_of_created;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date running_from;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date running_to;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date planed_when;

                                   @JsonIgnore @ManyToOne(cascade=CascadeType.ALL)  public Model_VersionObject version_object;
                                   @JsonIgnore @OneToOne(cascade=CascadeType.ALL) public Model_HomerInstance actual_running_instance;
    @OneToMany(mappedBy="homer_instance_record", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_ActualizationProcedure> procedures = new ArrayList<>();


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public  String b_program_version_id()    {  return version_object.id;}
    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public  String b_program_version_name()  {  return version_object.version_name;}
    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public  String instance_record_id()      {  return this.id;}

    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public List<Model_BProgramHwGroup> hardware_group()               {  return version_object.b_program_hw_groups;}
    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public List<Model_MProjectProgramSnapShot> m_project_snapshot()    {  return version_object.b_program_version_snapshots;}
    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public String status()    {

        if(planed_when.getTime() > new Date().getTime()) return "furure";
        if(actual_running_instance != null) return "now";
        else return "history";

    }


/* JSON IGNORE  ----------------------------------------------------------------------------------------------------*/
    
@ApiModelProperty(required = true, readOnly = true)  @JsonProperty(value = "procedures")
    public List<Model_ActualizationProcedure> getProcedures() {
        if(procedures == null) procedures = Model_ActualizationProcedure.find.where().eq("homer_instance_record.id", id).findList();
        return procedures;
    }

    @JsonIgnore public  Homer_InstanceRecord_Short_detail get_short_detail(){

        Homer_InstanceRecord_Short_detail detail = new Homer_InstanceRecord_Short_detail();
        detail.date_of_created = date_of_created;
        detail.running_from = running_from;
        detail.running_to = running_to;
        detail.planed_when = planed_when;

        return detail;
    }


    @JsonIgnore @Override
    public void save(){
        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_HomerInstanceRecord.find.byId(this.id) == null) break;
        }
        super.save();
    }

/* INSTANCE WEBSOCKET CONTROLLING ON HOMER SERVER-----------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public boolean contains_HW(String board_id) {
        try {

            // Složený SQL dotaz pro nalezení funkční běžící instance (B_Pair)
            Integer contains =  Model_HomerInstanceRecord.find.where().disjunction()
                                    .add( Expr.eq("version_object.b_program_hw_groups.main_board_pair.board.id", board_id) )
                                    .add( Expr.eq("version_object.b_program_hw_groups.device_board_pairs.board.id", board_id) )
                               .findRowCount();

            return contains > 0;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return false;
        }
    }


    @JsonIgnore @Transient
    public void create_actualization_request(WS_Message_Get_summary_information summary_information ) {
        try {

            terminal_logger.debug("create_actualization_request:: byl zavolán na Instance Record:: {}" , id);

            Model_ActualizationProcedure actualization_procedure = new Model_ActualizationProcedure();
            actualization_procedure.date_of_create = new Date();

            if(running_from != null) actualization_procedure.date_of_planing = running_from;

            actualization_procedure.homer_instance_record = this;
            actualization_procedure.type_of_update = Enum_Update_type_of_update.MANUALLY_BY_USER_BLOCKO_GROUP;
            actualization_procedure.save();


            // Projedu seznam HW - podle skupin instancí jak jsou poskládané podle Yody
            for(Model_BProgramHwGroup group : version_object.b_program_hw_groups) {

                List<Model_CProgramUpdatePlan> updates = new ArrayList<>();

                //1) Nejdříve Main Boad
                terminal_logger.debug("create_actualization_request:: Main Board Id {}" , group.main_board_pair.board.id);
                terminal_logger.debug("create_actualization_request:: Kontroluji a popřípadě maži předchozí procedury na overwritten");

                List<Model_CProgramUpdatePlan> old_plans_main_board = Model_CProgramUpdatePlan.find.where()
                        .eq("firmware_type", Enum_Firmware_type.FIRMWARE.name())
                        .eq("board.id", group.main_board_pair.board.id).where()
                        .disjunction()
                        .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                        .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                        .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                        .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                        .add(Expr.isNull("state"))
                        .endJunction()
                        .findList();

                terminal_logger.debug("create_actualization_request:: The number still valid update plans for Main Board Id:: {}that must be override:: {}",  group.main_board_pair.board.id , old_plans_main_board.size());

                for (Model_CProgramUpdatePlan old_plan : old_plans_main_board) {
                    terminal_logger.debug("create_actualization_request:: Old plan for override under B_Program in Cloud: {} ", old_plan.id);
                    old_plan.state = Enum_CProgram_updater_state.overwritten;
                    old_plan.date_of_finish = new Date();
                    old_plan.update();
                }

                Model_CProgramUpdatePlan plan_master_board = new Model_CProgramUpdatePlan();
                plan_master_board.board = group.main_board_pair.board;
                plan_master_board.firmware_type = Enum_Firmware_type.FIRMWARE;

                // Zkontroluji jestli je online a co má za verzi - Protože bych mohl proceduru hned označit za vykonanou
                if(summary_information.deviceIsOnline(group.main_board_pair.board.id)){
                    terminal_logger.debug("create_actualization_request:: Main Board Id:: is online", group.main_board_pair.board.id );
                    terminal_logger.debug("create_actualization_request:: Required Firmware id:: {}", group.main_board_pair.c_program_version.c_compilation.firmware_build_id);
                    terminal_logger.debug("create_actualization_request:: Actual Firmware id:: {}", summary_information.getDeviceStats(group.main_board_pair.board.id ).firmware_build_id);

                    // Verze se rovnají a není třeba proceduru na Homerovi vykonávat - označí se jako hotová
                    if(group.main_board_pair.c_program_version.c_compilation.firmware_build_id.equals(summary_information.getDeviceStats(group.main_board_pair.board.id ).firmware_build_id)){

                        terminal_logger.debug("create_actualization_request:: Firmware is already on board! - C_ProgramUpdater_State is Complete");
                        plan_master_board.state = Enum_CProgram_updater_state.complete;

                    }else {
                        plan_master_board.state = Enum_CProgram_updater_state.not_start_yet;
                    }

                }else {
                    terminal_logger.debug("create_actualization_request:: The number still valid update plans for Main Board Id:: {} that must be override:: {} " , group.main_board_pair.board.id ,  old_plans_main_board.size());
                    plan_master_board.state = Enum_CProgram_updater_state.not_start_yet;
                }

                plan_master_board.c_program_version_for_update = group.main_board_pair.c_program_version;
                updates.add(plan_master_board);





                //2) Device
                for (Model_BPair pair : group.device_board_pairs) {

                    terminal_logger.debug("create_actualization_request:: Device Board Id {} Under Master Device {} " , pair.board_id(), group.main_board_pair.board.id);
                    // Tady chci zrušit všechny předchozí procedury vázající se na seznam příchozího hardwaru!

                    //1. Najdu předchozí procedury, které nejsou nějakým způsobem ukončené
                    List<Model_CProgramUpdatePlan> old_plans = Model_CProgramUpdatePlan.find.where()
                            .eq("firmware_type", Enum_Firmware_type.FIRMWARE.name())
                            .eq("board.id", pair.board.id).where()
                            .disjunction()
                                 .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                                 .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                                 .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                                 .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                                 .add(Expr.isNull("state"))
                            .endJunction()
                            .findList();

                    //2 Měl bych zkontrolovat zda ještě nejsou nějaké aktualizace v chodu
                    terminal_logger.debug("create_actualization_request:: The number still valid update plans that must be override: " + old_plans.size());

                    //3. Neukončené procedury ukončím
                    for (Model_CProgramUpdatePlan old_plan : old_plans) {
                        terminal_logger.debug("Old plan for override under B_Program in Cloud: {}", old_plan.id);
                        old_plan.state = Enum_CProgram_updater_state.overwritten;
                        old_plan.date_of_finish = new Date();
                        old_plan.update();
                    }


                    if(pair.board.actual_c_program_version == null || !pair.c_program_version_id().equals(pair.board.actual_c_program_version.id)) {
                        terminal_logger.debug(" create_actualization_request:: Crating new update plan procedure ");
                        // Vytvářím nový aktualizační plán protože požadovaná verze je jiná než aktuální!!

                        Model_CProgramUpdatePlan plan = new Model_CProgramUpdatePlan();
                        plan.board = pair.board;
                        plan.firmware_type = Enum_Firmware_type.FIRMWARE;

                        if(summary_information.deviceIsOnline(group.main_board_pair.board.id)){

                            try {

                                terminal_logger.trace("create_actualization_request:: Device Board Id:: {} is online",  pair.board.id );
                                terminal_logger.trace("create_actualization_request:: Required Firmware id:: {}", pair.c_program_version.c_compilation.firmware_build_id);
                                terminal_logger.trace("create_actualization_request:: Actual Firmware id:: {}", summary_information.getDeviceStats(pair.board.id).firmware_build_id);

                                // Verze se rovnají a není třeba proceduru na Homerovi vykonávat - označí se jako hotoá
                                if (group.main_board_pair.c_program_version.c_compilation.firmware_build_id.equals(summary_information.getDeviceStats(group.main_board_pair.board.id).firmware_build_id)) {

                                    terminal_logger.debug("create_actualization_request:: Firmware is already on board! - C_ProgramUpdater_State is Complete");
                                    plan.state = Enum_CProgram_updater_state.complete;

                                } else {
                                    plan.state = Enum_CProgram_updater_state.not_start_yet;
                                }

                            }catch (NullPointerException e){
                                terminal_logger.debug("create_actualization_request:: Some parameter missing");
                                plan.state = Enum_CProgram_updater_state.not_start_yet;
                            }

                        }else {
                            terminal_logger.debug("create_actualization_request:: The number still valid update plans for Main Board Id:: " + group.main_board_pair.board.id + " that must be override:: " + old_plans_main_board.size());
                            plan.state = Enum_CProgram_updater_state.not_start_yet;
                        }

                        plan.c_program_version_for_update = pair.c_program_version;

                        updates.add(plan);

                        terminal_logger.debug("create_actualization_request:: Crating update procedure done");
                    }
                }


                actualization_procedure.updates.addAll(updates);

            }

            if(actualization_procedure.updates.size() > 0){

                for(Model_CProgramUpdatePlan plan_update : actualization_procedure.updates){

                    terminal_logger.debug("create_actualization_request:: Checking Model_CProgramUpdatePlan id:: " + plan_update.state);

                    if(plan_update.state != Enum_CProgram_updater_state.complete && actualization_procedure.state == null) {
                        terminal_logger.debug("create_actualization_request:: Set procedure to not_start_yet");
                        actualization_procedure.state = Enum_Update_group_procedure_state.not_start_yet;
                    }

                    plan_update.actualization_procedure = actualization_procedure;
                    plan_update.save();
                }

                if(actualization_procedure.state == null) {
                    terminal_logger.debug("create_actualization_request:: Set procedure to successful_complete");
                    actualization_procedure.state = Enum_Update_group_procedure_state.successful_complete;
                    actualization_procedure.update();
                }

            }



        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient
    public void add_new_actualization_request_bootloader() {
        try {

            // Projedu seznam HW - podle skupin instancí jak jsou poskládané podle Yody
            for(Model_BProgramHwGroup group : version_object.b_program_hw_groups) {

                Model_ActualizationProcedure procedure = new Model_ActualizationProcedure();
                procedure.date_of_create = new Date();
                procedure.state = Enum_Update_group_procedure_state.not_start_yet;
                procedure.homer_instance_record = this;

                // ID C_programu aktuálního != požadovanému -> zařadím do aktualizační procedury!
                if(!group.main_board_pair.board.actual_boot_loader.id.equals(group.main_board_pair.board.type_of_board.main_boot_loader.id)){

                    Model_CProgramUpdatePlan plan_master_board = new Model_CProgramUpdatePlan();
                    plan_master_board.board = group.main_board_pair.board;
                    plan_master_board.firmware_type = Enum_Firmware_type.BOOTLOADER;
                    plan_master_board.state = Enum_CProgram_updater_state.not_start_yet;
                    plan_master_board.bootloader = group.main_board_pair.board.type_of_board.main_boot_loader;
                    procedure.updates.add(plan_master_board);

                }


                for (Model_BPair pair : group.device_board_pairs) {

                    //1. Najdu předchozí procedury, které nejsou nějakým způsobem ukončené
                    List<Model_CProgramUpdatePlan> old_plans = Model_CProgramUpdatePlan.find.where()
                            .eq("board.id", pair.board.id).where()
                            .eq("firmware_type", Enum_Firmware_type.BOOTLOADER.name())
                            .disjunction()
                            .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                            .add(Expr.isNull("state"))
                            .endJunction()
                            .findList();

                    //2 Měl bych zkontrolovat zda ještě nejsou nějaké aktualizace v chodu
                    terminal_logger.debug("add_new_actualization_request_bootloader:: The number still valid update plans that must be override: {}", old_plans.size());

                    //3. Neukončené procedury ukončím
                    for (Model_CProgramUpdatePlan old_plan : old_plans) {
                        terminal_logger.debug("add_new_actualization_request_bootloader:: Old plan for override under B_Program in Cloud: {}", old_plan.id);
                        old_plan.state = Enum_CProgram_updater_state.overwritten;
                        old_plan.update();
                    }


                    if(!pair.c_program_version_id().equals(pair.board.actual_c_program_version.id)) {
                        terminal_logger.debug("add_new_actualization_request_bootloader:: Crating new update plan procedure ");
                        // Vytvářím nový aktualizační plán protože požadovaná verze je jiná než aktuální!!

                        Model_CProgramUpdatePlan plan = new Model_CProgramUpdatePlan();
                        plan.board = pair.board;
                        plan.firmware_type = Enum_Firmware_type.BOOTLOADER;
                        plan.state = Enum_CProgram_updater_state.not_start_yet;
                        plan.c_program_version_for_update = pair.c_program_version;
                        plan.actualization_procedure = procedure;
                        procedure.updates.add(plan);

                        terminal_logger.debug("add_new_actualization_request_bootloader:: Crating update procedure done");
                    }
                }

                // Mohu nahrávat instanci která nemusí mít vůbec žádný update hardwaru a tak je zbytečné vytvářet objekt
                if(procedure.updates.size() > 0){
                    this.procedures.add(procedure);
                    procedure.save();

                    terminal_logger.debug("add_new_actualization_request_bootloader:: Sending new Actualization procedure to Master Updater");
                    Utilities_HW_Updater_Master_thread_updater.add_new_Procedure(procedure);
                }
            }

        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    public class Homer_InstanceRecord_Short_detail{

        public Date date_of_created;
        public Date running_from;
        public Date running_to;
        public Date planed_when;


    }
/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_HomerInstanceRecord> find = new Finder<>(Model_HomerInstanceRecord.class);

}
