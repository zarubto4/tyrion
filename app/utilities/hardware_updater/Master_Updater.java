package utilities.hardware_updater;

import com.avaje.ebean.Expr;
import controllers.WebSocketController_Incoming;
import models.compiler.Board;
import models.compiler.FileRecord;
import models.project.b_program.Homer_Instance;
import models.project.c_program.actualization.Actualization_procedure;
import models.project.c_program.actualization.C_Program_Update_Plan;
import utilities.hardware_updater.States.C_ProgramUpdater_State;
import utilities.webSocket.WS_BlockoServer;
import utilities.webSocket.WebSCType;

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
        comprimator_thread.start();
    }


    public static void add_new_Procedure(Actualization_procedure procedure){

        logger.debug("Master Updater - new incoming procedure");

        procedures.add(procedure);

        if(comprimator_thread.getState() == Thread.State.TIMED_WAITING) {
            logger.debug("Thread is sleeping - wait for interrupt!");
            comprimator_thread.interrupt();
        }
    }

// ** Comprimator Thread -----------------------------------------------------------------------------------------------

   public static List<Actualization_procedure> procedures = new ArrayList<>();

    static Thread comprimator_thread = new Thread() {
        @Override
        public void run() {


            logger.info("Independent Thread in Master updater now working") ;

            while(true){
                try{


                    if(!procedures.isEmpty()) {

                        logger.debug("Master updater Thread is running. Tasks to solve: " + procedures.size() );

                        Actualization_procedure procedure =  procedures.get(0);
                        actualization_update_procedure( procedure );
                        procedures.remove(procedure);

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

    public static void actualization_update_procedure(Actualization_procedure procedure){


           Map<String, String> files_codes = new HashMap<>(); // < c_program_version_id, code of program >

        logger.debug("Master Updater: actualization_update_procedure or " + ( procedure.id == null ? "virtual procedure" : ("real procedure" + procedure.id) ) );

           for (C_Program_Update_Plan plan : procedure.updates) {
               try {

               // Desku kterou chci updatovat

               Board board  = plan.board;
               String id    = plan.c_program_version_for_update != null ? plan.c_program_version_for_update.id : ( plan.binary_file.file_name + "_" + plan.binary_file.id);
               WS_BlockoServer server = null;
               WebSCType actual_homer = null;

               // Verze k updatu
               //1
               Homer_Instance homer_instance = Homer_Instance.find.where()
                       .disjunction()
                           .add(Expr.eq("version_object.yoda_board_pair.board.id", board.id))
                           .add(Expr.eq("version_object.padavan_board_pairs.board.id", board.id))
                           .add(Expr.eq("private_instance_board.id", board.id))
                       .findUnique();

               if (homer_instance != null) {

                   logger.debug("Hardware (board) is running under cloud blocko program");
                   logger.debug("Blocko Instance: " + homer_instance.blocko_instance_name);
                   logger.debug("Server: " + homer_instance.cloud_homer_server.server_name) ;


                   if(! WebSocketController_Incoming.blocko_servers.containsKey( homer_instance.cloud_homer_server.server_name )){
                       logger.debug("Server is offline. Putting off the task for later ");
                       plan.state = C_ProgramUpdater_State.homer_server_is_offline;
                       plan.update();
                       break;
                   }
                   server = (WS_BlockoServer) WebSocketController_Incoming.blocko_servers.get( homer_instance.cloud_homer_server.server_name );

                   if (!WebSocketController_Incoming.incomingConnections_homers.containsKey(homer_instance.blocko_instance_name)) {
                       logger.debug("Homer is offline. Putting off the task for later ");
                       plan.state = C_ProgramUpdater_State.instance_inaccessible;
                       plan.update();
                       break;
                   }

                   logger.debug("Instance of blocko program is online and connected with Tyrion");
                   actual_homer = WebSocketController_Incoming.incomingConnections_homers.get(homer_instance.blocko_instance_name);

               }

               //2 Lokální Homer
               // TODO Lokální homer

               else if (board.latest_know_server != null) {

                   logger.error("Hardware is not paired with any Blocko program in the cloud or locally with homer, but still connect to the cloud_blocko_server: " + board.latest_know_server.server_name);


                    if(! WebSocketController_Incoming.blocko_servers.containsKey( board.latest_know_server.server_name )){
                        logger.debug("Server is offline");
                        plan.state = C_ProgramUpdater_State.homer_server_is_offline;
                        plan.update();
                        break;
                    }

                    server = (WS_BlockoServer) WebSocketController_Incoming.blocko_servers.get( board.latest_know_server.server_name );
                }


               // Zajištuji do paměti kod k nahrátí
               if (!files_codes.containsKey(id)) {

                   logger.debug("Actualization Bin file is not in buffer! Server must download that from Azure Blob cloud_blocko_server!");

                   if(plan.binary_file != null) {
                       logger.debug("User uploud own binary file to update");
                       FileRecord  file_record = plan.binary_file;
                       files_codes.put(  ( plan.binary_file.file_name + "_" + plan.binary_file.id) , file_record.get_fileRecord_from_Azure_inString() );
                   }
                   else if( plan.c_program_version_for_update != null){

                       if(plan.c_program_version_for_update.c_compilation.bin_compilation_file != null) {
                           logger.debug("User create own C_program and cloud_blocko_server has bin file of that");


                           files_codes.put(plan.c_program_version_for_update.id, plan.c_program_version_for_update.c_compilation.bin_compilation_file.get_fileRecord_from_Azure_inString() );
                       }
                       else{
                           System.out.println("..........V Blob serveru nebyla - musí se vytvořit");
                           System.out.println("..........Spouštím proceduru dodatečné procedury protože kompilačku v azure nemám");
                           System.out.println("..........Tato procedura chybí!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                           // TODO
                           break;
                       }
                   }
               }

                   if(server == null) {
                       logger.debug("The equipment has never entered into the system. Tyrion has no chance do anything! So Tyrion must wait for device!");

                       plan.state = C_ProgramUpdater_State.waiting_for_device;
                       plan.update();
                       break;
                   }

                   // Aktualizační task, který budu posílat na cloud_blocko_server!
                   Actualization_Task task = new Actualization_Task();
                   task.homer = actual_homer;
                   task.code = files_codes.get(id);
                   task.board = board;

                   plan.state = C_ProgramUpdater_State.in_progress;
                   plan.update();

                   logger.debug("Actualization task is ready. Sending to Server object to cloud_blocko_server blocko independent Thread");
                   server.add_task(task);

               }catch(Exception e) {
                   logger.error("Error while cloud_blocko_server tried compile version of C_program", e);
                   plan.state = C_ProgramUpdater_State.critical_error;
                   plan.update();
                   break;
               }
           }

    }


}
