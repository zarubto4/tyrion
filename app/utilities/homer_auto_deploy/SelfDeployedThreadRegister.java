package utilities.homer_auto_deploy;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import controllers._BaseFormFactory;
import models.Model_HomerServer;
import play.api.Play;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.enums.NetworkStatus;
import utilities.enums.ServerMode;
import utilities.logger.Logger;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class SelfDeployedThreadRegister extends Thread {

    @Inject
    public static _BaseFormFactory formFactory;

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(SelfDeployedThreadRegister.class);

    /*  VALUES -------------------------------------------------------------------------------------------------------------*/

    private UUID homer_server_id = null;
    private Model_HomerServer server = null;

    private Integer attems = 20;


    // Umožněno kontrolovat COmpilator i Homer server
    public SelfDeployedThreadRegister(Model_HomerServer server) {
        this.homer_server_id = server.id;
        this.server = server;
    }

    @Override
    public void run() {

        WSClient ws = Play.current().injector().instanceOf(WSClient.class);

        try {
            logger.trace("run:: Thread for Homer Server id: {} Configuration. Registration ID: {}.", homer_server_id);
            ObjectNode json = Json.newObject();
            json.put("server_identification", server.connection_identifier);
            json.put("token_hash", server.hash_certificate);
            json.put("tyrion_url", Server.clearAddress);
            json.put("wss", Server.mode != ServerMode.DEVELOPER);
            json.put("url", server.server_url);

            logger.trace("create_server::  Default Homer Server Port is 3000");
            String configuration_homer_url = "http://" + server.server_url + ":3000" + "/configuration";

            logger.trace("crate_server:: Try to request Homer Server on url {}", configuration_homer_url);
            logger.trace("crate_server:: Request Server Configuration will be: {}", json.toString());

            thr1: while (--attems > 0) {
                try {

                    logger.debug("crate_server:: Attempt: {} ", attems);

                    WSResponse response = ws.url(configuration_homer_url)
                            .setRequestTimeout(Duration.ofSeconds(10))
                            .setBody(json)
                            .post(json)
                            .toCompletableFuture()
                            .get();

                    int status = response.getStatus();

                    if (status == 200) {
                        logger.trace("crate_server:: Done! Server is deployed and now we will try to check if its connected to this server!");
                        sleep(10000);

                        // Second While for check if server are connect to Tyrion
                        attems = 20;
                        thr2: while(--attems > 0) {
                            try {

                                logger.trace("crate_server:: Online Checker Attempt {}", attems);

                                if (server.online_state == NetworkStatus.ONLINE) { // TODO injection
                                    logger.debug("crate_server::  Amazing -server deployed and running!");
                                    break thr1;
                                }

                                logger.trace("crate_server::  Server is Still Offline!");
                                sleep(5000);

                            } catch (Exception e) {
                                logger.warn("run:: Thread -Error");
                                logger.internalServerError(e);
                                break thr2;
                            }

                        }

                        logger.error("crate_server::  After Configuration - server is still Offline! Notification to Slack send.");
                        String slack_echo = "Automatic procedure for register and deploy homer server to ConfigSelf Deployed Server has Error! \n";
                        slack_echo += "Homer Serve ID: " + homer_server_id + ", Name:" + server.name + "\n";
                        slack_echo += "Droplet rul: " + server.server_url + ", Api URL:" + server.server_url + ":" + server.rest_api_port + " <---\n";
                        slack_echo += "Tyrion Server Type: " + Server.mode + ", Tyrion URL: " + Server.httpAddress + "\n";
                        // Slack.post_error(slack_echo, Server.slack_webhook_url_channel_servers); // TODO injection

                        break thr1;

                    } else {
                        logger.error("crate_server::  Something is wrong - we have incorect response on Rest Api request");
                        logger.error("crate_server::  Response  Head {} and body: {}", response.getStatus(), response.getBody());
                    }

                }catch (ExecutionException e) {
                    logger.warn("run:: Thread - ExecutionException - No Response");
                    sleep(1000 * 10);
                }catch (Exception e) {
                    logger.warn("run:: Thread - Still not ready! Time to sleep and do it again!");
                    logger.internalServerError(e);
                    sleep(1000 * 10);
                }
            }

        }catch (Exception e){
            logger.error("synchronize_configuration: TimeoutException");
            logger.internalServerError(e);
        }
    }

}