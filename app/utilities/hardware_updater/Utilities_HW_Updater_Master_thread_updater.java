package utilities.hardware_updater;

import com.avaje.ebean.Expr;
import models.*;
import utilities.enums.Enum_CProgram_updater_state;
import utilities.enums.Enum_Firmware_type;
import utilities.enums.Enum_Update_group_procedure_state;
import utilities.enums.Enum_Update_type_of_update;
import utilities.errors.ErrorCode;
import utilities.hardware_updater.helps_objects.Utilities_HW_Updater_Actualization_Task;
import utilities.hardware_updater.helps_objects.Utilities_HW_Updater_Actualization_procedure;
import utilities.hardware_updater.helps_objects.Utilities_HW_Updater_Target_pair;
import utilities.logger.Class_Logger;
import web_socket.message_objects.homer_instance.WS_Message_Update_device_summary_collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utilities_HW_Updater_Master_thread_updater {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Utilities_HW_Updater_Master_thread_updater.class);


    private static Utilities_HW_Updater_Master_thread_updater updater = null;
    protected Utilities_HW_Updater_Master_thread_updater() {/** Exists only to defeat instantiation.*/}

    public static Utilities_HW_Updater_Master_thread_updater getInstance() {
        if(updater == null) {updater = new Utilities_HW_Updater_Master_thread_updater(); start_thread_box();}
        return updater;
    }


    public static void start_thread_box(){
        terminal_logger.trace("start_thread_box:: will be started");

        if(!comprimator_thread.isAlive()) comprimator_thread.start();
    }


    public static void add_new_Procedure(Model_ActualizationProcedure procedure){

        terminal_logger.trace("start_thread_box:: new incoming procedure");

        procedures.add(procedure);

        if(comprimator_thread.getState() == Thread.State.TIMED_WAITING) {
            terminal_logger.trace("start_thread_box::  wait for interrupt!");
            comprimator_thread.interrupt();
        }
    }

// ** Comprimator Thread -----------------------------------------------------------------------------------------------

   public static List<Model_ActualizationProcedure> procedures = new ArrayList<>();

    static Thread comprimator_thread = new Thread() {

        @Override
        public void run() {


            terminal_logger.info("run:: run") ;

            while(true){
                try{

                    if(!procedures.isEmpty()) {

                        terminal_logger.debug("run:: Tasks to solve: " + procedures.size() );

                        new Utilities_HW_Updater_Master_thread_updater().actualization_update_procedure( procedures.get(0) );
                        procedures.remove( procedures.get(0) );

                    }

                    else{
                        terminal_logger.trace("run:: Going to sleep!");
                        sleep(500000000);
                    }



                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    terminal_logger.internalServerError("comprimator_thread:", e);
                }
            }
        }
    };

// ** Updater Thread -----------------------------------------------------------------------------------------------

    /**
     * Následující Tři třídy slouží k přehlednému roztřídění update procedury podle jendotlivých instancí, typů firmaru
     * a další... I přesto že v 99% je updatován jeden objekt, nebo objekty pod jendou instancí (frontend vyloženě podporuje
     * řízení updatů podle instnací podle obrazovek, přesto je toto řešení zvoleno záměrně, protože se dá používat globálně
     * na update všech deviců napříč projekty atd... a i když je metoda složitější na počet dotazů, ve výsledku to sníží
     * websocketovou zátěž v duplicitním zasílání stejných updatovacích požadavků.
     */
    class Actualization_structure {
        public HashMap<String, Instance> instances = new HashMap<>();
    }

    class Instance{
        public Model_HomerInstance instance;
        public HashMap<String, Program> programs = new HashMap<>(); // <Build_ID, Program>
    }

    class Program{

        public String program_identificator;
        public Enum_Firmware_type firmware_type;
        public Model_FileRecord file_record;
        public String name;
        public String version;
        public Enum_Update_type_of_update type_of_update;

        public List<Utilities_HW_Updater_Target_pair> target_pairs = new ArrayList<>();

    }



    private void actualization_update_procedure(Model_ActualizationProcedure procedure){

        terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} Start Execution. ", procedure.id );
        terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} . Actual state {} ", procedure.id , procedure.state.name());
        
        if(procedure.state == Enum_Update_group_procedure_state.complete || procedure.state == Enum_Update_group_procedure_state.successful_complete ){
            terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} is done  (successful_complete or complete) -> Return.", procedure.id);
            return;
        }

        if(procedure.state == Enum_Update_group_procedure_state.in_progress){
            terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} is already in progress. (This is only for debug) ", procedure.id);
        }


        terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} . Number of All updates of C_Procedures:: " + procedure.updates.size());

        if(procedure.updates.isEmpty()){

            procedure.state = Enum_Update_group_procedure_state.complete_with_error;
            procedure.update();
            terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} is empty and not set to any updates! -> Return." , procedure.id);
            return;
            
        }


        Actualization_structure structure = new Actualization_structure();

        List<Model_CProgramUpdatePlan> plans = Model_CProgramUpdatePlan.find.where().eq("actualization_procedure.id", procedure.id)
                .disjunction()
                    .eq("state", Enum_CProgram_updater_state.not_start_yet)
                    .eq("state", Enum_CProgram_updater_state.waiting_for_device)
                    .eq("state", Enum_CProgram_updater_state.instance_inaccessible)
                    .eq("state", Enum_CProgram_updater_state.homer_server_is_offline)
                    .eq("state", Enum_CProgram_updater_state.bin_file_not_found)
                .endJunction()
                .findList();

        if(plans.isEmpty()){
            terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} all updates is done or in progress. -> Return.", procedure.id );
            return;
        }

        terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} . Number of C_Procedures By database for execution:: {}" , procedure.id , plans.size());

        for (Model_CProgramUpdatePlan plan : plans) {
            try {

                terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} plan {} CProgramUpdatePlan:: ID:: {} - New Cycle" , procedure.id , plan.id);
                terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} plan {} CProgramUpdatePlan:: Board ID:: {}" , procedure.id , plan.id,  plan.board.id);
                terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} plan {} CProgramUpdatePlan:: Status:: {} ", procedure.id , plan.id,  plan.state);

                terminal_logger.debug("actualization_update_procedure:: Procedure id:: {} plan {} CProgramUpdatePlan:: Number of tries  ", procedure.id , plan.id,  plan.count_of_tries);

                if( plan.count_of_tries > 5 ){
                    plan.state = Enum_CProgram_updater_state.critical_error;
                    plan.error = ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_message();
                    plan.error_code = ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_code();
                    plan.update();
                    terminal_logger.warn("actualization_update_procedure:: Procedure id:: {} plan {} CProgramUpdatePlan:: Error:: {} Message:: {} Continue Cycle. " , procedure.id , plan.id, ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_code() , ErrorCode.NUMBER_OF_ATTEMPTS_EXCEEDED.error_message());
                    continue;
                }

                // Najdu instanci - pod kterou deska běží
                Model_HomerInstance homer_instance = Model_HomerInstance.find.where()
                        .disjunction()
                           .add(Expr.eq("actual_instance.version_object.b_program_hw_groups.main_board_pair.board.id", plan.board.id))
                           .add(Expr.eq("actual_instance.version_object.b_program_hw_groups.device_board_pairs.board.id", plan.board.id))
                           .add(Expr.eq("boards_in_virtual_instance.id", plan.board.id))
                        .endJunction()
                .findUnique();

                if (homer_instance == null) {

                    terminal_logger.internalServerError(new Exception("Procedure ID = " + procedure.id + "  plan ID = " + plan.id + " Device has not own instance! There is place for fix it!")); // TODO
                    plan.state = Enum_CProgram_updater_state.instance_inaccessible;
                    plan.update();
                    continue;
                }


                terminal_logger.debug("actualization_update_procedure:: Procedure id:: {}  plan {} Updates is for Homer_instance id: {} Server {} " , procedure.id , plan.id,homer_instance.id, homer_instance.cloud_homer_server.personal_server_name);

                if(!homer_instance.cloud_homer_server.server_is_online()){
                    terminal_logger.warn("actualization_update_procedure:: Procedure id:: {}  plan {}  Server {} is offline. Putting off the task for later. -> Return. ", procedure.id , plan.id,homer_instance.cloud_homer_server.personal_server_name);
                   plan.state = Enum_CProgram_updater_state.homer_server_is_offline;
                   plan.update();
                   continue;
                }

                if (!homer_instance.instance_online()) {
                    terminal_logger.internalServerError(new Exception("Procedure  ID = " + procedure.id + "  plan ID = " + plan.id + " Instance " + homer_instance.cloud_homer_server.personal_server_name + " is offline. Putting off the task for later. This is not standard situation but bug in State Machine!"));

                    // Pokusím se instanci zase nahodit.
                    terminal_logger.trace("actualization_update_procedure:: Procedure id:: {}  plan {} Instance {} is offline. Try to add instance to server.", procedure.id, plan.id, homer_instance.id);
                    WS_Message_Update_device_summary_collection add_instance = homer_instance.add_instance_to_server();

                    if (add_instance.status.equals("success")) {
                        terminal_logger.trace("actualization_update_procedure:: Procedure id:: {}  plan {}  Instance {} Upload instance was successful" , procedure.id, plan.id, homer_instance.id);
                        plan.state = Enum_CProgram_updater_state.instance_inaccessible;
                        plan.update();
                        continue;

                    } else if (add_instance.status.equals("error")) {
                        terminal_logger.warn("actualization_update_procedure:: Procedure id:: {}  plan {} Instance {} Fail when Tyrion try to add instance from Blocko cloud_blocko_server Response Message:: {} ", procedure.id, plan.id, homer_instance.id, add_instance.toString());
                    }

                }

                terminal_logger.debug("actualization_update_procedure:: Procedure id:: {}  plan {} Instance {}  of blocko program is online and connected with Tyrion", procedure.id, plan.id, homer_instance.id);

                // Založím ve Struktuře seznam instnací
                if (!structure.instances.containsKey(homer_instance.id)) {
                    Instance instance = new Instance();
                    instance.instance = homer_instance;
                    structure.instances.put(homer_instance.id, instance);
                }

                String program_identifier = null;
                Model_FileRecord file_record = null;
                String name = null;
                String version = null;


                if (plan.firmware_type == Enum_Firmware_type.FIRMWARE) {

                    program_identifier = "firmware_" + plan.c_program_version_for_update.c_compilation.firmware_build_id;

                    if(plan.c_program_version_for_update.c_compilation.bin_compilation_file != null) {

                        terminal_logger.debug("actualization_update_procedure for Firmware:: User create own C_program and cloud_blocko_server has bin file of that");
                        file_record = plan.c_program_version_for_update.c_compilation.bin_compilation_file;
                        name = plan.c_program_version_for_update.c_program.name;
                        version =   plan.c_program_version_for_update.version_name;
                    } else {

                        terminal_logger.internalServerError(new Exception("Missing FileRecord, compilation was probably not uploaded to Azure. (TODO procedure for fixing this state.)"));
                        plan.state = Enum_CProgram_updater_state.bin_file_not_found;
                        plan.update();
                        continue;
                    }

                } if (plan.firmware_type == Enum_Firmware_type.BACKUP) {

                    program_identifier = "backup_" + plan.c_program_version_for_update.c_compilation.firmware_build_id;

                    if(plan.c_program_version_for_update.c_compilation.bin_compilation_file != null) {

                        terminal_logger.debug("actualization_update_procedure for Backup:: User create own C_program and cloud_blocko_server has bin file of that");
                        file_record = plan.c_program_version_for_update.c_compilation.bin_compilation_file;
                        name = plan.c_program_version_for_update.c_program.name;
                        version =   plan.c_program_version_for_update.version_name;
                    } else {

                        terminal_logger.internalServerError(new Exception("Missing FileRecord, compilation was probably not uploaded to Azure. (TODO procedure for fixing this state.)"));
                        plan.state = Enum_CProgram_updater_state.bin_file_not_found;
                        plan.update();
                        continue;
                    }

                } else if (plan.firmware_type == Enum_Firmware_type.BOOTLOADER) {

                    program_identifier = "boot_loader_" + plan.bootloader.version_identificator;
                    file_record = plan.bootloader.file;
                    name = plan.bootloader.name;
                    version = plan.bootloader.version_identificator;

                // Update vlastního firmwaru
                } else if (plan.binary_file != null) {
                    program_identifier = "f" + plan.binary_file.id;
                    file_record = plan.binary_file;
                }

                if(program_identifier == null){
                    terminal_logger.internalServerError(new Exception("C_program updater has not any object for upload! (Program, Bootloader, File)"));
                    continue;
                }

                // Pod instnací podle typu programu vytvořím program
                if(!structure.instances.get(homer_instance.id).programs.containsKey(program_identifier)){

                    Program program = new Program();

                    program.type_of_update = plan.actualization_procedure.type_of_update;
                    program.program_identificator = program_identifier;
                    program.firmware_type  = plan.firmware_type;
                    program.file_record = file_record;
                    program.name = name;
                    program.version = version;
                    structure.instances.get(homer_instance.id).programs.put(program_identifier, program);
                }

                Utilities_HW_Updater_Target_pair pair = new Utilities_HW_Updater_Target_pair();
                pair.targetId = plan.board.id;
                pair.c_program_update_plan_id = plan.id;

                structure.instances.get(homer_instance.id).programs.get(program_identifier).target_pairs.add(pair);

                plan.state = Enum_CProgram_updater_state.in_progress;
                plan.update();

            } catch(Exception e) {
                terminal_logger.internalServerError("actualization_update_procedure:", e);
                plan.state = Enum_CProgram_updater_state.critical_error;
                plan.update();
                break;
            }
        }

        terminal_logger.debug("Summary for actualizations");

        new Thread(procedure::notification_update_procedure_start).start();

        if(procedure.state != Enum_Update_group_procedure_state.in_progress){

            terminal_logger.debug("actualization_update_procedure: updating state of procedure to 'in progress'");
            
            procedure.state = Enum_Update_group_procedure_state.in_progress;
            procedure.update();
        }

        for (Instance instance : structure.instances.values()) {

            terminal_logger.debug("Summary: Instance ::" + instance.instance.id);

            Utilities_HW_Updater_Actualization_Task task = new Utilities_HW_Updater_Actualization_Task();
            task.instance = instance.instance;

            for(Program program : instance.programs.values()){

                terminal_logger.debug("  Summary: Program :: {} " , program.program_identificator);
                terminal_logger.debug("  Summary: Type of Update :: {} " , program.type_of_update);

                Utilities_HW_Updater_Actualization_procedure actualization_procedure = new Utilities_HW_Updater_Actualization_procedure();

                actualization_procedure.typeOfUpdate = program.type_of_update;
                actualization_procedure.actualizationProcedureId = procedure.id;
                actualization_procedure.file_record = program.file_record;
                actualization_procedure.firmwareType = program.firmware_type;

                terminal_logger.debug("       Summary: Targets ::");

                for(Utilities_HW_Updater_Target_pair pair : program.target_pairs){
                    terminal_logger.debug("       Summary: Targets :: " + pair.targetId + " update_procedure_id:: " + pair.c_program_update_plan_id);
                }

                actualization_procedure.targetPairs.addAll(program.target_pairs);

                task.procedures.add(actualization_procedure);
            }

            instance.instance.cloud_homer_server.add_task(task);
        }
    }
}