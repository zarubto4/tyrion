package utilities.independent_threads.homer_server;

import models.*;
import play.libs.Json;
import utilities.logger.Class_Logger;
import web_socket.message_objects.homer_hardware_with_tyrion.WS_Message_Hardware_connected;
import web_socket.message_objects.homer_hardware_with_tyrion.WS_Message_Hardware_overview_Board;
import web_socket.message_objects.homer_with_tyrion.WS_Message_Homer_Hardware_list;
import web_socket.services.WS_HomerServer;

import java.util.List;


public class Synchronize_Homer_Hardware_after_connection extends Thread{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Synchronize_Homer_Unresolved_Updates.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    private WS_HomerServer ws_homerServer = null;
    private Model_HomerServer homer_server = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Synchronize_Homer_Hardware_after_connection(WS_HomerServer ws_homerServer) {
        this.ws_homerServer = ws_homerServer;
        this.homer_server = Model_HomerServer.get_byId(ws_homerServer.identifikator);
    }


    @Override
    public void run(){

        try {

            terminal_logger.info("4. Spouštím Sycnhronizační proceduru Synchronize_Homer_Hardware_after_connection");


            WS_Message_Homer_Hardware_list message_homer_hardware_list = homer_server.get_homer_server_list_of_hardware();

            if(!message_homer_hardware_list.status.equals("success")){
                terminal_logger.warn("Message WS_Message_Homer_Hardware_list: invalid response - something is wrong");
                return;
            }

            List<String> device_ids_on_server = message_homer_hardware_list.hardware_ids;
            terminal_logger.info("4. Number of registered or connected Devices on Server:: {} ", device_ids_on_server.size());
            check_device_on_server(device_ids_on_server);

            terminal_logger.trace("4, Number of required HW on this server: {}", Model_Board.find.where().eq("connected_server_id", this.ws_homerServer.get_identificator()).select("id").select("connected_server_id").findRowCount());

        }catch(Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    public void check_device_on_server(List<String> device_ids_on_server){

        for(String board_not_cached_id : device_ids_on_server) {

            Model_Board board = Model_Board.get_byId(board_not_cached_id);
            if(board == null) continue;

            if(board.connected_server_id == null){

                terminal_logger.debug("4.4 " + board.id + " Device se ještě nikdy nepřipojil a tak mu nastavuji výchozí server");
                board.connected_server_id = this.ws_homerServer.get_identificator();
                board.update();

            }else if(!board.connected_server_id.equals(this.ws_homerServer.get_identificator())){
                terminal_logger.debug("4.4  {} Device je na špatném serveru a tak ho relokuji!!", board.id);
                board.device_relocate_server(Model_HomerServer.get_byId(ws_homerServer.identifikator));
                continue;
            }else {
                terminal_logger.trace("4.4 {} Device je na správném serveru evidentně a tak ho jenom zkrontroluji", board.id);
            }

            WS_Message_Hardware_overview_Board overview = board.get_devices_overview();

            if(overview.status.equals("success")){
                terminal_logger.trace("4.4 {} Status HW je {}", board.id, overview.online_state);

                WS_Message_Hardware_connected connected = new WS_Message_Hardware_connected();
                connected.status = overview.status;
                connected.hardware_id = board.id;
                Model_Board.device_Connected(connected);

            }else {
                terminal_logger.warn("Something is wrong with WS_Help_Hardware_board_overview message");
                terminal_logger.warn("Incoming message WS_Help_Hardware_board_overview {}", Json.toJson(overview));
            }
        }
    }
}
