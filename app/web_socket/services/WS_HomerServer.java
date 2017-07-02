package web_socket.services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.Model_Board;
import models.Model_HomerInstance;
import models.Model_HomerServer;
import utilities.independent_threads.Security_WS_token_confirm_procedure;
import utilities.independent_threads.homer_server.Synchronize_Homer_Hardware_after_connection;
import utilities.independent_threads.homer_server.Synchronize_Homer_Instance_after_connection;
import utilities.independent_threads.homer_server.Synchronize_Homer_Synchronize_Settings;
import utilities.independent_threads.homer_server.Synchronize_Homer_Unresolved_Updates;
import utilities.logger.Class_Logger;
import web_socket.message_objects.homer_with_tyrion.WS_Message_Homer_ping;
import web_socket.message_objects.homer_with_tyrion.verification.WS_Message_Homer_Rejection;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.*;

public class WS_HomerServer extends WS_Interface_type {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(WS_HomerServer.class);


/* VALUES  -------------------------------------------------------------------------------------------------------------*/

    public boolean security_token_confirm;
    public String identifikator;


    public WS_HomerServer(Model_HomerServer server) {
        super();
        identifikator = server != null ? server.unique_identificator : null;
        super.webSCtype =  this;
    }


    @Override
    public boolean is_online() {
        try {

            terminal_logger.trace("is_online: Test online state Server ID {} ", identifikator);

            out.write( new WS_Message_Homer_ping().make_request().toString() );

            return true;

        }catch (Exception e){
            terminal_logger.warn("is_online: Test online state Server ID {} Exception - return false" , identifikator);
            onClose();
            return false;
        }
    }

    @Override
    public void add_to_map() {
        Controller_WebSocket.homer_servers.put(identifikator, this);
    }

    @Override
    public String get_identificator() {
        return identifikator;
    }

    @Override
    public void onClose() {

        terminal_logger.warn("onClose: Starting cancelled procedure with virtual Homers");

        Controller_WebSocket.homer_servers.remove(identifikator);
        Model_HomerServer.get_byId(identifikator).is_disconnect();
    }

    @Override
    public void onMessage(ObjectNode json) {

        terminal_logger.trace(identifikator + " onMessage: " + json.toString());

        // Pokud není token - není dovoleno zasílat nic do WebSocketu a ani nic z něj
        if(!security_token_confirm){
            terminal_logger.warn("onMessage: This Websocket is not confirm");
            security_token_confirm_procedure();

            super.write_without_confirmation(new WS_Message_Homer_Rejection().make_request());
            return;
        }

            if(json.has("messageChannel")){

                switch (json.get("messageChannel").asText()){

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
                        terminal_logger.internalServerError(new Exception("onMessage: message not recognize incoming messageChanel!!! ->" + json.get("messageChannel").asText()));

                    }

                }

            }else {
                terminal_logger.internalServerError(new Exception(identifikator + " Incoming message has not messageChannel!!!!"));
            }

    }


    // Independent Threads -----------------------------------------------------------------------------------------------

    public Security_WS_token_confirm_procedure procedure; // Vlákno po úspěšném ověření samo sebe odstraní z Objektu a tím se nechá odmazat Garbarage Collectorem
    public void security_token_confirm_procedure(){
        if(procedure == null ){
            procedure = new Security_WS_token_confirm_procedure(this);
            terminal_logger.trace(this.identifikator + " security_token_confirm_procedure: Independent Thread for secure starting");
            procedure.start();
        }else {
            terminal_logger.trace(this.identifikator + " security_token_confirm_procedure: Independent Thread for secure was started already");
        }
    }

    //
    public ObjectNode super_write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries)  throws TimeoutException, ClosedChannelException, ExecutionException, InterruptedException {
        if(procedure == null) {
            terminal_logger.internalServerError(new Exception("Message " + this.identifikator + ". It is prohibited to send WS message to Homer server with Super - its allowed only for Security_WS_token_confirm_procedure"));
        }
        return super.write_with_confirmation(json,time,delay, number_of_retries);
    }


    public void synchronize_configuration(){
        try{

            ExecutorService service = Executors.newFixedThreadPool(10);

            // Kontrola nastavení
            Synchronize_Homer_Synchronize_Settings synchronize_homer_synchronize_settings = new Synchronize_Homer_Synchronize_Settings(this);

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

            service.shutdown();

            service.awaitTermination(5L, TimeUnit.MINUTES);


        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    public WS_Interface_type get_Super(){
        return super.webSCtype;
    }

    // IO -----------------------------------------------------------------------------------------------------------------

    @Override
    public ObjectNode write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries)  throws TimeoutException, ClosedChannelException, ExecutionException, InterruptedException{

        if(!security_token_confirm){
            terminal_logger.warn(this.identifikator + " write_with_confirmation: This Websocket is not confirm");
            security_token_confirm_procedure();
            throw new InterruptedException();
        }

        return super.write_with_confirmation(json,time,delay, number_of_retries);

    }

    @Override
    public void write_without_confirmation(ObjectNode json){

        if(!security_token_confirm){
            terminal_logger.warn("write_without_confirmation: This Websocket is not confirm");
            security_token_confirm_procedure();
        }

        super.write_without_confirmation(json);
    }

    @Override
    public void write_without_confirmation(String messageId, ObjectNode json){

        if(!security_token_confirm){
            terminal_logger.warn(this.identifikator + " write_without_confirmation:: This Websocket is not confirm");
            security_token_confirm_procedure();
        }

        super.write_without_confirmation(messageId,json);
    }

    /**
     * Odešle se serveru - který není akceptován - Unique name není známo
     */
    public void unique_connection_name_not_valid(){

        // Potvrzení Homer serveru, že je vše v pořádku
        super.write_without_confirmation(new WS_Message_Homer_Rejection().make_request());
    }

}