package utilities.threads.homer_server;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import controllers._BaseFormFactory;
import models.Model_HomerServer;
import play.libs.Json;
import utilities.logger.Logger;
import websocket.WS_Message;
import websocket.interfaces.WS_Homer;
import websocket.messages.homer_with_tyrion.configuration.WS_Message_Homer_Get_homer_server_configuration;
import websocket.messages.homer_with_tyrion.verification.WS_Message_Check_homer_server_permission;

public class Synchronize_Homer_Synchronize_Settings extends Thread {

    @Inject public static _BaseFormFactory baseFormFactory;

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger terminal_logger = new Logger(Synchronize_Homer_Synchronize_Settings.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    WS_Homer homer_server = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Synchronize_Homer_Synchronize_Settings(WS_Homer server) {
        this.homer_server = server;
    }

    @Override
    public void run() {
        try {

            terminal_logger.trace("Spouštím Sycnhronizační proceduru Synchronize_Homer_Synchronize_Settings");

            ObjectNode ask_for_configuration = homer_server.sendWithResponse(new WS_Message(new WS_Message_Homer_Get_homer_server_configuration().make_request() , 0, 1000 * 60, 0));

            /*final Form<WS_Message_Homer_Get_homer_server_configuration> form = Form.form(WS_Message_Homer_Get_homer_server_configuration.class).bind(ask_for_configuration);
            if (form.hasErrors()) {

                terminal_logger.error("run:: invalid incoming message {}", ask_for_configuration);
                terminal_logger.error("run:: response", form.errorsAsJson(Lang.forCode("en-US")).toString());

                // TODO homer_server.send( ask_for_configuration.has("message_id") ? ask_for_configuration.get("message_id").asText() : UUID.randomUUID().toString(), WS_Message_Invalid_Message.make_request(WS_Message_Homer_Get_homer_server_configuration.message_type, form.errorsAsJson(Lang.forCode("en-US")).toString()));

                terminal_logger.error("Close Connection");
                homer_server.onClose();

                return;
            }*/

            WS_Message_Homer_Get_homer_server_configuration help = baseFormFactory.formFromJsonWithValidation(WS_Message_Homer_Get_homer_server_configuration.class, ask_for_configuration);

            // Homer server má novější novou konfiguraci
            terminal_logger.debug("synchronize_configuration: Homer server {} has new configuration", homer_server.id);

            Model_HomerServer homer = Model_HomerServer.getById(homer_server.id);

            //homer.name = help.server_name;
            if (homer.mqtt_port != help.mqtt_port || homer.grid_port != help.grid_port || homer.web_view_port != help.web_view_port ||  homer.hardware_logger_port != help.hw_logger_port ||  homer.rest_api_port != help.rest_api_port) {
                homer.mqtt_port = help.mqtt_port;   // 1881
                homer.grid_port = help.grid_port;   // 8503
                homer.web_view_port = help.web_view_port;  //8501
                homer.hardware_logger_port = help.hw_logger_port; // 8505
                homer.rest_api_port = help.rest_api_port; // 3000
                homer.update();
            }

            if (homer.server_version == null || !homer.server_version.equals(help.server_version)) {
                homer.server_version = help.server_version;
                homer.update();
            }

            if (homer.server_url == null ||  !homer.server_url.equals(help.server_url)) {
                homer.server_url = help.server_url;
                homer.update();
            }

            terminal_logger.trace(" " + homer_server.id + "synchronize_configuration: done!");

        } catch (Exception e) {
            terminal_logger.error("synchronize_configuration: TimeoutException");
            terminal_logger.internalServerError(e);
            homer_server.onClose();
        }


    }
}