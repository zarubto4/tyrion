package utilities.independent_threads.homer_server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.data.Form;
import play.i18n.Lang;
import utilities.enums.Enum_Log_level;
import utilities.logger.Class_Logger;
import web_socket.message_objects.homer_with_tyrion.configuration.WS_Message_Homer_set_configuration;
import web_socket.message_objects.homer_with_tyrion.configuration.WS_Message_Homer_Get_homer_server_configuration;
import web_socket.services.WS_HomerServer;

import java.nio.channels.ClosedChannelException;
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

            ObjectNode ask_for_configuration = homer_server.write_with_confirmation( new WS_Message_Homer_Get_homer_server_configuration().make_request() , 1000 * 5, 0, 2);
            final Form<WS_Message_Homer_Get_homer_server_configuration> form = Form.form(WS_Message_Homer_Get_homer_server_configuration.class).bind(ask_for_configuration);
            if(form.hasErrors()) throw new Exception("WS_Message_Homer_Get_homer_server_configuration: Incoming Json for Yoda has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());
            WS_Message_Homer_Get_homer_server_configuration help = form.get();

            if(help.get_Date().compareTo(Model_HomerServer.get_byId(homer_server.identifikator).time_stamp_configuration) == 0){
                // Nedochází k žádným změnám
                terminal_logger.trace("synchronize_configuration: configuration without changes");

            }else if(help.get_Date().compareTo( Model_HomerServer.get_byId(homer_server.identifikator).time_stamp_configuration) > 0){
                // Homer server má novější novou konfiguraci
                terminal_logger.debug("synchronize_configuration: Homer server {} has new configuration", homer_server.identifikator);

                Model_HomerServer homer = Model_HomerServer.get_byId(homer_server.identifikator);

                homer.personal_server_name = help.serverName;
                homer.mqtt_port = help.mqttPort;
                homer.mqtt_password = help.mqttPassword;
                homer.mqtt_username = help.mqttUser;
                homer.grid_port = help.gridPort;

                homer.web_view_port = help.beckiPort;
                homer.server_remote_port = help.webPort;

                homer.mqtt_username = help.mqttUser;
                homer.grid_port = help.gridPort;
                homer.days_in_archive = help.daysInArchive;
                homer.time_stamp_configuration = help.get_Date();

                homer.logging = help.logging;
                homer.interactive = help.interactive;
                homer.log_level = Enum_Log_level.fromString(help.logLevel);
                homer.update();

            }else {
                // Tyrion server má novější konfiguraci

                terminal_logger.trace("synchronize_configuration::  " + homer_server.identifikator + " Sending new Configuration to Homer Server");
                JsonNode result = homer_server.write_with_confirmation( new WS_Message_Homer_set_configuration().make_request( Model_HomerServer.get_byId(homer_server.identifikator)) , 1000 * 5, 0, 2);

                final Form<WS_Message_Homer_set_configuration> form_set = Form.form(WS_Message_Homer_set_configuration.class).bind(result);
                if(form_set.hasErrors()) throw new Exception("WS_Message_Homer_set_configuration: Incoming Json for Yoda has not right Form: " + form_set.errorsAsJson(Lang.forCode("en-US")).toString());

                WS_Message_Homer_set_configuration help_conf = form_set.get();

                if(help_conf.status.equals("success")){
                    terminal_logger.trace("synchronize_configuration:: New Config state:: success! ");
                }else {
                    terminal_logger.internalServerError(new Exception("New Config state: unsuccessful!"));
                }
            }

            terminal_logger.trace(" " + homer_server.identifikator + "synchronize_configuration: done!");

        }catch(ClosedChannelException e){
            terminal_logger.warn("synchronize_configuration: ClosedChannelException");
        }catch (TimeoutException e){
            terminal_logger.warn("synchronize_configuration: TimeoutException");
        }catch (Exception e){
            terminal_logger.internalServerError("run", e);
        }
    }
}