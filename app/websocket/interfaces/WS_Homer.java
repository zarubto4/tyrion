package websocket.interfaces;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.Model_Hardware;
import models.Model_HomerServer;
import models.Model_Instance;
import play.libs.Json;
import utilities.logger.Logger;
import utilities.threads.homer_server.Synchronize_Homer_Hardware_after_connection;
import utilities.threads.homer_server.Synchronize_Homer_Instance_after_connection;
import utilities.threads.homer_server.Synchronize_Homer_Synchronize_Settings;
import utilities.threads.homer_server.Synchronize_Homer_Unresolved_Updates;
import websocket.WS_Interface;
import websocket.messages.common.service_class.WS_Message_Invalid_Message;
import websocket.messages.homer_with_tyrion.verification.WS_Message_Check_homer_server_permission;
import websocket.messages.homer_with_tyrion.verification.WS_Message_Homer_Verification_result;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WS_Homer extends WS_Interface {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(WS_Homer.class);

/* STATIC  -------------------------------------------------------------------------------------------------------------*/

    public static Props props(ActorRef out) {
        return Props.create(WS_Homer.class, out);
    }

/* VALUES  -------------------------------------------------------------------------------------------------------------*/

    public static HashMap<String, WS_Homer> token_cache = new HashMap<>();

    public boolean authorized;
    public String token;
    private ExecutorService service;

    public WS_Homer(ActorRef out) {
        super(out);
        Controller_WebSocket.homers_not_sync.put(this.id, this);
    }

    @Override
    public void onMessage(ObjectNode json) {

        logger.trace("onMessage - {} {}" + json.toString());

        if (!authorized) {

            logger.trace("onMessage - homer is not authorized yet");

            verify(json);
            return;
        }

        if (json.has("message_channel")) {

            switch (json.get("message_channel").asText()) {

                case Model_Hardware.CHANNEL: {    // Komunikace mezi Hardware a Tyrionem
                    Model_Hardware.Messages(this, json);
                    return;
                }

                case Model_HomerServer.CHANNEL : { // Komunikace mezi Tyrion server a Homer Server
                    Model_HomerServer.Messages(this, json);
                    return;
                }

                case Model_Instance.CHANNEL: {    // Komunikace mezi Tyrion server a Homer Instance
                    Model_Instance.Messages(this, json);
                    return;
                }

                default: {
                    logger.internalServerError(new Exception("Unknown message_channel ->" + json.get("message_channel").asText()));
                }
            }
        } else {
            logger.internalServerError(new Exception(this.id + " Incoming message has not message_channel!!!!"));
        }
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public void onClose() {
        Controller_WebSocket.homers_not_sync.remove(this.id);
        Controller_WebSocket.homers.remove(this.id);

        Model_HomerServer.getById(this.id).is_disconnect();
    }

    public void verificationSuccess(String message_id) {

        this.authorized = true;
        this.token =  UUID.randomUUID().toString() + UUID.randomUUID().toString();

        Controller_WebSocket.homers.put(this.id, this);
        Controller_WebSocket.homers_not_sync.remove(this.id);

        token_cache.put(this.token, this);

        this.send(new WS_Message_Homer_Verification_result().make_request(true, this.token).put("message_id", message_id));
        synchronize_configuration();
    }

    public void verificationFail(String message_id) {
        super.send(new WS_Message_Homer_Verification_result().make_request(false, null).put("message_id", message_id));
    }

    private void verify(ObjectNode json) {
        try {

            if (json.get("message_channel").asText().equals(Model_HomerServer.CHANNEL) && json.get("message_type").asText().equals(WS_Message_Check_homer_server_permission.message_type)) {

                /*final Form<WS_Message_Check_homer_server_permission> form = Form.form(WS_Message_Check_homer_server_permission.class).bind(json);
                if (form.hasErrors()) {

                    logger.trace("verify:: invalid incoming message {}", json);
                    logger.trace("verify:: response", form.errorsAsJson(Lang.forCode("en-US")).toString());

                    this.send(WS_Message_Invalid_Message.make_request(WS_Message_Check_homer_server_permission.message_type, null).put("message_id", json.get("message_id").asText()));

                    return;
                }*/// TODO

                Model_HomerServer.approve_validation_for_homer_server(this, Json.fromJson(json, WS_Message_Check_homer_server_permission.class));

            } else {

                logger.warn("onMessage: This Websocket is not confirm");

                this.send(WS_Message_Invalid_Message.make_request(WS_Message_Check_homer_server_permission.message_type, null).put("message_id", json.get("message_id").asText()));

            }

        } catch (NullPointerException e) {

            this.send(WS_Message_Invalid_Message.make_request(WS_Message_Check_homer_server_permission.message_type, null).put("message_id", json.get("message_id").asText()));

        } catch (Exception e) {
            logger.internalServerError(new Exception("Invalid data came from Homer and also it is not verified connection", e));

            if (json.has("message_id")) {
                verificationFail(json.get("message_id").asText());
            } else {
                verificationFail(UUID.randomUUID().toString());
            }
        }
    }

    // Je voláno, až se server ověří
    public void synchronize_configuration() {
        new Thread( () -> {
            try {
                logger.trace("1. Spouštím Sycnhronizační proceduru (synchronize_configuration)- Několik vláken - dej tomu čas");

                if (service != null) service.shutdownNow();

                service = Executors.newSingleThreadExecutor();

                // Kontrola nastavení
                Synchronize_Homer_Synchronize_Settings synchronize_homer_synchronize_settings = new Synchronize_Homer_Synchronize_Settings(this);   // TODO - čeká na Homer Config APP

                // Kontrola instancí
                Synchronize_Homer_Instance_after_connection synchronize_homer_instance_after_connection = new Synchronize_Homer_Instance_after_connection(this);

                // Kontrola HW
                Synchronize_Homer_Hardware_after_connection synchronize_homer_hardware_after_connection = new Synchronize_Homer_Hardware_after_connection(this);

                // Kontrola Updatů
                Synchronize_Homer_Unresolved_Updates synchronize_homer_unresolved_updates = new Synchronize_Homer_Unresolved_Updates(this);

                service.submit(synchronize_homer_synchronize_settings);
                service.submit(synchronize_homer_instance_after_connection);
                service.submit(synchronize_homer_hardware_after_connection);
                service.submit(synchronize_homer_unresolved_updates);

                service.shutdown(); // Odmítnutí přidání nových

                service.awaitTermination(5L, TimeUnit.MINUTES);

                logger.trace("1. Dokončil jsem metodu (synchronize_configuration) ");


            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }).start();
    }
}


