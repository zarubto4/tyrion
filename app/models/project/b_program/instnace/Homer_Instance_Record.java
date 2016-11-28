package models.project.b_program.instnace;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;
import models.project.b_program.B_Pair;
import models.project.b_program.B_Program_Hw_Group;
import models.project.c_program.actualization.Actualization_procedure;
import models.project.c_program.actualization.C_Program_Update_Plan;
import models.project.m_program.M_Project_Program_SnapShot;
import utilities.enums.Firmware_type;
import utilities.hardware_updater.Master_Updater;
import utilities.hardware_updater.States.Actual_procedure_State;
import utilities.hardware_updater.States.C_ProgramUpdater_State;

import javax.persistence.*;
import java.util.*;

@Entity
public class Homer_Instance_Record  extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
    @JsonIgnore  public String websocket_grid_token;

    @JsonIgnore @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY) public Homer_Instance main_instance_history;

    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date date_of_created;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date running_from;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date running_to;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date planed_when;

    @JsonIgnore @ManyToOne() public Version_Object version_object;
    @JsonIgnore @OneToOne(cascade=CascadeType.ALL) public Homer_Instance actual_running_instance;
    @JsonIgnore @OneToMany(mappedBy="homer_instance_record", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Actualization_procedure> procedures = new ArrayList<>();


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public  String b_program_version_id()    {  return version_object.id;}
    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public  String b_program_version_name()  {  return version_object.version_name;}
    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public  String instance_record_id()      {  return this.id;}

    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public List<B_Program_Hw_Group> hardware_group()               {  return version_object.b_program_hw_groups;}
    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public List<M_Project_Program_SnapShot> m_project_snapshop()    {  return version_object.b_program_version_snapshots;}

/* ENUMS PARAMETERS ----------------------------------------------------------------------------------------------------*/

    @JsonIgnore public List<Actualization_procedure> getProcedures() {return procedures;}

    @Override
    public void save(){
        this.websocket_grid_token = UUID.randomUUID().toString() +"_"+ UUID.randomUUID().toString();
        super.save();
    }

/* INSTANCE WEBSOCKET CONTROLLING ON HOMER SERVER---------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    @JsonIgnore @Transient
    public void add_new_actualization_request() {
        try {

            // Projedu seznam HW - podle skupin instancí jak jsou poskládané podle Yody
            for(B_Program_Hw_Group group : version_object.b_program_hw_groups) {

                Actualization_procedure procedure = new Actualization_procedure();
                procedure.date_of_create = new Date();
                procedure.state = Actual_procedure_State.not_start_yet;
                procedure.homer_instance_record = this;

                // ID C_programu aktuálního != požadovanému -> zařadím do aktualizační procedury!
                if(group.main_board_pair.board.actual_c_program_version == null || !group.main_board_pair.c_program_version_id().equals(group.main_board_pair.board.actual_c_program_version.id)){

                    // Zrušit aktualizace předchozích!!
                    // TODO

                    C_Program_Update_Plan plan_master_board = new C_Program_Update_Plan();
                    plan_master_board.board = group.main_board_pair.board;
                    plan_master_board.firmware_type = Firmware_type.FIRMWARE;
                    plan_master_board.state = C_ProgramUpdater_State.not_start_yet;
                    plan_master_board.c_program_version_for_update = group.main_board_pair.c_program_version;
                    procedure.updates.add(plan_master_board);
                }


                for (B_Pair pair : group.device_board_pairs) {

                    // Tady chci zrušit všechny předchozí procedury vázající se na seznam příchozího hardwaru!

                    //1. Najdu předchozí procedury, které nejsou nějakým způsobem ukončené
                    List<C_Program_Update_Plan> old_plans = C_Program_Update_Plan.find.where()
                            .eq("firmware_type", Firmware_type.FIRMWARE.name())
                            .eq("board.id", pair.board.id).where()
                            .disjunction()
                                 .add(Expr.eq("state", C_ProgramUpdater_State.not_start_yet))
                                 .add(Expr.eq("state", C_ProgramUpdater_State.waiting_for_device))
                                 .add(Expr.eq("state", C_ProgramUpdater_State.instance_inaccessible))
                                 .add(Expr.eq("state", C_ProgramUpdater_State.homer_server_is_offline))
                                 .add(Expr.isNull("state"))
                            .endJunction()
                            .findList();

                    //2 Měl bych zkontrolovat zda ještě nejsou nějaké aktualizace v chodu
                    logger.debug("The number still valid update plans that must be override: " + old_plans.size());

                    //3. Neukončené procedury ukončím
                    for (C_Program_Update_Plan old_plan : old_plans) {
                        logger.debug("Old plan for override under B_Program in Cloud: " + old_plan.id);
                        old_plan.state = C_ProgramUpdater_State.overwritten;
                        old_plan.update();
                    }


                    if(pair.board.actual_c_program_version == null || !pair.c_program_version_id().equals(pair.board.actual_c_program_version.id)) {
                        logger.debug("Crating new update plan procedure ");
                        // Vytvářím nový aktualizační plán protože požadovaná verze je jiná než aktuální!!

                        C_Program_Update_Plan plan = new C_Program_Update_Plan();
                        plan.board = pair.board;
                        plan.firmware_type = Firmware_type.FIRMWARE;
                        plan.state = C_ProgramUpdater_State.not_start_yet;
                        plan.c_program_version_for_update = pair.c_program_version;
                        plan.actualization_procedure = procedure;
                        procedure.updates.add(plan);

                        logger.debug("Crating update procedure done");
                    }
                }

                // Mohu nahrávat instanci která nemusí mít vůbec žádný update hardwaru a tak je zbytečné vytvářet objekt
                if(procedure.updates.size() > 0){
                    this.procedures.add(procedure);
                    procedure.save();

                    logger.debug("Sending new Actualization procedure to Master Updater");
                    Master_Updater.add_new_Procedure(procedure);
                }
            }

        }catch (Exception e){
            logger.error("Homer_Instance_Record:: add_new_actualization_request:: Error ", e);
        }
    }

    @JsonIgnore @Transient
    public void add_new_actualization_request_bootloader() {
        try {

            // Projedu seznam HW - podle skupin instancí jak jsou poskládané podle Yody
            for(B_Program_Hw_Group group : version_object.b_program_hw_groups) {

                Actualization_procedure procedure = new Actualization_procedure();
                procedure.date_of_create = new Date();
                procedure.state = Actual_procedure_State.not_start_yet;
                procedure.homer_instance_record = this;

                // ID C_programu aktuálního != požadovanému -> zařadím do aktualizační procedury!
                if(!group.main_board_pair.board.actual_boot_loader.id.equals(group.main_board_pair.board.type_of_board.main_boot_loader.id)){

                    C_Program_Update_Plan plan_master_board = new C_Program_Update_Plan();
                    plan_master_board.board = group.main_board_pair.board;
                    plan_master_board.firmware_type = Firmware_type.BOOTLOADER;
                    plan_master_board.state = C_ProgramUpdater_State.not_start_yet;
                    plan_master_board.bootloader = group.main_board_pair.board.type_of_board.main_boot_loader;
                    procedure.updates.add(plan_master_board);

                }


                for (B_Pair pair : group.device_board_pairs) {

                    //1. Najdu předchozí procedury, které nejsou nějakým způsobem ukončené
                    List<C_Program_Update_Plan> old_plans = C_Program_Update_Plan.find.where()
                            .eq("board.id", pair.board.id).where()
                            .eq("firmware_type", Firmware_type.BOOTLOADER.name())
                            .disjunction()
                            .add(Expr.eq("state", C_ProgramUpdater_State.not_start_yet))
                            .add(Expr.eq("state", C_ProgramUpdater_State.waiting_for_device))
                            .add(Expr.eq("state", C_ProgramUpdater_State.instance_inaccessible))
                            .add(Expr.eq("state", C_ProgramUpdater_State.homer_server_is_offline))
                            .add(Expr.isNull("state"))
                            .endJunction()
                            .findList();

                    //2 Měl bych zkontrolovat zda ještě nejsou nějaké aktualizace v chodu
                    logger.debug("The number still valid update plans that must be override: " + old_plans.size());

                    //3. Neukončené procedury ukončím
                    for (C_Program_Update_Plan old_plan : old_plans) {
                        logger.debug("Old plan for override under B_Program in Cloud: " + old_plan.id);
                        old_plan.state = C_ProgramUpdater_State.overwritten;
                        old_plan.update();
                    }


                    if(!pair.c_program_version_id().equals(pair.board.actual_c_program_version.id)) {
                        logger.debug("Crating new update plan procedure ");
                        // Vytvářím nový aktualizační plán protože požadovaná verze je jiná než aktuální!!

                        C_Program_Update_Plan plan = new C_Program_Update_Plan();
                        plan.board = pair.board;
                        plan.firmware_type = Firmware_type.BOOTLOADER;
                        plan.state = C_ProgramUpdater_State.not_start_yet;
                        plan.c_program_version_for_update = pair.c_program_version;
                        plan.actualization_procedure = procedure;
                        procedure.updates.add(plan);

                        logger.debug("Crating update procedure done");
                    }
                }

                // Mohu nahrávat instanci která nemusí mít vůbec žádný update hardwaru a tak je zbytečné vytvářet objekt
                if(procedure.updates.size() > 0){
                    this.procedures.add(procedure);
                    procedure.save();

                    logger.debug("Sending new Actualization procedure to Master Updater");
                    Master_Updater.add_new_Procedure(procedure);
                }
            }

        }catch (Exception e){
            logger.error("Homer_Instance_Record:: add_new_actualization_request:: Error ", e);
        }
    }



/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Homer_Instance_Record> find = new Finder<>(Homer_Instance_Record.class);

}
