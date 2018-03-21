package utilities.threads.homer_server;

import models.*;
import play.libs.Json;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import websocket.interfaces.WS_Homer;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_connected;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_overview_Board;
import websocket.messages.homer_with_tyrion.WS_Message_Homer_Hardware_list;

import java.util.List;
import java.util.UUID;


public class Synchronize_Homer_Hardware_after_connection extends Thread{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Synchronize_Homer_Unresolved_Updates.class);

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

            logger.info("4. Spouštím Sycnhronizační proceduru Synchronize_Homer_Hardware_after_connection");

            WS_Message_Homer_Hardware_list message_homer_hardware_list = homer_server.get_homer_server_list_of_hardware();

            if (!message_homer_hardware_list.status.equals("success")) {
                logger.warn("Message WS_Message_Homer_Hardware_list: invalid response - something is wrong");
                return;
            }

            List<WS_Message_Homer_Hardware_list.WS_Message_Homer_Hardware_Pair> device_ids_on_server = message_homer_hardware_list.list;
            logger.info("4. Number of registered or connected Devices on Server:: {} ", device_ids_on_server.size());
            check_device_on_server(device_ids_on_server);

            logger.trace("4, Number of required HW on this server: {}", Model_Hardware.find.query().where().eq("connected_server_id", this.homer.id).select("id").select("connected_server_id").findCount());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void check_device_on_server(List<WS_Message_Homer_Hardware_list.WS_Message_Homer_Hardware_Pair> full_ids_on_server) {

        for (WS_Message_Homer_Hardware_list.WS_Message_Homer_Hardware_Pair pair : full_ids_on_server) {
            try {

                Model_Hardware board = Model_Hardware.getByFullId(pair.full_id);

                // Device je autorizován pro připojení, ale není k němu aktuálně žádná aktivní virtual entita
                // s nastavenou dominancí
                if (board == null) {
                    continue;
                }

                // Zařízení má přiřazenou jinou UUID k Full ID než by měl mít
                if(!board.id.equals(pair.uuid)) {
                    logger.warn("check_device_on_server:: Device: ID: {} there is a mistake with pair with full ID: {} and UUID {} from Server", board.id, pair.full_id, pair.full_id);
                    logger.warn("check_device_on_server:: Device: ID: {} Its required change pair on homer server!", board.id);
                    board.device_converted_id_clean_switch_on_server(pair.uuid);
                    continue;
                }

                if (board.connected_server_id == null) {
                    logger.debug("check_device_on_server:: Device: ID: {} has not set server parameters yet", board.id);
                    board.connected_server_id = this.homer.id;
                    board.update();

                } else if (!board.connected_server_id.equals(this.homer.id)) {
                    logger.debug("check_device_on_server:: Device: ID: {}  je na špatném serveru a tak ho relokuji!!", board.id);
                    board.device_relocate_server(Model_HomerServer.getById(homer.id));
                    continue;
                } else {
                    logger.trace("check_device_on_server:: Device: ID: {}  je na správném serveru evidentně a tak ho jenom zkrontroluji", board.id);
                }

                WS_Message_Hardware_overview_Board overview = board.get_devices_overview();

                if (overview.status.equals("success")) {
                    logger.trace("check_device_on_server:: Device: ID: {} Status HW je {}", board.id, overview.online_state);

                    WS_Message_Hardware_connected connected = new WS_Message_Hardware_connected();
                    connected.status = overview.status;
                    connected.uuid = board.id;
                    Model_Hardware.device_Connected(connected);

                } else {
                    logger.warn("Something is wrong with WS_Help_Hardware_board_overview message");
                    logger.warn("Incoming message WS_Help_Hardware_board_overview {}", Json.toJson(overview));
                }

            }catch (Exception e){
                logger.internalServerError(e);
            }
        }
    }
}
