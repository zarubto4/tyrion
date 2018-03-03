package utilities.homer_auto_deploy;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.myjeeva.digitalocean.pojo.Droplet;
import com.myjeeva.digitalocean.pojo.Network;
import controllers._BaseFormFactory;
import models.Model_HomerServer;
import play.api.Play;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.enums.NetworkStatus;
import utilities.homer_auto_deploy.models.service.Swagger_BlueOcean;
import utilities.logger.Logger;
import utilities.slack.Slack;
import utilities.threads.homer_server.Synchronize_Homer_Synchronize_Settings;
import websocket.interfaces.WS_Homer;

import java.time.Duration;
import java.util.UUID;

public class DigitalOceanThreadRegister extends Thread {

    @Inject
    public static _BaseFormFactory baseFormFactory;

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Synchronize_Homer_Synchronize_Settings.class);
    private static  WSClient ws = Play.current().injector().instanceOf(WSClient.class);

    /*  VALUES -------------------------------------------------------------------------------------------------------------*/

    private UUID homer_server_id = null;
    private Swagger_BlueOcean registration = null;

    private Integer attems = 6;


    // Umožněno kontrolovat COmpilator i Homer server
    public DigitalOceanThreadRegister(Model_HomerServer server, Swagger_BlueOcean registration) {
        this.homer_server_id = server.id;
        this.registration = registration;
    }

    @Override
    public void run() {
        try {
            logger.trace("run:: Thread for Homer Server id: {} Configuration. Registration ID: {} Fist Action is Wait 30 seconds. ITs time to copy and make a configuration on DigitalOcean side.", homer_server_id, registration.id);
            sleep(1000 * 30);
            logger.trace("run:: Thread Sleep 30 seconds is done! Time to while!");

            while (--attems > 0) {

                try {
                    Droplet droplet = DigitalOceanTyrionService.apiClient.getDropletInfo(registration.id);

                    if (!droplet.getNetworks().getVersion4Networks().isEmpty()) {
                        logger.trace("run:: Thread -Perfect - we have Netowrks");

                        Network network = droplet.getNetworks().getVersion4Networks().get(0);
                        logger.trace("create_server::    Server URL:    " + network.getIpAddress());

                        Model_HomerServer server = Model_HomerServer.getById(homer_server_id);
                        server.server_url = network.getIpAddress();
                        server.update();

                        // Time to configure Homer Server!!

                        ObjectNode json = Json.newObject();
                        json.put("server_identification", server.connection_identifier);
                        json.put("token_hash", server.hash_certificate);
                        json.put("tyrion_url", Server.clearAddress);
                        json.put("url", server.server_url);


                        logger.trace("create_server::   Default Homer Server Port is  3000");
                        String configuration_homer_url = server.server_url + ":3000" + "/configuration";

                        logger.trace("crate_server:: Try to request Homer Server on url {}", configuration_homer_url);
                        logger.trace("crate_server:: Request Server Configuration will be: {}", json.toString());

                        WSResponse response = ws.url(configuration_homer_url)
                                .setRequestTimeout(Duration.ofSeconds(10))
                                .post(json.toString())
                                .toCompletableFuture()
                                .get();

                        int status = response.getStatus();

                        if(status == 200) {
                            logger.debug("crate_server:: Done! Server is deployed and now we will try to check if its connected to this server!");
                            sleep(10000);
                            Model_HomerServer server_again_but_probably_after_cleaning_cache = Model_HomerServer.getById(homer_server_id);
                            if(server_again_but_probably_after_cleaning_cache.online_state() == NetworkStatus.ONLINE) {
                                logger.debug("crate_server::  Amazing -server deployed and running!");
                            } else {
                                logger.error("crate_server::  After Configuration - server is still Offline! Notification to Slack send.");
                                String slack_echo = "Automatic procedure for register and deploy homer server to Digital Ocean https://www.digitalocean.com has stuck. \n";
                                slack_echo += "Homer Serve ID: " + homer_server_id + ", Name:" + server_again_but_probably_after_cleaning_cache.name + "\n";
                                slack_echo += "Droplet ID: " + droplet.getId() + ", Droplet Name:" + droplet.getName() + "\n";
                                slack_echo += "Droplet rul: " + server.server_url + ", Api URL:" + server.server_url + ":" + server.rest_api_port + " <---\n";
                                slack_echo += "Tyrion Server Type: " + Server.mode + ", Tyrion URL: " + Server.httpAddress + "\n";
                                Slack.post_error(slack_echo);
                            }
                        }


                    } else {
                        logger.trace("run:: Thread - Still not Networks on Droplet!");
                    }
                } catch (Exception e){
                    logger.internalServerError(e);
                }

                logger.trace("run:: Thread - Still not ready! Time to sleep and do it again!");
                sleep(1000 * 10);
            }



        }catch (Exception e){
            logger.error("synchronize_configuration: TimeoutException");
            logger.internalServerError(e);
        }
    }

}