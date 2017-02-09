package utilities.independent_threads;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.project.b_program.servers.Model_HomerServer;
import play.libs.Json;
import utilities.enums.Log_Level;
import utilities.webSocket.WS_HomerServer;
import utilities.webSocket.messageObjects.WS_CheckHomerServerConfiguration;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.TimeoutException;

public class SynchronizeHomerServer extends Thread {

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    WS_HomerServer homer_server = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public SynchronizeHomerServer(WS_HomerServer server){
        this.homer_server = server;
    }

    @Override
    public void run() {

        try{
            // Požádáme o token
            ObjectNode request = Json.newObject();
            request.put("messageType", "getServerConfiguration");
            request.put("messageChannel", Model_HomerServer.CHANNEL);
            ObjectNode ask_for_configuration = homer_server.write_with_confirmation(request, 1000 * 5, 0, 2);

            // Vytovření objektu
            WS_CheckHomerServerConfiguration help = WS_CheckHomerServerConfiguration.getObject(ask_for_configuration);

            if(help.timeStamp.compareTo(homer_server.server.time_stamp_configuration) == 0){
                // Nedochází k žádným změnám
                logger.debug("SynchronizeHomerServer:: synchronize_configuration: configuration without changes");

            }else if(help.timeStamp.compareTo(homer_server.server.time_stamp_configuration) > 0){
                // Homer server má novější novou konfiguraci
                logger.debug("SynchronizeHomerServer:: synchronize_configuration: Homer server has new configuration");


                homer_server.server.personal_server_name = help.serverName;
                homer_server.server.mqtt_port = help.mqttPort;
                homer_server.server.mqtt_password = help.mqttPassword;
                homer_server.server.mqtt_username = help.mqttUser;
                homer_server.server.grid_port = help.gridPort;

                homer_server. server.webView_port = help.beckiPort;
                homer_server. server.server_remote_port = help.webPort;

                homer_server.server.mqtt_username = help.mqttUser;
                homer_server.server.grid_port = help.gridPort;
                homer_server.server.webView_port = help.beckiPort;
                homer_server.server.days_in_archive = help.daysInArchive;
                homer_server.server.time_stamp_configuration = help.timeStamp;

                homer_server. server.logging = help.logging;
                homer_server. server.interactive = help.interactive;
                homer_server. server.logLevel = Log_Level.fromString(help.logLevel);
                homer_server.server.update();


            }else {
                // Tyrion server má novější konfiguraci
                logger.debug("SynchronizeHomerServer:: synchronize_configuration: Tyrion server has new configuration");
                JsonNode response = homer_server.server.set_new_configuration_on_homer();
                logger.debug("SynchronizeHomerServer:: synchronize_configuration: New Config state:: " + response.get("status"));

            }

            logger.debug("SynchronizeHomerServer:: synchronize_configuration: done!");
            homer_server.synchronize = null;

        }catch(ClosedChannelException e){
            logger.warn("WS_HomerServer:: security_token_confirm_procedure :: ClosedChannelException");
        }catch (TimeoutException e){
            logger.error("WS_HomerServer:: security_token_confirm_procedure :: TimeoutException");
        }catch (Exception e){
            logger.error("WS_HomerServer:: security_token_confirm_procedure :: Error", e);
        }

    }

}
