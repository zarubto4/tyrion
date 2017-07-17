package web_socket.services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import controllers.Controller_Wiky;
import models.Model_Board;
import models.Model_HomerInstance;
import models.Model_HomerServer;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import play.mvc.Controller;
import utilities.errors.ErrorCode;
import utilities.independent_threads.homer_server.Synchronize_Homer_Hardware_after_connection;
import utilities.independent_threads.homer_server.Synchronize_Homer_Instance_after_connection;
import utilities.independent_threads.homer_server.Synchronize_Homer_Synchronize_Settings;
import utilities.independent_threads.homer_server.Synchronize_Homer_Unresolved_Updates;
import utilities.logger.Class_Logger;
import web_socket.message_objects.common.service_class.WS_Message_Invalid_Message;
import web_socket.message_objects.compilator_with_tyrion.WS_Message_Ping_compilation_server;
import web_socket.message_objects.homer_with_tyrion.WS_Message_Homer_ping;
import web_socket.message_objects.homer_with_tyrion.verification.WS_Message_Check_homer_server_permission;
import web_socket.message_objects.homer_with_tyrion.verification.WS_Message_Homer_Verification_result;

import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.*;

public class WS_HomerServer extends WS_Interface_type {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(WS_HomerServer.class);

/* TOKEN SECURITY  -----------------------------------------------------------------------------------------------------*/

    public static HashMap<String, WS_HomerServer> token_hash = new HashMap<>(); // Using for security Rest Api

/* VALUES  -------------------------------------------------------------------------------------------------------------*/

    public boolean security_token_confirm;
    private ExecutorService service;

    public String identifikator;

    public String rest_api_token;


    public WS_HomerServer(Model_HomerServer server) {
        super();
        identifikator = server != null ? server.unique_identificator : null;
        super.webSCtype =  this;
    }


    @Override
    public boolean is_online() {
        try {

            terminal_logger.trace("Homer Server is_online: Test online state Server ID {} ", identifikator);

            ObjectNode status = write_with_confirmation( new WS_Message_Homer_ping().make_request(), 1000 * 10, 0, 0);
            if( status.get("status").asText().equals("success")) {
                terminal_logger.trace("Homer Server: Server is online {} ", identifikator);
                return true;
            }else {
                terminal_logger.trace("Homer Server: Server is offline {} ", identifikator);
                onClose();
                return false;
            }

        }catch (Exception e){
            terminal_logger.warn("is_online: Test online state Server ID {} Exception - return false" , identifikator);
            onClose();
            return false;
        }
    }

    @Override
    public void add_to_map() {
        Controller_WebSocket.not_synchronize_homer_servers.put(identifikator, this);
    }

    @Override
    public String get_identificator() {
        return identifikator;
    }

    @Override
    public void onClose() {

        System.out.println("onClose: Starting cancelled procedure with virtual Homers");

        terminal_logger.warn("onClose: Starting cancelled procedure with virtual Homers");

        if(service != null) service.shutdownNow();

        token_hash.remove(rest_api_token);

        Controller_WebSocket.homer_servers.remove(identifikator);
        Model_HomerServer.get_byId(identifikator).is_disconnect();
    }

    @Override
    public void onMessage(ObjectNode json) {

        terminal_logger.trace(identifikator + " onMessage: " + json.toString());

        // Pokud není token - není dovoleno zasílat nic do WebSocketu a ani nic z něj - Odchytím zprávu a buď naní odpovím zamítavě nebo
        // v případě že je to zpráva s tokenem jí zařadím to metody ověřující oprávnění

        if(!security_token_confirm){

            System.out.println("Příchozí zpráva - Homer ještě nemá boolean security_token_confirm nastavený na true");

            validation_check(json);
            return;
        }

        if(json.has("message_channel")){

            switch (json.get("message_channel").asText()){

                case Model_Board.CHANNEL: {    // Komunikace mezi Hardware a Tyrionem
                    Model_Board.Messages(this, json);
                    return;
                }

                case Model_HomerServer.CHANNEL : { // Komunikace mezi Tyrion server a Homer Server
                    Model_HomerServer.Messages(this, json);
                    return;
                }


                case Model_HomerInstance.CHANNEL: {    // Komunikace mezi Tyrion server a Homer Instance
                    Model_HomerInstance.Messages(this, json);
                    return;
                }


                case WS_Becki_Website.CHANNEL: {    // Komunikace mezi Becki a Homer Instance
                    WS_Becki_Website.Messages(this, json);
                    return;
                }

                default: {
                    terminal_logger.internalServerError(new Exception("onMessage: message not recognize incoming messageChanel!!! ->" + json.get("message_channel").asText()));

                }

            }

        }else {
            terminal_logger.internalServerError(new Exception(identifikator + " Incoming message has not message_channel!!!!"));
        }

    }

    private void validation_check(ObjectNode json){
        try {

            if(json.get("message_channel").asText().equals(Model_HomerServer.CHANNEL) && json.get("message_type").asText().equals(WS_Message_Check_homer_server_permission.message_type)){

                final Form<WS_Message_Check_homer_server_permission> form = Form.form(WS_Message_Check_homer_server_permission.class).bind(json);
                if (form.hasErrors()){

                    System.out.println("Příchozí zpráva s Hash je nevalidní - Odpovídám co se mi nelíbí ");

                    webSCtype.write_without_confirmation(json.get("message_id").asText(), WS_Message_Invalid_Message.make_request(WS_Message_Check_homer_server_permission.message_type, form.errorsAsJson(Lang.forCode("en-US")).toString()));
                    return;

                }

                System.out.println("Příchozí zpráva s Hash je validní - přenechávám proceduře připojení ");

                Model_HomerServer.aprove_validation_for_homer_server(this, form.get());

            }else {

                terminal_logger.warn("onMessage: This Websocket is not confirm");
                webSCtype.write_without_confirmation(json.get("message_id").asText(), WS_Message_Invalid_Message.make_request(WS_Message_Check_homer_server_permission.message_type, null));

                return;
            }

        }catch (NullPointerException e){

            webSCtype.write_without_confirmation(json.get("message_id").asText(), WS_Message_Invalid_Message.make_request(WS_Message_Check_homer_server_permission.message_type, null));

        }catch (Exception e){
            terminal_logger.internalServerError("Incoming data from Homer is not in valid state and also its not verified connection", e);

            if(json.has("message_id")){
                reject_server_verification(json.get("message_id").asText());
            }else {
                reject_server_verification(UUID.randomUUID().toString());
            }

        }
    }




    // Je voláno, až se server ověří
    public void synchronize_configuration(){
        new Thread( () -> {
            try {
                    System.out.println("1. Spouštím Sycnhronizační proceduru (synchronize_configuration)- Několik vláken - dej tomu čas");

                    if(service != null) service.shutdownNow();

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

                    terminal_logger.warn("1. Dokončil jsem metodu (synchronize_configuration) ");


            }catch (Exception e){
                terminal_logger.internalServerError(e);
            }
        }).start();
    }

    public WS_Interface_type get_Super(){
        return super.webSCtype;
    }

    // IO -----------------------------------------------------------------------------------------------------------------

    @Override
    public ObjectNode write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries)  throws TimeoutException, ClosedChannelException, ExecutionException, InterruptedException{

        if(!security_token_confirm){

            terminal_logger.warn(this.identifikator + " write_with_confirmation: This Websocket is not confirm");

            throw new InterruptedException();
        }

        if(!json.has("message_id")){
            json.put("message_id", UUID.randomUUID().toString());
        }

        return super.write_with_confirmation(json,time,delay, number_of_retries);

    }

    @Override
    public void write_without_confirmation(ObjectNode json){

        if(!security_token_confirm){

            terminal_logger.warn("write_without_confirmation: This Websocket is not confirm");

        }

        super.write_without_confirmation(json);
    }

    @Override
    public void write_without_confirmation(String messageId, ObjectNode json){

        if(!security_token_confirm){

            terminal_logger.warn(this.identifikator + " write_without_confirmation:: This Websocket is not confirm");

        }

        super.write_without_confirmation(messageId,json);
    }


    public void reject_server_verification(String message_id){
        super.write_without_confirmation(message_id, new WS_Message_Homer_Verification_result().make_request(false, null));
    }

    public void approve_server_verification(String message_id){

        security_token_confirm = true;
        rest_api_token =  UUID.randomUUID().toString() + UUID.randomUUID().toString();

        Controller_WebSocket.homer_servers.put(identifikator, this);
        Controller_WebSocket.not_synchronize_homer_servers.remove(identifikator);

        token_hash.put(rest_api_token, this);

        super.write_without_confirmation(message_id,  new WS_Message_Homer_Verification_result().make_request(true, rest_api_token));
        synchronize_configuration();
    }

}