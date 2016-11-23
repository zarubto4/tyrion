package utilities.hardware_updater;

import com.avaje.ebean.Expr;
import controllers.WebSocketController;
import models.compiler.Board;
import models.compiler.FileRecord;
import models.project.b_program.instnace.Homer_Instance;
import models.project.c_program.actualization.Actualization_procedure;
import models.project.c_program.actualization.C_Program_Update_Plan;
import play.libs.Json;
import utilities.enums.Firmware_type;
import utilities.hardware_updater.States.C_ProgramUpdater_State;

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
        logger.debug("Master Update will be started");

        if(!comprimator_thread.isAlive()) comprimator_thread.start();
    }


    public static void add_new_Procedure(Actualization_procedure procedure){

        logger.debug("Master Updater - new incoming procedure");

        procedures.add(procedure.id);

        if(comprimator_thread.getState() == Thread.State.TIMED_WAITING) {
            logger.debug("Thread is sleeping - wait for interrupt!");
            comprimator_thread.interrupt();
        }
    }

// ** Comprimator Thread -----------------------------------------------------------------------------------------------

   public static List<String> procedures = new ArrayList<>();

    static Thread comprimator_thread = new Thread() {

        @Override
        public void run() {


            logger.info("Independent Thread in Master updater now working") ;

            while(true){
                try{

                    if(!procedures.isEmpty()) {

                        logger.debug("Master updater Thread is running. Tasks to solve: " + procedures.size() );

                        new Master_Updater().actualization_update_procedure( procedures.get(0) );
                        procedures.remove( procedures.get(0) );

                    }

                    else{
                        logger.debug("Master updater Thread has not other tasks. Going to sleep!");
                        sleep(500000000);
                    }



                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    logger.error("Master Updater Error", e);
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
        public Homer_Instance instance;
        public HashMap<String, Program> programs = new HashMap<>();
    }

    class Program{
        public String program_identificator;
        public Firmware_type firmware_type;
        public FileRecord file_record;
        public List<Board> boards = new ArrayList<>();
    }


    public void actualization_update_procedure(String procedure_id){



        Map<String, String> files_codes = new HashMap<>(); // < c_program_version_id, code of program >
        ActualizationStructure structure = new ActualizationStructure();

        List<C_Program_Update_Plan> plans = C_Program_Update_Plan.find.where().eq("actualization_procedure.id", procedure_id).findList();

           for (C_Program_Update_Plan plan : plans) {
               try {

                   System.err.println("Json: " + Json.toJson(plan));

                   logger.debug("Zkoumaná plan id: " + plan.id);


                   Board board  = plan.board;

                   logger.debug("Zkoumaná Boar id: "+ board.id);

                   // Najdu instanci - pod kterou deska běží
                   Homer_Instance homer_instance = Homer_Instance.find.where()
                           .disjunction()
                            .add(Expr.eq("version_object.b_program_hw_groups.main_board_pair.board.id", board.id))
                            .add(Expr.eq("version_object.b_program_hw_groups.device_board_pairs.board.id", board.id))
                            .add(Expr.eq("private_instance_board.id", board.id))
                   .findUnique();

                   if (homer_instance == null) {
                       logger.error("Device has not own instance!");
                       plan.state = C_ProgramUpdater_State.instance_inaccessible;
                       plan.update();
                       continue;
                   }

                   logger.debug("Homer_instance id: "+ homer_instance.blocko_instance_name);


                   logger.debug("Hardware (board) is running under cloud blocko program");
                   logger.debug("Blocko Instance: "+ homer_instance.blocko_instance_name);
                   logger.debug("Server: "+ homer_instance.cloud_homer_server.server_name) ;


                   if(! WebSocketController.blocko_servers.containsKey( homer_instance.cloud_homer_server.server_name )){
                      logger.warn("Server is offline. Putting off the task for later ");
                      plan.state = C_ProgramUpdater_State.homer_server_is_offline;
                      plan.update();
                      continue;
                   }

                   if (!homer_instance.instance_online()) {
                        logger.warn("Homer is offline. Putting off the task for later ");
                        plan.state = C_ProgramUpdater_State.instance_inaccessible;
                        plan.update();
                        continue;
                   }

                   logger.debug("Instance of blocko program is online and connected with Tyrion");


                   // Založím ve Struktuře seznam instnací
                  if (!structure.instances.containsKey(homer_instance.blocko_instance_name)) {
                      Instance instance = new Instance();
                      instance.instance = homer_instance;
                      structure.instances.put(homer_instance.blocko_instance_name, instance);
                  }

                  String program_identificator = null;
                  FileRecord file_record = null;


                  if(plan.c_program_version_for_update != null) {
                            program_identificator = "firmware_" + plan.c_program_version_for_update.c_compilation.firmware_build_id;

                            if(plan.c_program_version_for_update.c_compilation.bin_compilation_file != null) {
                                logger.debug("User create own C_program and cloud_blocko_server has bin file of that");
                                file_record = plan.c_program_version_for_update.c_compilation.bin_compilation_file;
                            }
                            else{
                                System.out.println("..........V Blob serveru nebyla - musí se vytvořit");
                                System.out.println("..........Spouštím proceduru dodatečné procedury protože kompilačku v azure nemám");
                                System.out.println("..........Tato procedura chybí!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                                // TODO
                                continue;
                            }

                  } else if(plan.firmware_type == Firmware_type.BOOTLOADER) {
                            program_identificator = "boot_loader_" + plan.binary_file.boot_loader.version_identificator;
                            file_record = plan.binary_file;

                  }else if(plan.binary_file != null) {
                            program_identificator = "file_" + plan.binary_file.id;
                            file_record = plan.binary_file;
                  }



                   if(program_identificator == null){
                       logger.error("C_program updateter has not any object for uploud! (Program, Bootloader, File) ");
                       continue;
                   }


                  // Pod instnací podle typu programu vytvořím program
                  if(!structure.instances.get(homer_instance.blocko_instance_name).programs.containsKey(program_identificator)){

                      Program program = new Program();
                      program.program_identificator = program_identificator;
                      program.firmware_type  = plan.firmware_type;
                      program.file_record = file_record;

                      structure.instances.get(homer_instance.blocko_instance_name).programs.put(program_identificator, program);

                  }

                   structure.instances.get(homer_instance.blocko_instance_name).programs.get(program_identificator).boards.add(board);

                   plan.state = C_ProgramUpdater_State.in_progress;
                   plan.update();

               }catch(Exception e) {
                   logger.error("Error while cloud_blocko_server tried compile version of C_program", e);
                   plan.state = C_ProgramUpdater_State.critical_error;
                   plan.update();
                   break;
               }
           }

        for (Instance instance : structure.instances.values()) {

            for(Program program : instance.programs.values()){

                Actualization_Task task = new Actualization_Task();
                task.actualization_procedure_id = procedure_id;
                task.instance = instance.instance;
                task.file_record = program.file_record;
                task.boards = program.boards;
                task.firmware_type = program.firmware_type;

                logger.debug("Actualization task is ready. Sending to Server object to cloud_blocko_server blocko independent Thread");
                instance.instance.cloud_homer_server.add_task(task);

            }
        }
    }
}
