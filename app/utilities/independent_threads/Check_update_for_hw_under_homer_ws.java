package utilities.independent_threads;

import utilities.logger.Class_Logger;
import web_socket.services.WS_HomerServer;
import web_socket.message_objects.homer_instance.WS_Message_Get_summary_information;

import java.util.ArrayList;
import java.util.List;

public class Check_update_for_hw_under_homer_ws  {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Check_update_for_hw_under_homer_ws.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    private WS_HomerServer homer_server = null;

    private List<WS_Message_Get_summary_information> list = new ArrayList<>();

    public Check_update_for_hw_under_homer_ws(WS_HomerServer homer_server){
        this.homer_server = homer_server;
        start_thread_box();
    }


    public void start_thread_box(){
        terminal_logger.trace("start_thread_box:: will be started");
        if(!hw_checker_thread.isAlive()) hw_checker_thread.start();
    }

    public void add_new_Procedure(WS_Message_Get_summary_information summary_information){

        terminal_logger.debug("add_new_Procedure:: new incoming procedure");

        list.add(summary_information);

        if(hw_checker_thread.getState() == Thread.State.TIMED_WAITING) {
            terminal_logger.trace("add_new_Procedure:: Thread is sleeping - wait for interrupt!");
            hw_checker_thread.interrupt();
        }
    }

    private Thread hw_checker_thread = new Thread() {

        @Override
        public void run() {

            terminal_logger.trace("add_new_Procedure:: Independent Thread in Check_update_for_hw_under_homer_ws now working"); ;

            while(true){
                try{

                    if(!list.isEmpty()){

                        Check_Update_for_hw_on_homer.check_Update(homer_server, list.get(0));
                        list.remove(list.get(0));

                    } else{
                        terminal_logger.trace("add_new_Procedure:: Thread has not other tasks. Going to sleep!");
                        sleep(500000000);
                    }

                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    terminal_logger.internalServerError("run:", e);

                }
            }
        }
    };
}
