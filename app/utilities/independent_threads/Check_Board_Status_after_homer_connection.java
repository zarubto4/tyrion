package utilities.independent_threads;

import models.*;
import utilities.enums.Enum_Homer_instance_type;
import utilities.logger.Class_Logger;
import web_socket.message_objects.homerServer_with_tyrion.WS_Message_Get_instance_list;
import web_socket.message_objects.homer_instance.WS_Message_Online_states_devices;
import web_socket.services.WS_HomerServer;

import java.util.ArrayList;
import java.util.List;


public class Check_Board_Status_after_homer_connection  extends Thread{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Check_Update_for_hw_on_homer.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    WS_HomerServer ws_homerServer = null;
    Model_HomerServer model_server = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Check_Board_Status_after_homer_connection(WS_HomerServer ws_homerServer, Model_HomerServer model_server){
        this.ws_homerServer = ws_homerServer;
        this.model_server = model_server;
    }


    @Override
    public void run(){
        Long interrupter = (long) 6000;
        try {

            while (interrupter > 0) {

                sleep(1000);
                interrupter -= 500;

                if (ws_homerServer.isReady()){

                    terminal_logger.debug("Check_Homer_instance_after_connection:: run:: Tyrion send to Homer Server request for listInstances");

                    WS_Message_Get_instance_list list_instances = model_server.get_homer_server_listOfInstance();

                    // Vytvořím kopii seznamu instancí, které by měli běžet na Homer Serveru
                    for(String  identificator : list_instances.instances){

                        try{

                           Model_HomerInstance instance = Model_HomerInstance.get_model(identificator);

                           List<String> boards_ids = new ArrayList<>();

                           if( instance.instance_type == Enum_Homer_instance_type.VIRTUAL){

                               for(Model_Board board :instance.boards_in_virtual_instance) {
                                   boards_ids.add(board.id);
                               }

                           }

                           if( instance.instance_type == Enum_Homer_instance_type.INDIVIDUAL ) {

                               for (Model_BProgramHwGroup group : instance.actual_instance.hardware_group()) {

                                   boards_ids.add(group.main_board_pair.board_id());

                                   if (!group.device_board_pairs.isEmpty()) {

                                       for (Model_BPair pair : group.device_board_pairs) {
                                           boards_ids.add(pair.board_id());
                                       }
                                   }
                               }
                           }

                           if(boards_ids.isEmpty()){
                               terminal_logger.warn(this.getClass().getSimpleName() + ": run:: list of Hardware under instance " + identificator + " is empty");
                               continue;
                           }

                            terminal_logger.debug("Check_Homer_instance_after_connection:: run:: Required instance id:: " + identificator + " Number of hardwares:: " + boards_ids.size() );

                           WS_Message_Online_states_devices result = instance.get_devices_online_state(boards_ids);

                           if(result.status.equals("success")){

                               terminal_logger.debug(this.getClass().getSimpleName() + ": run:: WS_Message_Online_states_devices success: Počet odpovědí:: "+ result.deviceList.size());



                               for(WS_Message_Online_states_devices.DeviceStatus device : result.deviceList){

                                   terminal_logger.debug(this.getClass().getSimpleName() + ": run:: Updatuji Cache status Devicu:: " + device.deviceId+  " na "  + device.online_status);

                                   Model_Board.cache_status.put(device.deviceId, device.online_status);
                               }

                           }else {
                               terminal_logger.warn(this.getClass().getSimpleName() + ": run:: WS_Message_Online_states_devices error: " + result.error + " ErrorCode:: " + result.errorCode);
                           }

                        }catch (Exception e){
                            terminal_logger.internalServerError(e);
                        }
                    }


                    break;
                }
            }

            model_server.check_HW_updates_on_server();

        }catch(Exception e){
            terminal_logger.internalServerError(e);

        }
    }

}
