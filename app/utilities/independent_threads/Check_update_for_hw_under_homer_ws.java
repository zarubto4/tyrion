package utilities.independent_threads;

import web_socket.services.WS_HomerServer;
import web_socket.message_objects.homer_instance.WS_Message_Get_summary_information;

import java.util.ArrayList;
import java.util.List;

public class Check_update_for_hw_under_homer_ws  {

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    private WS_HomerServer homer_server = null;

    private List<WS_Message_Get_summary_information> list = new ArrayList<>();

    public Check_update_for_hw_under_homer_ws(WS_HomerServer homer_server){
        this.homer_server = homer_server;
        start_thread_box();
    }


    public void start_thread_box(){
        logger.trace("Check_update_for_hw_under_homer_ws:: start_thread_box:: will be started");
        if(!hw_checker_thread.isAlive()) hw_checker_thread.start();
    }

    public void add_new_Procedure(WS_Message_Get_summary_information summary_information){

        logger.debug("Check_update_for_hw_under_homer_ws:: add_new_Procedure:: new incoming procedure");

        list.add(summary_information);

        if(hw_checker_thread.getState() == Thread.State.TIMED_WAITING) {
            logger.trace("Thread is sleeping - wait for interrupt!");
            hw_checker_thread.interrupt();
        }
    }

    private Thread hw_checker_thread = new Thread() {

        @Override
        public void run() {

            logger.trace("Check_update_for_hw_under_homer_ws:: add_new_Procedure:: Independent Thread in Check_update_for_hw_under_homer_ws now working"); ;

            while(true){
                try{

                    if(!list.isEmpty()){

                        Check_Update_for_hw_on_homer.check_Update(homer_server, list.get(0));
                        list.remove(list.get(0));

                    } else{
                        logger.trace("Check_update_for_hw_under_homer_ws:: Thread has not other tasks. Going to sleep!");
                        sleep(500000000);
                    }

                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    logger.error("Check_update_for_hw_under_homer_ws:: Error", e);
                }
            }
        }
    };
}
