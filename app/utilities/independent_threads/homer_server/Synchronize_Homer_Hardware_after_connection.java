package utilities.independent_threads.homer_server;

import com.avaje.ebean.PagedList;
import models.*;
import utilities.logger.Class_Logger;
import web_socket.message_objects.homer_hardware_with_tyrion.WS_Message_Hardware_overview;
import web_socket.services.WS_HomerServer;

import java.util.ArrayList;
import java.util.List;


public class Synchronize_Homer_Hardware_after_connection extends Thread{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Synchronize_Homer_Unresolved_Updates.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    WS_HomerServer ws_homerServer = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Synchronize_Homer_Hardware_after_connection(WS_HomerServer ws_homerServer) {
        this.ws_homerServer = ws_homerServer;
    }


    @Override
    public void run(){

        try {

            int page = 0;
            int page_size = 100;


            List<String> device_ids_on_server = Model_HomerServer.get_byId(ws_homerServer.identifikator).get_homer_server_list_of_hardware().hardware_ids;
            List<String> device_ids_required = new ArrayList<>();

            while(true){


                PagedList<Model_Board> pagingList = Model_Board.find.where().findPagedList(page, page_size);

                for(Model_Board board : pagingList.getList()){

                    device_ids_required.add(board.id);

                    if(!device_ids_on_server.contains(board.id)){
                        board.add_to_server();
                    }

                    WS_Message_Hardware_overview overview = board.get_devices_overview();

                }


                for(String device_id : device_ids_on_server){

                    if(!device_ids_required.contains(device_id)){

                        terminal_logger.warn("Recolate Hardware required: {}", device_id);
                        Model_Board.get_byId(device_id).device_relocate_server(null); // TODO

                    }

                }



                // Pokud je počet stránek shodný
                if( pagingList.getTotalPageCount() == page) break;

                page++;

            }


            for(String device_id : device_ids_on_server){

                if(!device_ids_required.contains(device_id)){

                    // TODO Anomalie - přesměrovat na jiný server!
                    terminal_logger.warn("TODO chybí přesměrování na jiný server {}", device_id);
                }

            }


        }catch(Exception e){
            terminal_logger.internalServerError("run:", e);

        }
    }

}
