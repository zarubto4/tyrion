package utilities.hardware_updater;

import com.avaje.ebean.Expr;
import controllers.Controller_WebSocket;
import models.Model_Board;
import models.Model_FileRecord;
import models.Model_HomerInstance;
import models.Model_ActualizationProcedure;
import models.Model_CProgramUpdatePlan;
import play.libs.Json;
import utilities.enums.Enum_Update_group_procedure_state;
import utilities.enums.Enum_CProgram_updater_state;
import utilities.enums.Enum_Update_type_of_update;
import utilities.enums.Firmware_type;
import utilities.hardware_updater.helper_objects.Target_pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Master_Updater{

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    private static Master_Updater instance = null;
    protected Master_Updater() {/** Exists only to defeat instantiation.*/}

    public static Master_Updater getInstance() {
        if(instance == null) {instance = new Master_Updater(); start_thread_box();}
        return instance;
    }


    public static void start_thread_box(){
        logger.trace("Master_Updater:: start_thread_box:: will be started");

        if(!comprimator_thread.isAlive()) comprimator_thread.start();
    }


    public static void add_new_Procedure(Model_ActualizationProcedure procedure){

        logger.trace("Master_Updater:: start_thread_box:: new incoming procedure");

        procedures.add(procedure);

        if(comprimator_thread.getState() == Thread.State.TIMED_WAITING) {
            logger.trace("Master_Updater:: start_thread_box::  wait for interrupt!");
            comprimator_thread.interrupt();
        }
    }

// ** Comprimator Thread -----------------------------------------------------------------------------------------------

   public static List<Model_ActualizationProcedure> procedures = new ArrayList<>();

    static Thread comprimator_thread = new Thread() {

        @Override
        public void run() {


            logger.info("Master_Updater:: run:: run") ;

            while(true){
                try{

                    if(!procedures.isEmpty()) {

                        logger.debug("Master_Updater:: run:: Tasks to solve: " + procedures.size() );

                        new Master_Updater().actualization_update_procedure( procedures.get(0) );
                        procedures.remove( procedures.get(0) );

                    }

                    else{
                        logger.trace("Master_Updater:: run:: Going to sleep!");
                        sleep(500000000);
                    }



                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    logger.error("Master_Updater:: run:: Error", e);
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
    class ActualizationStructure {
        public HashMap<String, Instance> instances = new HashMap<>();
    }

    class Instance{
        public Model_HomerInstance instance;
        public HashMap<String, Program> programs = new HashMap<>(); // <Build_ID, Program>
    }

    class Program{
        public String program_identificator;
        public Firmware_type firmware_type;
        public Model_FileRecord file_record;
        public String name;
        public Enum_Update_type_of_update type_of_update;

        public List<Target_pair> target_pairs = new ArrayList<>();
    }



    private void actualization_update_procedure(Model_ActualizationProcedure procedure){


        if(procedure.state == Enum_Update_group_procedure_state.complete || procedure.state == Enum_Update_group_procedure_state.successful_complete ){
            logger.debug("Master_Updater:: actualization_update_procedure:: Procedure id:: " +procedure.id + " is done");
            return;
        }

        if(procedure.state == Enum_Update_group_procedure_state.in_progress){
            logger.debug("Master_Updater:: actualization_update_procedure:: Procedure id:: " +procedure.id + " already in progress");
            return;
        }


        if(procedure.updates.isEmpty()){

            procedure.state = Enum_Update_group_procedure_state.complete_with_error;
            procedure.update();
            logger.error("Master_Updater:: actualization_update_procedure:: Procedure id:: " +procedure.id + " is empty and not set to any updates!!!");
            return;
        }


        logger.debug("Master_Updater:: actualization_update_procedure:: Procedure id:: " +procedure.id + " state::  " + procedure.state);


        Map<String, String> files_codes = new HashMap<>(); // < c_program_version_id, code of program >
        ActualizationStructure structure = new ActualizationStructure();

        List<Model_CProgramUpdatePlan> plans = Model_CProgramUpdatePlan.find.where().eq("actualization_procedure.id", procedure.id)
                .disjunction()
                    .eq("state", Enum_CProgram_updater_state.not_start_yet)
                    .eq("state", Enum_CProgram_updater_state.waiting_for_device)
                    .eq("state", Enum_CProgram_updater_state.instance_inaccessible)
                    .eq("state", Enum_CProgram_updater_state.homer_server_is_offline)
                .endJunction()
                .findList();

            if(plans.isEmpty()){
                logger.debug("Master_Updater:: actualization_update_procedure:: Procedure id:: " +procedure.id + " all updates is done or in progress");
                return;
            }

           for (Model_CProgramUpdatePlan plan : plans) {
               try {

                   logger.debug("Master_Updater:: actualization_update_procedure:: Json CProgramUpdatePlan:: " + Json.toJson(plan));
                   logger.debug("Master_Updater:: actualization_update_procedure:: Json CProgramUpdatePlan:: ID:: " + plan.id);
                   logger.debug("Master_Updater:: actualization_update_procedure:: Json CProgramUpdatePlan:: Board ID:: " +  plan.board.id);
                   logger.debug("Master_Updater:: actualization_update_procedure:: Json CProgramUpdatePlan:: Status:: " +  plan.state);


                   Model_Board board  = plan.board;


                   // Najdu instanci - pod kterou deska běží
                   Model_HomerInstance homer_instance = Model_HomerInstance.find.where()
                           .disjunction()
                              .add(Expr.eq("actual_instance.version_object.b_program_hw_groups.main_board_pair.board.id", board.id))
                              .add(Expr.eq("actual_instance.version_object.b_program_hw_groups.device_board_pairs.board.id", board.id))
                              .add(Expr.eq("boards_in_virtual_instance.id", board.id))
                           .endJunction()
                   .findUnique();

                   if (homer_instance == null) {
                       logger.error("Master_Updater:: actualization_update_procedure:: Device has not own instance!");
                       plan.state = Enum_CProgram_updater_state.instance_inaccessible;
                       plan.update();
                       continue;
                   }

                   logger.debug("Master_Updater:: actualization_update_procedure:: Homer_instance id: "+ homer_instance.blocko_instance_name);


                   logger.debug("Master_Updater:: actualization_update_procedure:: Hardware (board) is running under cloud blocko program");
                   logger.debug("Master_Updater:: actualization_update_procedure:: Blocko Instance: "+ homer_instance.blocko_instance_name);
                   logger.debug("Master_Updater:: actualization_update_procedure:: Server: "+ homer_instance.cloud_homer_server.unique_identificator) ;


                   if(! Controller_WebSocket.homer_servers.containsKey( homer_instance.cloud_homer_server.unique_identificator )){
                      logger.warn("Master_Updater:: actualization_update_procedure:: Server is offline. Putting off the task for later ");
                      plan.state = Enum_CProgram_updater_state.homer_server_is_offline;
                      plan.update();
                      continue;
                   }

                   if (!homer_instance.instance_online()) {
                        logger.warn("Master_Updater:: actualization_update_procedure:: Homer is offline. Putting off the task for later ");
                        plan.state = Enum_CProgram_updater_state.instance_inaccessible;
                        plan.update();
                        continue;
                   }

                   logger.debug("Master_Updater:: actualization_update_procedure::  Instance of blocko program is online and connected with Tyrion");


                   // Založím ve Struktuře seznam instnací
                  if (!structure.instances.containsKey(homer_instance.blocko_instance_name)) {
                      Instance instance = new Instance();
                      instance.instance = homer_instance;
                      structure.instances.put(homer_instance.blocko_instance_name, instance);
                  }

                  String program_identificator = null;
                  Model_FileRecord file_record = null;
                  String name = null;
                  String version = null;


                  if(plan.firmware_type == Firmware_type.FIRMWARE) {

                            program_identificator = "firmware_" + plan.c_program_version_for_update.c_compilation.firmware_build_id;

                            if(plan.c_program_version_for_update.c_compilation.bin_compilation_file != null) {
                                logger.debug("Master_Updater:: actualization_update_procedure for Firmware:: User create own C_program and cloud_blocko_server has bin file of that");
                                file_record = plan.c_program_version_for_update.c_compilation.bin_compilation_file;
                                name = plan.c_program_version_for_update.c_program.name;
                                version =   plan.c_program_version_for_update.version_name;
                            }
                            else{
                                logger.error("..........V Blob serveru nebyla - musí se vytvořit");
                                logger.error("..........Spouštím proceduru dodatečné procedury protože kompilačku v azure nemám");
                                logger.error("..........Tato procedura chybí!");
                                plan.state = Enum_CProgram_updater_state.bin_file_not_found;
                                plan.update();
                                continue;
                            }

                  }if(plan.firmware_type == Firmware_type.BACKUP) {

                       program_identificator = "backup_" + plan.c_program_version_for_update.c_compilation.firmware_build_id;

                       if(plan.c_program_version_for_update.c_compilation.bin_compilation_file != null) {
                           logger.debug("Master_Updater:: actualization_update_procedure for Backup:: User create own C_program and cloud_blocko_server has bin file of that");
                           file_record = plan.c_program_version_for_update.c_compilation.bin_compilation_file;
                           name = plan.c_program_version_for_update.c_program.name;
                           version =   plan.c_program_version_for_update.version_name;
                       }
                       else{
                           logger.error("..........V Blob serveru nebyla - musí se vytvořit");
                           logger.error("..........Spouštím proceduru dodatečné procedury protože kompilačku v azure nemám");
                           logger.error("..........Tato procedura chybí!");
                           plan.state = Enum_CProgram_updater_state.bin_file_not_found;
                           plan.update();
                           continue;
                       }

                   }else if(plan.firmware_type == Firmware_type.BOOTLOADER) {

                            program_identificator = "boot_loader_" + plan.bootloader.version_identificator;
                            file_record = plan.bootloader.file;
                            name = plan.bootloader.name;
                            version = plan.bootloader.version_identificator;

                  // Update vlastního firmwaru
                  }else if(plan.binary_file != null) {
                            program_identificator = "f" + plan.binary_file.id;
                            file_record = plan.binary_file;
                  }



                   if(program_identificator == null){
                       logger.error("Master_Updater:: actualization_update_procedure:: C_program updateter has not any object for uploud! (Program, Bootloader, File) ");
                       continue;
                   }


                  // Pod instnací podle typu programu vytvořím program
                  if(!structure.instances.get(homer_instance.blocko_instance_name).programs.containsKey(program_identificator)){

                      Program program = new Program();
                      program.type_of_update = plan.actualization_procedure.type_of_update;
                      program.program_identificator = program_identificator;
                      program.firmware_type  = plan.firmware_type;
                      program.file_record = file_record;
                      program.name = name;
                      program.name = version;
                      structure.instances.get(homer_instance.blocko_instance_name).programs.put(program_identificator, program);

                  }

                   Target_pair pair = new Target_pair();
                   pair.targetId = board.id;
                   pair.c_program_update_plan_id = plan.id;

                   structure.instances.get(homer_instance.blocko_instance_name).programs.get(program_identificator).target_pairs.add(pair);

                   plan.state = Enum_CProgram_updater_state.in_progress;
                   plan.update();

               }catch(Exception e) {
                   logger.error("Master_Updater:: actualization_update_procedure:: Error:: ", e);
                   plan.state = Enum_CProgram_updater_state.critical_error;
                   plan.update();
                   break;
               }
           }

        for (Instance instance : structure.instances.values()) {

            Actualization_Task task = new Actualization_Task();
            task.instance = instance.instance;

            for(Program program : instance.programs.values()){

                Actualization_procedure actualization_procedure= new Actualization_procedure();
                actualization_procedure.typeOfUpdate = program.type_of_update;
                actualization_procedure.actualizationProcedureId = procedure.id;
                actualization_procedure.file_record = program.file_record;
                actualization_procedure.firmwareType = program.firmware_type;
                actualization_procedure.targetPairs.addAll(program.target_pairs);

                task.procedures.add(actualization_procedure);
            }

            instance.instance.cloud_homer_server.add_task(task);
        }


    }
}
