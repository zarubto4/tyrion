package utilities.homer_auto_deploy;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.myjeeva.digitalocean.pojo.Domain;
import com.myjeeva.digitalocean.pojo.DomainRecord;
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

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class DigitalOceanThreadRegister extends Thread {

    @Inject
    public static _BaseFormFactory formFactory;

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(DigitalOceanThreadRegister.class);

    /*  VALUES -------------------------------------------------------------------------------------------------------------*/

    private UUID homer_server_id = null;
    private Swagger_BlueOcean registration = null;
    private Model_HomerServer server;

    private Integer attems = 6;


    // Umožněno kontrolovat COmpilator i Homer server
    public DigitalOceanThreadRegister(Model_HomerServer server, Swagger_BlueOcean registration) {
        this.homer_server_id = server.id;
        this.registration = registration;

        // Set Cache to Deploy state - we add random UUID as a mark that DigitalOceanThreadRegister is in progress
        server.idCache().add(DigitalOceanThreadRegister.class,  UUID.randomUUID());
    }

    @Override
    public void run() {

        WSClient ws = Play.current().injector().instanceOf(WSClient.class);

        try {
            logger.trace("run:: Thread for Homer Server id: {} Configuration. Registration ID: {} Fist Action is Wait 30 seconds. ITs time to copy and make a configuration on DigitalOcean side.", homer_server_id, registration.id);
            sleep(1000 * 30);
            logger.trace("run:: Thread Sleep 30 seconds is done! Time to while!");

            thr1: while (--attems > 0) {
                try {

                    Droplet droplet = DigitalOceanTyrionService.apiClient.getDropletInfo(registration.id);

                    if (!droplet.getNetworks().getVersion4Networks().isEmpty()) {

                        logger.trace("run:: Thread -Perfect - we have Networks");

                        Network network = droplet.getNetworks().getVersion4Networks().get(0);
                        logger.trace("create_server:: Server URL:    " + network.getIpAddress());


                        // Registrace domény
                        Domain domain = DigitalOceanTyrionService.apiClient.getDomainInfo("do.byzance.cz");

                        DomainRecord domainRecord = new DomainRecord();
                        domainRecord.setType("A");
                        domainRecord.setName(this.homer_server_id.toString());
                        domainRecord.setData(network.getIpAddress());

                        DigitalOceanTyrionService.apiClient.createDomainRecord(domain.getName(), domainRecord);


                        this.server = Model_HomerServer.find.byId(homer_server_id);
                        server.server_url = homer_server_id + ".do.byzance.cz";
                        server.update();

                        // Time to configure Homer Server!!

                        ObjectNode json = Json.newObject();
                        json.put("server_identification", server.connection_identifier);
                        json.put("token_hash", server.hash_certificate);
                        json.put("tyrion_url", Server.clearAddress);
                        json.put("url", server.server_url);


                        logger.trace("create_server:: Default Homer Server Port is 3000");
                        String configuration_homer_url = "http://" + server.server_url + ":3000" + "/configuration";

                        logger.trace("crate_server:: Try to request Homer Server on url {}", configuration_homer_url);
                        logger.trace("crate_server:: Request Server Configuration will be: {}", json.toString());

                        // Set Attemps to new Value -
                        this.attems = 20;
                        thr2: while (--attems > 0) {
                            try {

                                WSResponse response = ws.url(configuration_homer_url)
                                        .setRequestTimeout(Duration.ofSeconds(5))
                                        .setBody(json)
                                        .post(json)
                                        .toCompletableFuture()
                                        .get();

                                int status = response.getStatus();

                                if (status == 200) {

                                    logger.debug("crate_server:: Done! Server is deployed and now we will try to check if its connected to this server!");

                                    sleep(10000);

                                    // Second While for check if server are connect to Tyrion
                                    attems = 20;
                                    thr3: while (--attems > 0) {

                                        try {

                                            logger.trace("crate_server:: Online DigitalOcean Checker Attempt {}", attems);

                                            if (server.online_state == NetworkStatus.ONLINE) { // TODO injection
                                                logger.debug("crate_server::  Amazing -server deployed and running!");
                                                server.idCache().removeAll(DigitalOceanThreadRegister.class);
                                                break thr1;
                                            }

                                            logger.debug("crate_server::  Server is Still Offline!");
                                            sleep(5000);

                                        } catch (Exception e) {
                                            logger.internalServerError(e);
                                            break thr1;
                                        }

                                    }

                                    logger.error("crate_server::  After Configuration - server is still Offline! Notification to Slack send.");
                                    String slack_echo = "Automatic procedure for register and deploy homer server to ConfigSelf Deployed Server has Error! \n";
                                    slack_echo += "Homer Serve ID: " + homer_server_id + ", Name:" + server.name + "\n";
                                    slack_echo += "Droplet rul: " + server.server_url + ", Api URL:" + server.server_url + ":" + server.rest_api_port + " <---\n";
                                    slack_echo += "Tyrion Server Type: " + Server.mode + ", Tyrion URL: " + Server.httpAddress + "\n";
                                    Slack.post_error(slack_echo, Server.slack_webhook_url_channel_homer);
                                    server.idCache().removeAll(DigitalOceanThreadRegister.class);

                                    break thr1;
                                } else {
                                    logger.error("crate_server::  Something is wrong - we have incorect response on Rest Api request");
                                    logger.error("crate_server::  Response  Head {} and body: ", response.getStatus(), response.getBody());

                                }



                            } catch (ExecutionException e) {
                                sleep(1000 * 4);
                                // Nothing
                            } catch (Exception e) {
                                logger.internalServerError(e);
                            }

                        }

                    } else {
                        logger.trace("run:: Thread - Still not Networks on Droplet! Time To sleep");
                        sleep(1000*2);
                    }

                    server.deployment_in_progress = false;
                    server.update();

                } catch (Exception e){
                    logger.internalServerError(e);
                    server.deployment_in_progress = false;
                    server.update();
                }

                logger.trace("run:: Thread - Still not ready! Time to sleep and do it again!");
                sleep(1000 * 10);
            }

            server.idCache().removeAll(DigitalOceanThreadRegister.class);

        }catch (Exception e){
            logger.error("synchronize_configuration: TimeoutException");
            server.deployment_in_progress = false;
            server.update();
            server.idCache().removeAll(DigitalOceanThreadRegister.class);
            logger.internalServerError(e);
        }
    }

}