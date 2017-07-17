package utilities.independent_threads.homer_server;

import com.avaje.ebean.Model;
import com.avaje.ebean.PagedList;
import models.*;
import utilities.logger.Class_Logger;
import web_socket.message_objects.homer_hardware_with_tyrion.WS_Message_Hardware_overview;
import web_socket.message_objects.homer_with_tyrion.WS_Message_Homer_Hardware_list;
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

            System.out.println("4. Spouštím Sycnhronizační proceduru Synchronize_Homer_Hardware_after_connection");

            int page = 0;
            int page_size = 100;

            System.out.println("4.1 Teď žádám homera o všechen Hardware");
            List<String> device_ids_on_server = Model_HomerServer.get_byId(ws_homerServer.identifikator).get_homer_server_list_of_hardware().hardware_ids;

            System.out.println("4.2 Hardware, který by měl běžet ");
            List<String> device_ids_required = new ArrayList<>();

            while(true){


                PagedList<Model_Board> paging_list = Model_Board.find.where().select("id").select("connected_server_id").findPagedList(page, page_size);

                System.out.println("4.3 Jdu kontrolovat seznam z databáze a hledat co na serveru mám navíc");
                for(Model_Board board : paging_list.getList()){

                    // Přidám do seznamu harwaru, který má na serveru být
                    device_ids_required.add(board.id);

                    if(!device_ids_on_server.contains(board.id)){

                        System.out.println("4.3 " + board.id + " Na server není harware který bych očekával - což znamená dvě  věci - je na jiném serveru nebo je offline");

                        if(board.connected_server_id == null){
                            System.out.println("4.4 " + board.id + " Device se ještě nikdy nepřipojil ");
                        }
                        else if(board.connected_server_id .equals( Model_HomerServer.get_byId( ws_homerServer.get_identificator()).unique_identificator)){
                             System.out.println("4.4 " + board.id + " Device je ofline a tak na něj seru ");

                        }else {
                            System.out.println("4.4 " + board.id + " Device je na špatném serveru a tak ho relokuji!!");
                            board.device_relocate_server(Model_HomerServer.get_byId(ws_homerServer.identifikator));
                        }

                    }else {
                        System.out.println("4.3 " + board.id + " Na server hardware je online");

                        // TODO dělat něco s overview??
                        System.out.println("4.4 Device overview?");
                        WS_Message_Hardware_overview overview = board.get_devices_overview();
                    }

                }


                // Pokud je počet stránek shodný
                if( paging_list.getTotalPageCount() == page) break;

                System.out.println("Page == " + page);
                page++;

            }


        }catch(Exception e){
            terminal_logger.internalServerError("run:", e);

        }
    }

}
