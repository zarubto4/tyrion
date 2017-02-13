package models.project.b_program.instnace;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Model_VersionObject;
import models.project.b_program.Model_BPair;
import models.project.b_program.Model_BProgramHwGroup;
import models.project.c_program.actualization.Model_ActualizationProcedure;
import models.project.c_program.actualization.Model_CProgramUpdatePlan;
import models.project.m_program.Model_MProjectProgramSnapShot;
import utilities.enums.Actual_procedure_State;
import utilities.enums.C_ProgramUpdater_State;
import utilities.enums.Firmware_type;
import utilities.hardware_updater.Master_Updater;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of HomerInstanceRecord",
        value = "HomerInstanceRecord")
public class Model_HomerInstanceRecord extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                                @JsonIgnore @Id   public String id;

    @JsonIgnore @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public Model_HomerInstance main_instance_history;

    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date date_of_created;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date running_from;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date running_to;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date planed_when;

                                                        @JsonIgnore @ManyToOne()  public Model_VersionObject version_object;
                                   @JsonIgnore @OneToOne(cascade=CascadeType.ALL) public Model_HomerInstance actual_running_instance;
    @JsonIgnore @OneToMany(mappedBy="homer_instance_record", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_ActualizationProcedure> procedures = new ArrayList<>();


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public  String b_program_version_id()    {  return version_object.id;}
    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public  String b_program_version_name()  {  return version_object.version_name;}
    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public  String instance_record_id()      {  return this.id;}

    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public List<Model_BProgramHwGroup> hardware_group()               {  return version_object.b_program_hw_groups;}
    @Transient @JsonProperty @ApiModelProperty(required = true, readOnly = true) public List<Model_MProjectProgramSnapShot> m_project_snapshop()    {  return version_object.b_program_version_snapshots;}

/* ENUMS PARAMETERS ----------------------------------------------------------------------------------------------------*/

    @JsonIgnore public List<Model_ActualizationProcedure> getProcedures() {return procedures;}

    @JsonIgnore @Override
    public void save(){
       // this.websocket_grid_token = UUID.randomUUID().toString() +"_"+ UUID.randomUUID().toString();
        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_HomerInstanceRecord.find.byId(this.id) == null) break;
        }
        super.save();
    }

/* INSTANCE WEBSOCKET CONTROLLING ON HOMER SERVER-----------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void add_new_actualization_request() {
        try {

            if(!getProcedures().isEmpty() || version_object.b_program_hw_groups.isEmpty()) return;

            // Projedu seznam HW - podle skupin instancí jak jsou poskládané podle Yody
            for(Model_BProgramHwGroup group : version_object.b_program_hw_groups) {

                List<Model_CProgramUpdatePlan> updates = new ArrayList<>();

                // ID C_programu aktuálního != požadovanému -> zařadím do aktualizační procedury!
                if(group.main_board_pair.board.actual_c_program_version == null || !group.main_board_pair.c_program_version_id().equals(group.main_board_pair.board.actual_c_program_version.id)){
                    // Zrušit aktualizace předchozích!!
                    // TODO

                    Model_CProgramUpdatePlan plan_master_board = new Model_CProgramUpdatePlan();
                    plan_master_board.board = group.main_board_pair.board;
                    plan_master_board.firmware_type = Firmware_type.FIRMWARE;
                    plan_master_board.state = C_ProgramUpdater_State.not_start_yet;
                    plan_master_board.c_program_version_for_update = group.main_board_pair.c_program_version;
                    updates.add(plan_master_board);
                }


                for (Model_BPair pair : group.device_board_pairs) {

                    // Tady chci zrušit všechny předchozí procedury vázající se na seznam příchozího hardwaru!

                    //1. Najdu předchozí procedury, které nejsou nějakým způsobem ukončené
                    List<Model_CProgramUpdatePlan> old_plans = Model_CProgramUpdatePlan.find.where()
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
                    for (Model_CProgramUpdatePlan old_plan : old_plans) {
                        logger.debug("Old plan for override under B_Program in Cloud: " + old_plan.id);
                        old_plan.state = C_ProgramUpdater_State.overwritten;
                        old_plan.update();
                    }


                    if(pair.board.actual_c_program_version == null || !pair.c_program_version_id().equals(pair.board.actual_c_program_version.id)) {
                        logger.debug("Crating new update plan procedure ");
                        // Vytvářím nový aktualizační plán protože požadovaná verze je jiná než aktuální!!

                        Model_CProgramUpdatePlan plan = new Model_CProgramUpdatePlan();
                        plan.board = pair.board;
                        plan.firmware_type = Firmware_type.FIRMWARE;
                        plan.state = C_ProgramUpdater_State.not_start_yet;
                        plan.c_program_version_for_update = pair.c_program_version;
                        updates.add(plan);

                        logger.debug("Crating update procedure done");
                    }
                }

                // Mohu nahrávat instanci která nemusí mít vůbec žádný update hardwaru a tak je zbytečné vytvářet objekt
                if(updates.size() > 0){

                    Model_ActualizationProcedure procedure = new Model_ActualizationProcedure();
                    procedure.date_of_create = new Date();
                    procedure.state = Actual_procedure_State.not_start_yet;
                    procedure.homer_instance_record = this;
                    procedure.save();


                    procedure.updates.addAll(updates);
                    procedure.update();

                    this.procedures.add(procedure);
                    this.update();

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
            for(Model_BProgramHwGroup group : version_object.b_program_hw_groups) {

                Model_ActualizationProcedure procedure = new Model_ActualizationProcedure();
                procedure.date_of_create = new Date();
                procedure.state = Actual_procedure_State.not_start_yet;
                procedure.homer_instance_record = this;

                // ID C_programu aktuálního != požadovanému -> zařadím do aktualizační procedury!
                if(!group.main_board_pair.board.actual_boot_loader.id.equals(group.main_board_pair.board.type_of_board.main_boot_loader.id)){

                    Model_CProgramUpdatePlan plan_master_board = new Model_CProgramUpdatePlan();
                    plan_master_board.board = group.main_board_pair.board;
                    plan_master_board.firmware_type = Firmware_type.BOOTLOADER;
                    plan_master_board.state = C_ProgramUpdater_State.not_start_yet;
                    plan_master_board.bootloader = group.main_board_pair.board.type_of_board.main_boot_loader;
                    procedure.updates.add(plan_master_board);

                }


                for (Model_BPair pair : group.device_board_pairs) {

                    //1. Najdu předchozí procedury, které nejsou nějakým způsobem ukončené
                    List<Model_CProgramUpdatePlan> old_plans = Model_CProgramUpdatePlan.find.where()
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
                    for (Model_CProgramUpdatePlan old_plan : old_plans) {
                        logger.debug("Old plan for override under B_Program in Cloud: " + old_plan.id);
                        old_plan.state = C_ProgramUpdater_State.overwritten;
                        old_plan.update();
                    }


                    if(!pair.c_program_version_id().equals(pair.board.actual_c_program_version.id)) {
                        logger.debug("Crating new update plan procedure ");
                        // Vytvářím nový aktualizační plán protože požadovaná verze je jiná než aktuální!!

                        Model_CProgramUpdatePlan plan = new Model_CProgramUpdatePlan();
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


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_HomerInstanceRecord> find = new Finder<>(Model_HomerInstanceRecord.class);

}
