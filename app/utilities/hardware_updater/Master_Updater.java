package utilities.hardware_updater;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.WebSocketController_Incoming;
import models.compiler.Board;
import models.compiler.C_Program_Update_Plan;
import models.compiler.FileRecord;
import models.compiler.Version_Object;
import models.project.b_program.B_Program_Cloud;
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
        update_thread.start();
        comprimator_thread.start();
    }

    public static void destroy_thread(){
        update_thread.interrupt();
        comprimator_thread.interrupt();
    }

    public static void add_new_device_for_update(String project_id, List<C_Program_Update_Plan> device_for_update){
        devices_for_update.addAll(device_for_update);
    }

    public static void add_new_device_for_update(String project_id, C_Program_Update_Plan device_for_update){
        devices_for_update.add(device_for_update);
    }

// ** Comprimator Thread -----------------------------------------------------------------------------------------------

    static List<C_Program_Update_Plan> devices_for_update = new ArrayList<>();

    static Thread comprimator_thread = new Thread() {
        @Override
        public void run() {
            while(true){
                try{

                    if(!devices_for_update.isEmpty()) {

                        System.out.println("Vlákno přípravy C_programu šlape") ;

                        C_Program_Update_Plan plan =  devices_for_update.get(0);

                        new Master_Updater().board_update_Pair( plan );

                        devices_for_update.remove(plan);
                    }
                    else sleep(5000);

                }catch (Exception e){
                    logger.error("Master Updater Error", e);
                }
            }
        }
    };

// ** Updater Thread -----------------------------------------------------------------------------------------------

    static public ArrayList<Actualization_Task> task_list = new ArrayList<>();

    static Thread update_thread = new Thread() {
        @Override
        public void run() {
            while(true){
                try{

                    if(!task_list.isEmpty()) {

                        logger.debug("Počet zařízení k aktualizaci ještě: " + task_list.size() );

                        Actualization_Task task = task_list.get(0);

                        System.out.println("Odesílám požadavek na aktualizaci!");
                        JsonNode result = WebSocketController_Incoming.homer_update_embeddedHW(task.homer, task.device_ids, task.code );

                        System.out.println("Odpověď na Aktualizaci:" + result.toString());
                        System.out.println("Ještě neřeším reakci");
                        task_list.remove(task);

                    }
                    else sleep(5000);

                }catch (Exception e){
                    logger.error("Master Updater Error", e);
                }
            }
        }
    };



 //   /Users/zaruba/ownCloud/Git/Tyrion/build.sbt
    public void board_update_Pair(C_Program_Update_Plan plan){
       try{

            Map<String, String> files_codes = new HashMap<>(); // < c_program_version_id, code of program >

            System.out.println("Updatuji seznam Hardwaru");


                // Desku kterou chci updatovat
                Board board = plan.board_for_update;
                String id = plan.c_program_version_for_update.id;

                // Verze k updatu
                Version_Object c_program_version_for_update = plan.c_program_version_for_update;


                B_Program_Cloud b_program_cloud = B_Program_Cloud.find.where().or(
                        com.avaje.ebean.Expr.eq("version_object.master_board_b_pair.board.id", board.id),
                        com.avaje.ebean.Expr.eq("version_object.b_pairs_b_program.board.id", board.id)
                ).findUnique();


                System.out.println("Blocko Instnance: " + b_program_cloud.blocko_instance_name);
                System.out.println("Server Kde to běží" + b_program_cloud.server.server_name);


                // Zajištuji do paměti kod k nahrátí
                if (!files_codes.containsKey(id)) {

                    System.out.println("Nemám compilačku v bufferu a tak jí tahám z azure");

                    FileRecord file_record = FileRecord.find.where().eq("version_object.id", id).where().eq("file_name", "compilation.bin").findUnique();
                    if (file_record != null) {
                        System.out.println("V azure byla");
                        files_codes.put(id, file_record.get_fileRecord_from_Azure_inString());
                    } else {
                        System.out.println("V azure nebyla");
                        System.out.println("Spouštím proceduru dodatečné procedury");
                        // TODO
                        return;
                    }
                }

                System.out.println("Komprimace updatu proběhla!");



                // Zkontroluji instanci Homera zda běží
                if (!WebSocketController_Incoming.incomingConnections_homers.containsKey(b_program_cloud.blocko_instance_name)) {

                    logger.debug("Homer is offline. Putting off the task for later ");
                    return;

                }

                logger.debug("Homer is online for update ");
                WebSCType homer = WebSocketController_Incoming.incomingConnections_homers.get(b_program_cloud.blocko_instance_name);


                Actualization_Task task = new Actualization_Task();
                task.homer = homer;
                task.code = files_codes.get(id);
                task.device_ids.add( plan.board_for_update.id );
                task_list.add(task);


        }catch (Exception e){
            logger.error("Error while server tried compile version of C_program", e);
        }


    }



}
