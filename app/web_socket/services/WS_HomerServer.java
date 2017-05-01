package web_socket.services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.Model_Board;
import models.Model_HomerInstance;
import models.Model_HomerServer;
import utilities.hardware_updater.helps_objects.Utilities_HW_Updater_Actualization_Task;
import utilities.independent_threads.Check_update_for_hw_under_homer_ws;
import utilities.independent_threads.Security_WS_token_confirm_procedure;
import utilities.independent_threads.SynchronizeHomerServer;
import utilities.logger.Class_Logger;
import web_socket.message_objects.homer_instance.WS_Message_Update_device_firmware;
import web_socket.message_objects.homerServer_with_tyrion.WS_Message_Rejection_homer_server;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class WS_HomerServer extends WS_Interface_type {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(WS_HomerServer.class);


/* VALUES  -------------------------------------------------------------------------------------------------------------*/

    public boolean security_token_confirm;

    public Check_update_for_hw_under_homer_ws check_update_for_hw_under_homer_ws = null;

    public String identifikator;


    public WS_HomerServer(Model_HomerServer server) {
        super();

        identifikator = server != null ? server.unique_identificator : null;
        super.webSCtype =  this;
        this.update_thread.start();
        this.check_update_for_hw_under_homer_ws = new Check_update_for_hw_under_homer_ws(this);
    }


    @Override
    public boolean is_online() {
        try {

            for(String key :  Controller_WebSocket.homer_servers.keySet() ){
                System.out.println("Mám v " + getClass().getSimpleName() + " Identifikator :: " + key);
            }


            out.write(" Něco posílám???");
            return true;
        }catch (Exception e){
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

        terminal_logger.warn("WS_HomerServer:: onClose - Starting cancaled procedure with virtual Homers");

        this.update_thread.stop();
        Controller_WebSocket.homer_servers.remove(identifikator);
        Model_HomerServer.get_model(identifikator).is_disconnect();
    }

    @Override
    public void onMessage(ObjectNode json) {

        terminal_logger.trace("WS_HomerServer:: "+ identifikator + " onMessage:: " + json.toString());

        // Pokud není token - není dovoleno zasílat nic do WebSocketu a ani nic z něj
        if(!security_token_confirm){
            terminal_logger.warn("WS_HomerServer:: onMessage:: This Websocket is not confirm");
            security_token_confirm_procedure();

            super.write_without_confirmation(new WS_Message_Rejection_homer_server().make_request());
            return;
        }


            if(json.has("messageChannel")){

                switch (json.get("messageChannel").asText()){


                    case Model_HomerServer.CHANNEL : { // Komunikace mezi Tyrion server a Homer Server
                        Model_HomerServer.Messages(this, json);
                        return;
                    }


                    case Model_HomerInstance.CHANNEL: {    // Komunikace mezi Tyrion server a Homer Instance
                        Model_HomerInstance.Messages(this, json);
                        return;
                    }


                    case WS_Becki_Website.CHANNEL: {    // Komunikace mezi Becki a Homer Instance
                        WS_Becki_Website.Messages_HomerInstance(this, json);
                        return;
                    }

                    default: {
                        terminal_logger.error("WS_HomerServer:: onMessage:: message not recognize incoming messageChanel!!! ->" + json.get("messageChannel").asText());

                    }

                }

            }else {
                terminal_logger.error("Homer Server:: "+ identifikator + " Incoming message has not messageChannel!!!!");
            }

    }


    // Independent Threads -----------------------------------------------------------------------------------------------

    public Security_WS_token_confirm_procedure procedure; // Vlákno po úspěšném ověření samo sebe odstraní z Objektu a tím se nechá odmazat Garbarage Collectorem
    public void security_token_confirm_procedure(){
        if(procedure == null ){
            procedure = new Security_WS_token_confirm_procedure(this);
            terminal_logger.trace("WS_HomerServer:: " + this.identifikator + " security_token_confirm_procedure: Independent Thread for secure starting");
            procedure.start();
        }else {
            terminal_logger.trace("WS_HomerServer:: " + this.identifikator + " security_token_confirm_procedure: Independent Thread for secure was started already");
        }
    }

    //
    public ObjectNode super_write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries)  throws TimeoutException, ClosedChannelException, ExecutionException, InterruptedException {
        if(procedure == null) {
            terminal_logger.error("WS_HomerServer:: " + this.identifikator + " Its prohybited send WS message to Homer server witch Super - its allowed only for Security_WS_token_confirm_procedure");
        }
        return super.write_with_confirmation(json,time,delay, number_of_retries);
    }

    public SynchronizeHomerServer synchronize;
    public void synchronize_configuration(){
        if(synchronize == null ){
            synchronize = new SynchronizeHomerServer(this);
            terminal_logger.trace("WS_HomerServer:: " + this.identifikator + " synchronize_configuration: Independent Thread for secure starting");
            synchronize.start();
        }else {
            terminal_logger.trace("WS_HomerServer:: " + this.identifikator + " synchronize_configuration: Independent Thread for secure was started already");
        }
    }

    public WS_Interface_type get_Super(){
        return super.webSCtype;
    }


    // IO -----------------------------------------------------------------------------------------------------------------

    @Override
    public ObjectNode write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries)  throws TimeoutException, ClosedChannelException, ExecutionException, InterruptedException{

        if(!security_token_confirm){
            terminal_logger.warn("WS_HomerServer:: " + this.identifikator + " write_with_confirmation:: This Websocket is not confirm");
            security_token_confirm_procedure();
            throw new InterruptedException();
        }

        return super.write_with_confirmation(json,time,delay, number_of_retries);

    }

    @Override
    public void write_without_confirmation(ObjectNode json){

        if(!security_token_confirm){
            terminal_logger.warn("WS_HomerServer:: write_without_confirmation:: This Websocket is not confirm");
            security_token_confirm_procedure();
        }

        super.write_without_confirmation(json);
    }

    @Override
    public void write_without_confirmation(String messageId, ObjectNode json){

        if(!security_token_confirm){
            terminal_logger.warn("WS_HomerServer:: " + this.identifikator + " write_without_confirmation:: This Websocket is not confirm");
            security_token_confirm_procedure();
        }

        super.write_without_confirmation(messageId,json);
    }


    /**
     * Odešle se serveru - který není akceptován - Unique name není známo
     */
    public void unique_connection_name_not_valid(){

        // Potvrzení Homer serveru, že je vše v pořádku
        super.write_without_confirmation(new WS_Message_Rejection_homer_server().make_request());
    }




// Aktualizační procedury ---------------------------------------------------------------------------------------------


    private ArrayList<Utilities_HW_Updater_Actualization_Task> task_list = new ArrayList<>();

    public void add_task(Utilities_HW_Updater_Actualization_Task task){

        task_list.add(task);

        if(update_thread.getState() == Thread.State.TIMED_WAITING) {
            update_thread.interrupt();
        }
    }

    private Thread update_thread = new Thread() {

        @Override
        public void run() {
            while(true){
                try {

                    if (!task_list.isEmpty()) {

                        terminal_logger.debug("WS_HomerServer:: update_thread:: Task List:: " + task_list.size());

                        Utilities_HW_Updater_Actualization_Task task = task_list.get(0);

                        if(task.instance != null){

                            WS_Message_Update_device_firmware result = Model_Board.update_devices_firmware(task.instance, task.procedures);
                            task_list.remove(task);
                        }
                        else {
                            terminal_logger.error("WS_HomerServer:: update_thread:: Instance noc Exxist ");
                        }

                    } else {
                            sleep(500000000);
                    }
                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    terminal_logger.error("WS_HomerServer:: update_thread:: Error", e);
                }
            }
        }
    };


}
