package utilities.independent_threads.homer_server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import utilities.enums.Enum_Log_level;
import utilities.errors.ErrorCode;
import utilities.logger.Class_Logger;
import web_socket.message_objects.common.service_class.WS_Message_Invalid_Message;
import web_socket.message_objects.homer_with_tyrion.configuration.WS_Message_Homer_Get_configuration;
import web_socket.message_objects.homer_with_tyrion.configuration.WS_Message_Homer_Get_homer_server_configuration;
import web_socket.message_objects.homer_with_tyrion.verification.WS_Message_Check_homer_server_permission;
import web_socket.services.WS_HomerServer;

import java.nio.channels.ClosedChannelException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class Synchronize_Homer_Synchronize_Settings extends Thread {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Synchronize_Homer_Synchronize_Settings.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    WS_HomerServer homer_server = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Synchronize_Homer_Synchronize_Settings(WS_HomerServer server){
        this.homer_server = server;
    }

    @Override
    public void run() {
        try{

            terminal_logger.trace("Spouštím Sycnhronizační proceduru Synchronize_Homer_Synchronize_Settings");

            ObjectNode ask_for_configuration = homer_server.write_with_confirmation( new WS_Message_Homer_Get_homer_server_configuration().make_request() , 1000 * 60, 0, 0);

            final Form<WS_Message_Homer_Get_homer_server_configuration> form = Form.form(WS_Message_Homer_Get_homer_server_configuration.class).bind(ask_for_configuration);
            if(form.hasErrors()){

                terminal_logger.error("run:: invalid incoming message {}", ask_for_configuration);
                terminal_logger.error("run:: response", form.errorsAsJson(Lang.forCode("en-US")).toString());

                homer_server.write_without_confirmation( ask_for_configuration.has("message_id") ? ask_for_configuration.get("message_id").asText() : UUID.randomUUID().toString(), WS_Message_Invalid_Message.make_request(WS_Message_Homer_Get_homer_server_configuration.message_type, form.errorsAsJson(Lang.forCode("en-US")).toString()));

                terminal_logger.error("Close Connection");
                homer_server.onClose();

                return;
            }

            WS_Message_Homer_Get_homer_server_configuration help = form.get();

            // Homer server má novější novou konfiguraci
            terminal_logger.debug("synchronize_configuration: Homer server {} has new configuration", homer_server.identifikator);

            Model_HomerServer homer = Model_HomerServer.get_byId(homer_server.identifikator);

            //homer.personal_server_name = help.server_name;
            if(homer.mqtt_port == null || homer.mqtt_port != help.mqtt_port) {
                homer.mqtt_port = help.mqtt_port;   // 1881
                homer.update();
            }

            if(homer.grid_port == null || homer.grid_port != help.grid_port) {
                homer.grid_port = help.grid_port;   // 8503
                homer.update();
            }

            if(homer.web_view_port == null || homer.web_view_port != help.web_view_port) {
                homer.web_view_port = help.web_view_port;  //8501
                homer.update();
            }

            if(homer.server_remote_port == null || homer.server_remote_port != help.hw_logger_port) {
                homer.server_remote_port = help.hw_logger_port; // 8505
                homer.update();
            }

            if(homer.rest_api_port == null || homer.rest_api_port != help.rest_api_port) {
                homer.rest_api_port = help.rest_api_port; // 3000
                homer.update();
            }

            if(homer.server_version == null || homer.server_version.equals(help.server_version)) {
                homer.server_version = help.server_version;
                homer.update();
            }

            if(homer.server_url == null ||  homer.server_url.equals(help.server_url)) {
                homer.server_url = help.server_url;
                homer.update();
            }

            terminal_logger.trace(" " + homer_server.identifikator + "synchronize_configuration: done!");

        }catch(ClosedChannelException e){
            System.out.println("synchronize_configuration: ClosedChannelException");
            terminal_logger.error("synchronize_configuration: ClosedChannelException");
            homer_server.onClose();
        }catch (TimeoutException e){
            System.out.println("synchronize_configuration: TimeoutException");
            terminal_logger.error("synchronize_configuration: TimeoutException");
            homer_server.onClose();
        }catch (Exception e){
            System.out.println("synchronize_configuration: TimeoutException");
            terminal_logger.internalServerError(e);
            homer_server.onClose();
        }


    }
}