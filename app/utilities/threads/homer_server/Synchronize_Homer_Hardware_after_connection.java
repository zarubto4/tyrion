package utilities.threads.homer_server;

import models.*;
import play.libs.Json;
import utilities.logger.Logger;
import websocket.interfaces.WS_Homer;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_connected;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_overview_Board;
import websocket.messages.homer_with_tyrion.WS_Message_Homer_Hardware_list;

import java.util.List;
import java.util.UUID;


public class Synchronize_Homer_Hardware_after_connection extends Thread{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger terminal_logger = new Logger(Synchronize_Homer_Unresolved_Updates.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    private WS_Homer homer = null;
    private Model_HomerServer homer_server = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Synchronize_Homer_Hardware_after_connection(WS_Homer homer) {
        this.homer = homer;
        this.homer_server = Model_HomerServer.getById(homer.id);
    }


    @Override
    public void run() {

        try {

            terminal_logger.info("4. Spouštím Sycnhronizační proceduru Synchronize_Homer_Hardware_after_connection");

            WS_Message_Homer_Hardware_list message_homer_hardware_list = homer_server.get_homer_server_list_of_hardware();

            if (!message_homer_hardware_list.status.equals("success")) {
                terminal_logger.warn("Message WS_Message_Homer_Hardware_list: invalid response - something is wrong");
                return;
            }

            List<String> device_ids_on_server = message_homer_hardware_list.full_ids;
            terminal_logger.info("4. Number of registered or connected Devices on Server:: {} ", device_ids_on_server.size());
            check_device_on_server(device_ids_on_server);

            terminal_logger.trace("4, Number of required HW on this server: {}", Model_Hardware.find.query().where().eq("connected_server_id", this.homer.id).select("id").select("connected_server_id").findCount());

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    public void check_device_on_server(List<String> full_ids_on_server) {

        for (String full_id : full_ids_on_server) {

            Model_Hardware board = Model_Hardware.getByFullId(full_id);
            if (board == null) continue;

            if (board.connected_server_id == null) {

                terminal_logger.debug("4.4 " + board.id + " Device se ještě nikdy nepřipojil a tak mu nastavuji výchozí server");
                board.connected_server_id = this.homer.id;
                board.update();

            } else if (!board.connected_server_id.equals(this.homer.id)) {
                terminal_logger.debug("4.4  {} Device je na špatném serveru a tak ho relokuji!!", board.id);
                board.device_relocate_server(Model_HomerServer.getById(homer.id));
                continue;
            } else {
                terminal_logger.trace("4.4 {} Device je na správném serveru evidentně a tak ho jenom zkrontroluji", board.id);
            }

            WS_Message_Hardware_overview_Board overview = board.get_devices_overview();

            if (overview.status.equals("success")) {
                terminal_logger.trace("4.4 {} Status HW je {}", board.id, overview.online_state);

                WS_Message_Hardware_connected connected = new WS_Message_Hardware_connected();
                connected.status = overview.status;
                connected.full_id = board.full_id;
                Model_Hardware.device_Connected(connected);

            } else {
                terminal_logger.warn("Something is wrong with WS_Help_Hardware_board_overview message");
                terminal_logger.warn("Incoming message WS_Help_Hardware_board_overview {}", Json.toJson(overview));
            }
        }
    }
}
